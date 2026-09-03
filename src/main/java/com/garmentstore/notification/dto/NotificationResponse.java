package com.garmentstore.notification.dto;

import com.garmentstore.notification.domain.NotificationChannelType;
import com.garmentstore.notification.domain.NotificationType;
import com.garmentstore.notification.domain.NotificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/** Response DTO for a single notification sent to the UI. */
@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private NotificationChannelType channel;
    private String subject;
    private String body;
    private NotificationStatus status;
    private boolean read;
    private Instant createdAt;
    private Long orderId;

    /** Friendly display label used in UI. */
    private String displayTitle;
    private String displayIcon;
}
