package com.garmentstore.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Typed config properties for the notification module.
 * All values are injected from application.yml under "notification:".
 */
@Getter @Setter
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private boolean emailEnabled = true;
    private boolean whatsappEnabled = false;
    private int lowStockThreshold = 5;

    /** Admin contact details for admin-facing notifications. */
    private Admin admin = new Admin();

    /** Twilio credentials for WhatsApp. */
    private Twilio twilio = new Twilio();

    @Getter @Setter
    public static class Admin {
        private String email = "admin@vastra.in";
        private String whatsapp = "";        // Admin WhatsApp E.164 number
    }

    @Getter @Setter
    public static class Twilio {
        private String accountSid = "";
        private String authToken = "";
        private String whatsappFrom = "whatsapp:+14155238886"; // Twilio sandbox default
    }
}
