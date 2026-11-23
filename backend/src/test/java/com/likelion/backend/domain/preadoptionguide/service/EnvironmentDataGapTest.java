package com.likelion.backend.domain.preadoptionguide.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentDataGapTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 16, 14, 0);
    private static final Duration MAX_GAP = Duration.ofMinutes(30);

    private static LocalDateTime at(int minutes) {
        return START.plusMinutes(minutes);
    }

    @Test
    @DisplayName("1. 시작 직후(30분 이내) 측정값이 없어도 공백 아님")
    void noDataWithinThreshold() {
        assertThat(EnvironmentDataGap.exists(START, at(30), List.of(), MAX_GAP)).isFalse();
    }

    @Test
    @DisplayName("2. 측정값 없이 30분을 넘기면 공백")
    void noDataBeyondThreshold() {
        assertThat(EnvironmentDataGap.exists(START, at(31), List.of(), MAX_GAP)).isTrue();
    }

    @Test
    @DisplayName("3. 10분 간격으로 꾸준히 들어오면 공백 아님")
    void regularData() {
        assertThat(EnvironmentDataGap.exists(START, at(65),
                List.of(at(10), at(20), at(30), at(40), at(50), at(60)), MAX_GAP)).isFalse();
    }

    @Test
    @DisplayName("4. 중간에 30분을 넘게 끊기면 공백")
    void gapInMiddle() {
        assertThat(EnvironmentDataGap.exists(START, at(80),
                List.of(at(10), at(20), at(51), at(60), at(70)), MAX_GAP)).isTrue();
    }

    @Test
    @DisplayName("5. 정확히 30분 간격은 공백 아님")
    void exactlyThreshold() {
        assertThat(EnvironmentDataGap.exists(START, at(90),
                List.of(at(30), at(60), at(90)), MAX_GAP)).isFalse();
    }

    @Test
    @DisplayName("6. 시작 후 첫 측정까지 30분을 넘기면 공백")
    void gapAtBeginning() {
        assertThat(EnvironmentDataGap.exists(START, at(50), List.of(at(31), at(40), at(50)), MAX_GAP)).isTrue();
    }

    @Test
    @DisplayName("7. 마지막 측정 후 기준 시각까지 30분을 넘기면 공백")
    void gapAtEnd() {
        assertThat(EnvironmentDataGap.exists(START, at(51), List.of(at(10), at(20)), MAX_GAP)).isTrue();
    }
}
