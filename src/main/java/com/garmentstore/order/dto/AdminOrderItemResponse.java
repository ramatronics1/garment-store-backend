package com.garmentstore.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** Single order item in the admin detail view — includes product thumbnail, SKU etc. */
@Data
@Builder
public class AdminOrderItemResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String skuCode;
    private String sizeCode;
    private String thumbnailUrl;
    private String genderTag;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal lineTotal;
}
