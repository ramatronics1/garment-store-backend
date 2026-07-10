package com.garmentstore.auth.dto;

public record ResetPasswordResponse(
        String status,
        int revokedRefreshTokenCount
) {}
