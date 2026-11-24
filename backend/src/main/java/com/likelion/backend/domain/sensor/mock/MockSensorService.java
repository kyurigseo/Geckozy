package com.likelion.backend.domain.sensor.mock;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import com.likelion.backend.domain.sensor.repository.SensorMeasurementRepository;
import com.likelion.backend.domain.sensor.repository.SensorRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import com.likelion.backend.global.util.KstTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * [테스트 전용] 가상 센서.
 * 실제 센서 없이 프론트 화면(상태/그래프/코치)을 확인할 수 있도록 측정값을 생성한다.
 * mock-sensor.enabled=true 일 때만 빈으로 등록된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mock-sensor.enabled", havingValue = "true")
public class MockSensorService {

    public static final Duration INTERVAL = Duration.ofMinutes(10);
    private static final Duration MAX_BACKFILL = Duration.ofHours(24);

    private final EnclosureRepository enclosureRepository;
    private final SensorRepository sensorRepository;
    private final SensorMeasurementRepository sensorMeasurementRepository;

    /**
     * 마지막 측정 이후 비어 있는 구간(최대 24시간)을 INTERVAL 간격으로 채운다.
     * 정상 동작 중이면 현재 시각 1건만 추가된다.
     */
    @Transactional
    public int fillUntilNow(Long enclosureId) {
        Sensor sensor = ensureSensor(enclosureId);
        LocalDateTime now = KstTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime from = now.minus(MAX_BACKFILL);

        LocalDateTime last = sensorMeasurementRepository
                .findTopBySensor_SensorIdOrderByMeasuredAtDesc(sensor.getSensorId())
                .map(SensorMeasurement::getMeasuredAt)
                .orElse(null);
        if (last != null && last.plus(INTERVAL).isAfter(from)) {
            from = last.plus(INTERVAL);
        }

        List<SensorMeasurement> batch = new ArrayList<>();
        for (LocalDateTime t = from; !t.isAfter(now); t = t.plus(INTERVAL)) {
            batch.add(generate(sensor, t));
        }
        // 마지막 측정이 INTERVAL 이내라 생성할 게 없으면 건너뜀
        sensorMeasurementRepository.saveAll(batch);
        return batch.size();
    }

    /** 최근 hours시간 데이터를 지우고 새로 생성 (수동 리셋용) */
    @Transactional
    public int regenerate(Long enclosureId, int hours) {
        Sensor sensor = ensureSensor(enclosureId);
        LocalDateTime now = KstTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime from = now.minusHours(hours);

        sensorMeasurementRepository.deleteBySensorAndMeasuredAtAfter(sensor, from.minusMinutes(1));

        List<SensorMeasurement> batch = new ArrayList<>();
        for (LocalDateTime t = from; !t.isAfter(now); t = t.plus(INTERVAL)) {
            batch.add(generate(sensor, t));
        }
        sensorMeasurementRepository.saveAll(batch);
        return batch.size();
    }

    /** 사육장에 센서가 없으면 가상 센서를 연결 */
    private Sensor ensureSensor(Long enclosureId) {
        Sensor sensor = sensorRepository.findByEnclosure_Id(enclosureId).orElseGet(() -> {
            Enclosure enclosure = enclosureRepository.findById(enclosureId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));
            log.info("[MOCK_SENSOR] 사육장 {}에 가상 센서 연결", enclosureId);
            return sensorRepository.save(Sensor.builder()
                    .enclosure(enclosure)
                    .deviceIdentifier("mock-device-" + enclosureId)
                    .build());
        });
        sensor.markConnected();
        return sensor;
    }

    /**
     * 하루 주기 패턴
     * - 온도: 새벽 2시 최저(약 23℃) ~ 오후 2시 최고(약 29℃)
     * - 습도: 평소 55% 내외, 08:00·20:00 분무 직후 약 85%까지 상승 후 서서히 하강
     */
    private SensorMeasurement generate(Sensor sensor, LocalDateTime t) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double h = t.getHour() + t.getMinute() / 60.0;

        double temp = 26 + 3 * Math.sin((h - 8) / 24 * 2 * Math.PI) + r.nextDouble(-0.2, 0.2);

        double minutesSinceMist = Math.min((h - 8 + 24) % 24, (h - 20 + 24) % 24) * 60;
        double humidity = 55 + 2 * Math.sin(h / 24 * 2 * Math.PI)
                + 30 * Math.exp(-minutesSinceMist / 45)
                + r.nextDouble(-0.5, 0.5);
        humidity = Math.min(95, humidity);

        return SensorMeasurement.builder()
                .sensor(sensor)
                .temperature(BigDecimal.valueOf(temp).setScale(2, RoundingMode.HALF_UP))
                .humidity(BigDecimal.valueOf(humidity).setScale(2, RoundingMode.HALF_UP))
                .measuredAt(t)
                .build();
    }
}
