package com.garmentstore.notification.dto;

/** Request body for updating notification preferences. */
public record UpdatePreferencesRequest(
        boolean emailEnabled,
        boolean whatsappEnabled,
        boolean orderUpdatesEnabled
) {}
