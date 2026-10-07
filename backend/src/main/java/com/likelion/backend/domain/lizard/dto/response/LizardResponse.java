package com.likelion.backend.domain.lizard.dto.response;

import com.likelion.backend.domain.lizard.entity.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LizardResponse(
        Long lizardId,
        Long speciesId,
        String species,
        String name,
        LocalDate birthDate,
        boolean birthDateUnknown,
        String characterColor,
        Gender gender,
        BigDecimal sizeCm,
        BigDecimal weightG,
        boolean sizeWeightUnknown
) {
}
