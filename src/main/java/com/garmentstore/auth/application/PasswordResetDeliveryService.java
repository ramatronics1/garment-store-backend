package com.garmentstore.auth.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PasswordResetDeliveryService {
    public void sendResetToken(String target, String rawToken) {
        // Provider integration will be added in Notification module.
        // Do not log raw reset token in production.
        log.info("Password reset token delivery requested for masked target {}", mask(target));
    }

    private String mask(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }
}
