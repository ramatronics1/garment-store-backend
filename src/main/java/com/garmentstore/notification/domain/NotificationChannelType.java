package com.garmentstore.notification.domain;

/** Delivery channels. New channels = new enum value + new NotificationChannel impl. */
public enum NotificationChannelType {
    EMAIL,
    WHATSAPP,
    IN_APP
}
