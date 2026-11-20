package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStartResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStartResponse.EnvironmentCheckStatus;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuide;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import com.likelion.backend.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 7일 환경 점검의 진행 일수 / 완료 기준(시작 후 만 7일 = 168시간) 계산 검증
 */
class PreAdoptionGuideProgressEnvironmentCheckTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 16, 14, 0);

    private PreAdoptionGuideProgress startedProgress() {
        PreAdoptionGuideProgress progress =
                PreAdoptionGuideProgress.start(mock(User.class), mock(PreAdoptionGuide.class));
        progress.startEnvironmentCheck(START);
        return progress;
    }

    @Test
    @DisplayName("1. 시작 전에는 시작/완료 모두 false")
    void notStarted() {
        PreAdoptionGuideProgress progress =
                PreAdoptionGuideProgress.start(mock(User.class), mock(PreAdoptionGuide.class));

        assertThat(progress.isEnvironmentCheckStarted()).isFalse();
        assertThat(progress.isEnvironmentCheckCompleted(START.plusDays(30))).isFalse();
    }

    @Test
    @DisplayName("2. 시작한 날이 1일차, 날짜가 바뀌면 하루씩 증가")
    void elapsedDays_CountsCalendarDays() {
        PreAdoptionGuideProgress progress = startedProgress();

        assertThat(progress.environmentCheckElapsedDays(START)).isEqualTo(1);
        assertThat(progress.environmentCheckElapsedDays(LocalDateTime.of(2026, 10, 16, 23, 59))).isEqualTo(1);
        assertThat(progress.environmentCheckElapsedDays(LocalDateTime.of(2026, 10, 17, 0, 0))).isEqualTo(2);
        assertThat(progress.environmentCheckElapsedDays(LocalDateTime.of(2026, 10, 22, 12, 0))).isEqualTo(7);
    }

    @Test
    @DisplayName("3. 진행 일수는 최대 7")
    void elapsedDays_CappedAtTotalDays() {
        assertThat(startedProgress().environmentCheckElapsedDays(START.plusDays(20))).isEqualTo(7);
    }

    @Test
    @DisplayName("4. 시작 후 만 7일(168시간)이 되는 순간부터 완료")
    void completed_After168Hours() {
        PreAdoptionGuideProgress progress = startedProgress();

        assertThat(progress.isEnvironmentCheckCompleted(START.plusDays(7).minusMinutes(1))).isFalse();
        assertThat(progress.isEnvironmentCheckCompleted(START.plusDays(7))).isTrue();
    }

    @Test
    @DisplayName("5. 응답: 시작일, 진행 일수, 전체 일수, 상태")
    void response() {
        PreAdoptionGuideProgress progress = startedProgress();

        EnvironmentCheckStartResponse inProgress = EnvironmentCheckStartResponse.of(progress, START);
        assertThat(inProgress.getStartDate()).isEqualTo(LocalDate.of(2026, 10, 16));
        assertThat(inProgress.getElapsedDays()).isEqualTo(1);
        assertThat(inProgress.getTotalDays()).isEqualTo(7);
        assertThat(inProgress.getStatus()).isEqualTo(EnvironmentCheckStatus.IN_PROGRESS);

        // 7일차 날짜는 지났지만 168시간 전이면 아직 진행 중
        EnvironmentCheckStartResponse day8Morning =
                EnvironmentCheckStartResponse.of(progress, LocalDateTime.of(2026, 10, 23, 9, 0));
        assertThat(day8Morning.getElapsedDays()).isEqualTo(7);
        assertThat(day8Morning.getStatus()).isEqualTo(EnvironmentCheckStatus.IN_PROGRESS);

        EnvironmentCheckStartResponse completed = EnvironmentCheckStartResponse.of(progress, START.plusDays(7));
        assertThat(completed.getStatus()).isEqualTo(EnvironmentCheckStatus.COMPLETED);
    }
}
