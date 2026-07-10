package com.garmentstore.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email or mobile is required")
        @Size(max = 180, message = "Email or mobile must not exceed 180 characters")
        String identifier,

        @NotBlank(message = "Password is required")
        @Size(max = 72, message = "Password must not exceed 72 characters")
        String password
) {}
