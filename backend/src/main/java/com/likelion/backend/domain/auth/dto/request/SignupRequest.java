package com.likelion.backend.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank
        @Size(max = 50)
        String username,

        @NotBlank
        @Size(min = 8, max = 255)
        String password,

        @NotBlank
        String passwordConfirm,

        @NotBlank
        @Email
        @Size(max = 255)
        String email

) {
}