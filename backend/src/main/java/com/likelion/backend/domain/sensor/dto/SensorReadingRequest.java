package com.likelion.backend.domain.sensor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "IoT 센서 측정값 수집 요청")
public record SensorReadingRequest(
        @Schema(description = "센서 ID", example = "101")
        @NotNull Long sensorId,

        @Schema(description = "측정 시각 (ISO-8601, 오프셋 포함). 생략 시 서버 수신 시각으로 저장", example = "2026-09-29T09:00:00Z")
        OffsetDateTime measuredAt,

        @Schema(description = "온도(℃)", example = "27.1")
        @NotNull BigDecimal temperature,

        @Schema(description = "습도(%)", example = "68.2")
        @NotNull BigDecimal humidity
) {}
