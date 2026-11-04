package com.likelion.backend.domain.enclosure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemperatureStatusResponse {
    private Long enclosureId;
    private String lizardName;
    private String speciesName;
    private BigDecimal currentTemperature;
    private TimeOfDay timeOfDay; // DAYTIME, NIGHTTIME
    private StandardTemperatureDto standardTemperature;
    private Status status; // OPTIMAL, HIGH, LOW, UNKNOWN, SENSOR_ERROR
    private String cautionText;
    private String displayMessage;

    public enum TimeOfDay {
        DAYTIME, NIGHTTIME
    }

    public enum Status {
        OPTIMAL, HIGH, LOW, UNKNOWN, SENSOR_ERROR
    }

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

    // 3. 정상 상태 판정 응답 (추가)
    public static TemperatureStatusResponse of(Long enclosureId, String lizardName, String speciesName,
                                               BigDecimal currentTemp, TimeOfDay timeOfDay,
                                               StandardTemperatureDto standardTemp, Status status,
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