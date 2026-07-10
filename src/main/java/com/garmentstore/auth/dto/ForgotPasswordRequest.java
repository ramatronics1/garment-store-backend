package com.garmentstore.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @NotBlank(message = "Email or mobile is required")
        @Size(max = 180, message = "Email or mobile must not exceed 180 characters")
        String identifier
) {}
