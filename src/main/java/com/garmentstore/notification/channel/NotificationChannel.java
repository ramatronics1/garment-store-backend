package com.garmentstore.notification.channel;

import com.garmentstore.notification.domain.NotificationChannelType;

/**
 * Strategy interface for notification delivery channels.
 *
 * To add a new channel (e.g., SMS, Push notification):
 *   1. Create a new class that implements this interface.
 *   2. Annotate it with @Component.
 *   3. That is ALL — NotificationService auto-discovers it.
 *
 * No existing code needs modification.
 */
public interface NotificationChannel {

    /** Returns which channel type this implementation handles. */
    NotificationChannelType supports();

    /** Sends the notification. Implementations must handle their own exceptions gracefully. */
    void send(NotificationRequest request);
}
