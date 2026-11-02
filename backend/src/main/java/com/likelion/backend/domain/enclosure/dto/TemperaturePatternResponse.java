package com.likelion.backend.domain.enclosure.dto;

import com.likelion.backend.domain.enclosure.enums.PatternStatus;
import com.likelion.backend.domain.enclosure.enums.TemperatureTrend;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "온도 변화 패턴 분석 응답 DTO")
public class TemperaturePatternResponse {

    @Schema(description = "사육장 식별자", example = "1")
    private Long enclosureId;

    @Schema(description = "개체 이름", example = "피카소")
    private String lizardName;

    @Schema(description = "현재 측정 온도(℃)", example = "29.50")
    private BigDecimal currentTemperature;

    @Schema(description = "온도 변화 추세")
    private TemperatureTrend temperatureTrend;

    @Schema(description = "분당 변화 온도 (℃/min)", example = "1.2")
    private Double trendSpeed;

    @Schema(description = "패턴 분석 상태")
    private PatternStatus patternStatus;

    @Schema(description = "순간 이상값(노이즈) 경고 여부", example = "false")
    private Boolean hasNoiseWarning;

    @Schema(description = "화면 표시 메시지", example = "최근 15분간 온도가 급격히 상승하고 있습니다.")
    private String displayMessage;
}