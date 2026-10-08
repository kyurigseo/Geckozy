package com.likelion.backend.domain.enclosure.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EnclosureTemperatureSuggestionResponse {

    private Long enclosureId;
    private String primaryAction;
    private String actionTitle;
    private String actionGuide;
    private String disclaimer;
}
