package com.likelion.backend.domain.preadoptionguide.dto.response;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PreAdoptionGuideProgressResponse {

    private Long progressId;
    private Long preGuideId;
    private Integer currentStep;
    private Integer completedStep;

    public static PreAdoptionGuideProgressResponse of(PreAdoptionGuideProgress progress, Long preGuideId) {
        return PreAdoptionGuideProgressResponse.builder()
                .progressId(progress.getProgressId())
                .preGuideId(preGuideId)
                .currentStep(progress.getCurrentStep())
                .completedStep(progress.getCompletedStep())
                .build();
    }
}
