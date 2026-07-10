package com.garmentstore.auth.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.password-reset")
public record PasswordResetProperties(
        long tokenExpiryMinutes,
        boolean exposeDevToken
) {}
