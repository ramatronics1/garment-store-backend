package com.garmentstore.auth.api;

import com.garmentstore.auth.application.AuthRegistrationService;
import com.garmentstore.auth.dto.*;
import com.garmentstore.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthRegistrationController {
    private final AuthRegistrationService authRegistrationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Registration successful. Please verify OTP.", authRegistrationService.register(request));
    }

    @PostMapping("/verify-otp")
    public ApiResponse<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ApiResponse.success("OTP verified successfully", authRegistrationService.verifyRegistrationOtp(request));
    }
}
