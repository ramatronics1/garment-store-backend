package com.garmentstore.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminDashboardResponse {

    @JsonProperty("meta")
    private DashboardMeta meta;

    @JsonProperty("kpis")
    private DashboardKpis kpis;

    @JsonProperty("revenue_chart_data")
    @Builder.Default
    private List<RevenueChartPoint> revenueChartData = new ArrayList<>();

    @JsonProperty("order_status_distribution")
    @Builder.Default
    private List<OrderStatusDistribution> orderStatusDistribution = new ArrayList<>();

    @JsonProperty("top_selling_products")
    @Builder.Default
    private List<TopSellingProduct> topSellingProducts = new ArrayList<>();

    @JsonProperty("low_stock_alerts")
    @Builder.Default
    private List<LowStockAlert> lowStockAlerts = new ArrayList<>();

    @JsonProperty("recent_orders")
    @Builder.Default
    private List<RecentOrder> recentOrders = new ArrayList<>();

    @JsonProperty("category_revenue")
    @Builder.Default
    private List<CategoryRevenue> categoryRevenue = new ArrayList<>();

    // ── Nested DTOs ─────────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DashboardMeta {
        @JsonProperty("generated_at")
        private Instant generatedAt;

        @JsonProperty("version")
        private String version;

        @JsonProperty("period")
        private String period;

        @JsonProperty("description")
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DashboardKpis {
        @JsonProperty("total_revenue")
        private KpiMetric<BigDecimal> totalRevenue;

        @JsonProperty("total_orders")
        private KpiMetric<Long> totalOrders;

        @JsonProperty("pending_orders")
        private KpiMetric<Long> pendingOrders;

        @JsonProperty("total_customers")
        private KpiMetric<Long> totalCustomers;

        @JsonProperty("active_products")
        private KpiMetric<Long> activeProducts;

        @JsonProperty("out_of_stock_skus")
        private KpiMetric<Long> outOfStockSkus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class KpiMetric<T> {
        @JsonProperty("value")
        private T value;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("trend_percent")
        private Double trendPercent;

        @JsonProperty("trend_direction")
        private String trendDirection; // "UP" | "DOWN"

        @JsonProperty("previous_period_value")
        private T previousPeriodValue;

        @JsonProperty("needs_attention")
        private Boolean needsAttention;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RevenueChartPoint {
        @JsonProperty("label")
        private String label;

        @JsonProperty("date")
        private String date;

        @JsonProperty("week")
        private Integer week;

        @JsonProperty("month")
        private String month;

        @JsonProperty("year")
        private Integer year;

        @JsonProperty("revenue")
        private BigDecimal revenue;

        @JsonProperty("orders")
        private Long orders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusDistribution {
        @JsonProperty("status")
        private String status;

        @JsonProperty("count")
        private Long count;

        @JsonProperty("label")
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TopSellingProduct {
        @JsonProperty("product_id")
        private Long productId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("slug")
        private String slug;

        @JsonProperty("category_id")
        private Long categoryId;

        @JsonProperty("category_name")
        private String categoryName;

        @JsonProperty("gender_tag")
        private String genderTag;

        @JsonProperty("units_sold")
        private Long unitsSold;

        @JsonProperty("revenue")
        private BigDecimal revenue;

        @JsonProperty("selling_price")
        private BigDecimal sellingPrice;

        @JsonProperty("mrp")
        private BigDecimal mrp;

        @JsonProperty("thumbnail_url")
        private String thumbnailUrl;

        @JsonProperty("total_stock_quantity")
        private Integer totalStockQuantity;

        @JsonProperty("status")
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockAlert {
        @JsonProperty("variant_id")
        private Long variantId;

        @JsonProperty("product_id")
        private Long productId;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("sku_code")
        private String skuCode;

        @JsonProperty("size_code")
        private String sizeCode;

        @JsonProperty("stock_quantity")
        private Integer stockQuantity;

        @JsonProperty("is_active")
        private Boolean isActive;

        @JsonProperty("reorder_threshold")
        private Integer reorderThreshold;

        @JsonProperty("severity")
        private String severity; // "OUT_OF_STOCK" | "CRITICAL_LOW" | "LOW"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("order_number")
        private String orderNumber;

        @JsonProperty("user_id")
        private Long userId;

        @JsonProperty("address_id")
        private Long addressId;

        @JsonProperty("customer_name")
        private String customerName;

        @JsonProperty("customer_email")
        private String customerEmail;

        @JsonProperty("status")
        private String status;

        @JsonProperty("payment_method")
        private String paymentMethod;

        @JsonProperty("grand_total")
        private BigDecimal grandTotal;

        @JsonProperty("item_count")
        private Integer itemCount;

        @JsonProperty("created_at")
        private Instant createdAt;

        @JsonProperty("updated_at")
        private Instant updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryRevenue {
        @JsonProperty("category_id")
        private Long categoryId;

        @JsonProperty("category_name")
        private String categoryName;

        @JsonProperty("slug")
        private String slug;

        @JsonProperty("revenue")
        private BigDecimal revenue;

        @JsonProperty("order_count")
        private Long orderCount;

        @JsonProperty("percent_of_total")
        private Double percentOfTotal;

        @JsonProperty("trend_percent")
        private Double trendPercent;
    }
}
