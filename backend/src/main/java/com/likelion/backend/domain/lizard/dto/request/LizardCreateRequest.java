package com.likelion.backend.domain.lizard.dto.request;

import com.likelion.backend.domain.lizard.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LizardCreateRequest(

        @NotNull
        Long speciesId,

        @NotBlank
        @Size(max = 50)
        String name,

        LocalDate birthDate,

        @NotNull
        Boolean birthDateUnknown,

        @NotNull
        Gender gender,

        @NotBlank
        @Size(max = 20)
        String characterColor,

        BigDecimal sizeCm,

        BigDecimal weightG,

        @NotNull
        Boolean sizeWeightUnknown
) {
}
