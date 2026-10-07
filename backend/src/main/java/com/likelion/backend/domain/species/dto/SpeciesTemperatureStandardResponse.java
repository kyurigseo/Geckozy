package com.likelion.backend.domain.species.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SpeciesTemperatureStandardResponse {

    private Long speciesId;
    private String speciesName;

    @JsonProperty("isRegistered")
    private Boolean isRegistered;

    private Double dayTemperatureMin;
    private Double dayTemperatureMax;
    private Double nightTemperatureMin;
    private Double nightTemperatureMax;

    private String cautionText;
    private String displayMessage;

    public static SpeciesTemperatureStandardResponse registered(
            Long speciesId,
            String speciesName,
            Double dayTemperatureMin,
            Double dayTemperatureMax,
            Double nightTemperatureMin,
            Double nightTemperatureMax,
            String cautionText
    ) {
        return SpeciesTemperatureStandardResponse.builder()
                .speciesId(speciesId)
                .speciesName(speciesName)
                .isRegistered(true)
                .dayTemperatureMin(dayTemperatureMin)
                .dayTemperatureMax(dayTemperatureMax)
                .nightTemperatureMin(nightTemperatureMin)
                .nightTemperatureMax(nightTemperatureMax)
                .cautionText(cautionText)
                .displayMessage(null)
                .build();
    }

    public static SpeciesTemperatureStandardResponse unregistered(Long speciesId, String displayMessage) {
        return SpeciesTemperatureStandardResponse.builder()
                .speciesId(speciesId)
                .speciesName(null)
                .isRegistered(false)
                .dayTemperatureMin(null)
                .dayTemperatureMax(null)
                .nightTemperatureMin(null)
                .nightTemperatureMax(null)
                .cautionText(null)
                .displayMessage(displayMessage)
                .build();
    }
}
