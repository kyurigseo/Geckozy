package com.likelion.backend.domain.enclosure.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class TemperatureHistoryResponseDto {

    private String date;
    private List<ActualTemperatureDto> actual;
    private ReferenceTemperatureDto reference;

    @Getter
    @Builder
    public static class ActualTemperatureDto {
        private String measuredAt; // ISO 8601 형식
        private BigDecimal value;
    }

    @Getter
    @Builder
    public static class ReferenceTemperatureDto {
        private BigDecimal dayMin;
        private BigDecimal dayMax;
        private BigDecimal nightMin;
        private BigDecimal nightMax;
    }
}