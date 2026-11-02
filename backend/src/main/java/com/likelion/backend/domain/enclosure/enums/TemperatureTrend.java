package com.likelion.backend.domain.enclosure.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TemperatureTrend {
    RAPID_RISING("급상승"),
    GRADUAL_RISING("완만한 상승"),
    STABLE("유지"),
    GRADUAL_FALLING("완만 하락"),
    RAPID_FALLING("급하락"),
    PENDING_VERIFICATION("검증 대기"),
    UNKNOWN("알 수 없음");

    private final String description;
}