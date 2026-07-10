package com.garmentstore.auth.infrastructure;

import com.garmentstore.auth.domain.RefreshToken;
import com.garmentstore.auth.domain.RefreshTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findByUserIdAndStatus(Long userId, RefreshTokenStatus status);

    @Modifying
    @Query("update RefreshToken rt set rt.status = :newStatus, rt.revokedAt = :revokedAt where rt.user.id = :userId and rt.status = :currentStatus")
    int updateTokenStatusByUserIdAndCurrentStatus(@Param("userId") Long userId,
                                                  @Param("currentStatus") RefreshTokenStatus currentStatus,
                                                  @Param("newStatus") RefreshTokenStatus newStatus,
                                                  @Param("revokedAt") Instant revokedAt);
}
