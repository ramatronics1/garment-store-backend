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
class PasswordResetControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        Role customer = roleRepository.findByCode("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().code("CUSTOMER").name("Customer").build()));
        if (userRepository.findByEmailIgnoreCase("reset@example.com").isEmpty()) {
            User user = User.builder().userType(UserType.CUSTOMER).name("Reset User")
                    .email("reset@example.com").mobile("9876500002")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .accountStatus(AccountStatus.ACTIVE).emailVerified(true).mobileVerified(true)
                    .roles(new HashSet<>()).build();
            user.getRoles().add(customer);
            userRepository.save(user);
        }
    }

    @Test
    void forgotAndResetPasswordShouldWork() throws Exception {
        String forgotBody = """
                {"identifier":"reset@example.com"}
                """;
        String forgotResponse = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON).content(forgotBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REQUEST_ACCEPTED"))
                .andExpect(jsonPath("$.data.devResetToken").exists())
                .andReturn().getResponse().getContentAsString();
        String resetToken = objectMapper.readTree(forgotResponse).at("/data/devResetToken").asText();
        String resetBody = """
                {"resetToken":"%s","newPassword":"NewPassword@123"}
                """.formatted(resetToken);
        mockMvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(resetBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PASSWORD_RESET_SUCCESSFUL"));
        String loginBody = """
                {"identifier":"reset@example.com","password":"NewPassword@123"}
                """;
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    void forgotPasswordShouldUseGenericResponseForUnknownIdentifier() throws Exception {
        String forgotBody = """
                {"identifier":"unknown@example.com"}
                """;
        mockMvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content(forgotBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REQUEST_ACCEPTED"))
                .andExpect(jsonPath("$.data.devResetToken").doesNotExist());
    }
}
