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
class AuthLoginControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        Role customer = roleRepository.findByCode("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().code("CUSTOMER").name("Customer").build()));
        if (userRepository.findByEmailIgnoreCase("login@example.com").isEmpty()) {
            User user = User.builder().userType(UserType.CUSTOMER).name("Login User")
                    .email("login@example.com").mobile("9876500000")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .accountStatus(AccountStatus.ACTIVE).emailVerified(true).mobileVerified(true)
                    .roles(new HashSet<>()).build();
            user.getRoles().add(customer);
            userRepository.save(user);
        }
    }

    @Test
    void loginRefreshAndMeShouldWork() throws Exception {
        String loginBody = """
                {"identifier":"login@example.com","password":"Password@123"}
                """;
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn().getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String accessToken = loginJson.at("/data/accessToken").asText();
        String refreshToken = loginJson.at("/data/refreshToken").asText();

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("login@example.com"));

        String refreshBody = """
                {"refreshToken":"%s"}
                """.formatted(refreshToken);
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON).content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void loginShouldRejectInvalidPassword() throws Exception {
        String body = """
                {"identifier":"login@example.com","password":"Wrong@123"}
                """;
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }
}
