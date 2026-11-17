package com.likelion.backend.domain.enclosure.service;

import com.likelion.backend.domain.enclosure.dto.environment.EnvironmentCoachResponse;
import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.enums.CoachRule;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.management.repository.ManagementSettingRepository;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import com.likelion.backend.domain.sensor.repository.SensorMeasurementRepository;
import com.likelion.backend.domain.sensor.repository.SensorRepository;
import com.likelion.backend.domain.species.entity.SpeciesEnvironmentStandard;
import com.likelion.backend.domain.species.repository.SpeciesEnvironmentStandardRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import com.likelion.backend.global.util.KstTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 환경 코치: 최근 측정값을 종별 기준과 비교해 규칙 기반 안내 문장을 제공한다.
 * 우선순위: (온도 > 습도), (지속 > 최근 1~2회)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnclosureCoachService {

    private static final Duration ANALYSIS_WINDOW = Duration.ofMinutes(60); // 최근 1시간 측정값만 사용
    private static final Duration STALE_LIMIT = Duration.ofMinutes(30);     // 마지막 측정이 30분 넘으면 데이터 부족
    private static final int MIN_READINGS = 3;
    private static final int CONTINUOUS_COUNT = 3;                          // 최근 3회 연속 이탈 → 지속

    private final EnclosureRepository enclosureRepository;
    private final SensorRepository sensorRepository;
    private final SensorMeasurementRepository sensorMeasurementRepository;
    private final SpeciesEnvironmentStandardRepository standardRepository;
    private final ManagementSettingRepository managementSettingRepository;

    public EnvironmentCoachResponse getCoach(Long enclosureId, Long currentUserId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));
        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Optional<Sensor> sensorOpt = sensorRepository.findByEnclosure_Id(enclosureId);
        Optional<SpeciesEnvironmentStandard> standardOpt =
                standardRepository.findBySpecies_SpeciesId(enclosure.getLizard().getSpecies().getSpeciesId());
        if (sensorOpt.isEmpty() || standardOpt.isEmpty()) {
            return EnvironmentCoachResponse.insufficient();
        }

        LocalDateTime now = KstTime.now();
        List<SensorMeasurement> recent = new ArrayList<>(sensorMeasurementRepository
                .findTop10BySensor_SensorIdOrderByMeasuredAtDesc(sensorOpt.get().getSensorId()));
        recent.removeIf(m -> m.getMeasuredAt().isBefore(now.minus(ANALYSIS_WINDOW)));
        Collections.reverse(recent); // 시간 오름차순

        if (recent.size() < MIN_READINGS
                || Duration.between(recent.get(recent.size() - 1).getMeasuredAt(), now).compareTo(STALE_LIMIT) > 0) {
            return EnvironmentCoachResponse.insufficient();
        }

        SpeciesEnvironmentStandard std = standardOpt.get();
        boolean daytime = isDaytime(enclosureId);
        BigDecimal tMin = daytime ? std.getDayTemperatureMin() : std.getNightTemperatureMin();
        BigDecimal tMax = daytime ? std.getDayTemperatureMax() : std.getNightTemperatureMax();
        BigDecimal hMin = std.getHumidityMin();
        BigDecimal hMax = std.getHumidityMax();

        Predicate<SensorMeasurement> highTemp = m -> above(m.getTemperature(), tMax);
        Predicate<SensorMeasurement> lowTemp = m -> below(m.getTemperature(), tMin);
        Predicate<SensorMeasurement> highHum = m -> above(m.getHumidity(), hMax);
        Predicate<SensorMeasurement> lowHum = m -> below(m.getHumidity(), hMin);

        // 1) 지속 이탈 (온도 우선)
        if (tailCount(recent, highTemp) >= CONTINUOUS_COUNT) return EnvironmentCoachResponse.of(CoachRule.HIGH_TEMPERATURE_CONTINUOUS);
        if (tailCount(recent, lowTemp) >= CONTINUOUS_COUNT) return EnvironmentCoachResponse.of(CoachRule.LOW_TEMPERATURE_CONTINUOUS);
        if (tailCount(recent, highHum) >= CONTINUOUS_COUNT) return EnvironmentCoachResponse.of(CoachRule.HIGH_HUMIDITY_CONTINUOUS);
        if (tailCount(recent, lowHum) >= CONTINUOUS_COUNT) return EnvironmentCoachResponse.of(CoachRule.LOW_HUMIDITY_CONTINUOUS);

        // 2) 최근 측정에서만 이탈
        SensorMeasurement latest = recent.get(recent.size() - 1);
        if (highTemp.test(latest)) return EnvironmentCoachResponse.of(CoachRule.HIGH_TEMPERATURE_RECENT);
        if (lowTemp.test(latest)) return EnvironmentCoachResponse.of(CoachRule.LOW_TEMPERATURE_RECENT);
        if (highHum.test(latest)) return EnvironmentCoachResponse.of(CoachRule.HIGH_HUMIDITY_RECENT);
        if (lowHum.test(latest)) return EnvironmentCoachResponse.of(CoachRule.LOW_HUMIDITY_RECENT);

        return EnvironmentCoachResponse.of(CoachRule.STABLE);
    }

    /** 최근 측정부터 거꾸로 세어 조건을 연속으로 만족하는 개수 */
    private int tailCount(List<SensorMeasurement> data, Predicate<SensorMeasurement> cond) {
        int count = 0;
        for (int i = data.size() - 1; i >= 0 && cond.test(data.get(i)); i--) count++;
        return count;
    }

    private boolean above(BigDecimal value, BigDecimal max) {
        return value != null && max != null && value.compareTo(max) > 0;
    }

    private boolean below(BigDecimal value, BigDecimal min) {
        return value != null && min != null && value.compareTo(min) < 0;
    }

    private boolean isDaytime(Long enclosureId) {
        LocalTime now = KstTime.now().toLocalTime();
        return managementSettingRepository.findByEnclosure_Id(enclosureId)
                .filter(s -> s.getLightingStartTime() != null && s.getLightingEndTime() != null)
                .map(s -> {
                    LocalTime start = s.getLightingStartTime();
                    LocalTime end = s.getLightingEndTime();
                    return start.isBefore(end)
                            ? !now.isBefore(start) && now.isBefore(end)
                            : !now.isBefore(start) || now.isBefore(end);
                })
                .orElseGet(() -> !now.isBefore(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(20, 0)));
    }
}
