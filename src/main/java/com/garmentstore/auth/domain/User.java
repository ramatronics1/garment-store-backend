package com.garmentstore.auth.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email",  columnList = "email"),
    @Index(name = "idx_users_mobile", columnList = "mobile"),
    @Index(name = "idx_users_status", columnList = "account_status")
})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 30)
    private UserType userType;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "email", length = 180, unique = true)
    private String email;

    @Column(name = "mobile", length = 20, unique = true)
    private String mobile;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 40)
    private AccountStatus accountStatus;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "mobile_verified", nullable = false)
    private boolean mobileVerified;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public boolean isMobileVerified() { return mobileVerified; }
    public void setMobileVerified(boolean mobileVerified) { this.mobileVerified = mobileVerified; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private UserType userType;
        private String name;
        private String email;
        private String mobile;
        private String passwordHash;
        private AccountStatus accountStatus;
        private boolean emailVerified;
        private boolean mobileVerified;
        private Instant lastLoginAt;
        private Set<Role> roles = new HashSet<>();

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder userType(UserType userType) { this.userType = userType; return this; }
        public UserBuilder name(String name) { this.name = name; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder mobile(String mobile) { this.mobile = mobile; return this; }
        public UserBuilder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public UserBuilder accountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; return this; }
        public UserBuilder emailVerified(boolean emailVerified) { this.emailVerified = emailVerified; return this; }
        public UserBuilder mobileVerified(boolean mobileVerified) { this.mobileVerified = mobileVerified; return this; }
        public UserBuilder lastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; return this; }
        public UserBuilder roles(Set<Role> roles) { this.roles = roles; return this; }

        public User build() {
            User user = new User();
            user.setId(this.id);
            user.setUserType(this.userType);
            user.setName(this.name);
            user.setEmail(this.email);
            user.setMobile(this.mobile);
            user.setPasswordHash(this.passwordHash);
            user.setAccountStatus(this.accountStatus);
            user.setEmailVerified(this.emailVerified);
            user.setMobileVerified(this.mobileVerified);
            user.setLastLoginAt(this.lastLoginAt);
            user.setRoles(this.roles != null ? this.roles : new HashSet<>());
            return user;
        }
    }
}