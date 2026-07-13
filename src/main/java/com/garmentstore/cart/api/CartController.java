package com.garmentstore.cart.api;

import com.garmentstore.cart.application.CartService;
import com.garmentstore.cart.dto.*;
import com.garmentstore.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cart REST API — all endpoints require authentication (JWT).
 *
 * GET    /api/v1/cart              → get current user's cart
 * POST   /api/v1/cart/items        → add/update an item (upsert by variant)
 * DELETE /api/v1/cart/items/{id}   → remove a specific item
 * DELETE /api/v1/cart              → clear entire cart
 * POST   /api/v1/cart/sync         → merge guest localStorage cart after login
 * POST   /api/v1/cart/validate     → pre-checkout stock validation (read-only)
 *
 * Security note: userId is ALWAYS taken from the authenticated JWT principal.
 * The client never sends userId — it cannot be trusted.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItemResponse>> getCart(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = extractUserId(principal);
        return ApiResponse.success("Cart fetched successfully", cartService.getCart(userId));
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CartItemResponse> addItem(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CartItemRequest req) {
        Long userId = extractUserId(principal);
        return ApiResponse.success("Item added to cart", cartService.addOrUpdateItem(userId, req));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> removeItem(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        Long userId = extractUserId(principal);
        cartService.removeItem(userId, id);
        return ApiResponse.success("Item removed from cart", null);
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = extractUserId(principal);
        cartService.clearCart(userId);
        return ApiResponse.success("Cart cleared", null);
    }

    /**
     * Called immediately after login to merge the guest localStorage cart
     * with the server cart.
     */
    @PostMapping("/sync")
    public ApiResponse<List<CartItemResponse>> syncCart(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CartSyncRequest req) {
        Long userId = extractUserId(principal);
        return ApiResponse.success("Cart synced successfully", cartService.syncCart(userId, req));
    }

    /**
     * Called before placing an order to verify all items still have sufficient stock.
     * Read-only — does NOT modify cart or stock.
     */
    @PostMapping("/validate")
    public ApiResponse<CartValidationResponse> validateCart(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CartSyncRequest req) {
        Long userId = extractUserId(principal);
        CartValidationResponse result = cartService.validateCart(userId, req);
        String message = result.valid() ? "All items are in stock" : "Some items have stock issues";
        return ApiResponse.success(message, result);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Extracts numeric userId from the JWT principal username.
     * Our UserDetails implementation uses the userId as the username.
     */
    private Long extractUserId(UserDetails principal) {
        try {
            return Long.parseLong(principal.getUsername());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid principal — expected numeric userId");
        }
    }
}
