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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getContactIdentifier() { return contactIdentifier; }
    public void setContactIdentifier(String contactIdentifier) { this.contactIdentifier = contactIdentifier; }
    public OtpPurpose getPurpose() { return purpose; }
    public void setPurpose(OtpPurpose purpose) { this.purpose = purpose; }
    public OtpDeliveryChannel getDeliveryChannel() { return deliveryChannel; }
    public void setDeliveryChannel(OtpDeliveryChannel deliveryChannel) { this.deliveryChannel = deliveryChannel; }
    public String getOtpCodeHash() { return otpCodeHash; }
    public void setOtpCodeHash(String otpCodeHash) { this.otpCodeHash = otpCodeHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static OtpTokenBuilder builder() {
        return new OtpTokenBuilder();
    }

    public static class OtpTokenBuilder {
        private Long id;
        private User user;
        private String contactIdentifier;
        private OtpPurpose purpose;
        private OtpDeliveryChannel deliveryChannel;
        private String otpCodeHash;
        private Instant expiresAt;
        private Instant verifiedAt;
        private int attemptCount;

        public OtpTokenBuilder id(Long id) { this.id = id; return this; }
        public OtpTokenBuilder user(User user) { this.user = user; return this; }
        public OtpTokenBuilder contactIdentifier(String contactIdentifier) { this.contactIdentifier = contactIdentifier; return this; }
        public OtpTokenBuilder purpose(OtpPurpose purpose) { this.purpose = purpose; return this; }
        public OtpTokenBuilder deliveryChannel(OtpDeliveryChannel deliveryChannel) { this.deliveryChannel = deliveryChannel; return this; }
        public OtpTokenBuilder otpCodeHash(String otpCodeHash) { this.otpCodeHash = otpCodeHash; return this; }
        public OtpTokenBuilder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public OtpTokenBuilder verifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; return this; }
        public OtpTokenBuilder attemptCount(int attemptCount) { this.attemptCount = attemptCount; return this; }

        public OtpToken build() {
            OtpToken token = new OtpToken();
            token.setId(this.id);
            token.setUser(this.user);
            token.setContactIdentifier(this.contactIdentifier);
            token.setPurpose(this.purpose);
            token.setDeliveryChannel(this.deliveryChannel);
            token.setOtpCodeHash(this.otpCodeHash);
            token.setExpiresAt(this.expiresAt);
            token.setVerifiedAt(this.verifiedAt);
            token.setAttemptCount(this.attemptCount);
            return token;
        }
    }
}
