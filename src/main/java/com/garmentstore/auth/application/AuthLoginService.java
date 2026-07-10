package com.garmentstore.auth.application;

import com.garmentstore.auth.domain.AccountStatus;
import com.garmentstore.auth.domain.LoginAttemptType;
import com.garmentstore.auth.domain.Role;
import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.dto.AuthTokenResponse;
import com.garmentstore.auth.dto.LoginRequest;
import com.garmentstore.auth.dto.MeResponse;
import com.garmentstore.auth.dto.RefreshTokenRequest;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.common.exception.BusinessException;
import com.garmentstore.common.audit.AuditLogService;
import com.garmentstore.common.security.JwtProperties;
import com.garmentstore.common.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthLoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
        private final AuditLogService auditLogService;

    @Transactional
    public AuthTokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String identifier = normalizeIdentifier(request.identifier());
        User user = userRepository.findByEmailOrMobile(identifier).orElse(null);

        if (user == null) {
            loginAttemptService.record(null, identifier, LoginAttemptType.CUSTOMER_LOGIN, false, ipAddress, userAgent);
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptService.record(user, identifier, LoginAttemptType.CUSTOMER_LOGIN, false, ipAddress, userAgent);
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        validateLoginAllowed(user);

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        loginAttemptService.record(user, identifier, LoginAttemptType.CUSTOMER_LOGIN, true, ipAddress, userAgent);
            auditLogService.record(user.getId(), "CUSTOMER", "CUSTOMER_LOGIN_SUCCESS", "USER", String.valueOf(user.getId()), null, ipAddress);

        RefreshTokenService.CreatedRefreshToken refresh = refreshTokenService.create(user, ipAddress, userAgent);
        return buildTokenResponse(user, refresh.rawToken(), refresh.expiresAt());
    }

    @Transactional
    public AuthTokenResponse refresh(RefreshTokenRequest request, String ipAddress, String userAgent) {
        RefreshTokenService.TokenRotationResult rotation = refreshTokenService.rotate(request.refreshToken(), ipAddress, userAgent);
        User user = rotation.user();
        validateLoginAllowed(user);
        return buildTokenResponse(user, rotation.rawRefreshToken(), rotation.refreshTokenExpiresAt());
    }

    @Transactional(readOnly = true)
    public MeResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
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

    private void validateLoginAllowed(User user) {
        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            throw new BusinessException("ACCOUNT_NOT_VERIFIED", "Please verify OTP before login", HttpStatus.FORBIDDEN);
        }
        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            throw new BusinessException("ACCOUNT_LOCKED", "Account is locked", HttpStatus.FORBIDDEN);
        }
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("ACCOUNT_NOT_ACTIVE", "Account is not active", HttpStatus.FORBIDDEN);
        }
    }

    private List<String> roleCodes(User user) {
        return user.getRoles().stream().map(Role::getCode).sorted().toList();
    }

    private String normalizeIdentifier(String identifier) {
        return identifier == null ? null : identifier.trim().toLowerCase(Locale.ROOT);
    }
}
