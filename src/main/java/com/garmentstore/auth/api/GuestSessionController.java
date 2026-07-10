package com.garmentstore.auth.api;

import com.garmentstore.auth.application.GuestSessionService;
import com.garmentstore.auth.dto.GuestSessionRequest;
import com.garmentstore.auth.dto.GuestSessionResponse;
import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.common.util.RequestMetadataExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GuestSessionController {
    private final GuestSessionService guestSessionService;
    private final RequestMetadataExtractor requestMetadataExtractor;

    @PostMapping("/guest-session")
    public ApiResponse<GuestSessionResponse> createGuestSession(@Valid @RequestBody(required = false) GuestSessionRequest request,
                                                                HttpServletRequest servletRequest) {
        GuestSessionRequest safeRequest = request == null ? new GuestSessionRequest(null, null) : request;
        return ApiResponse.success("Guest session created successfully", guestSessionService.createGuestSession(
                safeRequest,
                requestMetadataExtractor.clientIp(servletRequest),
                requestMetadataExtractor.userAgent(servletRequest)
        ));
    }
}
