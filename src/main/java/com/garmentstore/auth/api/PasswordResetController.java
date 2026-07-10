package com.garmentstore.auth.api;

import com.garmentstore.auth.application.PasswordResetService;
import com.garmentstore.auth.dto.*;
import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.common.util.RequestMetadataExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;
    private final RequestMetadataExtractor requestMetadataExtractor;

    @PostMapping("/forgot-password")
    public ApiResponse<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                              HttpServletRequest servletRequest) {
        return ApiResponse.success("Password reset request accepted", passwordResetService.forgotPassword(
                request,
                requestMetadataExtractor.clientIp(servletRequest),
                requestMetadataExtractor.userAgent(servletRequest)
        ));
    }

    @PostMapping("/reset-password")
    public ApiResponse<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.success("Password reset successful", passwordResetService.resetPassword(request));
    }
}
