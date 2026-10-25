package com.garmentstore.auth.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.admin")
public record AdminAuthProperties(
        int lockoutThreshold,
        boolean bootstrapEnabled,
        String bootstrapEmail,
        String bootstrapMobile,
        String bootstrapName,
        String bootstrapPassword,
        int refreshTokenExpiryDays
) {
    public AdminAuthProperties {
        if (refreshTokenExpiryDays <= 0) {
            refreshTokenExpiryDays = 1;
        }
    }
}
