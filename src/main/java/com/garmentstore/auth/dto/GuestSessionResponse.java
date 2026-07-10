package com.garmentstore.auth.dto;

import java.time.Instant;

public record GuestSessionResponse(
        Long guestIdentityId,
        String guestSessionId,
        String status,
        Instant expiresAt
) {}
