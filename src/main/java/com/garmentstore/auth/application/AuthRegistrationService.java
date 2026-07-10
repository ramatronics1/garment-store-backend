package com.garmentstore.auth.application;

import com.garmentstore.auth.application.event.*;
import com.garmentstore.auth.domain.*;
import com.garmentstore.auth.dto.*;
import com.garmentstore.auth.infrastructure.*;
import com.garmentstore.common.exception.BusinessException;
import com.garmentstore.common.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthRegistrationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpGenerator otpGenerator;
    private final OtpDeliveryService otpDeliveryService;
    private final OtpProperties otpProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String mobile = normalizeMobile(request.mobile());
        if (email != null && userRepository.findByEmailIgnoreCase(email).isPresent())
            throw new BusinessException("EMAIL_ALREADY_REGISTERED", "Email is already registered", HttpStatus.CONFLICT);
        if (mobile != null && userRepository.findByMobile(mobile).isPresent())
            throw new BusinessException("MOBILE_ALREADY_REGISTERED", "Mobile is already registered", HttpStatus.CONFLICT);
        Role customerRole = roleRepository.findByCode(SecurityConstants.ROLE_CUSTOMER).orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND", "Customer role is not configured", HttpStatus.INTERNAL_SERVER_ERROR));
        User user = User.builder().userType(UserType.CUSTOMER).name(request.name().trim()).email(email).mobile(mobile).passwordHash(passwordEncoder.encode(request.password())).accountStatus(AccountStatus.PENDING_VERIFICATION).emailVerified(false).mobileVerified(false).roles(new HashSet<>()).build();
        user.getRoles().add(customerRole);
        user = userRepository.save(user);
        OtpCreation otp = createRegistrationOtp(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), user.getMobile(), Instant.now()));
        return new RegisterResponse(user.getId(), user.getAccountStatus().name(), otp.target(), otp.channel().name(), otp.expiresAt(), otpProperties.exposeDevOtp() ? otp.otp() : null);
    }

    @Transactional
    public VerifyOtpResponse verifyRegistrationOtp(VerifyOtpRequest request) {
        OtpToken token = findOtpToken(request);
        if (token.isVerified())
            throw new BusinessException("OTP_ALREADY_VERIFIED", "OTP is already verified", HttpStatus.CONFLICT);
        if (token.isExpired()) throw new BusinessException("OTP_EXPIRED", "OTP has expired", HttpStatus.BAD_REQUEST);
        if (token.getAttemptCount() >= otpProperties.maxAttempts())
            throw new BusinessException("OTP_ATTEMPTS_EXCEEDED", "Maximum OTP attempts exceeded", HttpStatus.TOO_MANY_REQUESTS);
        token.setAttemptCount(token.getAttemptCount() + 1);
        if (!passwordEncoder.matches(request.otp(), token.getOtpCodeHash())) {
            otpTokenRepository.save(token);
            throw new BusinessException("INVALID_OTP", "Invalid OTP", HttpStatus.BAD_REQUEST);
        }
        User user = token.getUser();
        token.setVerifiedAt(Instant.now());
        if (token.getDeliveryChannel() == OtpDeliveryChannel.EMAIL) user.setEmailVerified(true);
        else user.setMobileVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        otpTokenRepository.save(token);
        eventPublisher.publishEvent(new OtpVerifiedEvent(user.getId(), OtpPurpose.REGISTRATION, token.getVerifiedAt()));
        return new VerifyOtpResponse(user.getId(), user.getAccountStatus().name(), user.isEmailVerified(), user.isMobileVerified());
    }

    private OtpToken findOtpToken(VerifyOtpRequest r) {
        if (r.userId() != null)
            return otpTokenRepository.findTopByUserIdAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc(r.userId(), OtpPurpose.REGISTRATION).orElseThrow(() -> new BusinessException("OTP_NOT_FOUND", "No active OTP found", HttpStatus.NOT_FOUND));
        String id = normalizeMobile(r.mobile());
        if (id == null) id = normalizeEmail(r.email());
        if (id == null)
            throw new BusinessException("IDENTIFIER_REQUIRED", "userId or email or mobile is required", HttpStatus.BAD_REQUEST);
        return otpTokenRepository.findTopByContactIdentifierAndPurposeAndVerifiedAtIsNullOrderByCreatedAtDesc(id, OtpPurpose.REGISTRATION).orElseThrow(() -> new BusinessException("OTP_NOT_FOUND", "No active OTP found", HttpStatus.NOT_FOUND));
    }

    private OtpCreation createRegistrationOtp(User user) {
        String otp = otpGenerator.generateSixDigitOtp();
        String target;
        OtpDeliveryChannel channel;
        if (user.getMobile() != null && !user.getMobile().isBlank()) {
            target = user.getMobile();
            channel = OtpDeliveryChannel.MOBILE;
        } else {
            target = user.getEmail();
            channel = OtpDeliveryChannel.EMAIL;
        }
        Instant expiresAt = Instant.now().plusSeconds(otpProperties.registrationExpiryMinutes() * 60);
        OtpToken token = OtpToken.builder().user(user).contactIdentifier(target).purpose(OtpPurpose.REGISTRATION).deliveryChannel(channel).otpCodeHash(passwordEncoder.encode(otp)).expiresAt(expiresAt).attemptCount(0).build();
        otpTokenRepository.save(token);
        otpDeliveryService.sendOtp(target, channel, otp);
        return new OtpCreation(otp, target, channel, expiresAt);
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeMobile(String mobile) {
        return mobile == null || mobile.isBlank() ? null : mobile.trim();
    }

    private record OtpCreation(String otp, String target, OtpDeliveryChannel channel, Instant expiresAt) {
    }
}
