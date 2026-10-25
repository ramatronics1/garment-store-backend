package com.garmentstore.inventory.api;

import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.inventory.application.AdminInventoryService;
import com.garmentstore.inventory.domain.InventoryTransactionType;
import com.garmentstore.inventory.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Slf4j
public class AdminInventoryController {

    private final AdminInventoryService inventoryService;

    /**
     * GET /api/v1/admin/inventory
     * Paginated variant stock list with KPI overview, category distributions, and top selling variants.
     */
    @GetMapping
    public ApiResponse<AdminInventoryPageResponse> getInventory(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "stockQuantity") String sort,
            @RequestParam(defaultValue = "asc") String dir
    ) {
        String catParam = (categoryId != null && !categoryId.isBlank()) ? categoryId : category;
        AdminInventoryPageResponse response = inventoryService.getInventory(
                q, status, catParam, gender, page, size, sort, dir
        );
        return ApiResponse.success("Inventory fetched successfully", response);
    }

    /**
     * PATCH /api/v1/admin/inventory/variants/{id}/stock
     * Adjust stock for a single variant (SET quantity or DELTA +/-) with reason & audit logging.
     */
    @PatchMapping("/variants/{id}/stock")
    public ApiResponse<AdminInventoryVariantResponse> adjustStock(
            @PathVariable Long id,
            @Valid @RequestBody AdminStockAdjustmentRequest request,
            Authentication authentication
    ) {
        Long adminId = resolveAdminId(authentication);
        String adminName = authentication != null ? authentication.getName() : "Admin";
        AdminInventoryVariantResponse response = inventoryService.adjustStock(id, request, adminId, adminName);
        return ApiResponse.success("Stock updated successfully", response);
    }

    /**
     * POST /api/v1/admin/inventory/bulk-adjust
     * Bulk update stock across multiple variants.
     */
    @PostMapping("/bulk-adjust")
    public ApiResponse<List<AdminInventoryVariantResponse>> bulkAdjustStock(
            @Valid @RequestBody AdminBulkStockAdjustmentRequest request,
            Authentication authentication
    ) {
        Long adminId = resolveAdminId(authentication);
        String adminName = authentication != null ? authentication.getName() : "Admin";
        List<AdminInventoryVariantResponse> response = inventoryService.bulkAdjustStock(request, adminId, adminName);
        return ApiResponse.success("Bulk stock adjustment completed successfully", response);
    }

    /**
     * GET /api/v1/admin/inventory/transactions
     * View audit log of stock transactions and movements.
     */
    @GetMapping("/transactions")
    public ApiResponse<Page<AdminInventoryTransactionResponse>> getTransactions(
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) InventoryTransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Page<AdminInventoryTransactionResponse> response = inventoryService.getTransactions(
                variantId, sku, type, page, size
        );
        return ApiResponse.success("Inventory transactions fetched successfully", response);
    }

    /**
     * GET /api/v1/admin/inventory/export
     * Download CSV report of current stock levels and inventory valuation.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportInventoryCsv() {
        byte[] csvData = inventoryService.exportInventoryCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventory_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    private Long resolveAdminId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return null;
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
