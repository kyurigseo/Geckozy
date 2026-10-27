package com.likelion.backend.domain.enclosure.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AlertResponseDto {

    private AlertDetail primaryAlert;
    private List<AlertDetail> additionalAlerts;

    @Getter
    @Builder
    public static class AlertDetail {
        private String code;
        private String type;
        private String severity; // HIGH, MEDIUM, LOW
        private String message;
        private String detailType; // HUMIDITY, TEMPERATURE, SENSOR 등
    }
}