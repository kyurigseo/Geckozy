package com.likelion.backend.domain.sensor.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnvironmentalAlertType {
    NORMAL("이상 없음"),
    PERSISTENT_ENVIRONMENT_ABNORMAL("환경 이상 지속"),
    SENSOR_CHECK_REQUIRED("센서 점검 필요");

    private final String description;
}
