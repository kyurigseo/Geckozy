package com.likelion.backend.domain.enclosure.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class HumidityStatusResponseDto {

    private Long enclosureId;
    private String lizardName;
    private String speciesName;
    private BigDecimal currentHumidity;
    private StandardHumidity standardHumidity;
    private String humidityTrend; // RISING, MAINTAINING, FALLING, UNKNOWN, null
    private Integer trendDurationMinutes;
    private String cycleStatus; // HUMID_CYCLE_NORMAL, INSUFFICIENT_DATA, UNKNOWN, SENSOR_ERROR
    private String cautionText;
    private String displayMessage;

    @Getter
    @Builder
    public static class StandardHumidity {
        private BigDecimal min;
        private BigDecimal max;
    }
}