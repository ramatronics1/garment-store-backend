package com.garmentstore.notification.dto;

import lombok.Builder;
import lombok.Getter;

/** Notification preferences returned to the frontend. */
@Getter
@Builder
public class NotificationPreferencesResponse {
    private Long userId;
    private boolean emailEnabled;
    private boolean whatsappEnabled;
    private boolean orderUpdatesEnabled;
    private boolean promotionsEnabled;
}
