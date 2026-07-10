package com.garmentstore.auth.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthHardeningControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    void unauthenticatedRequestShouldReturnJsonErrorAndCorrelationId() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").header("X-Correlation-Id", "test-correlation-id"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Correlation-Id", "test-correlation-id"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void responseShouldContainSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("Referrer-Policy"));
    }
}
