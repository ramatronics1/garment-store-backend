package com.garmentstore.catalog.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = false, unique = true, length = 220)
    private String slug;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender_tag", nullable = false)
    private GenderTag genderTag;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal mrp;
    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;
    @Column(name = "discount_percent", nullable = false)
    private int discountPercent;
    private String color;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "fabric_details")
    private String fabricDetails;
    @Column(name = "care_instructions")
    private String careInstructions;
    @Column(name = "country_of_origin")
    private String countryOfOrigin;
    @Column(name = "return_policy_enabled", nullable = false)
    private boolean returnPolicyEnabled;
    @Column(name = "meta_title")
    private String metaTitle;
    @Column(name = "meta_description")
    private String metaDescription;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;
    @Column(name = "deleted_at")
    private Instant deletedAt;
}