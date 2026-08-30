package com.garmentstore.customer.api;

import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.customer.application.AdminCustomerService;
import com.garmentstore.customer.dto.AdminCustomerListResponse;
import com.garmentstore.customer.dto.CustomerAccountActionRequest;
import com.garmentstore.customer.dto.CustomerAccountActionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * AdminCustomerController — REST controller for Admin Customer management.
 *
 * Base path: /api/v1/admin/customers
 * Security:  ROLE_ADMIN or ROLE_SUPER_ADMIN (enforced here + SecurityConfig via /api/v1/admin/**)
 *
 * Endpoints:
 *   GET /api/v1/admin/customers  → paginated, filtered, sorted customer list + KPI overview
 */
@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    /**
     * GET /api/v1/admin/customers
     *
     * Query params:
     *   q       - search across name, email, mobile
     *   status  - ACTIVE | LOCKED | DISABLED | PENDING_VERIFICATION | DELETED (null = all)
     *   sort    - name | created_at | last_login | total_spent | total_orders | last_order
     *   dir     - asc | desc (default: desc)
     *   page    - 1-indexed page number (default: 1)
     *   size    - page size, max 100 (default: 10)
     */
    @GetMapping
    public ApiResponse<AdminCustomerListResponse> listCustomers(
            @RequestParam(required = false)                          String q,
            @RequestParam(required = false)                          String status,
            @RequestParam(required = false, defaultValue = "total_spent") String sort,
            @RequestParam(required = false, defaultValue = "desc")   String dir,
            @RequestParam(required = false, defaultValue = "1")      int page,
            @RequestParam(required = false, defaultValue = "10")     int size
    ) {
        AdminCustomerListResponse result =
                adminCustomerService.listCustomers(q, status, sort, dir, page, size);
        return ApiResponse.success("Customers fetched successfully", result);
    }

    /**
     * PATCH /api/v1/admin/customers/{userId}/status
     *
     * Updates a customer's account status.
     *
     * Request body:
     *   { "action": "BAN" | "UNBAN" | "DISABLE", "reason": "optional string" }
     *
     * Responses:
     *   200 — action applied, returns updated status + revoked session count
     *   400 — validation error (missing action)
     *   403 — self-action or targeting a non-customer account
     *   404 — user not found
     *   409 — invalid transition (e.g. UNBAN on an ACTIVE account)
     */
    @PatchMapping("/{userId}/status")
    public ApiResponse<CustomerAccountActionResponse> updateStatus(
            @PathVariable Long userId,
            @Valid @RequestBody CustomerAccountActionRequest request,
            Authentication authentication
    ) {
        CustomerAccountActionResponse result =
                adminCustomerService.updateAccountStatus(userId, request, authentication);
        return ApiResponse.success(result.message(), result);
    }
}
