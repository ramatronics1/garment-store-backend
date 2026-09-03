package com.garmentstore.order.api;

import com.garmentstore.common.response.ApiResponse;
import com.garmentstore.order.application.OrderService;
import com.garmentstore.order.dto.OrderRequest;
import com.garmentstore.order.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> placeOrder(Authentication authentication, @Valid @RequestBody OrderRequest request) {
        Long userId = Long.valueOf(authentication.getName());
        return ApiResponse.success("Order placed successfully", orderService.placeOrder(userId, request));
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> getMyOrders(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ApiResponse.success("Orders fetched successfully", orderService.getOrdersForUser(userId));
    }
}
