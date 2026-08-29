package com.garmentstore.order.api;

import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.order.application.AdminOrderService;
import com.garmentstore.order.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * AdminOrderController — REST controller for Admin Order management.
 *
 * Base path: /api/v1/admin/orders
 * Security:  ROLE_ADMIN or ROLE_SUPER_ADMIN (enforced in SecurityConfig via /api/v1/admin/**)
 *
 * Endpoints:
 *   GET    /api/v1/admin/orders               → paginated order list
 *   GET    /api/v1/admin/orders/{id}          → single order detail
 *   PATCH  /api/v1/admin/orders/{id}/status   → update single order status
 *   PATCH  /api/v1/admin/orders/bulk-status   → update multiple order statuses
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    /**
     * GET /api/v1/admin/orders
     *
     * Query params:
     *   q         - search string (order number, customer name/email)
     *   status    - PENDING | CONFIRMED | SHIPPED | DELIVERED | CANCELLED
     *   dateRange - ALL | TODAY | 7D | 30D
     *   sort      - order_number | customer | grand_total | created_at (default)
     *   dir       - asc | desc (default: desc)
     *   page      - 0-indexed page number (default: 0)
     *   size      - page size, max 100 (default: 10)
     */
    @GetMapping
    public ApiResponse<AdminOrderPageResponse> listOrders(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "ALL") String dateRange,
            @RequestParam(required = false, defaultValue = "created_at") String sort,
            @RequestParam(required = false, defaultValue = "desc") String dir,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        AdminOrderPageResponse result = adminOrderService.getOrders(q, status, dateRange, sort, dir, page, size);
        return ApiResponse.success("Orders fetched successfully", result);
    }

    /**
     * GET /api/v1/admin/orders/{id}
     * Returns full order detail: items, status history, customer, address.
     */
    @GetMapping("/{id}")
    public ApiResponse<AdminOrderDetailResponse> getOrderDetail(@PathVariable Long id) {
        AdminOrderDetailResponse detail = adminOrderService.getOrderDetail(id);
        return ApiResponse.success("Order detail fetched successfully", detail);
    }

    /**
     * PATCH /api/v1/admin/orders/{id}/status
     * Updates a single order's status and appends to the history timeline.
     * The updated status is instantly visible to the customer in their order list.
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<AdminOrderDetailResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            Authentication authentication
    ) {
        Long adminId = Long.valueOf(authentication.getName());
        AdminOrderDetailResponse updated = adminOrderService.updateOrderStatus(id, request, adminId);
        return ApiResponse.success("Order status updated to " + request.getStatus(), updated);
    }

    /**
     * PATCH /api/v1/admin/orders/bulk-status
     * Updates multiple orders' statuses in one call.
     * Reports partial failures individually — one bad order won't cancel valid ones.
     */
    @PatchMapping("/bulk-status")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<BulkStatusUpdateResponse> bulkUpdateOrderStatus(
            @Valid @RequestBody BulkStatusUpdateRequest request,
            Authentication authentication
    ) {
        Long adminId = Long.valueOf(authentication.getName());
        BulkStatusUpdateResponse result = adminOrderService.bulkUpdateOrderStatus(request, adminId);
        return ApiResponse.success(result.getMessage(), result);
    }
}
