package com.likelion.backend.domain.sensor.service;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.sensor.dto.*;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import com.likelion.backend.domain.sensor.repository.SensorMeasurementRepository;
import com.likelion.backend.domain.sensor.repository.SensorRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import com.likelion.backend.global.util.KstTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorService {

    /** 마지막 수신 후 이 시간이 지나면 연결 끊김으로 판단 */
    public static final Duration DISCONNECT_THRESHOLD = Duration.ofMinutes(30);

    // 센서(DHT 계열) 측정 가능 범위 + 사육장 환경에서 물리적으로 가능한 범위
    private static final BigDecimal TEMP_MIN = new BigDecimal("-10");
    private static final BigDecimal TEMP_MAX = new BigDecimal("60");
    private static final BigDecimal HUMIDITY_MIN = BigDecimal.ZERO;
    private static final BigDecimal HUMIDITY_MAX = new BigDecimal("100");
    private static final Duration FUTURE_TOLERANCE = Duration.ofMinutes(5);

    private final SensorRepository sensorRepository;
    private final SensorMeasurementRepository sensorMeasurementRepository;
    private final EnclosureRepository enclosureRepository;

    @Transactional
    public SensorConnectResponse connect(Long currentUserId, Long enclosureId, SensorConnectRequest request) {
        Enclosure enclosure = findMyEnclosure(currentUserId, enclosureId);

        if (sensorRepository.findByEnclosure_Id(enclosureId).isPresent()
                || sensorRepository.findByDeviceIdentifier(request.deviceIdentifier()).isPresent()) {
            throw new BusinessException(ErrorCode.SENSOR_ALREADY_CONNECTED);
        }

        Sensor sensor = sensorRepository.save(
                Sensor.builder()
                        .enclosure(enclosure)
                        .deviceIdentifier(request.deviceIdentifier())
                        .build()   // status는 기본값 CONNECTED
        );

        return SensorConnectResponse.from(sensor);
    }

    @Transactional
    public void disconnect(Long currentUserId, Long enclosureId) {
        findMyEnclosure(currentUserId, enclosureId);

        Sensor sensor = sensorRepository.findByEnclosure_Id(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SENSOR_NOT_FOUND));

        sensorMeasurementRepository.deleteBySensor(sensor); // 측정값 먼저
        sensorRepository.delete(sensor);
    }

    /**
     * IoT 센서 측정값 수집.
     * 센서(아두이노)는 JWT 로그인을 할 수 없으므로 X-Device-Id 헤더(등록된 deviceIdentifier)로 인증한다.
     */
    @Transactional
    public SensorReadingResponse saveReading(Long enclosureId, String deviceId, SensorReadingRequest request) {
        if (!enclosureRepository.existsById(enclosureId)) {
            throw new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND);
        }

        Sensor sensor = sensorRepository.findById(request.sensorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SENSOR_NOT_FOUND));

        if (deviceId == null || !deviceId.equals(sensor.getDeviceIdentifier())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_SENSOR);
        }

        if (!sensor.getEnclosure().getId().equals(enclosureId)) {
            throw new BusinessException(ErrorCode.SENSOR_ENCLOSURE_MISMATCH);
        }

        LocalDateTime now = KstTime.now();
        LocalDateTime measuredAt = request.measuredAt() != null ? KstTime.toLocal(request.measuredAt()) : now;

        // 비정상 값은 저장(=분석 대상)에서 제외하고 오류 로그로 기록
        if (isOutOfRange(request.temperature(), TEMP_MIN, TEMP_MAX)
                || isOutOfRange(request.humidity(), HUMIDITY_MIN, HUMIDITY_MAX)
                || measuredAt.isAfter(now.plus(FUTURE_TOLERANCE))) {
            log.warn("[INVALID_SENSOR_VALUE] enclosureId={}, sensorId={}, temperature={}, humidity={}, measuredAt={}",
                    enclosureId, sensor.getSensorId(), request.temperature(), request.humidity(), measuredAt);
            throw new BusinessException(ErrorCode.INVALID_SENSOR_VALUE);
        }

        SensorMeasurement saved = sensorMeasurementRepository.save(
                SensorMeasurement.builder()
                        .sensor(sensor)
                        .temperature(request.temperature())
                        .humidity(request.humidity())
                        .measuredAt(measuredAt)
                        .build()
        );

        // 데이터가 다시 들어오면 연결 상태 복구
        if (sensor.getStatus() == Sensor.Status.DISCONNECTED) {
            sensor.markConnected();
        }

        return SensorReadingResponse.of(saved, enclosureId);
    }

    /**
     * 센서 연결 상태 확인.
     * 마지막 수신(없으면 연결 시각)으로부터 DISCONNECT_THRESHOLD 이상 지나면 DISCONNECTED.
     */
    @Transactional
    public SensorStatusResponse getStatus(Long currentUserId, Long enclosureId) {
        findMyEnclosure(currentUserId, enclosureId);

        Sensor sensor = sensorRepository.findByEnclosure_Id(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SENSOR_NOT_FOUND));

        Optional<SensorMeasurement> latest =
                sensorMeasurementRepository.findTopBySensor_SensorIdOrderByMeasuredAtDesc(sensor.getSensorId());

        LocalDateTime lastSeen = latest.map(SensorMeasurement::getMeasuredAt).orElse(sensor.getConnectedAt());
        boolean connected = Duration.between(lastSeen, KstTime.now()).compareTo(DISCONNECT_THRESHOLD) < 0;

        // 다른 API(온습도 상태, 알림)가 엔티티 상태를 참조하므로 동기화
        if (connected) sensor.markConnected();
        else sensor.markDisconnected();

        String message;
        if (!connected) {
            message = "센서 연결을 확인해주세요.";
        } else if (latest.isEmpty()) {
            message = "센서가 연결되었습니다. 첫 측정 데이터를 기다리고 있습니다.";
        } else {
            message = "센서가 정상적으로 연결되어 있습니다.";
        }

        return new SensorStatusResponse(
                sensor.getSensorId(),
                sensor.getStatus(),
                latest.map(m -> KstTime.toOffset(m.getMeasuredAt())).orElse(null),
                latest.map(SensorMeasurement::getTemperature).orElse(null),
                latest.map(SensorMeasurement::getHumidity).orElse(null),
                message
        );
    }

    private boolean isOutOfRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value.compareTo(min) < 0 || value.compareTo(max) > 0;
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
