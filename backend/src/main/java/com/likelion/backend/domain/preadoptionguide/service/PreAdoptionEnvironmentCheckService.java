package com.likelion.backend.domain.preadoptionguide.service;

import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStartResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStatusResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStatusResponse.SensorStatus;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideProgressRepository;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideRepository;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import com.likelion.backend.domain.sensor.repository.SensorMeasurementRepository;
import com.likelion.backend.domain.sensor.repository.SensorRepository;
import com.likelion.backend.domain.sensor.service.SensorService;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import com.likelion.backend.global.util.KstTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreAdoptionEnvironmentCheckService {

    // 기존 센서 상태 API와 같은 기준: 마지막 수신 후 30분이 지나면 연결 끊김, 30분 넘게 측정이 없으면 데이터 공백
    private static final Duration MAX_DATA_GAP = SensorService.DISCONNECT_THRESHOLD;

    private final UserRepository userRepository;
    private final PreAdoptionGuideRepository preAdoptionGuideRepository;
    private final PreAdoptionGuideProgressRepository progressRepository;
    private final SensorRepository sensorRepository;
    private final SensorMeasurementRepository sensorMeasurementRepository;

    /*
     * 환경 점검 진행 상태 조회
     * - 점검 대상 센서: 가이드 종의 (삭제되지 않은) 도마뱀 사육장 중 가장 최근에 등록한 사육장의 센서
     * - sensorStatus: 마지막 수신(없으면 센서 연결 시각) 후 30분 미만이면 CONNECTED, 아니면 DISCONNECTED, 센서가 없으면 NOT_REGISTERED
     * - missingData: 점검 시작 ~ 지금(최대 7일) 사이에 30분 넘게 측정값이 없는 구간이 있으면 true
     * - latestMeasuredAt: 센서의 가장 최근 측정 시각 (측정값이 없으면 null)
     */
    public EnvironmentCheckStatusResponse getStatus(Long userId, Long preGuideId) {

        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!preAdoptionGuideRepository.existsById(preGuideId)) {
            throw new BusinessException(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
        }

        PreAdoptionGuideProgress progress = progressRepository
                .findByUser_UserIdAndPreAdoptionGuide_PreGuideId(userId, preGuideId)
                .filter(PreAdoptionGuideProgress::isEnvironmentCheckStarted)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENVIRONMENT_CHECK_NOT_FOUND));

        LocalDateTime now = KstTime.now();
        LocalDateTime windowStart = progress.getEnvironmentCheckStartedAt();
        LocalDateTime windowEnd = min(now, windowStart.plusDays(PreAdoptionGuideProgress.ENVIRONMENT_CHECK_DAYS));

        Optional<Sensor> sensor = findCheckSensor(userId, preGuideId);
        Optional<SensorMeasurement> latest = sensor.flatMap(s ->
                sensorMeasurementRepository.findTopBySensor_SensorIdOrderByMeasuredAtDesc(s.getSensorId()));

        List<LocalDateTime> measuredAts = sensor
                .map(s -> sensorMeasurementRepository
                        .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(s.getSensorId(), windowStart, windowEnd)
                        .stream()
                        .map(SensorMeasurement::getMeasuredAt)
                        .toList())
                .orElse(List.of());

        EnvironmentCheckStartResponse check = EnvironmentCheckStartResponse.of(progress, now);

        return EnvironmentCheckStatusResponse.builder()
                .startDate(check.getStartDate())
                .elapsedDays(check.getElapsedDays())
                .totalDays(check.getTotalDays())
                .status(check.getStatus())
                .sensorStatus(sensorStatus(sensor, latest, now))
                .latestMeasuredAt(latest.map(m -> m.getMeasuredAt().truncatedTo(ChronoUnit.SECONDS)).orElse(null))
                .missingData(EnvironmentDataGap.exists(windowStart, windowEnd, measuredAts, MAX_DATA_GAP))
                .build();
    }

    private Optional<Sensor> findCheckSensor(Long userId, Long preGuideId) {
        return preAdoptionGuideRepository.findEnclosureIdsForGuideSpecies(userId, preGuideId).stream()
                .findFirst()
                .flatMap(sensorRepository::findByEnclosure_Id);
    }

    private SensorStatus sensorStatus(Optional<Sensor> sensor, Optional<SensorMeasurement> latest, LocalDateTime now) {
        if (sensor.isEmpty()) {
            return SensorStatus.NOT_REGISTERED;
        }

        LocalDateTime lastSeen = latest.map(SensorMeasurement::getMeasuredAt).orElse(sensor.get().getConnectedAt());

        return Duration.between(lastSeen, now).compareTo(SensorService.DISCONNECT_THRESHOLD) < 0
                ? SensorStatus.CONNECTED
                : SensorStatus.DISCONNECTED;
    }

    private static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }
}
