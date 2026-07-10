package com.garmentstore.auth.api;

import com.garmentstore.auth.application.AuthLoginService;
import com.garmentstore.auth.dto.AuthTokenResponse;
import com.garmentstore.auth.dto.LoginRequest;
import com.garmentstore.auth.dto.MeResponse;
import com.garmentstore.auth.dto.RefreshTokenRequest;
import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.common.util.RequestMetadataExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthLoginController {
    private final AuthLoginService authLoginService;
    private final RequestMetadataExtractor requestMetadataExtractor;

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest servletRequest) {
        return ApiResponse.success("Login successful", authLoginService.login(
                request,
                requestMetadataExtractor.clientIp(servletRequest),
                requestMetadataExtractor.userAgent(servletRequest)
        ));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                                       HttpServletRequest servletRequest) {
        return ApiResponse.success("Token refreshed successfully", authLoginService.refresh(
                request,
                requestMetadataExtractor.clientIp(servletRequest),
                requestMetadataExtractor.userAgent(servletRequest)
        ));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ApiResponse.success("Current user fetched successfully", authLoginService.me(userId));
    }
}
