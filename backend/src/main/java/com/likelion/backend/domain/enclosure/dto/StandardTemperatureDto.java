package com.likelion.backend.domain.enclosure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StandardTemperatureDto {
    private BigDecimal min;
    private BigDecimal max;
}