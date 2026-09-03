package com.garmentstore.notification.channel;

import com.garmentstore.notification.config.NotificationProperties;
import com.garmentstore.notification.domain.NotificationChannelType;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * WhatsApp channel implementation using Twilio WhatsApp API.
 *
 * SANDBOX MODE (development):
 *   - Uses Twilio sandbox number: whatsapp:+14155238886
 *   - Only pre-verified numbers can receive messages.
 *   - To verify your number: WhatsApp the sandbox number with the join phrase.
 *
 * PRODUCTION MODE:
 *   - Change twilio.whatsapp-from to your approved WhatsApp Business number.
 *   - Messages must use WhatsApp-approved templates.
 *   - Just update config — no code change required.
 *
 * Disabled if notification.whatsapp.enabled=false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.whatsapp.enabled", havingValue = "true", matchIfMissing = false)
public class WhatsAppNotificationChannel implements NotificationChannel {

    private final NotificationProperties properties;

    @PostConstruct
    public void init() {
        Twilio.init(properties.getTwilio().getAccountSid(), properties.getTwilio().getAuthToken());
        log.info("[WhatsApp] Twilio initialized. From: {}", properties.getTwilio().getWhatsappFrom());
    }

    @Override
    public NotificationChannelType supports() {
        return NotificationChannelType.WHATSAPP;
    }

    @Override
    public void send(NotificationRequest request) {
        try {
            // Ensure phone is in WhatsApp format: whatsapp:+91XXXXXXXXXX
            String to = formatWhatsAppNumber(request.getRecipient());
            String from = properties.getTwilio().getWhatsappFrom();

            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(from),
                    request.getBody()
            ).create();

            log.info("[WhatsApp] Sent {} to {} (SID: {})", request.getType(), to, message.getSid());

        } catch (Exception e) {
            log.error("[WhatsApp] Failed to send {} to {}: {}", request.getType(), request.getRecipient(), e.getMessage(), e);
            throw new RuntimeException("WhatsApp send failed: " + e.getMessage(), e);
        }
    }

    /**
     * Normalises a phone number to Twilio WhatsApp format.
     * Input: "9876543210" or "+919876543210" or "whatsapp:+919876543210"
     * Output: "whatsapp:+919876543210"
     */
    private String formatWhatsAppNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required for WhatsApp");
        }
        if (phone.startsWith("whatsapp:")) return phone;
        // Strip non-digit chars except leading +
        String digits = phone.replaceAll("[^+\\d]", "");
        // Add India country code if not present
        if (!digits.startsWith("+")) {
            digits = "+91" + digits;
        }
        return "whatsapp:" + digits;
    }
}
