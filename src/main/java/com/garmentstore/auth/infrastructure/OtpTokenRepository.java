package com.garmentstore.auth.infrastructure;

import com.garmentstore.auth.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByUserIdAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc(Long userId, OtpPurpose purpose);

    Optional<OtpToken> findTopByContactIdentifierAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc(String mobileOrEmail, OtpPurpose purpose);
}
