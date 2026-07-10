package com.garmentstore.auth.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garmentstore.auth.domain.Role;
import com.garmentstore.auth.infrastructure.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthRegistrationControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository.findByCode("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().code("CUSTOMER").name("Customer").build()));
    }

    @Test
    void registerShouldCreatePendingUserAndReturnDevOtp() throws Exception {
        String body = """
                {"name":"Test Customer","email":"customer@example.com","mobile":"9876543210","password":"Password@123"}
                """;
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountStatus").value("PENDING_VERIFICATION"))
                .andExpect(jsonPath("$.data.devOtp").exists())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        assertThat(json.at("/data/userId").asLong()).isPositive();
    }

    @Test
    void registerShouldRejectWeakPassword() throws Exception {
        String body = """
                {"name":"Test Customer","mobile":"9876543211","password":"password"}
                """;
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
