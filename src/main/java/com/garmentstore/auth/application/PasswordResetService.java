package com.garmentstore.auth.application;

import com.garmentstore.auth.domain.*;
import com.garmentstore.auth.dto.*;
import com.garmentstore.auth.infrastructure.PasswordResetTokenRepository;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.common.exception.BusinessException;
import com.garmentstore.common.audit.AuditLogService;
import com.garmentstore.common.security.TokenHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final String GENERIC_FORGOT_MESSAGE = "If an account exists for the provided identifier, password reset instructions will be sent.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenHashService tokenHashService;
    private final PasswordResetProperties passwordResetProperties;
    private final PasswordResetDeliveryService passwordResetDeliveryService;
    private final RefreshTokenService refreshTokenService;
        private final AuditLogService auditLogService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request, String ipAddress, String userAgent) {
        String identifier = normalizeIdentifier(request.identifier());
        User user = userRepository.findByEmailOrMobile(identifier).orElse(null);

        if (user == null || user.getAccountStatus() == AccountStatus.DELETED || user.getAccountStatus() == AccountStatus.DISABLED) {
            return new ForgotPasswordResponse("REQUEST_ACCEPTED", GENERIC_FORGOT_MESSAGE, null, null);
        }

        passwordResetTokenRepository.updateStatusByUserIdAndCurrentStatus(
                user.getId(),
                PasswordResetTokenStatus.ACTIVE,
                PasswordResetTokenStatus.REVOKED
        );

        String rawToken = generateSecureToken();
        Instant expiresAt = Instant.now().plusSeconds(passwordResetProperties.tokenExpiryMinutes() * 60);
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHashService.sha256(rawToken))
                .status(PasswordResetTokenStatus.ACTIVE)
                .expiresAt(expiresAt)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        passwordResetTokenRepository.save(resetToken);

        String target = user.getEmail() != null ? user.getEmail() : user.getMobile();
        passwordResetDeliveryService.sendResetToken(target, rawToken);

        return new ForgotPasswordResponse(
                "REQUEST_ACCEPTED",
                GENERIC_FORGOT_MESSAGE,
                passwordResetProperties.exposeDevToken() ? expiresAt : null,
                passwordResetProperties.exposeDevToken() ? rawToken : null
        );
    }

    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHashService.sha256(request.resetToken()))
                .orElseThrow(() -> new BusinessException("INVALID_RESET_TOKEN", "Invalid or expired reset token", HttpStatus.BAD_REQUEST));

        if (token.getStatus() != PasswordResetTokenStatus.ACTIVE) {
            throw new BusinessException("INVALID_RESET_TOKEN", "Invalid or expired reset token", HttpStatus.BAD_REQUEST);
        }
        if (token.isExpired()) {
            token.setStatus(PasswordResetTokenStatus.EXPIRED);
            passwordResetTokenRepository.save(token);
            throw new BusinessException("RESET_TOKEN_EXPIRED", "Reset token has expired", HttpStatus.BAD_REQUEST);
        }

        User user = token.getUser();
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException("PASSWORD_REUSE_NOT_ALLOWED", "New password must be different from old password", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
        token.setStatus(PasswordResetTokenStatus.USED);
        token.setUsedAt(Instant.now());

        passwordResetTokenRepository.save(token);
        int revokedCount = refreshTokenService.revokeAllForUser(user.getId());
        auditLogService.record(user.getId(), null, "PASSWORD_RESET_SUCCESS", "USER", String.valueOf(user.getId()), "{\"revokedRefreshTokenCount\":" + revokedCount + "}", null);
            return new ResetPasswordResponse("PASSWORD_RESET_SUCCESSFUL", revokedCount);
    }

    private String normalizeIdentifier(String identifier) {
        return identifier == null ? null : identifier.trim().toLowerCase(Locale.ROOT);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
