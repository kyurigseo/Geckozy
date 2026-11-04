package com.likelion.backend.domain.carerecord.dto.response;

import lombok.Getter;

@Getter
public class EnvironmentSummaryResponse {

    private final boolean dataAvailable;
    private final String summary;

    public EnvironmentSummaryResponse(boolean dataAvailable, String summary) {
        this.dataAvailable = dataAvailable;
        this.summary = summary;
    }
}