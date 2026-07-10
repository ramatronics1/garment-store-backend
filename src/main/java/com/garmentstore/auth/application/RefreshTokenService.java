package com.garmentstore.auth.application;

import com.garmentstore.auth.domain.RefreshToken;
import com.garmentstore.auth.domain.RefreshTokenStatus;
import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.infrastructure.RefreshTokenRepository;
import com.garmentstore.common.exception.BusinessException;
import com.garmentstore.common.security.JwtProperties;
import com.garmentstore.common.security.TokenHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashService tokenHashService;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public CreatedRefreshToken create(User user, String ipAddress, String userAgent) {
        String rawToken = generateSecureToken();
        Instant expiresAt = Instant.now().plusSeconds(jwtProperties.refreshTokenExpiryDays() * 24 * 60 * 60);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHashService.sha256(rawToken))
                .status(RefreshTokenStatus.ACTIVE)
                .expiresAt(expiresAt)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        refreshToken = refreshTokenRepository.save(refreshToken);
        return new CreatedRefreshToken(refreshToken.getId(), rawToken, expiresAt);
    }

    @Transactional
    public TokenRotationResult rotate(String rawRefreshToken, String ipAddress, String userAgent) {
        String tokenHash = tokenHashService.sha256(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (existing.getStatus() != RefreshTokenStatus.ACTIVE) {
            if (existing.getStatus() == RefreshTokenStatus.REVOKED && existing.getReplacedByTokenId() != null) {
                revokeAllForUserDueToTokenReuse(existing.getUser());
                throw new BusinessException("REFRESH_TOKEN_REUSE_DETECTED", "Refresh token reuse detected. All sessions revoked.", HttpStatus.UNAUTHORIZED);
            }
            throw new BusinessException("REFRESH_TOKEN_NOT_ACTIVE", "Refresh token is not active", HttpStatus.UNAUTHORIZED);
        }
        if (Instant.now().isAfter(existing.getExpiresAt())) {
            existing.setStatus(RefreshTokenStatus.EXPIRED);
            refreshTokenRepository.save(existing);
            throw new BusinessException("REFRESH_TOKEN_EXPIRED", "Refresh token has expired", HttpStatus.UNAUTHORIZED);
        }

        existing.setStatus(RefreshTokenStatus.REVOKED);
        existing.setRevokedAt(Instant.now());
        RefreshToken savedExisting = refreshTokenRepository.save(existing);

        CreatedRefreshToken created = create(existing.getUser(), ipAddress, userAgent);
        savedExisting.setReplacedByTokenId(created.id());
        refreshTokenRepository.save(savedExisting);

        return new TokenRotationResult(existing.getUser(), created.rawToken(), created.expiresAt());
    }

    @Transactional
    public int revokeCurrent(String rawRefreshToken) {
        String tokenHash = tokenHashService.sha256(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (existing.getStatus() == RefreshTokenStatus.ACTIVE) {
            existing.setStatus(RefreshTokenStatus.REVOKED);
            existing.setRevokedAt(Instant.now());
            refreshTokenRepository.save(existing);
            return 1;
        }
        return 0;
    }

    @Transactional
    public int revokeAllForUser(Long userId) {
        return refreshTokenRepository.updateTokenStatusByUserIdAndCurrentStatus(
                userId,
                RefreshTokenStatus.ACTIVE,
                RefreshTokenStatus.REVOKED,
                Instant.now()
        );
    }

    @Transactional
    public void revokeAllForUserDueToTokenReuse(User user) {
        refreshTokenRepository.updateTokenStatusByUserIdAndCurrentStatus(
                user.getId(),
                RefreshTokenStatus.ACTIVE,
                RefreshTokenStatus.REVOKED,
                Instant.now()
        );
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record CreatedRefreshToken(Long id, String rawToken, Instant expiresAt) {}
    public record TokenRotationResult(User user, String rawRefreshToken, Instant refreshTokenExpiresAt) {}
}
