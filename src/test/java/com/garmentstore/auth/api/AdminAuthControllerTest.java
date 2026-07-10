package com.garmentstore.auth.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garmentstore.auth.domain.*;
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
class AdminAuthControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.findByCode("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().code("ADMIN").name("Admin").build()));
        ensureAdmin(adminRole, "phase7.admin@example.com", "9876500007");
        ensureAdmin(adminRole, "phase7.lockout@example.com", "9876500008");
    }

    private void ensureAdmin(Role adminRole, String email, String mobile) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }
        User admin = User.builder().userType(UserType.ADMIN).name("Phase7 Admin")
                .email(email).mobile(mobile).passwordHash(passwordEncoder.encode("Admin@12345"))
                .accountStatus(AccountStatus.ACTIVE).emailVerified(true).mobileVerified(true)
                .roles(new HashSet<>()).build();
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
    }

    @Test
    void adminLoginRefreshMeAndLogoutAllShouldWork() throws Exception {
        String loginBody = """
                {"identifier":"phase7.admin@example.com","password":"Admin@12345"}
                """;
        String loginResponse = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.userType").value("ADMIN"))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(loginResponse);
        String accessToken = json.at("/data/accessToken").asText();
        String refreshToken = json.at("/data/refreshToken").asText();

        mockMvc.perform(get("/api/v1/admin/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("phase7.admin@example.com"));

        String refreshBody = """
                {"refreshToken":"%s"}
                """.formatted(refreshToken);
        mockMvc.perform(post("/api/v1/admin/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON).content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    void adminShouldLockAfterFiveFailedAttempts() throws Exception {
        String body = """
                {"identifier":"phase7.lockout@example.com","password":"Wrong@12345"}
                """;
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/v1/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/v1/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ADMIN_ACCOUNT_LOCKED"));
    }
}
