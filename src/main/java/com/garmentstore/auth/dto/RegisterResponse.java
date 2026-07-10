package com.garmentstore.auth.dto;
import java.time.Instant;
public record RegisterResponse(Long userId,String accountStatus,String verificationTarget,String deliveryChannel,Instant otpExpiresAt,String devOtp){}
