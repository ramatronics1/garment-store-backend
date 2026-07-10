package com.garmentstore.customer.api;

import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.customer.application.CustomerProfileService;
import com.garmentstore.customer.dto.ChangePasswordRequest;
import com.garmentstore.customer.dto.ChangePasswordResponse;
import com.garmentstore.customer.dto.ProfileResponse;
import com.garmentstore.customer.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerProfileController {
    private final CustomerProfileService service;

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> get(Authentication a) {
        return ApiResponse.success("Customer profile fetched successfully", service.getProfile(id(a)));
    }

    @PutMapping("/profile")
    public ApiResponse<ProfileResponse> update(Authentication a, @Valid @RequestBody UpdateProfileRequest r) {
        return ApiResponse.success("Customer profile updated successfully", service.updateProfile(id(a), r));
    }

    @PutMapping("/password")
    public ApiResponse<ChangePasswordResponse> password(Authentication a, @Valid @RequestBody ChangePasswordRequest r) {
        return ApiResponse.success("Password changed successfully", service.changePassword(id(a), r));
    }

    private Long id(Authentication a) {
        return Long.valueOf(a.getName());
    }
}
