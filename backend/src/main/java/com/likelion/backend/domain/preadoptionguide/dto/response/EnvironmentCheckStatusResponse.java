package com.likelion.backend.domain.preadoptionguide.dto.response;

import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStartResponse.EnvironmentCheckStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EnvironmentCheckStatusResponse {

    private LocalDate startDate;
    private Integer elapsedDays;
    private Integer totalDays;
    private EnvironmentCheckStatus status;
    private SensorStatus sensorStatus;
    private LocalDateTime latestMeasuredAt;
    private Boolean missingData;

    public enum SensorStatus {
        CONNECTED,
        DISCONNECTED,
        // 점검 대상 사육장이 없거나 사육장에 센서가 등록되지 않음
        NOT_REGISTERED
    }
}
