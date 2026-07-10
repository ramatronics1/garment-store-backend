package com.garmentstore.auth.application;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="app.security.otp") public record OtpProperties(long registrationExpiryMinutes,int maxAttempts,boolean exposeDevOtp){}
