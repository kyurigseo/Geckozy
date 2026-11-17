package com.likelion.backend.domain.sensor.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HumidityCyclePhase {
    HUMIDITY_SPIKE_DETECTED("급상승"),
    HIGH_HUMIDITY_MAINTAINED("고습 유지"),
    HUMIDITY_DECLINING("하강"),
    STABLE("변화 없음"),
    INSUFFICIENT_DATA("데이터 부족");

    private final String description;
}
