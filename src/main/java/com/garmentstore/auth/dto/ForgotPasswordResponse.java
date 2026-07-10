package com.garmentstore.auth.dto;

import java.time.Instant;

public record ForgotPasswordResponse(
        String status,
        String message,
        Instant resetTokenExpiresAt,
        String devResetToken
) {}
