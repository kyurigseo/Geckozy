package com.likelion.backend.domain.preadoptionguide.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 7일 환경 점검 기간의 데이터 공백 판단.
 * 점검 시작 ~ 기준 시각 사이에서 측정값 간격(시작 → 첫 측정, 측정 → 측정, 마지막 측정 → 기준 시각)이
 * 허용 간격을 넘는 구간이 하나라도 있으면 공백으로 본다.
 */
final class EnvironmentDataGap {

    private EnvironmentDataGap() {
    }

    /**
     * @param windowStart  점검 시작 시각
     * @param windowEnd    기준 시각 (지금, 단 점검 종료 시각을 넘지 않음)
     * @param measuredAts  windowStart ~ windowEnd 사이 측정 시각 (오름차순)
     * @param maxGap       허용 간격 (이 간격을 넘으면 공백)
     */
    static boolean exists(
            LocalDateTime windowStart,
            LocalDateTime windowEnd,
            List<LocalDateTime> measuredAts,
            Duration maxGap
    ) {
        LocalDateTime previous = windowStart;

        for (LocalDateTime measuredAt : measuredAts) {
            if (Duration.between(previous, measuredAt).compareTo(maxGap) > 0) {
                return true;
            }
            previous = measuredAt;
        }

        return Duration.between(previous, windowEnd).compareTo(maxGap) > 0;
    }
}
