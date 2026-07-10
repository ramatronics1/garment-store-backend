package com.garmentstore.auth.application;

import com.garmentstore.auth.domain.*;
import com.garmentstore.auth.dto.*;
import com.garmentstore.auth.infrastructure.LoginAttemptRepository;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.common.exception.BusinessException;
import com.garmentstore.common.audit.AuditLogService;
import com.garmentstore.common.security.JwtProperties;
import com.garmentstore.common.security.JwtService;
import com.garmentstore.common.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private static final Instant EPOCH = Instant.parse("1970-01-01T00:00:00Z");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final LoginAttemptRepository loginAttemptRepository;
    private final AdminAuthProperties adminAuthProperties;
        private final AuditLogService auditLogService;

    @Transactional
    public AuthTokenResponse login(AdminLoginRequest request, String ipAddress, String userAgent) {
        String identifier = normalizeIdentifier(request.identifier());
        User user = userRepository.findByEmailOrMobile(identifier).orElse(null);

        if (user == null || !isAdminUser(user)) {
            loginAttemptService.record(user, identifier, LoginAttemptType.ADMIN_LOGIN, false, ipAddress, userAgent);
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            loginAttemptService.record(user, identifier, LoginAttemptType.ADMIN_LOGIN, false, ipAddress, userAgent);
            throw new BusinessException("ADMIN_ACCOUNT_LOCKED", "Admin account is locked", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptService.record(user, identifier, LoginAttemptType.ADMIN_LOGIN, false, ipAddress, userAgent);
            long failedCount = loginAttemptRepository.countFailuresAfterLastSuccess(identifier, LoginAttemptType.ADMIN_LOGIN, EPOCH);
            if (failedCount >= adminAuthProperties.lockoutThreshold()) {
                user.setAccountStatus(AccountStatus.LOCKED);
                userRepository.save(user);
                auditLogService.record(user.getId(), "ADMIN", "ADMIN_ACCOUNT_LOCKED", "USER", String.valueOf(user.getId()), null, ipAddress);
                    throw new BusinessException("ADMIN_ACCOUNT_LOCKED", "Admin account locked after failed login attempts", HttpStatus.FORBIDDEN);
            }
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        validateAdminLoginAllowed(user);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        loginAttemptService.record(user, identifier, LoginAttemptType.ADMIN_LOGIN, true, ipAddress, userAgent);
            auditLogService.record(user.getId(), "ADMIN", "ADMIN_LOGIN_SUCCESS", "USER", String.valueOf(user.getId()), null, ipAddress);

        RefreshTokenService.CreatedRefreshToken refresh = refreshTokenService.create(user, ipAddress, userAgent);
        return buildTokenResponse(user, refresh.rawToken(), refresh.expiresAt());
    }

    @Transactional
    public AuthTokenResponse refresh(RefreshTokenRequest request, String ipAddress, String userAgent) {
        RefreshTokenService.TokenRotationResult rotation = refreshTokenService.rotate(request.refreshToken(), ipAddress, userAgent);
        User user = rotation.user();
        validateAdminLoginAllowed(user);
        return buildTokenResponse(user, rotation.rawRefreshToken(), rotation.refreshTokenExpiresAt());
    }

    @Transactional(readOnly = true)
    public MeResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        validateAdminLoginAllowed(user);
        return new MeResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobile(),
                user.getUserType().name(),
                user.getAccountStatus().name(),
                user.isEmailVerified(),
                user.isMobileVerified(),
                roleCodes(user)
        );
    }

    private AuthTokenResponse buildTokenResponse(User user, String rawRefreshToken, Instant refreshExpiresAt) {
        List<String> roles = roleCodes(user);
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
        Instant accessExpiresAt = Instant.now().plusSeconds(jwtProperties.accessTokenExpiryMinutes() * 60);
        return new AuthTokenResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                accessExpiresAt,
                refreshExpiresAt,
                new AuthTokenResponse.UserSummary(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getMobile(),
                        user.getUserType().name(),
                        user.getAccountStatus().name(),
                        roles
                )
        );
    }

    private void validateAdminLoginAllowed(User user) {
        if (!isAdminUser(user)) {
            throw new BusinessException("ADMIN_ACCESS_REQUIRED", "Admin access required", HttpStatus.FORBIDDEN);
        }
        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            throw new BusinessException("ADMIN_ACCOUNT_LOCKED", "Admin account is locked", HttpStatus.FORBIDDEN);
        }
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("ACCOUNT_NOT_ACTIVE", "Account is not active", HttpStatus.FORBIDDEN);
        }
    }

    private boolean isAdminUser(User user) {
        if (user == null || user.getUserType() != UserType.ADMIN) {
            return false;
        }
        Set<String> roles = user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet());
        return roles.contains(SecurityConstants.ROLE_ADMIN) || roles.contains(SecurityConstants.ROLE_SUPER_ADMIN);
    }

    private List<String> roleCodes(User user) {
        return user.getRoles().stream().map(Role::getCode).sorted().toList();
    }

    private String normalizeIdentifier(String identifier) {
        return identifier == null ? null : identifier.trim().toLowerCase(Locale.ROOT);
    }
}
