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
@Table(name = "login_attempts", indexes = {
    @Index(name = "idx_login_attempts_user", columnList = "user_id"),
    @Index(name = "idx_login_attempts_identifier", columnList = "email_or_mobile"),
    @Index(name = "idx_login_attempts_time", columnList = "attempted_at")
})
public class LoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "email_or_mobile", length = 180)
    private String emailOrMobile;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_type", nullable = false, length = 40)
    private LoginAttemptType attemptType;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    @Column(name = "ip_address", length = 80)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @PrePersist
    void prePersist() {
        this.attemptedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getEmailOrMobile() { return emailOrMobile; }
    public void setEmailOrMobile(String emailOrMobile) { this.emailOrMobile = emailOrMobile; }
    public LoginAttemptType getAttemptType() { return attemptType; }
    public void setAttemptType(LoginAttemptType attemptType) { this.attemptType = attemptType; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public Instant getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(Instant attemptedAt) { this.attemptedAt = attemptedAt; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public static LoginAttemptBuilder builder() { return new LoginAttemptBuilder(); }

    public static class LoginAttemptBuilder {
        private Long id;
        private User user;
        private String emailOrMobile;
        private LoginAttemptType attemptType;
        private boolean success;
        private Instant attemptedAt;
        private String ipAddress;
        private String userAgent;

        public LoginAttemptBuilder id(Long id) { this.id = id; return this; }
        public LoginAttemptBuilder user(User user) { this.user = user; return this; }
        public LoginAttemptBuilder emailOrMobile(String emailOrMobile) { this.emailOrMobile = emailOrMobile; return this; }
        public LoginAttemptBuilder attemptType(LoginAttemptType attemptType) { this.attemptType = attemptType; return this; }
        public LoginAttemptBuilder success(boolean success) { this.success = success; return this; }
        public LoginAttemptBuilder attemptedAt(Instant attemptedAt) { this.attemptedAt = attemptedAt; return this; }
        public LoginAttemptBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public LoginAttemptBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }

        public LoginAttempt build() {
            LoginAttempt l = new LoginAttempt();
            l.setId(this.id);
            l.setUser(this.user);
            l.setEmailOrMobile(this.emailOrMobile);
            l.setAttemptType(this.attemptType);
            l.setSuccess(this.success);
            l.setAttemptedAt(this.attemptedAt);
            l.setIpAddress(this.ipAddress);
            l.setUserAgent(this.userAgent);
            return l;
        }
    }
}
