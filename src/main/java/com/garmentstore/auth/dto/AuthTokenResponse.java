package com.garmentstore.auth.dto;

import java.time.Instant;
import java.util.List;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt,
        UserSummary user
) {
    public record UserSummary(
            Long id,
            String name,
            String email,
            String mobile,
            String userType,
            String accountStatus,
            List<String> roles
    ) {}
}
