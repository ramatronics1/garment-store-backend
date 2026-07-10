package com.garmentstore.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    @Column(name = "media_url", nullable = false, length = 700)
    private String mediaUrl;
    @Column(name = "display_order", nullable = false)
    private int displayOrder;
    @Column(name = "is_thumbnail", nullable = false)
    private boolean thumbnail;
}