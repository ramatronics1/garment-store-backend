package com.garmentstore.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        int adminLoginRpm,
        int authLoginRpm
) {
    public RateLimitProperties {
        if (adminLoginRpm <= 0) {
            adminLoginRpm = 10;
        }
        if (authLoginRpm <= 0) {
            authLoginRpm = 20;
        }
    }
}
