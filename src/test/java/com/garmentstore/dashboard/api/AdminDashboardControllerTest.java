package com.garmentstore.dashboard.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garmentstore.auth.domain.AccountStatus;
import com.garmentstore.auth.domain.Role;
import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.domain.UserType;
import com.garmentstore.auth.infrastructure.RoleRepository;
import com.garmentstore.auth.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminDashboardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        Role adminRole = roleRepository.findByCode("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().code("ADMIN").name("Admin").build()));

        String email = "dash.admin@example.com";
        if (userRepository.findByEmailIgnoreCase(email).isEmpty()) {
            User admin = User.builder()
                    .userType(UserType.ADMIN)
                    .name("Dashboard Admin")
                    .email(email)
                    .mobile("9876543299")
                    .passwordHash(passwordEncoder.encode("Admin@12345"))
                    .accountStatus(AccountStatus.ACTIVE)
                    .emailVerified(true)
                    .mobileVerified(true)
                    .roles(new HashSet<>())
                    .build();
            admin.getRoles().add(adminRole);
            userRepository.save(admin);
        }

        String loginBody = """
                {"identifier":"dash.admin@example.com","password":"Admin@12345"}
                """;
        String loginResponse = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(loginResponse);
        adminToken = root.path("data").path("accessToken").asText();
    }


    @Test
    void getDashboardMonthlyShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard?period=MONTHLY")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.kpis.total_revenue").exists())
                .andExpect(jsonPath("$.data.kpis.total_orders").exists())
                .andExpect(jsonPath("$.data.kpis.pending_orders").exists())
                .andExpect(jsonPath("$.data.kpis.total_customers").exists())
                .andExpect(jsonPath("$.data.kpis.active_products").exists())
                .andExpect(jsonPath("$.data.kpis.out_of_stock_skus").exists())
                .andExpect(jsonPath("$.data.revenue_chart_data").isArray())
                .andExpect(jsonPath("$.data.order_status_distribution").isArray())
                .andExpect(jsonPath("$.data.top_selling_products").isArray())
                .andExpect(jsonPath("$.data.low_stock_alerts").isArray())
                .andExpect(jsonPath("$.data.recent_orders").isArray())
                .andExpect(jsonPath("$.data.category_revenue").isArray());
    }

    @Test
    void getDashboardDailyShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard?period=DAILY")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.revenue_chart_data.length()").value(7));
    }

    @Test
    void getDashboardWeeklyShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard?period=WEEKLY")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.revenue_chart_data.length()").value(8));
    }

    @Test
    void getDashboardYearlyShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard?period=YEARLY")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.revenue_chart_data.length()").value(5));
    }

    @Test
    void unauthenticatedRequestShouldFail() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard?period=MONTHLY"))
                .andExpect(status().isUnauthorized());
    }
}
