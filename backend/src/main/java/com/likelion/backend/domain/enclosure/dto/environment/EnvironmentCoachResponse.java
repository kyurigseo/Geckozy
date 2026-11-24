package com.likelion.backend.domain.enclosure.dto.environment;

import com.likelion.backend.domain.enclosure.enums.CoachRule;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "환경 코치 해석 응답")
public record EnvironmentCoachResponse(
        @Schema(description = "현재 상황", example = "최근 습도가 권장 범위보다 높게 유지되고 있어요.")
        String currentSituation,
        @Schema(description = "확인 항목", example = "사육장 내부의 환기 상태를 확인해 주세요.")
        String checkItem,
        @Schema(description = "추천 행동", example = "환기 상태를 점검하고 분무량을 조절해 주세요.")
        String recommendedAction,
        @Schema(description = "적용된 규칙 코드 (데이터 부족 시 null)")
        CoachRule ruleCode,
        @Schema(description = "데이터 부족 여부")
        boolean insufficientData
) {
    public static EnvironmentCoachResponse of(CoachRule rule) {
        return new EnvironmentCoachResponse(
                rule.getCurrentSituation(), rule.getCheckItem(), rule.getRecommendedAction(), rule, false);
    }

    public static EnvironmentCoachResponse insufficient() {
        return new EnvironmentCoachResponse(
                null, "환경 데이터를 조금 더 관찰해 주세요.", "추가 측정 후 환경 상태를 다시 확인해 주세요.", null, true);
    }
}
