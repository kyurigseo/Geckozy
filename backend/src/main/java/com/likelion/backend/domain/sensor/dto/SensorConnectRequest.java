package com.likelion.backend.domain.sensor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SensorConnectRequest(
        @NotBlank @Size(max = 100)
        String deviceIdentifier
) {}