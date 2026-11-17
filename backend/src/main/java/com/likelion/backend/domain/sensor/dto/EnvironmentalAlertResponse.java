package com.likelion.backend.domain.sensor.dto;

import com.likelion.backend.domain.sensor.enums.EnvironmentalAlertType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "환경 이상상황 감지 응답")
public record EnvironmentalAlertResponse(
        Long enclosureId,
        EnvironmentalAlertType alertType,
        @Schema(description = "순간 1회 이상값을 분석에서 제외했는지 여부") boolean isSingleSpikeFiltered,
        String alertTitle,
        String alertMessage,
        LocalDateTime detectedAt
) {}
