package com.likelion.backend.domain.lizard.dto.request;

import com.likelion.backend.domain.lizard.entity.Lizard;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LizardUpdateRequest(
        Long speciesId,

        @Size(max = 50)
        String name,

        LocalDate birthDate,

        Boolean birthDateUnknown,

        Lizard.Gender gender,

        @Size(max = 20)
        String characterColor,

        BigDecimal sizeCm,

        BigDecimal weightG
) {
}
