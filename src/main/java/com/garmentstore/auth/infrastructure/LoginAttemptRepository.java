package com.garmentstore.auth.infrastructure;

import com.garmentstore.auth.domain.LoginAttempt;
import com.garmentstore.auth.domain.LoginAttemptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    @Query("""
            select count(la) from LoginAttempt la
            where la.emailOrMobile = :identifier
              and la.attemptType = :attemptType
              and la.success = false
              and la.attemptedAt > coalesce(
                    (select max(ls.attemptedAt) from LoginAttempt ls
                     where ls.emailOrMobile = :identifier
                       and ls.attemptType = :attemptType
                       and ls.success = true), :epoch)
            """)
    long countFailuresAfterLastSuccess(@Param("identifier") String identifier,
                                       @Param("attemptType") LoginAttemptType attemptType,
                                       @Param("epoch") Instant epoch);
}
