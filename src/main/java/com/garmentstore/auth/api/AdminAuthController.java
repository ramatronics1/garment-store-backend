package com.garmentstore.auth.api;

import com.garmentstore.auth.application.AdminAuthService;
import com.garmentstore.auth.application.AuthLogoutService;
import com.garmentstore.auth.dto.*;
import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.common.util.RequestMetadataExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminAuthService adminAuthService;
    private final AuthLogoutService authLogoutService;
    private final RequestMetadataExtractor requestMetadataExtractor;

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody AdminLoginRequest request,
                                                HttpServletRequest servletRequest) {
        return ApiResponse.success("Admin login successful", adminAuthService.login(
                request,
                requestMetadataExtractor.clientIp(servletRequest),
                requestMetadataExtractor.userAgent(servletRequest)
        ));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                                       HttpServletRequest servletRequest) {
        return ApiResponse.success("Admin token refreshed successfully", adminAuthService.refresh(
                request,
                requestMetadataExtractor.clientIp(servletRequest),
                requestMetadataExtractor.userAgent(servletRequest)
        ));
    }

    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
        return ApiResponse.success("Admin logout successful", authLogoutService.logout(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping("/logout-all")
    public ApiResponse<LogoutResponse> logoutAll(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ApiResponse.success("Admin logged out from all devices successfully", authLogoutService.logoutAll(userId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/me")
    public ApiResponse<MeResponse> me(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ApiResponse.success("Current admin fetched successfully", adminAuthService.me(userId));
    }
}
