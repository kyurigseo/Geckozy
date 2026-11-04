package com.likelion.backend.domain.enclosure.dto;

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
public class EnclosureHumiditySuggestionResponse {

    private Long enclosureId;
    private String primaryAction;
    private String actionTitle;
    private String actionGuide;

    @JsonProperty("isAutomatedSprayingRecommended")
    private Boolean isAutomatedSprayingRecommended;
}
