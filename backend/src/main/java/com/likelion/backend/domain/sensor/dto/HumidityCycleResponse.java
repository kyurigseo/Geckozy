package com.likelion.backend.domain.sensor.dto;

import com.likelion.backend.domain.sensor.enums.HumidityCyclePhase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "습윤 사이클 패턴 감지 응답")
public record HumidityCycleResponse(
        Long enclosureId,
        @Schema(description = "사이클 단계") HumidityCyclePhase cyclePhase,
        @Schema(description = "분무 확정 여부 (센서 데이터만으로는 확정 불가 → 항상 false)") boolean isMistingConfirmed,
        BigDecimal currentHumidity,
        @Schema(description = "최근 10분 습도 변화량(%p)", example = "+15.0%") String humidityChangeRate,
        String displayMessage
) {}
