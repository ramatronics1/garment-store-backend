package com.garmentstore.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private String name;
    private String size;
    private Integer quantity;
    private BigDecimal price;
    private String image;
}
