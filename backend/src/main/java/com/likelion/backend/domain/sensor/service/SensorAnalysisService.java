package com.likelion.backend.domain.sensor.service;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.management.repository.ManagementSettingRepository;
import com.likelion.backend.domain.sensor.dto.EnvironmentalAlertResponse;
import com.likelion.backend.domain.sensor.dto.HumidityCycleResponse;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import com.likelion.backend.domain.sensor.enums.EnvironmentalAlertType;
import com.likelion.backend.domain.sensor.enums.HumidityCyclePhase;
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

/**
 * 센서 시계열 데이터 기반 분석 (습윤 사이클, 환경 이상상황)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SensorAnalysisService {

    // ── 습윤 사이클 기준 ──
    private static final Duration CYCLE_WINDOW = Duration.ofMinutes(60);   // 사이클 추적 구간
    private static final Duration CHANGE_WINDOW = Duration.ofMinutes(10);  // 변화율 계산 구간
    private static final double SPIKE_RISE = 10.0;      // 10분 내 +10%p 이상 → 급상승
    private static final double MAINTAIN_MARGIN = 5.0;  // 최고점 대비 -5%p 이내 → 고습 유지

    // ── 이상상황 기준 ──
    private static final Duration STALE_LIMIT = SensorService.DISCONNECT_THRESHOLD;
    private static final int RECENT_COUNT = 10;          // 분석에 쓰는 최근 측정 개수
    private static final int PERSIST_COUNT = 3;          // 연속 N회 범위 이탈 → 지속 이상
    private static final double TEMP_JUMP = 5.0;         // 연속 측정 간 ±5℃ 이상 튐
    private static final double HUMIDITY_JUMP = 30.0;    // 연속 측정 간 ±30%p 이상 튐
    private static final int FAULT_JUMP_COUNT = 2;       // 튐이 2번 이상 반복 → 센서 결함 의심

    private final EnclosureRepository enclosureRepository;
    private final SensorRepository sensorRepository;
    private final SensorMeasurementRepository sensorMeasurementRepository;
    private final SpeciesEnvironmentStandardRepository standardRepository;
    private final ManagementSettingRepository managementSettingRepository;

    // ================================================================
    // 습윤 사이클 패턴 감지
    // ================================================================
    public HumidityCycleResponse getHumidityCycle(Long currentUserId, Long enclosureId) {
        findMyEnclosure(currentUserId, enclosureId);
        Sensor sensor = findSensor(enclosureId);

        LocalDateTime now = KstTime.now();
        List<SensorMeasurement> data = sensorMeasurementRepository
                .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                        sensor.getSensorId(), now.minus(CYCLE_WINDOW), now)
                .stream().filter(m -> m.getHumidity() != null).toList();

        if (data.size() < 2) {
            BigDecimal current = data.isEmpty() ? null : data.get(data.size() - 1).getHumidity();
            return new HumidityCycleResponse(enclosureId, HumidityCyclePhase.INSUFFICIENT_DATA, false,
                    current, null, "최근 습도 데이터가 부족하여 습윤 사이클을 분석할 수 없습니다.");
        }

        int last = data.size() - 1;
        SensorMeasurement latest = data.get(last);
        double current = latest.getHumidity().doubleValue();
        double change = current - h(data.get(referenceIndex(data, last)));

        // 구간 내에서 가장 최근에 발생한 급상승 지점 탐색
        int spikeAt = -1;
        for (int i = last; i >= 1; i--) {
            double rise = h(data.get(i)) - h(data.get(referenceIndex(data, i)));
            if (rise >= SPIKE_RISE) {
                spikeAt = i;
                break;
            }
        }

        HumidityCyclePhase phase;
        String message;
        if (change >= SPIKE_RISE) {
            phase = HumidityCyclePhase.HUMIDITY_SPIKE_DETECTED;
            message = "습도가 급상승한 패턴이 감지되었습니다. (분무 또는 환경 변화 가능성)";
        } else if (spikeAt >= 0) {
            double peak = data.subList(spikeAt, data.size()).stream()
                    .mapToDouble(this::h).max().orElse(current);
            if (current >= peak - MAINTAIN_MARGIN) {
                phase = HumidityCyclePhase.HIGH_HUMIDITY_MAINTAINED;
                message = "습도 급상승 이후 높은 습도가 유지되고 있습니다.";
            } else {
                phase = HumidityCyclePhase.HUMIDITY_DECLINING;
                message = "높아졌던 습도가 다시 낮아지고 있습니다. 습윤 사이클이 진행 중입니다.";
            }
        } else {
            phase = HumidityCyclePhase.STABLE;
            message = "최근 1시간 동안 뚜렷한 습도 급상승 패턴이 없습니다.";
        }

        return new HumidityCycleResponse(enclosureId, phase, false, latest.getHumidity(),
                String.format("%+.1f%%", change), message);
    }

    /** i번째 측정 시점에서 CHANGE_WINDOW 이전과 가장 가까운(그 이전) 측정 인덱스. 없으면 0 */
    private int referenceIndex(List<SensorMeasurement> data, int i) {
        LocalDateTime target = data.get(i).getMeasuredAt().minus(CHANGE_WINDOW);
        int ref = 0;
        for (int j = 0; j < i; j++) {
            if (!data.get(j).getMeasuredAt().isAfter(target)) ref = j;
            else break;
        }
        return ref;
    }

    // ================================================================
    // 환경 이상상황 감지
    // ================================================================
    public EnvironmentalAlertResponse getEnvironmentalAlert(Long currentUserId, Long enclosureId) {
        Enclosure enclosure = findMyEnclosure(currentUserId, enclosureId);
        Sensor sensor = findSensor(enclosureId);

        List<SensorMeasurement> recent = new ArrayList<>(sensorMeasurementRepository
                .findTop10BySensor_SensorIdOrderByMeasuredAtDesc(sensor.getSensorId()));
        Collections.reverse(recent); // 시간 오름차순

        LocalDateTime now = KstTime.now();

        // 1) 수신 없음 / 장시간 미수신
        if (recent.isEmpty()
                || Duration.between(recent.get(recent.size() - 1).getMeasuredAt(), now).compareTo(STALE_LIMIT) >= 0) {
            return new EnvironmentalAlertResponse(enclosureId, EnvironmentalAlertType.SENSOR_CHECK_REQUIRED, false,
                    "센서 데이터 수신 중단",
                    "최근 센서 데이터가 수신되지 않고 있습니다. 센서 전원과 연결 상태를 확인해주세요.",
                    now.withNano(0));
        }

        // 2) 비정상적인 값 튐이 반복 → 센서 결함 가능성 (환경 변화가 아닌 센서 오류로 분리)
        int jumps = 0;
        LocalDateTime firstJumpAt = null;
        for (int i = 1; i < recent.size(); i++) {
            if (isJump(recent.get(i - 1), recent.get(i))) {
                jumps++;
                if (firstJumpAt == null) firstJumpAt = recent.get(i).getMeasuredAt();
            }
        }
        if (jumps >= FAULT_JUMP_COUNT) {
            return new EnvironmentalAlertResponse(enclosureId, EnvironmentalAlertType.SENSOR_CHECK_REQUIRED, true,
                    "센서 연결 및 동작 점검 필요",
                    "데이터 수신 패턴 상 센서 결함 가능성이 높습니다. 센서 수신 상태와 설치 위치를 확인해주세요.",
                    firstJumpAt);
        }

        // 3) 종(species) 적정 범위 기준 이탈 판단
        Optional<SpeciesEnvironmentStandard> standardOpt =
                standardRepository.findBySpecies_SpeciesId(enclosure.getLizard().getSpecies().getSpeciesId());
        if (standardOpt.isEmpty()) {
            return normal(enclosureId, false);
        }
        SpeciesEnvironmentStandard std = standardOpt.get();
        boolean daytime = isDaytime(enclosureId);

        List<String> outOfRange = recent.stream().map(m -> abnormalReason(m, std, daytime)).toList();
        int last = recent.size() - 1;

        // 최근 PERSIST_COUNT회 연속 이탈 → 지속 이상
        if (recent.size() >= PERSIST_COUNT
                && outOfRange.subList(recent.size() - PERSIST_COUNT, recent.size()).stream().allMatch(r -> r != null)) {
            int start = last;
            while (start > 0 && outOfRange.get(start - 1) != null) start--;
            String reason = outOfRange.get(last);
            return new EnvironmentalAlertResponse(enclosureId, EnvironmentalAlertType.PERSISTENT_ENVIRONMENT_ABNORMAL,
                    hasIsolatedSpike(outOfRange),
                    reason + " 상태 지속",
                    String.format("최근 %d회 연속 측정에서 %s 상태가 확인되었습니다. 사육장 환경을 즉시 확인해주세요.",
                            last - start + 1, reason),
                    recent.get(start).getMeasuredAt());
        }

        // 일시적 이탈(1회성 이상값)은 알림에서 제외
        return normal(enclosureId, outOfRange.stream().anyMatch(r -> r != null));
    }

    private EnvironmentalAlertResponse normal(Long enclosureId, boolean filtered) {
        return new EnvironmentalAlertResponse(enclosureId, EnvironmentalAlertType.NORMAL, filtered,
                "환경 정상",
                filtered ? "일시적인 이상값이 있었지만 지속되지 않아 정상으로 판단했습니다."
                        : "현재 사육장 환경에 즉시 확인이 필요한 이상 변화가 없습니다.",
                null);
    }

    /** 범위 이탈 사유 (정상이면 null) */
    private String abnormalReason(SensorMeasurement m, SpeciesEnvironmentStandard std, boolean daytime) {
        BigDecimal tMin = daytime ? std.getDayTemperatureMin() : std.getNightTemperatureMin();
        BigDecimal tMax = daytime ? std.getDayTemperatureMax() : std.getNightTemperatureMax();
        if (m.getTemperature() != null) {
            if (tMax != null && m.getTemperature().compareTo(tMax) > 0) return "고온";
            if (tMin != null && m.getTemperature().compareTo(tMin) < 0) return "저온";
        }
        if (m.getHumidity() != null) {
            if (std.getHumidityMax() != null && m.getHumidity().compareTo(std.getHumidityMax()) > 0) return "고습";
            if (std.getHumidityMin() != null && m.getHumidity().compareTo(std.getHumidityMin()) < 0) return "저습";
        }
        return null;
    }

    /** 앞뒤가 정상인데 혼자 이탈한 1회성 값이 있었는지 */
    private boolean hasIsolatedSpike(List<String> outOfRange) {
        for (int i = 1; i < outOfRange.size() - 1; i++) {
            if (outOfRange.get(i) != null && outOfRange.get(i - 1) == null && outOfRange.get(i + 1) == null) {
                return true;
            }
        }
        return false;
    }

    private boolean isJump(SensorMeasurement a, SensorMeasurement b) {
        boolean temp = a.getTemperature() != null && b.getTemperature() != null
                && Math.abs(a.getTemperature().doubleValue() - b.getTemperature().doubleValue()) >= TEMP_JUMP;
        boolean hum = a.getHumidity() != null && b.getHumidity() != null
                && Math.abs(a.getHumidity().doubleValue() - b.getHumidity().doubleValue()) >= HUMIDITY_JUMP;
        return temp || hum;
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

    // ================================================================
    private double h(SensorMeasurement m) {
        return m.getHumidity().doubleValue();
    }

    private Sensor findSensor(Long enclosureId) {
        return sensorRepository.findByEnclosure_Id(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SENSOR_NOT_FOUND));
    }

    private Enclosure findMyEnclosure(Long currentUserId, Long enclosureId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));
        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return enclosure;
    }
}
