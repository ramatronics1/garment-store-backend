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
@Table(name = "otp_tokens", indexes = {@Index(name = "idx_otp_tokens_user", columnList = "user_id"), @Index(name = "idx_otp_tokens_identifier", columnList = "mobile_or_email"), @Index(name = "idx_otp_tokens_purpose_expiry", columnList = "purpose, expires_at")})
public class OtpToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "mobile_or_email", nullable = false, length = 180)
    private String contactIdentifier;
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 40)
    private OtpPurpose purpose;
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_channel", nullable = false, length = 20)
    private OtpDeliveryChannel deliveryChannel;
    @Column(name = "otp_code_hash", nullable = false, length = 255)
    private String otpCodeHash;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "verified_at")
    private Instant verifiedAt;
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }
}
