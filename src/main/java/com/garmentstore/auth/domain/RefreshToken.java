package com.garmentstore.auth.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
    @Index(name = "idx_refresh_tokens_hash", columnList = "token_hash"),
    @Index(name = "idx_refresh_tokens_status", columnList = "status")
})
public class RefreshToken extends BaseEntity {
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
    private RefreshTokenStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_id")
    private Long replacedByTokenId;

    @Column(name = "ip_address", length = 80)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public RefreshTokenStatus getStatus() { return status; }
    public void setStatus(RefreshTokenStatus status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
    public Long getReplacedByTokenId() { return replacedByTokenId; }
    public void setReplacedByTokenId(Long replacedByTokenId) { this.replacedByTokenId = replacedByTokenId; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public static RefreshTokenBuilder builder() { return new RefreshTokenBuilder(); }

    public static class RefreshTokenBuilder {
        private Long id;
        private User user;
        private String tokenHash;
        private RefreshTokenStatus status;
        private Instant expiresAt;
        private Instant revokedAt;
        private Long replacedByTokenId;
        private String ipAddress;
        private String userAgent;

        public RefreshTokenBuilder id(Long id) { this.id = id; return this; }
        public RefreshTokenBuilder user(User user) { this.user = user; return this; }
        public RefreshTokenBuilder tokenHash(String tokenHash) { this.tokenHash = tokenHash; return this; }
        public RefreshTokenBuilder status(RefreshTokenStatus status) { this.status = status; return this; }
        public RefreshTokenBuilder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public RefreshTokenBuilder revokedAt(Instant revokedAt) { this.revokedAt = revokedAt; return this; }
        public RefreshTokenBuilder replacedByTokenId(Long replacedByTokenId) { this.replacedByTokenId = replacedByTokenId; return this; }
        public RefreshTokenBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public RefreshTokenBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }

        public RefreshToken build() {
            RefreshToken t = new RefreshToken();
            t.setId(this.id);
            t.setUser(this.user);
            t.setTokenHash(this.tokenHash);
            t.setStatus(this.status);
            t.setExpiresAt(this.expiresAt);
            t.setRevokedAt(this.revokedAt);
            t.setReplacedByTokenId(this.replacedByTokenId);
            t.setIpAddress(this.ipAddress);
            t.setUserAgent(this.userAgent);
            return t;
        }
    }
}
