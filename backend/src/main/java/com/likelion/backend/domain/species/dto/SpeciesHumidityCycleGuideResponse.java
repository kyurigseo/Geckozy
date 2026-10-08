package com.likelion.backend.domain.species.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SpeciesHumidityCycleGuideResponse {

    private Long speciesId;
    private String speciesName;

    @JsonProperty("hasGuideContent")
    private Boolean hasGuideContent;

    private HumidityRangeDto humidityRange;
    private List<CycleStepDto> cycleSteps;
    private String displayMessage;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class HumidityRangeDto {
        private Integer min;
        private Integer max;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class CycleStepDto {
        private Integer step;
        private String title;
        private String description;
    }

    public static SpeciesHumidityCycleGuideResponse withGuide(
            Long speciesId,
            String speciesName,
            HumidityRangeDto humidityRange,
            List<CycleStepDto> cycleSteps
    ) {
        return SpeciesHumidityCycleGuideResponse.builder()
                .speciesId(speciesId)
                .speciesName(speciesName)
                .hasGuideContent(true)
                .humidityRange(humidityRange)
                .cycleSteps(cycleSteps)
                .displayMessage(null)
                .build();
    }

    public static SpeciesHumidityCycleGuideResponse withoutGuide(
            Long speciesId,
            String displayMessage
    ) {
        return SpeciesHumidityCycleGuideResponse.builder()
                .speciesId(speciesId)
                .speciesName(null)
                .hasGuideContent(false)
                .humidityRange(null)
                .cycleSteps(Collections.emptyList())
                .displayMessage(displayMessage)
                .build();
    }
}
