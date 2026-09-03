package com.garmentstore.order.application;

import com.garmentstore.auth.domain.User;
import com.garmentstore.catalog.domain.ProductImage;
import com.garmentstore.catalog.infrastructure.ProductImageRepository;
import com.garmentstore.common.exception.BusinessException;
import com.garmentstore.order.domain.Order;
import com.garmentstore.order.domain.OrderItem;
import com.garmentstore.order.domain.OrderStatus;
import com.garmentstore.order.domain.OrderStatusHistory;
import com.garmentstore.order.dto.*;
import com.garmentstore.order.infrastructure.OrderRepository;
import com.garmentstore.order.infrastructure.OrderStatusHistoryRepository;
import com.garmentstore.notification.application.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AdminOrderService — production-grade service for the Admin Orders feature.
 *
 * Key design principles:
 *  - Status transitions are validated (illegal moves return 409 Conflict)
 *  - Every status change appends an immutable row to order_status_history
 *  - The exact same orders table is shared with the customer-facing API,
 *    so any status update here is instantly visible to the buyer.
 *  - Bulk updates run individually and report partial failures rather than
 *    rolling back the whole batch.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ProductImageRepository productImageRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ── Allowed transitions ───────────────────────────────────────────────────
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING,   EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPED,   OrderStatus.CANCELLED),
            OrderStatus.SHIPPED,   EnumSet.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Collections.emptySet(),   // terminal
            OrderStatus.CANCELLED, Collections.emptySet()    // terminal
    );

    // ────────────────────────────────────────────────────────────────────────
    //  1. GET paginated orders list
    // ────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public AdminOrderPageResponse getOrders(String q, String status, String dateRange,
                                            String sort, String dir, int page, int size) {

        // ── Parse filter params ────────────────────────────────────────────
        OrderStatus statusEnum = parseStatus(status);
        Instant[] range = parseDateRange(dateRange);

        // ── Build Pageable with dynamic sort ──────────────────────────────
        String sortField = switch (sort != null ? sort : "created_at") {
            case "order_number" -> "orderNumber";
            case "grand_total"  -> "grandTotal";
            case "customer"     -> "user.name";
            default             -> "createdAt";
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortField));

        // ── Fetch page ────────────────────────────────────────────────────
        String normalizedQ = (q != null && !q.isBlank()) ? q.strip() : null;
        Page<Order> orderPage = orderRepository.findAdminOrders(statusEnum, range[0], range[1], normalizedQ, pageable);

        // ── Status counts (always full dataset, not just filtered page) ───
        Map<String, Long> statusCounts = buildStatusCounts();

        List<AdminOrderSummaryResponse> orders = orderPage.getContent()
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());

        return AdminOrderPageResponse.builder()
                .orders(orders)
                .statusCounts(statusCounts)
                .total(orderPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  2. GET single order detail
    // ────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public AdminOrderDetailResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND",
                        "Order not found with id: " + orderId, HttpStatus.NOT_FOUND));

        List<OrderStatusHistory> history = historyRepository.findByOrderIdOrderByCreatedAtAsc(orderId);

        return toDetail(order, history);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  3. PATCH single order status
    // ────────────────────────────────────────────────────────────────────────
    @Transactional
    public AdminOrderDetailResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, Long adminId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND",
                        "Order not found with id: " + orderId, HttpStatus.NOT_FOUND));

        validateTransition(order.getStatus(), request.getStatus());

        order.setStatus(request.getStatus());
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        appendHistory(order, request.getStatus(), "ADMIN", adminId, request.getNote());

        List<OrderStatusHistory> history = historyRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        log.info("Admin [{}] updated order [{}] status: {} → {}",
                adminId, orderId, order.getStatus(), request.getStatus());

        // Publish event so notification module can alert the customer
        User customer = order.getUser();
        eventPublisher.publishEvent(new OrderStatusChangedEvent(
                order.getId(), order.getOrderNumber(), order.getGrandTotal(),
                request.getStatus(),
                customer.getId(),
                customer.getName() != null ? customer.getName() : "Valued Customer",
                customer.getEmail(), customer.getMobile()
        ));

        return toDetail(order, history);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  4. PATCH bulk order status
    // ────────────────────────────────────────────────────────────────────────
    @Transactional
    public BulkStatusUpdateResponse bulkUpdateOrderStatus(BulkStatusUpdateRequest request, Long adminId) {
        if (request.getOrderIds() == null || request.getOrderIds().isEmpty()) {
            throw new BusinessException("EMPTY_IDS", "Order IDs must not be empty", HttpStatus.BAD_REQUEST);
        }
        if (request.getOrderIds().size() > 200) {
            throw new BusinessException("TOO_MANY_IDS", "Cannot update more than 200 orders at once", HttpStatus.BAD_REQUEST);
        }

        List<Long> failedIds = new ArrayList<>();
        int updatedCount = 0;

        // Validate and update each order independently (no atomic rollback on partial failure)
        List<Order> orders = orderRepository.findAllById(request.getOrderIds());

        // Check for IDs not found in DB
        Set<Long> foundIds = orders.stream().map(Order::getId).collect(Collectors.toSet());
        request.getOrderIds().stream()
                .filter(id -> !foundIds.contains(id))
                .forEach(failedIds::add);

        for (Order order : orders) {
            try {
                validateTransition(order.getStatus(), request.getStatus());
                order.setStatus(request.getStatus());
                order.setUpdatedAt(Instant.now());
                orderRepository.save(order);
                appendHistory(order, request.getStatus(), "ADMIN", adminId, request.getNote());
                // Notify customer of status change
                User customer = order.getUser();
                eventPublisher.publishEvent(new OrderStatusChangedEvent(
                        order.getId(), order.getOrderNumber(), order.getGrandTotal(),
                        request.getStatus(),
                        customer.getId(),
                        customer.getName() != null ? customer.getName() : "Valued Customer",
                        customer.getEmail(), customer.getMobile()
                ));
                updatedCount++;
            } catch (BusinessException e) {
                log.warn("Bulk update skipped order [{}]: {}", order.getId(), e.getMessage());
                failedIds.add(order.getId());
            }
        }

        log.info("Admin [{}] bulk updated {} orders to {}", adminId, updatedCount, request.getStatus());

        String message = updatedCount + " order" + (updatedCount != 1 ? "s" : "") +
                " updated to " + request.getStatus().name() +
                (failedIds.isEmpty() ? "" : ". " + failedIds.size() + " failed.");

        return BulkStatusUpdateResponse.builder()
                .updatedCount(updatedCount)
                .failedIds(failedIds)
                .message(message)
                .build();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Private helpers
    // ────────────────────────────────────────────────────────────────────────

    /** Validates that the status transition is allowed. Throws 409 if not. */
    private void validateTransition(OrderStatus current, OrderStatus next) {
        if (current == next) {
            throw new BusinessException("SAME_STATUS",
                    "Order is already in " + current + " status", HttpStatus.CONFLICT);
        }
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Collections.emptySet());
        if (!allowed.contains(next)) {
            throw new BusinessException("ILLEGAL_TRANSITION",
                    "Cannot transition order from " + current + " to " + next +
                    ". Allowed next states: " + allowed, HttpStatus.CONFLICT);
        }
    }

    /** Appends an immutable history entry to the status timeline. */
    private void appendHistory(Order order, OrderStatus status, String byType, Long byId, String note) {
        OrderStatusHistory entry = OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .changedByType(byType)
                .changedById(byId)
                .note(note)
                .build();
        historyRepository.save(entry);
    }

    /** Fetches total status counts across ALL orders (ignores current filters). */
    private Map<String, Long> buildStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        // Pre-fill all known statuses with 0
        for (OrderStatus s : OrderStatus.values()) {
            counts.put(s.name(), 0L);
        }
        orderRepository.countByStatus().forEach(row -> {
            counts.put(row[0].toString(), (Long) row[1]);
        });
        return counts;
    }

    /** Resolves date range filter to [from, to] Instant pair. */
    private Instant[] parseDateRange(String range) {
        Instant now = Instant.now();
        return switch (range != null ? range : "ALL") {
            case "TODAY" -> new Instant[]{now.truncatedTo(ChronoUnit.DAYS), null};
            case "7D"    -> new Instant[]{now.minus(7, ChronoUnit.DAYS), null};
            case "30D"   -> new Instant[]{now.minus(30, ChronoUnit.DAYS), null};
            default      -> new Instant[]{null, null};
        };
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("INVALID_STATUS",
                    "Unknown order status: " + status, HttpStatus.BAD_REQUEST);
        }
    }

    // ── Mappers ────────────────────────────────────────────────────────────

    private AdminOrderSummaryResponse toSummary(Order o) {
        AdminOrderSummaryResponse.AdminCustomerSummary customer = null;
        if (o.getUser() != null) {
            customer = AdminOrderSummaryResponse.AdminCustomerSummary.builder()
                    .userId(o.getUser().getId())
                    .name(o.getUser().getName())
                    .email(o.getUser().getEmail())
                    .mobile(o.getUser().getMobile())
                    .build();
        }
        return AdminOrderSummaryResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .status(o.getStatus())
                .grandTotal(o.getGrandTotal())
                .itemCount(o.getItems().size())
                .paymentMethod(o.getPaymentMethod())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .customer(customer)
                .build();
    }

    private AdminOrderDetailResponse toDetail(Order o, List<OrderStatusHistory> history) {
        AdminOrderSummaryResponse.AdminCustomerSummary customer = null;
        if (o.getUser() != null) {
            customer = AdminOrderSummaryResponse.AdminCustomerSummary.builder()
                    .userId(o.getUser().getId())
                    .name(o.getUser().getName())
                    .email(o.getUser().getEmail())
                    .mobile(o.getUser().getMobile())
                    .build();
        }

        AdminDeliveryAddressResponse address = null;
        if (o.getAddress() != null) {
            address = AdminDeliveryAddressResponse.builder()
                    .addressId(o.getAddress().getId())
                    .fullName(o.getAddress().getFullName())
                    .phone(o.getAddress().getPhone())
                    .flatHouseNo(o.getAddress().getFlatHouseNo())
                    .street(o.getAddress().getStreet())
                    .areaLandmark(o.getAddress().getAreaLandmark())
                    .city(o.getAddress().getCity())
                    .state(o.getAddress().getState())
                    .pincode(o.getAddress().getPincode())
                    .addressType(o.getAddress().getAddressType() != null ? o.getAddress().getAddressType().name() : null)
                    .build();
        }

        List<AdminOrderItemResponse> items = o.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        // Mark the last history entry as isCurrent
        List<AdminOrderStatusHistoryResponse> historyDtos = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            OrderStatusHistory h = history.get(i);
            historyDtos.add(AdminOrderStatusHistoryResponse.builder()
                    .id(h.getId())
                    .status(h.getStatus())
                    .changedByType(h.getChangedByType())
                    .changedById(h.getChangedById())
                    .note(h.getNote())
                    .createdAt(h.getCreatedAt())
                    .isCurrent(i == history.size() - 1)
                    .build());
        }

        return AdminOrderDetailResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .status(o.getStatus())
                .grandTotal(o.getGrandTotal())
                .itemCount(o.getItems().size())
                .paymentMethod(o.getPaymentMethod())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .customer(customer)
                .deliveryAddress(address)
                .items(items)
                .statusHistory(historyDtos)
                .build();
    }

    private AdminOrderItemResponse toItemResponse(OrderItem item) {
        // Resolve thumbnail URL
        String thumbnailUrl = productImageRepository
                .findFirstByProductIdAndThumbnailTrueOrderByDisplayOrderAscIdAsc(item.getProduct().getId())
                .map(ProductImage::getMediaUrl)
                .orElseGet(() -> productImageRepository
                        .findByProductIdOrderByDisplayOrderAscIdAsc(item.getProduct().getId())
                        .stream().findFirst().map(ProductImage::getMediaUrl).orElse(null));

        return AdminOrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .variantId(item.getVariant().getId())
                .productName(item.getProduct().getName())
                .skuCode(item.getVariant().getSku())
                .sizeCode(item.getVariant().getSize().getName())
                .thumbnailUrl(thumbnailUrl)
                .genderTag(item.getProduct().getGenderTag() != null ? item.getProduct().getGenderTag().name() : null)
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .lineTotal(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .build();
    }
}
