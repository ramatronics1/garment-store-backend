package com.garmentstore.catalog.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Immutable business identifier, e.g. SHRT001. Generated once on creation. */
    @Column(name = "product_code", unique = true, length = 30)
    private String productCode;

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

    @Column(length = 100)
    private String brand;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "fabric_details")
    private String fabricDetails;

    @Column(length = 80)
    private String fit;

    @Column(length = 80)
    private String season;

    @Column(name = "care_instructions")
    private String careInstructions;

    @Column(name = "country_of_origin")
    private String countryOfOrigin;

    @Column(name = "return_policy_enabled", nullable = false)
    private boolean returnPolicyEnabled = true;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description")
    private String metaDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Product() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public GenderTag getGenderTag() { return genderTag; }
    public void setGenderTag(GenderTag genderTag) { this.genderTag = genderTag; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFabricDetails() { return fabricDetails; }
    public void setFabricDetails(String fabricDetails) { this.fabricDetails = fabricDetails; }
    public String getFit() { return fit; }
    public void setFit(String fit) { this.fit = fit; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public String getCareInstructions() { return careInstructions; }
    public void setCareInstructions(String careInstructions) { this.careInstructions = careInstructions; }
    public String getCountryOfOrigin() { return countryOfOrigin; }
    public void setCountryOfOrigin(String countryOfOrigin) { this.countryOfOrigin = countryOfOrigin; }
    public boolean isReturnPolicyEnabled() { return returnPolicyEnabled; }
    public void setReturnPolicyEnabled(boolean returnPolicyEnabled) { this.returnPolicyEnabled = returnPolicyEnabled; }
    public String getMetaTitle() { return metaTitle; }
    public void setMetaTitle(String metaTitle) { this.metaTitle = metaTitle; }
    public String getMetaDescription() { return metaDescription; }
    public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public static ProductBuilder builder() { return new ProductBuilder(); }

    public static class ProductBuilder {
        private Long id;
        private String productCode;
        private String name;
        private String slug;
        private Category category;
        private GenderTag genderTag;
        private String brand;
        private String description;
        private String fabricDetails;
        private String fit;
        private String season;
        private String careInstructions;
        private String countryOfOrigin;
        private boolean returnPolicyEnabled = true;
        private String metaTitle;
        private String metaDescription;
        private ProductStatus status;
        private Instant deletedAt;

        public ProductBuilder id(Long id) { this.id = id; return this; }
        public ProductBuilder productCode(String productCode) { this.productCode = productCode; return this; }
        public ProductBuilder name(String name) { this.name = name; return this; }
        public ProductBuilder slug(String slug) { this.slug = slug; return this; }
        public ProductBuilder category(Category category) { this.category = category; return this; }
        public ProductBuilder genderTag(GenderTag genderTag) { this.genderTag = genderTag; return this; }
        public ProductBuilder brand(String brand) { this.brand = brand; return this; }
        public ProductBuilder description(String description) { this.description = description; return this; }
        public ProductBuilder fabricDetails(String fabricDetails) { this.fabricDetails = fabricDetails; return this; }
        public ProductBuilder fit(String fit) { this.fit = fit; return this; }
        public ProductBuilder season(String season) { this.season = season; return this; }
        public ProductBuilder careInstructions(String careInstructions) { this.careInstructions = careInstructions; return this; }
        public ProductBuilder countryOfOrigin(String countryOfOrigin) { this.countryOfOrigin = countryOfOrigin; return this; }
        public ProductBuilder returnPolicyEnabled(boolean returnPolicyEnabled) { this.returnPolicyEnabled = returnPolicyEnabled; return this; }
        public ProductBuilder metaTitle(String metaTitle) { this.metaTitle = metaTitle; return this; }
        public ProductBuilder metaDescription(String metaDescription) { this.metaDescription = metaDescription; return this; }
        public ProductBuilder status(ProductStatus status) { this.status = status; return this; }
        public ProductBuilder deletedAt(Instant deletedAt) { this.deletedAt = deletedAt; return this; }

        public Product build() {
            Product p = new Product();
            p.setId(this.id);
            p.setProductCode(this.productCode);
            p.setName(this.name);
            p.setSlug(this.slug);
            p.setCategory(this.category);
            p.setGenderTag(this.genderTag);
            p.setBrand(this.brand);
            p.setDescription(this.description);
            p.setFabricDetails(this.fabricDetails);
            p.setFit(this.fit);
            p.setSeason(this.season);
            p.setCareInstructions(this.careInstructions);
            p.setCountryOfOrigin(this.countryOfOrigin);
            p.setReturnPolicyEnabled(this.returnPolicyEnabled);
            p.setMetaTitle(this.metaTitle);
            p.setMetaDescription(this.metaDescription);
            p.setStatus(this.status);
            p.setDeletedAt(this.deletedAt);
            return p;
        }
    }
}