package com.garmentstore.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guest_identities", indexes = {
        @Index(name = "idx_guest_identities_session_hash", columnList = "guest_session_hash"),
        @Index(name = "idx_guest_identities_status_expiry", columnList = "status, expires_at"),
        @Index(name = "idx_guest_identities_mobile", columnList = "mobile"),
        @Index(name = "idx_guest_identities_email", columnList = "email")
})
public class GuestIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guest_session_hash", nullable = false, unique = true, length = 128)
    private String guestSessionHash;

    @Column(name = "email", length = 180)
    private String email;

    @Column(name = "mobile", length = 20)
    private String mobile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private GuestIdentityStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "merged_to_user_id")
    private Long mergedToUserId;

    @Column(name = "merged_at")
    private Instant mergedAt;

    @Column(name = "ip_address", length = 80)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
