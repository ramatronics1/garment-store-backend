package com.garmentstore.cart.application;

import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.cart.domain.CartItem;
import com.garmentstore.cart.dto.*;
import com.garmentstore.cart.infrastructure.CartItemRepository;
import com.garmentstore.catalog.domain.Product;
import com.garmentstore.catalog.domain.ProductStatus;
import com.garmentstore.catalog.domain.ProductVariant;
import com.garmentstore.catalog.infrastructure.ProductImageRepository;
import com.garmentstore.catalog.infrastructure.ProductRepository;
import com.garmentstore.catalog.infrastructure.ProductVariantRepository;
import com.garmentstore.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cart Service — all business logic for the cart.
 *
 * Design notes:
 * - Cart is user-scoped. Every operation takes a userId (from JWT).
 * - Prices are ALWAYS read from the DB. Client-submitted prices are ignored.
 * - Stock is validated on add/update and again at checkout (via validate endpoint).
 * - Sync strategy: on login, client sends its localStorage items. Server merges them
 *   using MAX(server_qty, client_qty) strategy to be generous to the user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItems;
    private final UserRepository users;
    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final ProductImageRepository images;

    // ── Get Cart ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCart(Long userId) {
        return cartItems.findByUserIdOrderByAddedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Add or Update Item ────────────────────────────────────────────────────

    @Transactional
    public CartItemResponse addOrUpdateItem(Long userId, CartItemRequest req) {
        User user = getUser(userId);
        Product product = getActiveProduct(req.productId());
        ProductVariant variant = getVariantForProduct(req.variantId(), req.productId());

        // Stock check
        validateStock(variant, req.quantity());

        // Upsert: if same variant already in cart, update qty; else create new row
        CartItem item = cartItems.findByUserIdAndVariantId(userId, req.variantId())
                .orElse(CartItem.builder()
                        .user(user)
                        .product(product)
                        .variant(variant)
                        .build());

        int newQty = Math.min(req.quantity(), variant.getStockQuantity());
        item.setQuantity(newQty);
        CartItem saved = cartItems.save(item);
        log.info("[Cart] userId={} upserted variantId={} qty={}", userId, variant.getId(), newQty);
        return toResponse(saved);
    }

    // ── Remove Item ───────────────────────────────────────────────────────────

    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        int removedCount = cartItems.deleteByIdAndUserId(cartItemId, userId);
        if (removedCount == 0) {
            throw new BusinessException(
                    "CART_ITEM_NOT_FOUND", "Cart item not found", HttpStatus.NOT_FOUND);
        }
        log.info("[Cart] userId={} removed cartItemId={}", userId, cartItemId);
    }

    // ── Clear Cart ────────────────────────────────────────────────────────────

    @Transactional
    public void clearCart(Long userId) {
        cartItems.deleteByUserId(userId);
        log.info("[Cart] userId={} cart cleared", userId);
    }

    // ── Sync Cart (post-login merge) ──────────────────────────────────────────

    /**
     * Merges the guest localStorage cart into the server cart.
     * Strategy: for each incoming item, if the variant is already in server cart,
     * take MAX(server qty, client qty). Otherwise add it (stock permitting).
     * Items with out-of-stock variants are silently skipped (user is notified via UI).
     */
    @Transactional
    public List<CartItemResponse> syncCart(Long userId, CartSyncRequest req) {
        User user = getUser(userId);

        for (CartItemRequest incoming : req.items()) {
            try {
                Product product = getActiveProduct(incoming.productId());
                ProductVariant variant = getVariantForProduct(incoming.variantId(), incoming.productId());

                if (variant.getStockQuantity() <= 0) continue; // skip OOS variants

                int allowedQty = Math.min(incoming.quantity(), variant.getStockQuantity());

                Optional<CartItem> existing = cartItems.findByUserIdAndVariantId(userId, incoming.variantId());
                if (existing.isPresent()) {
                    CartItem item = existing.get();
                    // Server wins on conflict, but we take max to be generous
                    int merged = Math.min(Math.max(item.getQuantity(), allowedQty), variant.getStockQuantity());
                    item.setQuantity(merged);
                    cartItems.save(item);
                } else {
                    cartItems.save(CartItem.builder()
                            .user(user)
                            .product(product)
                            .variant(variant)
                            .quantity(allowedQty)
                            .build());
                }
            } catch (BusinessException e) {
                // Skip invalid product/variant references from guest cart
                log.warn("[Cart] Sync skipping invalid item productId={} variantId={}: {}",
                        incoming.productId(), incoming.variantId(), e.getMessage());
            }
        }

        return getCart(userId);
    }

    // ── Validate Cart (pre-checkout) ──────────────────────────────────────────

    /**
     * Validates that all requested cart items have sufficient stock.
     * This is called right before the user places an order.
     * Does NOT modify cart state — read-only.
     */
    @Transactional(readOnly = true)
    public CartValidationResponse validateCart(Long userId, CartSyncRequest req) {
        List<CartValidationResponse.ItemValidation> results = new ArrayList<>();
        boolean allValid = true;

        for (CartItemRequest item : req.items()) {
            try {
                ProductVariant variant = getVariantForProduct(item.variantId(), item.productId());
                Product product = variant.getProduct();
                int available = variant.getStockQuantity();
                boolean sufficient = item.quantity() <= available;
                if (!sufficient) allValid = false;

                String msg = sufficient
                        ? "In stock"
                        : available == 0
                        ? "Out of stock — please remove this item"
                        : "Only " + available + " left — please update quantity";

                results.add(new CartValidationResponse.ItemValidation(
                        variant.getId(), variant.getSize().getName(),
                        product.getName(),
                        item.quantity(), available,
                        sufficient, msg
                ));
            } catch (BusinessException e) {
                allValid = false;
                results.add(new CartValidationResponse.ItemValidation(
                        item.variantId(), "?", "Unknown product",
                        item.quantity(), 0, false,
                        "Product no longer available"
                ));
            }
        }

        return new CartValidationResponse(allValid, results);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private CartItemResponse toResponse(CartItem item) {
        Product p = item.getProduct();
        ProductVariant v = item.getVariant();

        String thumbnail = images
                .findFirstByProductIdAndThumbnailTrueOrderByDisplayOrderAscIdAsc(p.getId())
                .or(() -> images.findByProductIdOrderByDisplayOrderAscIdAsc(p.getId()).stream().findFirst())
                .map(img -> img.getMediaUrl())
                .orElse(null);

        return new CartItemResponse(
                item.getId(),
                p.getId(), p.getName(), thumbnail,
                p.getCategory().getName(),
                p.getGenderTag().name(),
                v.getId(),
                v.getSize() != null ? v.getSize().getName() : null,
                v.getColor() != null ? v.getColor().getName() : null,
                v.getColor() != null ? v.getColor().getCode() : null,
                v.getColor() != null ? v.getColor().getHexCode() : null,
                v.getSku(),
                v.getStockQuantity(), item.getQuantity(),
                v.getSellingPrice(), v.getMrp(), v.getDiscountPercent()
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private User getUser(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        "USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
    }

    private Product getActiveProduct(Long productId) {
        return products.findByIdAndStatus(productId, ProductStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND", "Product not found or no longer active", HttpStatus.NOT_FOUND));
    }

    private ProductVariant getVariantForProduct(Long variantId, Long productId) {
        return variants.findById(variantId)
                .filter(v -> v.getProduct().getId().equals(productId) && v.isActive())
                .orElseThrow(() -> new BusinessException(
                        "VARIANT_NOT_FOUND", "Size variant not found for this product", HttpStatus.NOT_FOUND));
    }

    private void validateStock(ProductVariant variant, int requestedQty) {
        if (variant.getStockQuantity() <= 0) {
            throw new BusinessException(
                    "OUT_OF_STOCK",
                    "Size " + variant.getSize().getName() + " is out of stock",
                    HttpStatus.CONFLICT);
        }
        if (requestedQty > variant.getStockQuantity()) {
            throw new BusinessException(
                    "INSUFFICIENT_STOCK",
                    "Only " + variant.getStockQuantity() + " unit(s) available in size " + variant.getSize().getName(),
                    HttpStatus.CONFLICT);
        }
    }
}
