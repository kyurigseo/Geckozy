package com.likelion.backend.domain.enclosure.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class HumidityHistoryResponseDto {

    private String date;
    private List<ActualHumidityDto> actual;
    private List<RecommendedCycleDto> recommendedCycle;

    @Getter
    @Builder
    public static class ActualHumidityDto {
        private String measuredAt;
        private BigDecimal value;
    }

    @Getter
    @Builder
    public static class RecommendedCycleDto {
        private String time; // MORNING, DAY, NIGHT
        private BigDecimal min;
        private BigDecimal max;
    }
}