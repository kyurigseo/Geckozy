package com.likelion.backend.domain.enclosure.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PatternStatus {
    NORMAL("정상"),
    SUDDEN_HEAT_SPIKE("급격한 온열 이상"),
    PERSISTENT_HIGH("지속 고온"),
    REPEATED_FLUCTUATION("반복 변동"),
    NOISE_SUSPECTED("이상값 감지됨"),
    INSUFFICIENT_DATA("데이터 수신 공백");

    private final String description;
}