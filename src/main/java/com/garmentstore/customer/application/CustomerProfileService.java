package com.garmentstore.customer.application;

import com.garmentstore.auth.application.RefreshTokenService;
import com.garmentstore.auth.domain.AccountStatus;
import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.domain.UserType;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.common.audit.AuditLogService;
import com.garmentstore.common.exception.BusinessException;
import com.garmentstore.customer.domain.CustomerProfile;
import com.garmentstore.customer.dto.ChangePasswordRequest;
import com.garmentstore.customer.dto.ChangePasswordResponse;
import com.garmentstore.customer.dto.ProfileResponse;
import com.garmentstore.customer.dto.UpdateProfileRequest;
import com.garmentstore.customer.infrastructure.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {
    private final UserRepository users;
    private final CustomerProfileRepository profiles;
    private final PasswordEncoder encoder;
    private final RefreshTokenService refresh;
    private final AuditLogService audit;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long uid) {
        User u = customer(uid);
        CustomerProfile p = profiles.findByUserId(uid).orElseGet(() -> fromUser(u));
        return resp(u, p);
    }

    @Transactional
    public ProfileResponse updateProfile(Long uid, UpdateProfileRequest r) {
        User u = customer(uid);
        CustomerProfile p = profiles.findByUserId(uid).orElseGet(() -> fromUser(u));
        p.setUser(u);
        p.setFirstName(r.firstName().trim());
        p.setLastName(blank(r.lastName()));
        p.setProfileImageUrl(blank(r.profileImageUrl()));
        p = profiles.save(p);
        u.setName(p.getFirstName() + (p.getLastName() == null ? "" : " " + p.getLastName()));
        users.save(u);
        audit.record(uid, "CUSTOMER", "CUSTOMER_PROFILE_UPDATED", "CUSTOMER_PROFILE", String.valueOf(p.getId()), null, null);
        return resp(u, p);
    }

    @Transactional
    public ChangePasswordResponse changePassword(Long uid, ChangePasswordRequest r) {
        User u = customer(uid);
        if (!encoder.matches(r.currentPassword(), u.getPasswordHash()))
            throw new BusinessException("INVALID_CURRENT_PASSWORD", "Current password is incorrect", HttpStatus.BAD_REQUEST);
        if (encoder.matches(r.newPassword(), u.getPasswordHash()))
            throw new BusinessException("PASSWORD_REUSE_NOT_ALLOWED", "New password must be different", HttpStatus.BAD_REQUEST);
        u.setPasswordHash(encoder.encode(r.newPassword()));
        users.save(u);
        int revoked = refresh.revokeAllForUser(uid);
        audit.record(uid, "CUSTOMER", "CUSTOMER_PASSWORD_CHANGED", "USER", String.valueOf(uid), "{\"revokedRefreshTokenCount\":" + revoked + "}", null);
        return new ChangePasswordResponse("PASSWORD_CHANGED", revoked);
    }

    private User customer(Long uid) {
        User u = users.findById(uid).orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        if (u.getAccountStatus() != AccountStatus.ACTIVE)
            throw new BusinessException("ACCOUNT_NOT_ACTIVE", "Account is not active", HttpStatus.FORBIDDEN);
        return u;
    }

    private CustomerProfile fromUser(User u) {
        String n = u.getName() == null ? "Customer" : u.getName().trim();
        String[] p = n.split("\s+", 2);
        return CustomerProfile.builder().user(u).firstName(p.length > 0 ? p[0] : "Customer").lastName(p.length > 1 ? p[1] : null).build();
    }

    private ProfileResponse resp(User u, CustomerProfile p) {
        String full = p.getFirstName() + (p.getLastName() == null ? "" : " " + p.getLastName());
        return new ProfileResponse(u.getId(), p.getId(), p.getFirstName(), p.getLastName(), full, u.getEmail(), u.getMobile(), u.isEmailVerified(), u.isMobileVerified(), p.getProfileImageUrl());
    }

    private String blank(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
