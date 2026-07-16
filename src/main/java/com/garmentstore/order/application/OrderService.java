package com.garmentstore.order.application;

import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.cart.domain.CartItem;
import com.garmentstore.cart.infrastructure.CartItemRepository;
import com.garmentstore.catalog.domain.ProductVariant;
import com.garmentstore.catalog.infrastructure.ProductVariantRepository;
import com.garmentstore.catalog.infrastructure.ProductImageRepository;
import com.garmentstore.catalog.domain.ProductImage;
import com.garmentstore.common.exception.BusinessException;
import com.garmentstore.customer.domain.Address;
import com.garmentstore.customer.dto.AddressResponse;
import com.garmentstore.customer.infrastructure.AddressRepository;
import com.garmentstore.order.domain.Order;
import com.garmentstore.order.domain.OrderItem;
import com.garmentstore.order.domain.OrderStatus;
import com.garmentstore.order.dto.OrderItemResponse;
import com.garmentstore.order.dto.OrderRequest;
import com.garmentstore.order.dto.OrderResponse;
import com.garmentstore.order.infrastructure.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), userId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Address not found", HttpStatus.NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByAddedAtDesc(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException("EMPTY_CART", "Cannot place order with an empty cart", HttpStatus.BAD_REQUEST);
        }

        BigDecimal grandTotal = BigDecimal.ZERO;
        Order order = Order.builder()
                .user(user)
                .orderNumber(generateOrderNumber())
                .address(address)
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.PENDING)
                .build();

        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getVariant();
            if (variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new BusinessException("INSUFFICIENT_STOCK", "Not enough stock for variant " + variant.getSkuCode(), HttpStatus.BAD_REQUEST);
            }
            // Decrement stock
            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            productVariantRepository.save(variant);

            BigDecimal price = variant.getProduct().getSellingPrice();
            grandTotal = grandTotal.add(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            OrderItem orderItem = OrderItem.builder()
                    .product(cartItem.getProduct())
                    .variant(variant)
                    .quantity(cartItem.getQuantity())
                    .price(price)
                    .build();
            order.addItem(orderItem);
        }

        order.setGrandTotal(grandTotal);
        orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteByUserId(userId);

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .grandTotal(order.getGrandTotal())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .shippingAddress(mapAddress(order.getAddress()))
                .items(order.getItems().stream().map(this::mapItem).collect(Collectors.toList()))
                .build();
    }

    private AddressResponse mapAddress(Address address) {
        if (address == null) return null;
        return new AddressResponse(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getFlatHouseNo(),
                address.getStreet(),
                address.getAreaLandmark(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getAddressType(),
                address.isDefaultAddress(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }

    private OrderItemResponse mapItem(OrderItem item) {
        String imageUrl = productImageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(item.getProduct().getId())
                .stream().findFirst().map(ProductImage::getMediaUrl).orElse(null);
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .variantId(item.getVariant().getId())
                .name(item.getProduct().getName())
                .size(item.getVariant().getSizeCode())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .image(imageUrl)
                .build();
    }

    private String generateOrderNumber() {
        int randomNum = secureRandom.nextInt(90000) + 10000;
        return "VAS-" + Year.now().getValue() + "-" + randomNum;
    }
}
