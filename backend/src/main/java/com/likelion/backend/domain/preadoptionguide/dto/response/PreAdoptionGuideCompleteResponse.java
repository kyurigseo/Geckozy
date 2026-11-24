package com.likelion.backend.domain.preadoptionguide.dto.response;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@AllArgsConstructor
public class PreAdoptionGuideCompleteResponse {

    private Boolean completed;
    private LocalDateTime completedAt;

    public static PreAdoptionGuideCompleteResponse of(PreAdoptionGuideProgress progress) {
        return new PreAdoptionGuideCompleteResponse(
                progress.isCompleted(),
                progress.getCompletedAt().truncatedTo(ChronoUnit.SECONDS)
        );
    }
}
