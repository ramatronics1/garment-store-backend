package com.garmentstore.customer.application;

import com.garmentstore.auth.application.RefreshTokenService;
import com.garmentstore.auth.domain.AccountStatus;
import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.domain.UserType;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.common.audit.AuditLogService;
import com.garmentstore.common.exception.BusinessException;
import com.garmentstore.customer.dto.*;
import com.garmentstore.customer.infrastructure.CustomerProfileRepository;
import com.garmentstore.order.infrastructure.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

/**
 * AdminCustomerService — business logic for the admin customers list page.
 *
 * Strategy (Database-level sorting & pagination):
 *   1. Single JPQL query joins User with Order aggregates and sorts/paginates at DB level.
 *   2. Batch-fetch category favourites ONLY for the target page's customer IDs.
 */
@Service
@RequiredArgsConstructor
public class AdminCustomerService {

    private final CustomerProfileRepository profileRepo;
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService audit;

    // ── Public API ──────────────────────────────────────────────────────────────

    /**
     * Returns a paginated + filtered + sorted list of customers along with KPI overview.
     *
     * @param q      search term (name / email / mobile), nullable
     * @param status AccountStatus filter, nullable (null = all)
     * @param sort   sort field: name | created_at | last_login | total_spent | total_orders | last_order
     * @param dir    asc | desc
     * @param page   1-indexed page number
     * @param size   page size (max 100)
     */
    @Transactional(readOnly = true)
    public AdminCustomerListResponse listCustomers(
            String q,
            String status,
            String sort,
            String dir,
            int page,
            int size
    ) {
        size = Math.min(Math.max(size, 1), 100);
        int zeroPage = Math.max(page - 1, 0);

        AccountStatus statusEnum = parseStatus(status);
        String cleanQ = (q == null || q.isBlank()) ? null : q.trim();

        // Database-level sorting — map frontend field names to JPQL aliases / entity fields
        Sort springSort = buildSort(sort, dir);
        PageRequest pageable = PageRequest.of(zeroPage, size, springSort);

        // Step 1: Paginate and aggregate users at database level
        Page<Object[]> pageResult = profileRepo.findAdminCustomerSummaries(
                UserType.CUSTOMER, statusEnum, cleanQ, pageable
        );

        List<Object[]> rows = pageResult.getContent();
        List<Long> userIds = rows.stream().map(r -> ((User) r[0]).getId()).toList();

        // Step 2: Batch-fetch category favourites for only the target page's customers
        Map<Long, String> favCategoryMap = fetchFavouriteCategories(userIds);

        // Step 3: Build overview KPIs (global, not filtered)
        AdminCustomerOverview overview = buildOverview();

        // Step 4: Assemble customer summaries
        List<AdminCustomerSummary> summaries = rows.stream().map(r -> {
            User u = (User) r[0];
            long count = r[1] instanceof Number n ? n.longValue() : 0L;
            BigDecimal spent = toBigDecimal(r[2]);
            Instant lastOrder = r[3] instanceof Instant inst ? inst : null;
            String fav = favCategoryMap.get(u.getId());
            return new AdminCustomerSummary(
                    u.getId(),
                    u.getName(),
                    u.getEmail(),
                    u.getMobile(),
                    u.getAccountStatus() != null ? u.getAccountStatus().name() : null,
                    u.isEmailVerified(),
                    u.isMobileVerified(),
                    u.getCreatedAt(),
                    u.getLastLoginAt(),
                    count,
                    spent,
                    lastOrder,
                    fav
            );
        }).toList();

        int totalPages = (int) Math.max(1, Math.ceil((double) pageResult.getTotalElements() / size));

        return new AdminCustomerListResponse(
                overview,
                summaries,
                pageResult.getTotalElements(),
                page,
                size,
                totalPages
        );
    }

    // ── Account Status Management ────────────────────────────────────────────────

    /**
     * Updates a customer's account status (BAN / UNBAN / DISABLE).
     *
     * Guard rails:
     *   - Target must be a CUSTOMER (not an admin)
     *   - Admin cannot act on themselves
     *   - UNBAN is only allowed when current status is LOCKED
     *   - DISABLE from LOCKED is allowed (belt-and-suspenders)
     *   - BAN & DISABLE revoke all active sessions immediately
     *   - Every action is recorded in the audit log
     *
     * @param targetUserId  ID of the customer to act on
     * @param request       action + optional reason
     * @param adminAuth     Spring Security authentication of the calling admin
     */
    @Transactional
    public CustomerAccountActionResponse updateAccountStatus(
            Long targetUserId,
            CustomerAccountActionRequest request,
            Authentication adminAuth
    ) {
        Long adminId = Long.valueOf(adminAuth.getName());

        // 1. Load target user
        User target = userRepo.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(
                        "CUSTOMER_NOT_FOUND", "Customer not found", HttpStatus.NOT_FOUND));

        // 2. Guard: target must be a CUSTOMER
        if (target.getUserType() != UserType.CUSTOMER) {
            throw new BusinessException(
                    "TARGET_NOT_CUSTOMER",
                    "Only CUSTOMER accounts can be managed via this endpoint",
                    HttpStatus.FORBIDDEN);
        }

        // 3. Guard: admin cannot act on themselves
        if (adminId.equals(targetUserId)) {
            throw new BusinessException(
                    "SELF_ACTION_FORBIDDEN",
                    "You cannot perform account actions on your own account",
                    HttpStatus.FORBIDDEN);
        }

        AccountStatus current = target.getAccountStatus();
        CustomerAccountActionRequest.Action action = request.action();

        // 4. Validate transition
        AccountStatus next = switch (action) {
            case BAN -> {
                if (current == AccountStatus.DISABLED) throw new BusinessException(
                        "INVALID_TRANSITION",
                        "Cannot ban a DISABLED account. The account is already deactivated.",
                        HttpStatus.CONFLICT);
                if (current == AccountStatus.LOCKED) throw new BusinessException(
                        "ALREADY_LOCKED",
                        "Customer is already banned (LOCKED).",
                        HttpStatus.CONFLICT);
                yield AccountStatus.LOCKED;
            }
            case UNBAN -> {
                if (current != AccountStatus.LOCKED) throw new BusinessException(
                        "INVALID_TRANSITION",
                        "UNBAN is only valid for LOCKED accounts. Current status: " + current,
                        HttpStatus.CONFLICT);
                yield AccountStatus.ACTIVE;
            }
            case DISABLE -> {
                if (current == AccountStatus.DISABLED) throw new BusinessException(
                        "ALREADY_DISABLED",
                        "Customer account is already DISABLED.",
                        HttpStatus.CONFLICT);
                yield AccountStatus.DISABLED;
            }
        };

        // 5. Apply status change
        target.setAccountStatus(next);
        userRepo.save(target);

        // 6. Revoke sessions for destructive actions
        int revoked = 0;
        if (action == CustomerAccountActionRequest.Action.BAN
                || action == CustomerAccountActionRequest.Action.DISABLE) {
            revoked = refreshTokenService.revokeAllForUser(targetUserId);
        }

        // 7. Audit log
        String afterJson = String.format(
                "{\"action\":\"%s\",\"previousStatus\":\"%s\",\"newStatus\":\"%s\",\"sessionsRevoked\":%d,\"reason\":\"%s\"}",
                action, current, next, revoked,
                request.reason() == null ? "" : request.reason().replace("\"", "'")
        );
        audit.record(adminId, "ADMIN", "CUSTOMER_STATUS_" + action.name(),
                "USER", String.valueOf(targetUserId), afterJson, null);

        // 8. Build human-readable message
        String message = switch (action) {
            case BAN     -> "Customer banned successfully. " + revoked + " session(s) revoked.";
            case UNBAN   -> "Customer unbanned successfully.";
            case DISABLE -> "Customer disabled successfully. " + revoked + " session(s) revoked.";
        };

        return new CustomerAccountActionResponse(targetUserId, next.name(), revoked, message);
    }

    private AdminCustomerOverview buildOverview() {
        long totalCustomers = profileRepo.countByUserType(UserType.CUSTOMER);
        long activeCustomers = profileRepo.countByUserTypeAndStatus(UserType.CUSTOMER, AccountStatus.ACTIVE);

        BigDecimal totalRevenue = orderRepo.findGlobalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        BigDecimal avgLtv = totalCustomers > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new AdminCustomerOverview(totalCustomers, activeCustomers, totalRevenue, avgLtv);
    }

    private Map<Long, String> fetchFavouriteCategories(List<Long> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        List<Object[]> rows = orderRepo.findCategoryAggregatesByUserIds(userIds);

        // For each user, accumulate quantity sums per gender tag, then pick the highest
        Map<Long, Map<String, Long>> perUser = new HashMap<>();
        for (Object[] row : rows) {
            Long uid      = ((Number) row[0]).longValue();
            String tag    = row[1] == null ? "UNISEX" : row[1].toString();
            long qty      = ((Number) row[2]).longValue();
            perUser.computeIfAbsent(uid, k -> new HashMap<>())
                   .merge(tag, qty, Long::sum);
        }

        Map<Long, String> result = new HashMap<>();
        perUser.forEach((uid, tagCounts) -> {
            String fav = tagCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            result.put(uid, fav);
        });
        return result;
    }

    private Sort buildSort(String sort, String dir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String field = switch (sort) {
            case "name"         -> "u.name";
            case "last_login"   -> "u.lastLoginAt";
            case "created_at"   -> "u.createdAt";
            case "total_spent"  -> "totalSpent";
            case "total_orders" -> "totalOrders";
            case "last_order"   -> "lastOrderDate";
            default             -> "totalSpent";
        };
        return Sort.by(direction, field).and(Sort.by(Sort.Direction.DESC, "u.id"));
    }

    private AccountStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try { return AccountStatus.valueOf(status.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        if (val instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}
