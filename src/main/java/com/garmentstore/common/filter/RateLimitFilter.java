package com.garmentstore.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.common.response.ErrorDetails;
import com.garmentstore.common.security.RateLimitProperties;
import com.garmentstore.common.security.RateLimiterService;
import com.garmentstore.common.util.RequestMetadataExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties rateLimitProperties;
    private final RequestMetadataExtractor requestMetadataExtractor;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equalsIgnoreCase(method)) {
            String clientIp = requestMetadataExtractor.clientIp(request);

            if ("/api/v1/admin/auth/login".equalsIgnoreCase(uri)) {
                if (!rateLimiterService.isAllowed(clientIp, "ADMIN_LOGIN", rateLimitProperties.adminLoginRpm())) {
                    writeRateLimitResponse(response, uri, "Too many admin login attempts from your IP. Please wait a minute before retrying.");
                    return;
                }
            } else if ("/api/v1/auth/login".equalsIgnoreCase(uri)) {
                if (!rateLimiterService.isAllowed(clientIp, "CUSTOMER_LOGIN", rateLimitProperties.authLoginRpm())) {
                    writeRateLimitResponse(response, uri, "Too many login attempts from your IP. Please wait a minute before retrying.");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeRateLimitResponse(HttpServletResponse response, String uri, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60");

        ApiResponse<Void> apiResponse = ApiResponse.failure(
                message,
                new ErrorDetails("RATE_LIMIT_EXCEEDED", uri, null)
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
