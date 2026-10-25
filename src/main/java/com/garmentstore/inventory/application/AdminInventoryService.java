package com.garmentstore.inventory.application;

import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.catalog.domain.GenderTag;
import com.garmentstore.catalog.domain.ProductImage;
import com.garmentstore.catalog.domain.ProductStatus;
import com.garmentstore.catalog.domain.ProductVariant;
import com.garmentstore.catalog.infrastructure.ProductImageRepository;
import com.garmentstore.catalog.infrastructure.ProductVariantRepository;
import com.garmentstore.common.exception.BusinessException;
import com.garmentstore.inventory.domain.InventoryTransaction;
import com.garmentstore.inventory.domain.InventoryTransactionType;
import com.garmentstore.inventory.dto.*;
import com.garmentstore.inventory.infrastructure.InventoryTransactionRepository;
import com.garmentstore.notification.application.event.LowStockEvent;
import com.garmentstore.notification.config.NotificationProperties;
import com.garmentstore.order.infrastructure.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminInventoryService {

    private final ProductVariantRepository variantRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ProductImageRepository productImageRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationProperties notificationProperties;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Paginated Inventory Query with KPIs, category stock distribution, and top sold variants.
     */
    @Transactional(readOnly = true)
    public AdminInventoryPageResponse getInventory(
            String q,
            String status,
            String categoryId,
            String gender,
            int page,
            int size,
            String sort,
            String dir
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));

        Sort sorting = buildSort(sort, dir);
        Pageable pageable = PageRequest.of(safePage, safeSize, sorting);

        Specification<ProductVariant> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude deleted products
            predicates.add(cb.notEqual(root.get("product").get("status"), ProductStatus.DELETED));

            // Status filter: HEALTHY (> 5), LOW_STOCK (1..5), OUT_OF_STOCK (0)
            if (status != null && !status.isBlank()) {
                String s = status.trim().toUpperCase();
                int threshold = notificationProperties.getLowStockThreshold();
                if ("OUT_OF_STOCK".equals(s)) {
                    predicates.add(cb.equal(root.get("stockQuantity"), 0));
                } else if ("LOW_STOCK".equals(s)) {
                    predicates.add(cb.and(
                            cb.lessThanOrEqualTo(root.get("stockQuantity"), threshold),
                            cb.greaterThan(root.get("stockQuantity"), 0)
                    ));
                } else if ("HEALTHY".equals(s) || "IN_STOCK".equals(s)) {
                    predicates.add(cb.greaterThan(root.get("stockQuantity"), threshold));
                }
            }

            // Category Filter
            if (categoryId != null && !categoryId.isBlank()) {
                try {
                    Long cId = Long.parseLong(categoryId.trim());
                    predicates.add(cb.equal(root.get("product").get("category").get("id"), cId));
                } catch (NumberFormatException ignored) {}
            }

            // Gender Filter
            if (gender != null && !gender.isBlank()) {
                try {
                    GenderTag gt = GenderTag.valueOf(gender.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("product").get("genderTag"), gt));
                } catch (IllegalArgumentException ignored) {}
            }

            // Text search
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("product").get("name")), pattern);
                Predicate skuMatch = cb.like(cb.lower(root.get("sku")), pattern);
                predicates.add(cb.or(nameMatch, skuMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProductVariant> variantPage = variantRepository.findAll(spec, pageable);

        List<AdminInventoryVariantResponse> variants = variantPage.getContent().stream()
                .map(this::toVariantResponse)
                .toList();

        // Build KPI Overview
        int lowStockThreshold = notificationProperties.getLowStockThreshold();
        AdminInventoryOverviewResponse overview = AdminInventoryOverviewResponse.builder()
                .totalItems(variantRepository.countAllActiveProducts())
                .totalVariants(variantRepository.countAllActiveVariants())
                .lowStock(variantRepository.countLowStockVariants(lowStockThreshold))
                .outOfStock(variantRepository.countOutOfStockVariants())
                .totalStockUnits(variantRepository.sumTotalStockUnits())
                .totalValuationMrp(variantRepository.sumTotalValuationMrp())
                .totalValuationCost(variantRepository.sumTotalValuationCost())
                .build();

        // Category / Gender Stock Distribution
        List<AdminInventoryCategoryStat> categories = buildCategoryStats();

        // Top Sold Variants in last 30 days
        List<AdminInventoryTopSold> mostSold = buildTopSoldVariants();

        return AdminInventoryPageResponse.builder()
                .overview(overview)
                .categories(categories)
                .mostSold(mostSold)
                .variants(variants)
                .total(variantPage.getTotalElements())
                .page(variantPage.getNumber() + 1)
                .size(variantPage.getSize())
                .totalPages(variantPage.getTotalPages())
                .build();
    }

    /**
     * Adjust stock for a single variant with audit logging.
     */
    @Transactional
    public AdminInventoryVariantResponse adjustStock(
            Long variantId,
            AdminStockAdjustmentRequest request,
            Long adminId,
            String adminName
    ) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException("VARIANT_NOT_FOUND", "Variant not found with id: " + variantId, HttpStatus.NOT_FOUND));

        int prevStock = variant.getStockQuantity();
        int newStock;
        int delta;

        if ("DELTA".equalsIgnoreCase(request.mode())) {
            delta = request.quantity();
            newStock = prevStock + delta;
        } else {
            newStock = request.quantity();
            delta = newStock - prevStock;
        }

        if (newStock < 0) {
            throw new BusinessException("INVALID_STOCK", "Stock quantity cannot be negative (would result in " + newStock + ")", HttpStatus.BAD_REQUEST);
        }

        variant.setStockQuantity(newStock);
        variant.setUpdatedAt(Instant.now());
        variantRepository.save(variant);

        // Determine transaction type
        InventoryTransactionType txType = request.transactionType();
        if (txType == null) {
            txType = delta >= 0 ? InventoryTransactionType.RESTOCK : InventoryTransactionType.MANUAL_CORRECTION;
        }

        User actor = adminId != null ? userRepository.findById(adminId).orElse(null) : null;
        String resolvedActorName = (actor != null && actor.getName() != null) ? actor.getName() : (adminName != null ? adminName : "Admin");

        // Record Audit Transaction
        recordTransaction(
                variant,
                prevStock,
                delta,
                newStock,
                txType,
                request.reason() != null ? request.reason() : "Manual stock update",
                request.referenceId(),
                actor,
                resolvedActorName
        );

        // Low stock alert check
        if (newStock <= notificationProperties.getLowStockThreshold()) {
            Long prodId = variant.getProduct() != null ? variant.getProduct().getId() : null;
            String prodName = variant.getProduct() != null ? variant.getProduct().getName() : "Unknown Product";
            eventPublisher.publishEvent(new LowStockEvent(prodId, prodName, variant.getSku(), newStock));
        }

        log.info("[Inventory] Variant {} (SKU: {}) stock adjusted: {} → {} (delta: {}) by {}",
                variantId, variant.getSku(), prevStock, newStock, delta, resolvedActorName);

        return toVariantResponse(variant);
    }

    /**
     * Bulk stock adjustments.
     */
    @Transactional
    public List<AdminInventoryVariantResponse> bulkAdjustStock(
            AdminBulkStockAdjustmentRequest request,
            Long adminId,
            String adminName
    ) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new BusinessException("EMPTY_REQUEST", "Bulk adjustment items cannot be empty", HttpStatus.BAD_REQUEST);
        }

        List<AdminInventoryVariantResponse> results = new ArrayList<>();
        for (AdminBulkStockAdjustmentItem item : request.items()) {
            AdminStockAdjustmentRequest singleReq = new AdminStockAdjustmentRequest(
                    item.mode(),
                    item.quantity(),
                    item.transactionType(),
                    item.reason(),
                    item.referenceId()
            );
            results.add(adjustStock(item.variantId(), singleReq, adminId, adminName));
        }
        return results;
    }

    /**
     * Record an inventory transaction audit entry.
     */
    @Transactional
    public void recordTransaction(
            ProductVariant variant,
            int previousQuantity,
            int quantityChange,
            int newQuantity,
            InventoryTransactionType type,
            String reason,
            String referenceId,
            User actorUser,
            String actorName
    ) {
        InventoryTransaction tx = InventoryTransaction.builder()
                .variant(variant)
                .product(variant.getProduct())
                .sku(variant.getSku())
                .previousQuantity(previousQuantity)
                .quantityChange(quantityChange)
                .newQuantity(newQuantity)
                .transactionType(type)
                .reason(reason)
                .referenceId(referenceId)
                .actorUser(actorUser)
                .actorName(actorName)
                .createdAt(Instant.now())
                .build();

        transactionRepository.save(tx);
    }

    /**
     * Fetch inventory audit transactions with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AdminInventoryTransactionResponse> getTransactions(
            Long variantId,
            String sku,
            InventoryTransactionType transactionType,
            int page,
            int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<InventoryTransaction> txPage = transactionRepository.findTransactions(variantId, sku, transactionType, pageable);

        return txPage.map(tx -> AdminInventoryTransactionResponse.builder()
                .id(tx.getId())
                .variantId(tx.getVariant() != null ? tx.getVariant().getId() : null)
                .productId(tx.getProduct() != null ? tx.getProduct().getId() : null)
                .productName(tx.getProduct() != null ? tx.getProduct().getName() : "")
                .sku(tx.getSku())
                .previousQuantity(tx.getPreviousQuantity())
                .quantityChange(tx.getQuantityChange())
                .newQuantity(tx.getNewQuantity())
                .transactionType(tx.getTransactionType())
                .reason(tx.getReason())
                .referenceId(tx.getReferenceId())
                .actorName(tx.getActorName())
                .createdAt(tx.getCreatedAt())
                .build());
    }

    /**
     * Export full active inventory report as CSV bytes.
     */
    @Transactional(readOnly = true)
    public byte[] exportInventoryCsv() {
        List<ProductVariant> allVariants = variantRepository.findAll(
                (root, query, cb) -> cb.notEqual(root.get("product").get("status"), ProductStatus.DELETED),
                Sort.by(Sort.Direction.ASC, "product.name").and(Sort.by(Sort.Direction.ASC, "sku"))
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {
            // CSV Header
            writer.println("Variant ID,Product ID,Product Name,Category,Gender,SKU,Color,Size,Stock Qty,Status,MRP,Selling Price,Cost Price,MRP Valuation,Cost Valuation");

            for (ProductVariant v : allVariants) {
                String prodName = "";
                String catName = "";
                String gender = "";
                Long prodId = 0L;
                try {
                    if (v.getProduct() != null) {
                        prodId = v.getProduct().getId() != null ? v.getProduct().getId() : 0L;
                        prodName = escapeCsv(v.getProduct().getName() != null ? v.getProduct().getName() : "");
                        if (v.getProduct().getCategory() != null) {
                            catName = escapeCsv(v.getProduct().getCategory().getName() != null ? v.getProduct().getCategory().getName() : "");
                        }
                        if (v.getProduct().getGenderTag() != null) {
                            gender = v.getProduct().getGenderTag().name();
                        }
                    }
                } catch (Exception ignored) {}

                String color = "";
                try {
                    if (v.getColor() != null && v.getColor().getId() != null && v.getColor().getId() > 0) {
                        color = escapeCsv(v.getColor().getName() != null ? v.getColor().getName() : "");
                    }
                } catch (Exception ignored) {}

                String size = "";
                try {
                    if (v.getSize() != null && v.getSize().getId() != null && v.getSize().getId() > 0) {
                        size = escapeCsv(v.getSize().getSizeCode() != null ? v.getSize().getSizeCode() : "");
                    }
                } catch (Exception ignored) {}

                String status = computeStockStatus(v.getStockQuantity());

                BigDecimal mrp = v.getMrp() != null ? v.getMrp() : BigDecimal.ZERO;
                BigDecimal sellingPrice = v.getSellingPrice() != null ? v.getSellingPrice() : BigDecimal.ZERO;
                BigDecimal costPrice = v.getCostPrice() != null ? v.getCostPrice() : sellingPrice;

                BigDecimal mrpValuation = mrp.multiply(BigDecimal.valueOf(v.getStockQuantity()));
                BigDecimal costValuation = costPrice.multiply(BigDecimal.valueOf(v.getStockQuantity()));

                writer.printf("%d,%d,\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\",%d,%s,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                        v.getId(),
                        prodId,
                        prodName,
                        catName,
                        gender,
                        v.getSku() != null ? v.getSku() : "",
                        color,
                        size,
                        v.getStockQuantity(),
                        status,
                        mrp,
                        sellingPrice,
                        costPrice,
                        mrpValuation,
                        costValuation
                );
            }
        }

        return baos.toByteArray();
    }

    // ── Helper Mappers ──────────────────────────────────────────────────────────

    private AdminInventoryVariantResponse toVariantResponse(ProductVariant v) {
        String thumbUrl = null;
        Long productId = null;
        String productName = "";
        String productSlug = "";
        Long categoryId = null;
        String categoryName = "";
        String genderTag = "";

        try {
            if (v.getProduct() != null) {
                productId = v.getProduct().getId();
                productName = v.getProduct().getName() != null ? v.getProduct().getName() : "";
                productSlug = v.getProduct().getSlug() != null ? v.getProduct().getSlug() : "";
                if (v.getProduct().getCategory() != null) {
                    categoryId = v.getProduct().getCategory().getId();
                    categoryName = v.getProduct().getCategory().getName() != null ? v.getProduct().getCategory().getName() : "";
                }
                if (v.getProduct().getGenderTag() != null) {
                    genderTag = v.getProduct().getGenderTag().name();
                }
                thumbUrl = productImageRepository
                        .findFirstByProductIdAndThumbnailTrueOrderByDisplayOrderAscIdAsc(productId)
                        .map(ProductImage::getMediaUrl)
                        .orElse(null);
            }
        } catch (Exception ignored) {}

        Long colorId = null;
        String colorName = "";
        String colorHex = "";
        try {
            if (v.getColor() != null && v.getColor().getId() != null && v.getColor().getId() > 0) {
                colorId = v.getColor().getId();
                colorName = v.getColor().getName() != null ? v.getColor().getName() : "";
                colorHex = v.getColor().getHexCode() != null ? v.getColor().getHexCode() : "";
            }
        } catch (Exception ignored) {}

        Long sizeId = null;
        String sizeCode = "";
        try {
            if (v.getSize() != null && v.getSize().getId() != null && v.getSize().getId() > 0) {
                sizeId = v.getSize().getId();
                sizeCode = v.getSize().getSizeCode() != null ? v.getSize().getSizeCode() : "";
            }
        } catch (Exception ignored) {}

        return AdminInventoryVariantResponse.builder()
                .id(v.getId())
                .productId(productId)
                .productName(productName)
                .productSlug(productSlug)
                .skuCode(v.getSku() != null ? v.getSku() : "")
                .colorId(colorId)
                .colorName(colorName)
                .colorHex(colorHex)
                .sizeId(sizeId)
                .sizeCode(sizeCode)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .genderTag(genderTag)
                .mrp(v.getMrp())
                .sellingPrice(v.getSellingPrice())
                .costPrice(v.getCostPrice())
                .stockQuantity(v.getStockQuantity())
                .status(computeStockStatus(v.getStockQuantity()))
                .thumbnailUrl(thumbUrl)
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }

    private String computeStockStatus(int stock) {
        if (stock == 0) return "OUT_OF_STOCK";
        if (stock <= notificationProperties.getLowStockThreshold()) return "LOW_STOCK";
        return "HEALTHY";
    }

    private List<AdminInventoryCategoryStat> buildCategoryStats() {
        List<Object[]> rawStats = variantRepository.findStockStatsByGender();
        List<AdminInventoryCategoryStat> stats = new ArrayList<>();

        for (Object[] row : rawStats) {
            GenderTag gt = (GenderTag) row[0];
            long prodCount = ((Number) row[1]).longValue();
            long varCount = ((Number) row[2]).longValue();
            long totalStock = ((Number) row[3]).longValue();

            String name = switch (gt) {
                case MEN -> "Men";
                case WOMEN -> "Women";
                case BOYS -> "Boys";
                case GIRLS -> "Girls";
                case UNISEX -> "Unisex";
            };

            stats.add(AdminInventoryCategoryStat.builder()
                    .name(name)
                    .genderTag(gt.name())
                    .totalStock(totalStock)
                    .totalProducts(prodCount)
                    .totalVariants(varCount)
                    .build());
        }
        return stats;
    }

    private List<AdminInventoryTopSold> buildTopSoldVariants() {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Object[]> rows = orderRepository.findTopSellingVariantAggregatesSince(thirtyDaysAgo, PageRequest.of(0, 5));

        List<AdminInventoryTopSold> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long variantId = (Long) row[0];
            long totalSold = ((Number) row[1]).longValue();

            variantRepository.findById(variantId).ifPresent(v -> {
                String thumb = null;
                if (v.getProduct() != null) {
                    thumb = productImageRepository
                            .findFirstByProductIdAndThumbnailTrueOrderByDisplayOrderAscIdAsc(v.getProduct().getId())
                            .map(ProductImage::getMediaUrl)
                            .orElse(null);
                }

                result.add(AdminInventoryTopSold.builder()
                        .id(v.getId())
                        .productId(v.getProduct() != null ? v.getProduct().getId() : null)
                        .productName(v.getProduct() != null ? v.getProduct().getName() : "")
                        .skuCode(v.getSku())
                        .sizeCode(v.getSize() != null ? v.getSize().getSizeCode() : "")
                        .thumbnailUrl(thumb)
                        .stockQuantity(v.getStockQuantity())
                        .soldLast30Days(totalSold)
                        .build());
            });
        }
        return result;
    }

    private Sort buildSort(String sort, String dir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        if (sort == null || sort.isBlank() || "stockQuantity".equalsIgnoreCase(sort) || "stock".equalsIgnoreCase(sort)) {
            return Sort.by(direction, "stockQuantity");
        }
        if ("sku".equalsIgnoreCase(sort)) {
            return Sort.by(direction, "sku");
        }
        if ("productName".equalsIgnoreCase(sort) || "name".equalsIgnoreCase(sort)) {
            return Sort.by(direction, "product.name");
        }
        if ("updatedAt".equalsIgnoreCase(sort)) {
            return Sort.by(direction, "updatedAt");
        }
        return Sort.by(direction, "stockQuantity");
    }

    private String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }
}
