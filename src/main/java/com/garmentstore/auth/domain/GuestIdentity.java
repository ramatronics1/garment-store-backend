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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGuestSessionHash() { return guestSessionHash; }
    public void setGuestSessionHash(String guestSessionHash) { this.guestSessionHash = guestSessionHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public GuestIdentityStatus getStatus() { return status; }
    public void setStatus(GuestIdentityStatus status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Long getMergedToUserId() { return mergedToUserId; }
    public void setMergedToUserId(Long mergedToUserId) { this.mergedToUserId = mergedToUserId; }
    public Instant getMergedAt() { return mergedAt; }
    public void setMergedAt(Instant mergedAt) { this.mergedAt = mergedAt; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static GuestIdentityBuilder builder() { return new GuestIdentityBuilder(); }

    public static class GuestIdentityBuilder {
        private Long id;
        private String guestSessionHash;
        private String email;
        private String mobile;
        private GuestIdentityStatus status;
        private Instant expiresAt;
        private Long mergedToUserId;
        private Instant mergedAt;
        private String ipAddress;
        private String userAgent;

        public GuestIdentityBuilder id(Long id) { this.id = id; return this; }
        public GuestIdentityBuilder guestSessionHash(String guestSessionHash) { this.guestSessionHash = guestSessionHash; return this; }
        public GuestIdentityBuilder email(String email) { this.email = email; return this; }
        public GuestIdentityBuilder mobile(String mobile) { this.mobile = mobile; return this; }
        public GuestIdentityBuilder status(GuestIdentityStatus status) { this.status = status; return this; }
        public GuestIdentityBuilder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public GuestIdentityBuilder mergedToUserId(Long mergedToUserId) { this.mergedToUserId = mergedToUserId; return this; }
        public GuestIdentityBuilder mergedAt(Instant mergedAt) { this.mergedAt = mergedAt; return this; }
        public GuestIdentityBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public GuestIdentityBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }

        public GuestIdentity build() {
            GuestIdentity g = new GuestIdentity();
            g.setId(this.id);
            g.setGuestSessionHash(this.guestSessionHash);
            g.setEmail(this.email);
            g.setMobile(this.mobile);
            g.setStatus(this.status);
            g.setExpiresAt(this.expiresAt);
            g.setMergedToUserId(this.mergedToUserId);
            g.setMergedAt(this.mergedAt);
            g.setIpAddress(this.ipAddress);
            g.setUserAgent(this.userAgent);
            return g;
        }
    }
}
