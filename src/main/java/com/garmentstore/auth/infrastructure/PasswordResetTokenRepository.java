package com.garmentstore.auth.infrastructure;

import com.garmentstore.auth.domain.PasswordResetToken;
import com.garmentstore.auth.domain.PasswordResetTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update PasswordResetToken prt set prt.status = :newStatus where prt.user.id = :userId and prt.status = :currentStatus")
    int updateStatusByUserIdAndCurrentStatus(@Param("userId") Long userId,
                                             @Param("currentStatus") PasswordResetTokenStatus currentStatus,
                                             @Param("newStatus") PasswordResetTokenStatus newStatus);

    @Modifying
    @Query("update PasswordResetToken prt set prt.status = :expiredStatus where prt.status = :activeStatus and prt.expiresAt < :now")
    int markExpiredTokens(@Param("now") Instant now,
                          @Param("activeStatus") PasswordResetTokenStatus activeStatus,
                          @Param("expiredStatus") PasswordResetTokenStatus expiredStatus);
}
