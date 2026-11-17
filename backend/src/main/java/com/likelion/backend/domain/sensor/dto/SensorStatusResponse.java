package com.likelion.backend.domain.sensor.dto;

import com.likelion.backend.domain.sensor.entity.Sensor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SensorStatusResponse(
        Long sensorId,
        Sensor.Status status,
        OffsetDateTime lastMeasuredAt,
        BigDecimal lastTemperature,
        BigDecimal lastHumidity,
        String message
) {}
