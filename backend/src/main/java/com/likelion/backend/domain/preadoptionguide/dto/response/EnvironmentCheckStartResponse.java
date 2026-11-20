package com.likelion.backend.domain.preadoptionguide.dto.response;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EnvironmentCheckStartResponse {

    private LocalDate startDate;
    private Integer elapsedDays;
    private Integer totalDays;
    private EnvironmentCheckStatus status;

    public enum EnvironmentCheckStatus {
        IN_PROGRESS, COMPLETED
    }

    public static EnvironmentCheckStartResponse of(PreAdoptionGuideProgress progress, LocalDateTime now) {
        return new EnvironmentCheckStartResponse(
                progress.getEnvironmentCheckStartedAt().toLocalDate(),
                progress.environmentCheckElapsedDays(now),
                PreAdoptionGuideProgress.ENVIRONMENT_CHECK_DAYS,
                progress.isEnvironmentCheckCompleted(now)
                        ? EnvironmentCheckStatus.COMPLETED
                        : EnvironmentCheckStatus.IN_PROGRESS
        );
    }
}
