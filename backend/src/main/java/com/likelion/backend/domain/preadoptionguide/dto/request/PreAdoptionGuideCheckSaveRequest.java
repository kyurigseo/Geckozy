package com.likelion.backend.domain.preadoptionguide.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PreAdoptionGuideCheckSaveRequest(
        @NotEmpty
        List<@NotNull @Valid CheckDto> checks
) {

    public record CheckDto(
            @NotNull
            Long checkItemId,
            @NotNull
            Boolean checked
    ) {
    }
}
