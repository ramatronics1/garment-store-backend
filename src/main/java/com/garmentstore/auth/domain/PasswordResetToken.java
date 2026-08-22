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
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_password_reset_tokens_user", columnList = "user_id"),
        @Index(name = "idx_password_reset_tokens_hash", columnList = "token_hash"),
        @Index(name = "idx_password_reset_tokens_status_expiry", columnList = "status, expires_at")
})
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PasswordResetTokenStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "ip_address", length = 80)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public PasswordResetTokenStatus getStatus() { return status; }
    public void setStatus(PasswordResetTokenStatus status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static PasswordResetTokenBuilder builder() { return new PasswordResetTokenBuilder(); }

    public static class PasswordResetTokenBuilder {
        private Long id;
        private User user;
        private String tokenHash;
        private PasswordResetTokenStatus status;
        private Instant expiresAt;
        private Instant usedAt;
        private String ipAddress;
        private String userAgent;

        public PasswordResetTokenBuilder id(Long id) { this.id = id; return this; }
        public PasswordResetTokenBuilder user(User user) { this.user = user; return this; }
        public PasswordResetTokenBuilder tokenHash(String tokenHash) { this.tokenHash = tokenHash; return this; }
        public PasswordResetTokenBuilder status(PasswordResetTokenStatus status) { this.status = status; return this; }
        public PasswordResetTokenBuilder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public PasswordResetTokenBuilder usedAt(Instant usedAt) { this.usedAt = usedAt; return this; }
        public PasswordResetTokenBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public PasswordResetTokenBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }

        public PasswordResetToken build() {
            PasswordResetToken t = new PasswordResetToken();
            t.setId(this.id);
            t.setUser(this.user);
            t.setTokenHash(this.tokenHash);
            t.setStatus(this.status);
            t.setExpiresAt(this.expiresAt);
            t.setUsedAt(this.usedAt);
            t.setIpAddress(this.ipAddress);
            t.setUserAgent(this.userAgent);
            return t;
        }
    }
}
