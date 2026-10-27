package com.likelion.backend.domain.enclosure.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TemperatureStatusResponseDto {

    private Long enclosureId;
    private String lizardName;
    private String speciesName;
    private BigDecimal currentTemperature;
    private String timeOfDay; // DAYTIME, NIGHTTIME, null
    private StandardTemperature standardTemperature;
    private String status; // OPTIMAL, HIGH, LOW, UNKNOWN, SENSOR_ERROR
    private String cautionText;
    private String displayMessage;

    @Getter
    @Builder
    public static class StandardTemperature {
        private BigDecimal min;
        private BigDecimal max;
    }
}