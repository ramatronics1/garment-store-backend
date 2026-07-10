package com.garmentstore.auth.application;

import com.garmentstore.auth.domain.LoginAttempt;
import com.garmentstore.auth.domain.LoginAttemptType;
import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.infrastructure.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(User user, String identifier, LoginAttemptType attemptType, boolean success, String ipAddress, String userAgent) {
        loginAttemptRepository.save(LoginAttempt.builder()
                .user(user)
                .emailOrMobile(identifier)
                .attemptType(attemptType)
                .success(success)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build());
    }
}
