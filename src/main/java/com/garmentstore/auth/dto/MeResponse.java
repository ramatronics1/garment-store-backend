package com.garmentstore.auth.dto;

import java.util.List;

public record MeResponse(
        Long id,
        String name,
        String email,
        String mobile,
        String userType,
        String accountStatus,
        boolean emailVerified,
        boolean mobileVerified,
        List<String> roles
) {}
