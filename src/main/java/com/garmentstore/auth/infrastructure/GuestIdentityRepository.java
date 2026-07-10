package com.garmentstore.auth.infrastructure;

import com.garmentstore.auth.domain.GuestIdentity;
import com.garmentstore.auth.domain.GuestIdentityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface GuestIdentityRepository extends JpaRepository<GuestIdentity, Long> {
    Optional<GuestIdentity> findByGuestSessionHash(String guestSessionHash);

    @Modifying
    @Query("update GuestIdentity gi set gi.status = :expiredStatus where gi.status = :activeStatus and gi.expiresAt < :now")
    int markExpiredSessions(@Param("now") Instant now,
                            @Param("activeStatus") GuestIdentityStatus activeStatus,
                            @Param("expiredStatus") GuestIdentityStatus expiredStatus);
}
