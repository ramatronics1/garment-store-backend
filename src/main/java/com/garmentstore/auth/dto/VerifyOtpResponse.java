package com.garmentstore.auth.dto;
public record VerifyOtpResponse(Long userId,String accountStatus,boolean emailVerified,boolean mobileVerified){}
