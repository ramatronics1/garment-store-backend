package com.garmentstore.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    @Column(name = "size_code", nullable = false)
    private String sizeCode;
    @Column(name = "sku_code", nullable = false, unique = true)
    private String skuCode;
    @Column(name = "is_active", nullable = false)
    private boolean active;
    /**
     * Real-time stock quantity. Updated by admin directly.
     * 0 = out of stock for this size. The UI uses this to disable the size button.
     */
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;
}