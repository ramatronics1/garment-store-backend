package com.garmentstore.auth.infrastructure;

import com.garmentstore.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByMobile(String mobile);

    @Query("select u from User u where lower(u.email) = lower(:identifier) or u.mobile = :identifier")
    Optional<User> findByEmailOrMobile(@Param("identifier") String identifier);

    /** Returns the first admin user — used by the notification module to route admin alerts. */
    @Query("select u from User u join u.roles r where r.code = 'ADMIN' or r.code = 'ROLE_ADMIN' order by u.id asc")
    Optional<User> findFirstAdmin();
}
