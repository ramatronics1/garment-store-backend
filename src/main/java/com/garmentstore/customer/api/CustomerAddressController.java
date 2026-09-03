package com.garmentstore.customer.api;

import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.customer.application.AddressService;
import com.garmentstore.customer.dto.AddressRequest;
import com.garmentstore.customer.dto.AddressResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers/me/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
public class CustomerAddressController {
    private final AddressService service;

    @GetMapping
    public ApiResponse<List<AddressResponse>> list(Authentication a) {
        return ApiResponse.success("Customer addresses fetched successfully", service.list(id(a)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressResponse> create(Authentication a, @Valid @RequestBody AddressRequest r) {
        return ApiResponse.success("Address created successfully", service.create(id(a), r));
    }

    @GetMapping("/{addressId}")
    public ApiResponse<AddressResponse> get(Authentication a, @PathVariable Long addressId) {
        return ApiResponse.success("Address fetched successfully", service.get(id(a), addressId));
    }

    @PutMapping("/{addressId}")
    public ApiResponse<AddressResponse> update(Authentication a, @PathVariable Long addressId, @Valid @RequestBody AddressRequest r) {
        return ApiResponse.success("Address updated successfully", service.update(id(a), addressId, r));
    }

    @DeleteMapping("/{addressId}")
    public ApiResponse<Void> delete(Authentication a, @PathVariable Long addressId) {
        service.delete(id(a), addressId);
        return ApiResponse.success("Address deleted successfully", null);
    }

    @PatchMapping("/{addressId}/default")
    public ApiResponse<AddressResponse> def(Authentication a, @PathVariable Long addressId) {
        return ApiResponse.success("Default address updated successfully", service.markDefault(id(a), addressId));
    }

    private Long id(Authentication a) {
        return Long.valueOf(a.getName());
    }
}
