package com.likelion.backend.domain.preadoptionguide.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PreAdoptionGuideStepCompleteRequest(
        @NotNull
        @Min(1)
        @Max(5)
        Integer completedStep
) {
}
