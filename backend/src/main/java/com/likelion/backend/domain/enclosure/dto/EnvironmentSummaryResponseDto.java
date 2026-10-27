package com.likelion.backend.domain.enclosure.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EnvironmentSummaryResponseDto {

    private String date;
    private Integer dataCount;
    private String startAt;
    private String endAt;
    private String summary;
}