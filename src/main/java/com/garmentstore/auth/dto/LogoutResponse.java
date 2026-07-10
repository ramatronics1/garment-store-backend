package com.garmentstore.auth.dto;

public record LogoutResponse(
        String status,
        int revokedTokenCount
) {}
