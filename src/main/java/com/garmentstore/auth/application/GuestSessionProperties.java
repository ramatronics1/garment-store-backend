package com.garmentstore.auth.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.guest-session")
public record GuestSessionProperties(
        long expiryDays
) {}
