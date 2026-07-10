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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthLogoutControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        Role customer = roleRepository.findByCode("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().code("CUSTOMER").name("Customer").build()));
        if (userRepository.findByEmailIgnoreCase("logout@example.com").isEmpty()) {
            User user = User.builder().userType(UserType.CUSTOMER).name("Logout User")
                    .email("logout@example.com").mobile("9876500001")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .accountStatus(AccountStatus.ACTIVE).emailVerified(true).mobileVerified(true)
                    .roles(new HashSet<>()).build();
            user.getRoles().add(customer);
            userRepository.save(user);
        }
    }

    @Test
    void logoutShouldRevokeCurrentRefreshToken() throws Exception {
        JsonNode loginJson = login();
        String refreshToken = loginJson.at("/data/refreshToken").asText();
        String logoutBody = """
                {"refreshToken":"%s"}
                """.formatted(refreshToken);
        mockMvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON).content(logoutBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LOGGED_OUT"));
        mockMvc.perform(post("/api/v1/auth/refresh-token").contentType(MediaType.APPLICATION_JSON).content(logoutBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutAllShouldRevokeAllUserRefreshTokens() throws Exception {
        JsonNode loginJson = login();
        String accessToken = loginJson.at("/data/accessToken").asText();
        String refreshToken = loginJson.at("/data/refreshToken").asText();
        mockMvc.perform(post("/api/v1/auth/logout-all").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LOGGED_OUT_ALL"));
        String refreshBody = """
                {"refreshToken":"%s"}
                """.formatted(refreshToken);
        mockMvc.perform(post("/api/v1/auth/refresh-token").contentType(MediaType.APPLICATION_JSON).content(refreshBody))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode login() throws Exception {
        String loginBody = """
                {"identifier":"logout@example.com","password":"Password@123"}
                """;
        String response = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }
}
