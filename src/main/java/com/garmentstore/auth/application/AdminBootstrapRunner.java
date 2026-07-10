package com.garmentstore.auth.application;

import com.garmentstore.auth.domain.*;
import com.garmentstore.auth.infrastructure.RoleRepository;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.common.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;

@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {
    private final AdminAuthProperties adminAuthProperties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!adminAuthProperties.bootstrapEnabled()) {
            return;
        }
        String email = normalizeEmail(adminAuthProperties.bootstrapEmail());
        if (email == null || userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }

        Role adminRole = roleRepository.findByCode(SecurityConstants.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().code(SecurityConstants.ROLE_ADMIN).name("Admin").build()));

        User admin = User.builder()
                .userType(UserType.ADMIN)
                .name(adminAuthProperties.bootstrapName())
                .email(email)
                .mobile(adminAuthProperties.bootstrapMobile())
                .passwordHash(passwordEncoder.encode(adminAuthProperties.bootstrapPassword()))
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .mobileVerified(true)
                .roles(new HashSet<>())
                .build();
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
        log.info("Default admin user bootstrapped with email {}. Disable ADMIN_BOOTSTRAP_ENABLED in production after first setup.", email);
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
