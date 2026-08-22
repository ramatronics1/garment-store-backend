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
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getSizeCode() { return sizeCode; }
    public void setSizeCode(String sizeCode) { this.sizeCode = sizeCode; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public static ProductVariantBuilder builder() { return new ProductVariantBuilder(); }

    public static class ProductVariantBuilder {
        private Long id;
        private Product product;
        private String sizeCode;
        private String skuCode;
        private boolean active;
        private int stockQuantity;

        public ProductVariantBuilder id(Long id) { this.id = id; return this; }
        public ProductVariantBuilder product(Product product) { this.product = product; return this; }
        public ProductVariantBuilder sizeCode(String sizeCode) { this.sizeCode = sizeCode; return this; }
        public ProductVariantBuilder skuCode(String skuCode) { this.skuCode = skuCode; return this; }
        public ProductVariantBuilder active(boolean active) { this.active = active; return this; }
        public ProductVariantBuilder stockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; return this; }

        public ProductVariant build() {
            ProductVariant v = new ProductVariant();
            v.setId(this.id);
            v.setProduct(this.product);
            v.setSizeCode(this.sizeCode);
            v.setSkuCode(this.skuCode);
            v.setActive(this.active);
            v.setStockQuantity(this.stockQuantity);
            return v;
        }
    }
}