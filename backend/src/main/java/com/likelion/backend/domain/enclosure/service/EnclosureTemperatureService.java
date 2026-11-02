package com.likelion.backend.domain.enclosure.service;

import com.likelion.backend.domain.enclosure.dto.TemperaturePatternResponse;
import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.enums.PatternStatus;
import com.likelion.backend.domain.enclosure.enums.TemperatureTrend;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import com.likelion.backend.domain.sensor.repository.SensorRepository;
import com.likelion.backend.domain.sensor.repository.SensorMeasurementRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnclosureTemperatureService {

    private final EnclosureRepository enclosureRepository;
    private final SensorRepository sensorRepository;
    private final SensorMeasurementRepository measurementRepository;

    public TemperaturePatternResponse getTemperaturePattern(Long enclosureId) {
        // 1. 사육장 및 등록된 센서 조회
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        Sensor sensor = sensorRepository.findByEnclosure_Id(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SENSOR_NOT_FOUND));

        String lizardName = enclosure.getLizard() != null ? enclosure.getLizard().getName() : "도마뱀";

        // 2. 기존 레포지토리 메서드 활용: 최근 15분간의 시계열 데이터 조회
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fifteenMinutesAgo = now.minusMinutes(15);

        List<SensorMeasurement> measurements = measurementRepository
                .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                        sensor.getSensorId(), fifteenMinutesAgo, now
                );

        // 예외 케이스 1: 데이터 수신 공백 (데이터가 2개 미만인 경우)
        if (measurements.size() < 2) {
            BigDecimal currentTemp = measurements.isEmpty() ? null : measurements.get(measurements.size() - 1).getTemperature();
            return TemperaturePatternResponse.builder()
                    .enclosureId(enclosureId)
                    .lizardName(lizardName)
                    .currentTemperature(currentTemp)
                    .temperatureTrend(TemperatureTrend.UNKNOWN)
                    .trendSpeed(null)
                    .patternStatus(PatternStatus.INSUFFICIENT_DATA)
                    .hasNoiseWarning(false)
                    .displayMessage("데이터 수신 공백 구간이 존재하여 온도 변화 패턴 분석이 일시 보류되었습니다.")
                    .build();
        }

        SensorMeasurement latest = measurements.get(measurements.size() - 1);
        SensorMeasurement previous = measurements.get(measurements.size() - 2);
        BigDecimal currentTemp = latest.getTemperature();

        // 예외 케이스 2: 일시적 온도 튐 (순간 노이즈/이상값 검증)
        double diff = Math.abs(latest.getTemperature().doubleValue() - previous.getTemperature().doubleValue());
        long secondsBetween = Duration.between(previous.getMeasuredAt(), latest.getMeasuredAt()).getSeconds();

        if (diff >= 5.0 && secondsBetween < 60) {
            return TemperaturePatternResponse.builder()
                    .enclosureId(enclosureId)
                    .lizardName(lizardName)
                    .currentTemperature(currentTemp)
                    .temperatureTrend(TemperatureTrend.PENDING_VERIFICATION)
                    .trendSpeed(null)
                    .patternStatus(PatternStatus.NOISE_SUSPECTED)
                    .hasNoiseWarning(true)
                    .displayMessage("일시적인 온도 튐(이상값)이 감지되어 재측정 데이터를 확인 중입니다.")
                    .build();
        }

        // 3. 정상 분석: 분당 변화 온도(℃/min) 기울기 계산
        SensorMeasurement oldest = measurements.get(0);
        double totalMinutes = Duration.between(oldest.getMeasuredAt(), latest.getMeasuredAt()).toSeconds() / 60.0;
        double tempDiff = latest.getTemperature().doubleValue() - oldest.getTemperature().doubleValue();

        double rawTrendSpeed = totalMinutes > 0 ? (tempDiff / totalMinutes) : 0.0;
        double trendSpeed = BigDecimal.valueOf(rawTrendSpeed)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        // 4. 추세 및 패턴 상태 판단
        TemperatureTrend trend;
        PatternStatus patternStatus;
        String displayMessage;

        if (trendSpeed >= 1.0) {
            trend = TemperatureTrend.RAPID_RISING;
            patternStatus = PatternStatus.SUDDEN_HEAT_SPIKE;
            displayMessage = "최근 15분간 온도가 급격히 상승하고 있습니다. 열원 및 조명을 확인해 주세요.";
        } else if (trendSpeed >= 0.3) {
            trend = TemperatureTrend.GRADUAL_RISING;
            patternStatus = PatternStatus.NORMAL;
            displayMessage = "온도가 완만하게 상승 중입니다.";
        } else if (trendSpeed <= -1.0) {
            trend = TemperatureTrend.RAPID_FALLING;
            patternStatus = PatternStatus.NORMAL;
            displayMessage = "온도가 급격히 하락하고 있습니다. 사육장 보온 상태를 점검해 주세요.";
        } else if (trendSpeed <= -0.3) {
            trend = TemperatureTrend.GRADUAL_FALLING;
            patternStatus = PatternStatus.NORMAL;
            displayMessage = "온도가 완만하게 하락 중입니다.";
        } else {
            trend = TemperatureTrend.STABLE;
            patternStatus = PatternStatus.NORMAL;
            displayMessage = "현재 온도 흐름이 안정적입니다.";
        }

        // 5. 응답 DTO 반환
        return TemperaturePatternResponse.builder()
                .enclosureId(enclosureId)
                .lizardName(lizardName)
                .currentTemperature(currentTemp)
                .temperatureTrend(trend)
                .trendSpeed(trendSpeed)
                .patternStatus(patternStatus)
                .hasNoiseWarning(false)
                .displayMessage(displayMessage)
                .build();
    }
}