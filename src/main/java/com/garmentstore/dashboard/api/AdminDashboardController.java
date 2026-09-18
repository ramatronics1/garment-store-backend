package com.garmentstore.dashboard.api;

import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.dashboard.application.AdminDashboardService;
import com.garmentstore.dashboard.dto.AdminDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdminDashboardController — REST controller for Admin Dashboard Analytics.
 *
 * Base path: /api/v1/admin/dashboard
 * Security:  ROLE_ADMIN or ROLE_SUPER_ADMIN (enforced in SecurityConfig via /api/v1/admin/**)
 *
 * Endpoints:
 *   GET /api/v1/admin/dashboard?period=MONTHLY
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * GET /api/v1/admin/dashboard
     *
     * @param period DAILY | WEEKLY | MONTHLY | YEARLY (default: MONTHLY)
     * @return Full dynamic dashboard analytics payload
     */
    @GetMapping
    public ApiResponse<AdminDashboardResponse> getDashboard(
            @RequestParam(required = false, defaultValue = "MONTHLY") String period
    ) {
        AdminDashboardResponse data = adminDashboardService.getDashboardData(period);
        return ApiResponse.success("Dashboard data fetched successfully", data);
    }
}
