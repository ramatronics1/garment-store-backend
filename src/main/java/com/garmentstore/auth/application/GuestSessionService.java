package com.garmentstore.auth.application;

import com.garmentstore.auth.domain.GuestIdentity;
import com.garmentstore.auth.domain.GuestIdentityStatus;
import com.garmentstore.auth.dto.GuestSessionRequest;
import com.garmentstore.auth.dto.GuestSessionResponse;
import com.garmentstore.auth.infrastructure.GuestIdentityRepository;
import com.garmentstore.common.security.TokenHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GuestSessionService {
    private final GuestIdentityRepository guestIdentityRepository;
    private final TokenHashService tokenHashService;
    private final GuestSessionProperties guestSessionProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public GuestSessionResponse createGuestSession(GuestSessionRequest request, String ipAddress, String userAgent) {
        String rawGuestSessionId = generateGuestSessionId();
        Instant expiresAt = Instant.now().plusSeconds(guestSessionProperties.expiryDays() * 24 * 60 * 60);

        GuestIdentity guestIdentity = GuestIdentity.builder()
                .guestSessionHash(tokenHashService.sha256(rawGuestSessionId))
                .email(normalizeEmail(request.email()))
                .mobile(normalizeMobile(request.mobile()))
                .status(GuestIdentityStatus.ACTIVE)
                .expiresAt(expiresAt)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        guestIdentity = guestIdentityRepository.save(guestIdentity);
        return new GuestSessionResponse(
                guestIdentity.getId(),
                rawGuestSessionId,
                guestIdentity.getStatus().name(),
                guestIdentity.getExpiresAt()
        );
    }

    @Transactional(readOnly = true)
    public GuestIdentity validateActiveGuestSession(String rawGuestSessionId) {
        GuestIdentity guestIdentity = guestIdentityRepository.findByGuestSessionHash(tokenHashService.sha256(rawGuestSessionId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid guest session"));
        if (guestIdentity.getStatus() != GuestIdentityStatus.ACTIVE || guestIdentity.isExpired()) {
            throw new IllegalArgumentException("Guest session is not active");
        }
        return guestIdentity;
    }

    private String generateGuestSessionId() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return "gst_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeMobile(String mobile) {
        return mobile == null || mobile.isBlank() ? null : mobile.trim();
    }
}
