package com.likelion.backend.domain.sensor.service;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.sensor.dto.SensorConnectRequest;
import com.likelion.backend.domain.sensor.dto.SensorConnectResponse;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.repository.SensorMeasurementRepository;
import com.likelion.backend.domain.sensor.repository.SensorRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SensorService {

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

    private Enclosure findMyEnclosure(Long currentUserId, Long enclosureId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return enclosure;
    }
}
