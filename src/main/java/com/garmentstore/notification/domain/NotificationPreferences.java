package com.garmentstore.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Stores per-user notification channel preferences.
 * Created automatically when a user first updates preferences or on registration.
 */
@Entity
@Table(name = "notification_preferences")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private boolean emailEnabled = true;

    @Column(name = "whatsapp_enabled", nullable = false)
    @Builder.Default
    private boolean whatsappEnabled = true;

    @Column(name = "order_updates_enabled", nullable = false)
    @Builder.Default
    private boolean orderUpdatesEnabled = true;

    @Column(name = "promotions_enabled", nullable = false)
    @Builder.Default
    private boolean promotionsEnabled = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
