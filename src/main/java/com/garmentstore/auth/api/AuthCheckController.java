package com.garmentstore.auth.api;

import com.garmentstore.common.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthCheckController {
    @GetMapping("/public-check")
    public ApiResponse<Map<String, String>> publicCheck() {
        return ApiResponse.success("Public authentication endpoint is reachable", Map.of("access", "public"));
    }

    @GetMapping("/protected-check")
    public ApiResponse<Map<String, Object>> protectedCheck(Authentication auth) {
        return ApiResponse.success("Protected authentication endpoint is reachable", Map.of("principal", auth.getName(), "authorities", auth.getAuthorities().toString()));
    }
}
