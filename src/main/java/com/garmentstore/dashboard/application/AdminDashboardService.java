package com.garmentstore.dashboard.application;

import com.garmentstore.auth.domain.UserType;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.catalog.domain.Category;
import com.garmentstore.catalog.domain.Product;
import com.garmentstore.catalog.domain.ProductImage;
import com.garmentstore.catalog.domain.ProductStatus;
import com.garmentstore.catalog.domain.ProductVariant;
import com.garmentstore.catalog.domain.VariantStatus;
import com.garmentstore.catalog.infrastructure.CategoryRepository;
import com.garmentstore.catalog.infrastructure.ProductImageRepository;
import com.garmentstore.catalog.infrastructure.ProductRepository;
import com.garmentstore.catalog.infrastructure.ProductVariantRepository;
import com.garmentstore.dashboard.dto.AdminDashboardResponse;
import com.garmentstore.dashboard.dto.AdminDashboardResponse.*;
import com.garmentstore.order.domain.Order;
import com.garmentstore.order.domain.OrderItem;
import com.garmentstore.order.domain.OrderStatus;
import com.garmentstore.order.infrastructure.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM ''yy", Locale.ENGLISH);

    public AdminDashboardResponse getDashboardData(String rawPeriod) {
        String period = parsePeriod(rawPeriod);
        Instant now = Instant.now();

        // 1. Calculate Time Windows for KPIs
        TimeWindows windows = computeTimeWindows(period, now);

        // 2. Compute KPIs
        DashboardKpis kpis = computeKpis(windows);

        // 3. Compute Revenue Chart Data
        List<RevenueChartPoint> revenueChart = computeRevenueChart(period, now);

        // 4. Compute Order Status Distribution
        List<OrderStatusDistribution> statusDistribution = computeOrderStatusDistribution();

        // 5. Compute Top Selling Products
        List<TopSellingProduct> topProducts = computeTopSellingProducts();

        // 6. Compute Low Stock Alerts
        List<LowStockAlert> lowStockAlerts = computeLowStockAlerts();

        // 7. Compute Recent Orders
        List<RecentOrder> recentOrders = computeRecentOrders();

        // 8. Compute Category Revenue
        List<CategoryRevenue> categoryRevenues = computeCategoryRevenue();

        // 9. Build Meta
        DashboardMeta meta = DashboardMeta.builder()
                .generatedAt(now)
                .version("1.0")
                .period(period)
                .description("Vastra Admin Dashboard live analytics.")
                .build();

        return AdminDashboardResponse.builder()
                .meta(meta)
                .kpis(kpis)
                .revenueChartData(revenueChart)
                .orderStatusDistribution(statusDistribution)
                .topSellingProducts(topProducts)
                .lowStockAlerts(lowStockAlerts)
                .recentOrders(recentOrders)
                .categoryRevenue(categoryRevenues)
                .build();
    }

    private String parsePeriod(String raw) {
        if (raw == null) return "MONTHLY";
        String upper = raw.trim().toUpperCase();
        return switch (upper) {
            case "DAILY", "WEEKLY", "YEARLY" -> upper;
            default -> "MONTHLY";
        };
    }

    private record TimeWindows(Instant currentStart, Instant currentEnd, Instant previousStart, Instant previousEnd) {}

    private TimeWindows computeTimeWindows(String period, Instant now) {
        return switch (period) {
            case "DAILY" -> {
                Instant start = now.minus(7, ChronoUnit.DAYS);
                Instant prevStart = now.minus(14, ChronoUnit.DAYS);
                yield new TimeWindows(start, now, prevStart, start);
            }
            case "WEEKLY" -> {
                Instant start = now.minus(56, ChronoUnit.DAYS); // 8 weeks
                Instant prevStart = now.minus(112, ChronoUnit.DAYS);
                yield new TimeWindows(start, now, prevStart, start);
            }
            case "YEARLY" -> {
                Instant start = now.minus(365L * 5, ChronoUnit.DAYS); // 5 years
                Instant prevStart = now.minus(365L * 10, ChronoUnit.DAYS);
                yield new TimeWindows(start, now, prevStart, start);
            }
            default -> { // MONTHLY: 12 months ~ 365 days
                Instant start = now.minus(365, ChronoUnit.DAYS);
                Instant prevStart = now.minus(730, ChronoUnit.DAYS);
                yield new TimeWindows(start, now, prevStart, start);
            }
        };
    }

    private DashboardKpis computeKpis(TimeWindows tw) {
        // Total Revenue & Total Orders
        List<Object[]> currSummaryList = orderRepository.getOrderSummaryBetween(tw.currentStart(), tw.currentEnd());
        long currentOrders = 0L;
        BigDecimal currentRevenue = BigDecimal.ZERO;
        if (currSummaryList != null && !currSummaryList.isEmpty()) {
            Object[] row = currSummaryList.get(0);
            if (row[0] != null) currentOrders = ((Number) row[0]).longValue();
            if (row[1] != null) currentRevenue = (BigDecimal) row[1];
        }

        List<Object[]> prevSummaryList = orderRepository.getOrderSummaryBetween(tw.previousStart(), tw.previousEnd());
        long prevOrders = 0L;
        BigDecimal prevRevenue = BigDecimal.ZERO;
        if (prevSummaryList != null && !prevSummaryList.isEmpty()) {
            Object[] row = prevSummaryList.get(0);
            if (row[0] != null) prevOrders = ((Number) row[0]).longValue();
            if (row[1] != null) prevRevenue = (BigDecimal) row[1];
        }

        // If current window has 0 all-time orders (e.g. brand new store), fallback to all-time revenue/orders
        if (currentOrders == 0 && currentRevenue.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal globalRev = orderRepository.findGlobalRevenue();
            if (globalRev != null && globalRev.compareTo(BigDecimal.ZERO) > 0) {
                currentRevenue = globalRev;
                currentOrders = orderRepository.count();
            }
        }

        double revTrend = calculateTrend(currentRevenue.doubleValue(), prevRevenue.doubleValue());
        String revDir = revTrend >= 0 ? "UP" : "DOWN";

        double ordTrend = calculateTrend((double) currentOrders, (double) prevOrders);
        String ordDir = ordTrend >= 0 ? "UP" : "DOWN";

        // Pending Orders (current active count)
        long pendingOrdersCount = 0;
        List<Object[]> statusCounts = orderRepository.countByStatus();
        for (Object[] sc : statusCounts) {
            if (OrderStatus.PENDING.equals(sc[0])) {
                pendingOrdersCount = ((Number) sc[1]).longValue();
                break;
            }
        }

        // Total Customers
        long totalCustomers = userRepository.countByUserType(UserType.CUSTOMER);
        long prevCustomers = userRepository.countByUserTypeAndCreatedAtBefore(UserType.CUSTOMER, tw.currentStart());
        double custTrend = calculateTrend((double) totalCustomers, (double) prevCustomers);
        String custDir = custTrend >= 0 ? "UP" : "DOWN";

        // Active Products
        long activeProducts = productRepository.countByStatus(ProductStatus.ACTIVE);
        long prevActiveProducts = productRepository.countByStatusAndCreatedAtBefore(ProductStatus.ACTIVE, tw.currentStart());
        double prodTrend = calculateTrend((double) activeProducts, (double) prevActiveProducts);
        String prodDir = prodTrend >= 0 ? "UP" : "DOWN";

        // Out of Stock SKUs
        long outOfStockSkus = productVariantRepository.countByStatusAndStockQuantity(VariantStatus.ACTIVE, 0);

        return DashboardKpis.builder()
                .totalRevenue(KpiMetric.<BigDecimal>builder()
                        .value(currentRevenue)
                        .currency("INR")
                        .trendPercent(revTrend)
                        .trendDirection(revDir)
                        .previousPeriodValue(prevRevenue)
                        .build())
                .totalOrders(KpiMetric.<Long>builder()
                        .value(currentOrders)
                        .trendPercent(ordTrend)
                        .trendDirection(ordDir)
                        .previousPeriodValue(prevOrders)
                        .build())
                .pendingOrders(KpiMetric.<Long>builder()
                        .value(pendingOrdersCount)
                        .trendPercent(0.0)
                        .trendDirection("DOWN")
                        .previousPeriodValue(pendingOrdersCount)
                        .needsAttention(pendingOrdersCount > 0)
                        .build())
                .totalCustomers(KpiMetric.<Long>builder()
                        .value(totalCustomers)
                        .trendPercent(custTrend)
                        .trendDirection(custDir)
                        .previousPeriodValue(prevCustomers)
                        .build())
                .activeProducts(KpiMetric.<Long>builder()
                        .value(activeProducts)
                        .trendPercent(prodTrend)
                        .trendDirection(prodDir)
                        .previousPeriodValue(prevActiveProducts)
                        .build())
                .outOfStockSkus(KpiMetric.<Long>builder()
                        .value(outOfStockSkus)
                        .needsAttention(outOfStockSkus > 0)
                        .build())
                .build();
    }

    private double calculateTrend(double current, double previous) {
        if (previous <= 0.0) {
            return current > 0.0 ? 100.0 : 0.0;
        }
        double diff = current - previous;
        return BigDecimal.valueOf((diff / previous) * 100.0)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private List<RevenueChartPoint> computeRevenueChart(String period, Instant now) {
        LocalDate today = LocalDate.ofInstant(now, DEFAULT_ZONE);

        return switch (period) {
            case "DAILY" -> {
                // 7 days timeline
                List<RevenueChartPoint> points = new ArrayList<>();
                LocalDate start = today.minusDays(6);
                Instant startInstant = start.atStartOfDay(DEFAULT_ZONE).toInstant();
                List<Object[]> rawList = orderRepository.findRevenueChartDataSince(startInstant);

                Map<LocalDate, BigDecimal> revMap = new HashMap<>();
                Map<LocalDate, Long> ordMap = new HashMap<>();

                for (Object[] r : rawList) {
                    Instant dt = (Instant) r[0];
                    BigDecimal amount = (BigDecimal) r[1];
                    LocalDate d = LocalDate.ofInstant(dt, DEFAULT_ZONE);
                    revMap.put(d, revMap.getOrDefault(d, BigDecimal.ZERO).add(amount != null ? amount : BigDecimal.ZERO));
                    ordMap.put(d, ordMap.getOrDefault(d, 0L) + 1);
                }

                for (int i = 0; i < 7; i++) {
                    LocalDate d = start.plusDays(i);
                    points.add(RevenueChartPoint.builder()
                            .label(d.format(DAY_FMT))
                            .date(d.toString())
                            .revenue(revMap.getOrDefault(d, BigDecimal.ZERO))
                            .orders(ordMap.getOrDefault(d, 0L))
                            .build());
                }
                yield points;
            }
            case "WEEKLY" -> {
                // 8 weeks timeline
                List<RevenueChartPoint> points = new ArrayList<>();
                LocalDate start = today.minusWeeks(7);
                Instant startInstant = start.atStartOfDay(DEFAULT_ZONE).toInstant();
                List<Object[]> rawList = orderRepository.findRevenueChartDataSince(startInstant);

                Map<Integer, BigDecimal> revMap = new HashMap<>();
                Map<Integer, Long> ordMap = new HashMap<>();

                for (Object[] r : rawList) {
                    Instant dt = (Instant) r[0];
                    BigDecimal amount = (BigDecimal) r[1];
                    LocalDate d = LocalDate.ofInstant(dt, DEFAULT_ZONE);
                    int week = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    revMap.put(week, revMap.getOrDefault(week, BigDecimal.ZERO).add(amount != null ? amount : BigDecimal.ZERO));
                    ordMap.put(week, ordMap.getOrDefault(week, 0L) + 1);
                }

                for (int i = 0; i < 8; i++) {
                    LocalDate d = start.plusWeeks(i);
                    int week = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    points.add(RevenueChartPoint.builder()
                            .label(d.format(DAY_FMT))
                            .week(week)
                            .revenue(revMap.getOrDefault(week, BigDecimal.ZERO))
                            .orders(ordMap.getOrDefault(week, 0L))
                            .build());
                }
                yield points;
            }
            case "YEARLY" -> {
                // 5 years timeline
                List<RevenueChartPoint> points = new ArrayList<>();
                int currentYear = today.getYear();
                int startYear = currentYear - 4;
                LocalDate start = LocalDate.of(startYear, 1, 1);
                Instant startInstant = start.atStartOfDay(DEFAULT_ZONE).toInstant();
                List<Object[]> rawList = orderRepository.findRevenueChartDataSince(startInstant);

                Map<Integer, BigDecimal> revMap = new HashMap<>();
                Map<Integer, Long> ordMap = new HashMap<>();

                for (Object[] r : rawList) {
                    Instant dt = (Instant) r[0];
                    BigDecimal amount = (BigDecimal) r[1];
                    LocalDate d = LocalDate.ofInstant(dt, DEFAULT_ZONE);
                    int yr = d.getYear();
                    revMap.put(yr, revMap.getOrDefault(yr, BigDecimal.ZERO).add(amount != null ? amount : BigDecimal.ZERO));
                    ordMap.put(yr, ordMap.getOrDefault(yr, 0L) + 1);
                }

                for (int yr = startYear; yr <= currentYear; yr++) {
                    points.add(RevenueChartPoint.builder()
                            .label(String.valueOf(yr))
                            .year(yr)
                            .revenue(revMap.getOrDefault(yr, BigDecimal.ZERO))
                            .orders(ordMap.getOrDefault(yr, 0L))
                            .build());
                }
                yield points;
            }
            default -> { // MONTHLY: 12 months timeline
                List<RevenueChartPoint> points = new ArrayList<>();
                YearMonth currentYm = YearMonth.from(today);
                YearMonth startYm = currentYm.minusMonths(11);
                Instant startInstant = startYm.atDay(1).atStartOfDay(DEFAULT_ZONE).toInstant();
                List<Object[]> rawList = orderRepository.findRevenueChartDataSince(startInstant);

                Map<YearMonth, BigDecimal> revMap = new HashMap<>();
                Map<YearMonth, Long> ordMap = new HashMap<>();

                for (Object[] r : rawList) {
                    Instant dt = (Instant) r[0];
                    BigDecimal amount = (BigDecimal) r[1];
                    LocalDate d = LocalDate.ofInstant(dt, DEFAULT_ZONE);
                    YearMonth ym = YearMonth.from(d);
                    revMap.put(ym, revMap.getOrDefault(ym, BigDecimal.ZERO).add(amount != null ? amount : BigDecimal.ZERO));
                    ordMap.put(ym, ordMap.getOrDefault(ym, 0L) + 1);
                }

                for (int i = 0; i < 12; i++) {
                    YearMonth ym = startYm.plusMonths(i);
                    points.add(RevenueChartPoint.builder()
                            .label(ym.format(MONTH_FMT))
                            .month(ym.toString())
                            .revenue(revMap.getOrDefault(ym, BigDecimal.ZERO))
                            .orders(ordMap.getOrDefault(ym, 0L))
                            .build());
                }
                yield points;
            }
        };
    }

    private List<OrderStatusDistribution> computeOrderStatusDistribution() {
        List<Object[]> counts = orderRepository.countByStatus();
        Map<OrderStatus, Long> map = new HashMap<>();
        for (Object[] r : counts) {
            OrderStatus status = (OrderStatus) r[0];
            Long cnt = ((Number) r[1]).longValue();
            map.put(status, cnt);
        }

        List<OrderStatusDistribution> list = new ArrayList<>();
        for (OrderStatus st : OrderStatus.values()) {
            String label = st.name().charAt(0) + st.name().substring(1).toLowerCase();
            list.add(OrderStatusDistribution.builder()
                    .status(st.name())
                    .count(map.getOrDefault(st, 0L))
                    .label(label)
                    .build());
        }
        return list;
    }

    private List<TopSellingProduct> computeTopSellingProducts() {
        List<Object[]> aggregates = orderRepository.findTopSellingProductAggregates(PageRequest.of(0, 5));
        List<TopSellingProduct> results = new ArrayList<>();

        if (aggregates != null && !aggregates.isEmpty()) {
            for (Object[] agg : aggregates) {
                Long productId = ((Number) agg[0]).longValue();
                Long unitsSold = ((Number) agg[1]).longValue();
                BigDecimal revenue = (BigDecimal) agg[2];

                Optional<Product> optProduct = productRepository.findById(productId);
                if (optProduct.isPresent()) {
                    Product p = optProduct.get();
                    results.add(mapProductToTopSelling(p, unitsSold, revenue));
                }
            }
        }

        // If no products have been sold yet, fallback to top active products so the table displays nicely
        if (results.isEmpty()) {
            List<Product> recentProducts = productRepository.findTop5ByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE);
            for (Product p : recentProducts) {
                results.add(mapProductToTopSelling(p, 0L, BigDecimal.ZERO));
            }
        }

        return results;
    }

    private TopSellingProduct mapProductToTopSelling(Product p, Long unitsSold, BigDecimal revenue) {
        List<ProductVariant> variants = productVariantRepository.findByProductIdOrderByColorDisplayOrderAscSizeSortOrderAsc(p.getId());
        BigDecimal minSellingPrice = BigDecimal.ZERO;
        BigDecimal maxMrp = BigDecimal.ZERO;
        int totalStock = 0;

        if (!variants.isEmpty()) {
            minSellingPrice = variants.stream()
                    .map(ProductVariant::getSellingPrice)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            maxMrp = variants.stream()
                    .map(ProductVariant::getMrp)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            totalStock = variants.stream().mapToInt(ProductVariant::getStockQuantity).sum();
        }

        Optional<ProductImage> thumb = productImageRepository.findFirstByProductIdAndThumbnailTrueOrderByDisplayOrderAscIdAsc(p.getId());
        String thumbUrl = thumb.map(ProductImage::getMediaUrl).orElse(null);

        return TopSellingProduct.builder()
                .productId(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : "Uncategorized")
                .genderTag(p.getGenderTag() != null ? p.getGenderTag().name() : "UNISEX")
                .unitsSold(unitsSold)
                .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                .sellingPrice(minSellingPrice)
                .mrp(maxMrp)
                .thumbnailUrl(thumbUrl)
                .totalStockQuantity(totalStock)
                .status(p.getStatus() != null ? p.getStatus().name() : "ACTIVE")
                .build();
    }

    private List<LowStockAlert> computeLowStockAlerts() {
        List<ProductVariant> lowStockVariants = productVariantRepository.findLowStockVariants(10, PageRequest.of(0, 10));
        List<LowStockAlert> alerts = new ArrayList<>();

        for (ProductVariant v : lowStockVariants) {
            int stock = v.getStockQuantity();
            String severity = stock == 0 ? "OUT_OF_STOCK" : (stock <= 3 ? "CRITICAL_LOW" : "LOW");
            String sizeCode = v.getSize() != null ? v.getSize().getSizeCode() : "N/A";

            alerts.add(LowStockAlert.builder()
                    .variantId(v.getId())
                    .productId(v.getProduct().getId())
                    .productName(v.getProduct().getName())
                    .skuCode(v.getSku())
                    .sizeCode(sizeCode)
                    .stockQuantity(stock)
                    .isActive(v.isActive())
                    .reorderThreshold(10)
                    .severity(severity)
                    .build());
        }

        return alerts;
    }

    private List<RecentOrder> computeRecentOrders() {
        List<Order> orders = orderRepository.findRecentOrdersWithDetails(PageRequest.of(0, 10));
        List<RecentOrder> result = new ArrayList<>();

        for (Order o : orders) {
            String customerName = "Customer";
            String customerEmail = "";

            if (o.getUser() != null) {
                if (o.getUser().getName() != null && !o.getUser().getName().isBlank()) {
                    customerName = o.getUser().getName();
                }
                if (o.getUser().getEmail() != null) {
                    customerEmail = o.getUser().getEmail();
                }
            } else if (o.getAddress() != null && o.getAddress().getFullName() != null) {
                customerName = o.getAddress().getFullName();
            }

            int itemCount = 0;
            if (o.getItems() != null) {
                itemCount = o.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
            }

            result.add(RecentOrder.builder()
                    .id(o.getId())
                    .orderNumber(o.getOrderNumber())
                    .userId(o.getUser() != null ? o.getUser().getId() : null)
                    .addressId(o.getAddress() != null ? o.getAddress().getId() : null)
                    .customerName(customerName)
                    .customerEmail(customerEmail)
                    .status(o.getStatus() != null ? o.getStatus().name() : "PENDING")
                    .paymentMethod(o.getPaymentMethod())
                    .grandTotal(o.getGrandTotal())
                    .itemCount(itemCount)
                    .createdAt(o.getCreatedAt())
                    .updatedAt(o.getUpdatedAt())
                    .build());
        }

        return result;
    }

    private List<CategoryRevenue> computeCategoryRevenue() {
        List<Object[]> rawAggs = orderRepository.findCategoryRevenueAggregates();
        Map<Long, BigDecimal> revMap = new HashMap<>();
        Map<Long, Long> ordMap = new HashMap<>();
        BigDecimal totalAllRevenue = BigDecimal.ZERO;

        for (Object[] r : rawAggs) {
            Long catId = ((Number) r[0]).longValue();
            BigDecimal rev = (BigDecimal) r[1];
            Long cnt = ((Number) r[2]).longValue();

            if (rev != null) {
                revMap.put(catId, revMap.getOrDefault(catId, BigDecimal.ZERO).add(rev));
                ordMap.put(catId, ordMap.getOrDefault(catId, 0L) + cnt);
                totalAllRevenue = totalAllRevenue.add(rev);
            }
        }

        // Fetch top level root categories
        List<Category> rootCategories = categoryRepository.findByParentCategoryIsNullAndActiveTrueOrderByDisplayOrderAscNameAsc();
        if (rootCategories.isEmpty()) {
            rootCategories = categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc();
        }

        List<CategoryRevenue> result = new ArrayList<>();
        for (Category cat : rootCategories) {
            BigDecimal catRev = revMap.getOrDefault(cat.getId(), BigDecimal.ZERO);
            long catOrders = ordMap.getOrDefault(cat.getId(), 0L);

            double pct = totalAllRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? BigDecimal.valueOf(catRev.doubleValue() / totalAllRevenue.doubleValue() * 100.0)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue()
                    : 0.0;

            result.add(CategoryRevenue.builder()
                    .categoryId(cat.getId())
                    .categoryName(cat.getName())
                    .slug(cat.getSlug())
                    .revenue(catRev)
                    .orderCount(catOrders)
                    .percentOfTotal(pct)
                    .trendPercent(0.0)
                    .build());
        }

        return result;
    }
}
