package com.garmentstore.auth.api;

import com.garmentstore.auth.application.AuthLogoutService;
import com.garmentstore.auth.dto.LogoutRequest;
import com.garmentstore.auth.dto.LogoutResponse;
import com.garmentstore.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthLogoutController {
    private final AuthLogoutService authLogoutService;

    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
        return ApiResponse.success("Logout successful", authLogoutService.logout(request));
    }

    @PostMapping("/logout-all")
    public ApiResponse<LogoutResponse> logoutAll(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ApiResponse.success("Logged out from all devices successfully", authLogoutService.logoutAll(userId));
    }
}
