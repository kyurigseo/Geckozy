package com.likelion.backend.domain.enclosure.dto.environment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사육장 현재 온도 상태 응답 DTO")
public class TemperatureStatusResponse {

    @Schema(description = "사육장 식별자", example = "1")
    private Long enclosureId;

    @Schema(description = "도마뱀 이름", example = "피카소")
    private String lizardName;

    @Schema(description = "종 이름", example = "크레스티드 게코")
    private String speciesName;

    @Schema(description = "현재 측정 온도(℃)", example = "25.50")
    private BigDecimal currentTemperature;

    @Schema(description = "시간대 (주간/야간)")
    private TimeOfDay timeOfDay; // DAYTIME, NIGHTTIME

    @Schema(description = "적정 기준 온도 범위")
    private StandardTemperature standardTemperature;

    @Schema(description = "온도 상태 판정 결과")
    private Status status; // OPTIMAL, HIGH, LOW, UNKNOWN, SENSOR_ERROR

    @Schema(description = "주의사항 텍스트")
    private String cautionText;

    @Schema(description = "화면 표시 메시지", example = "현재 사육장 온도가 적정 범위를 유지하고 있습니다.")
    private String displayMessage;

    // --- Enum 정의 ---

    public enum TimeOfDay {
        DAYTIME, NIGHTTIME
    }

    public enum Status {
        OPTIMAL, HIGH, LOW, UNKNOWN, SENSOR_ERROR
    }

    // --- 내부 객체 정의 (StandardTemperature) ---

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "기준 온도 범위 객체")
    public static class StandardTemperature {
        @Schema(description = "최저 적정 온도", example = "22.00")
        private BigDecimal min;

        @Schema(description = "최고 적정 온도", example = "26.00")
        private BigDecimal max;
    }

    // --- 정적 팩토리 메서드 ---

    // 1. 센서 에러 응답
    public static TemperatureStatusResponse ofSensorError(Long enclosureId, String lizardName, String speciesName) {
        return TemperatureStatusResponse.builder()
                .enclosureId(enclosureId)
                .lizardName(lizardName)
                .speciesName(speciesName)
                .status(Status.SENSOR_ERROR)
                .displayMessage("온도 센서가 연결되지 않았거나 측정 데이터를 수신할 수 없습니다.")
                .build();
    }

    // 2. 기준 미등록 응답
    public static TemperatureStatusResponse ofUnknown(Long enclosureId, String lizardName, String speciesName, BigDecimal currentTemp, TimeOfDay timeOfDay, String cautionText) {
        return TemperatureStatusResponse.builder()
                .enclosureId(enclosureId)
                .lizardName(lizardName)
                .speciesName(speciesName)
                .currentTemperature(currentTemp)
                .timeOfDay(timeOfDay)
                .status(Status.UNKNOWN)
                .cautionText(cautionText)
                .displayMessage("등록된 종별 권장 온도 기준이 없어 수치만 표시되며, 상태 판정이 보류됩니다.")
                .build();
    }

    // 3. 정상 상태 판정 응답
    public static TemperatureStatusResponse of(Long enclosureId, String lizardName, String speciesName,
                                               BigDecimal currentTemp, TimeOfDay timeOfDay,
                                               StandardTemperature standardTemp, Status status,
                                               String cautionText, String displayMessage) {
        return TemperatureStatusResponse.builder()
                .enclosureId(enclosureId)
                .lizardName(lizardName)
                .speciesName(speciesName)
                .currentTemperature(currentTemp)
                .timeOfDay(timeOfDay)
                .standardTemperature(standardTemp)
                .status(status)
                .cautionText(cautionText)
                .displayMessage(displayMessage)
                .build();
    }
}