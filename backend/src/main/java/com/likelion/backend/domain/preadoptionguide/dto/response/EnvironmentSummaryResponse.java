package com.likelion.backend.domain.preadoptionguide.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class EnvironmentSummaryResponse {

    private Boolean sufficientData;
    private String temperatureSummary;
    private String dayNightSummary;
    private String humiditySummary;
    private String durationSummary;
    private String patternSummary;
    private List<String> checkPoints;
    private String guidance;
}
