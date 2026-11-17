package com.likelion.backend.domain.sensor.dto;

import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import com.likelion.backend.global.util.KstTime;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SensorReadingResponse(
        Long readingId,
        Long sensorId,
        Long enclosureId,
        OffsetDateTime measuredAt,
        BigDecimal temperature,
        BigDecimal humidity
) {
    public static SensorReadingResponse of(SensorMeasurement m, Long enclosureId) {
        return new SensorReadingResponse(
                m.getMeasurementId(),
                m.getSensor().getSensorId(),
                enclosureId,
                KstTime.toOffset(m.getMeasuredAt()),
                m.getTemperature(),
                m.getHumidity()
        );
    }
}
