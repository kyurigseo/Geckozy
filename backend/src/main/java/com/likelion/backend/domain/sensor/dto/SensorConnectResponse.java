package com.likelion.backend.domain.sensor.dto;

import com.likelion.backend.domain.sensor.entity.Sensor;
import java.time.LocalDateTime;

public record SensorConnectResponse(
        Long sensorId,
        String deviceIdentifier,
        Sensor.Status status,
        LocalDateTime connectedAt
) {
    public static SensorConnectResponse from(Sensor sensor) {
        return new SensorConnectResponse(
                sensor.getSensorId(),
                sensor.getDeviceIdentifier(),
                sensor.getStatus(),
                sensor.getConnectedAt()
        );
    }
}