package com.likelion.backend.domain.carerecord.dto.response;

import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class EnvironmentGraphResponse {

    private final BigDecimal temperature;
    private final BigDecimal humidity;
    private final LocalDateTime measuredAt;

    public EnvironmentGraphResponse(SensorMeasurement measurement) {
        this.temperature = measurement.getTemperature();
        this.humidity = measurement.getHumidity();
        this.measuredAt = measurement.getMeasuredAt();
    }
}