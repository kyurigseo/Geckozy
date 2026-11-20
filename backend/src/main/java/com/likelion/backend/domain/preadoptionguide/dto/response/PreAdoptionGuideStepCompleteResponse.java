package com.likelion.backend.domain.preadoptionguide.dto.response;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreAdoptionGuideStepCompleteResponse {

    private Integer currentStep;
    private Integer completedStep;
    private Boolean completed;

    public static PreAdoptionGuideStepCompleteResponse of(PreAdoptionGuideProgress progress) {
        return new PreAdoptionGuideStepCompleteResponse(
                progress.getCurrentStep(),
                progress.getCompletedStep(),
                progress.isCompleted()
        );
    }
}
