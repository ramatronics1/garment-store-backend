package com.garmentstore.customer.application;

import com.garmentstore.auth.domain.AccountStatus;
import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.domain.UserType;
import com.garmentstore.customer.dto.AdminCustomerListResponse;
import com.garmentstore.customer.infrastructure.CustomerProfileRepository;
import com.garmentstore.order.infrastructure.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCustomerServiceTest {

    @Mock
    private CustomerProfileRepository profileRepo;

    @Mock
    private OrderRepository orderRepo;

    @InjectMocks
    private AdminCustomerService adminCustomerService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .mobile("9876543210")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        user1.setCreatedAt(Instant.now().minusSeconds(86400));

        user2 = User.builder()
                .id(2L)
                .name("Bob")
                .email("bob@example.com")
                .mobile("9876543211")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        user2.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should list customers sorted by total_spent descending at database level")
    void listCustomers_sortedByTotalSpentDesc() {
        // user2 spent 5000, user1 spent 1000
        Object[] row2 = new Object[]{user2, 2L, new BigDecimal("5000.00"), Instant.now()};
        Object[] row1 = new Object[]{user1, 1L, new BigDecimal("1000.00"), Instant.now().minusSeconds(3600)};

        Page<Object[]> page = new PageImpl<>(List.of(row2, row1));

        when(profileRepo.findAdminCustomerSummaries(eq(UserType.CUSTOMER), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);
        when(profileRepo.countByUserType(UserType.CUSTOMER)).thenReturn(2L);
        when(profileRepo.countByUserTypeAndStatus(UserType.CUSTOMER, AccountStatus.ACTIVE)).thenReturn(2L);
        when(orderRepo.findGlobalRevenue()).thenReturn(new BigDecimal("6000.00"));
        when(orderRepo.findCategoryAggregatesByUserIds(anyList())).thenReturn(List.of());

        AdminCustomerListResponse response = adminCustomerService.listCustomers(
                null, null, "total_spent", "desc", 1, 10
        );

        assertThat(response).isNotNull();
        assertThat(response.total()).isEqualTo(2);
        assertThat(response.overview().totalRevenue()).isEqualTo(new BigDecimal("6000.00"));
        assertThat(response.overview().avgLtv()).isEqualTo(new BigDecimal("3000.00"));

        // Verify top spender (user2: Bob) is first on page 1
        assertThat(response.customers()).hasSize(2);
        assertThat(response.customers().get(0).userId()).isEqualTo(2L);
        assertThat(response.customers().get(0).totalSpent()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(response.customers().get(1).userId()).isEqualTo(1L);
        assertThat(response.customers().get(1).totalSpent()).isEqualTo(new BigDecimal("1000.00"));
    }
}
