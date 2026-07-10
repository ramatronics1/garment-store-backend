package com.garmentstore.catalog.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "featured_products")
public class FeaturedProduct extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    @Column(name = "display_order", nullable = false)
    private int displayOrder;
    @Column(name = "is_active", nullable = false)
    private boolean active;
}