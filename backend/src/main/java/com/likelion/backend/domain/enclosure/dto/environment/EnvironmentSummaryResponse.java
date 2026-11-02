package com.likelion.backend.domain.enclosure.dto.environment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EnvironmentSummaryResponse {

    private String date;
    private Integer dataCount;
    private String startAt;
    private String endAt;
    private String summary;
}