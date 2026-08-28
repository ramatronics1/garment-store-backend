package com.garmentstore.catalog.domain;

import com.garmentstore.common.domain.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents one purchasable combination: a specific Color × Size with its own
 * SKU, price, and stock. This is what the customer actually adds to cart.
 *
 * combinationKey is a pipe-separated canonical string of (colorId|sizeId), used
 * for the UNIQUE(product_id, combination_key) constraint to prevent duplicates.
 */
@Entity
@Table(name = "product_variants",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_sku",                 columnNames = {"sku"}),
           @UniqueConstraint(name = "uk_product_combination", columnNames = {"product_id", "combination_key"})
       })
public class ProductVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "color_id")
    private Color color;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "size_id")
    private Size size;

    /** Globally unique business identifier, e.g. SHRT001-BLK-M */
    @Column(nullable = false, unique = true, length = 120)
    private String sku;

    /** Physical / retail barcode (EAN, UPC…) */
    @Column(length = 100)
    private String barcode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    /** Shipping weight in grams */
    @Column(name = "weight_grams")
    private Integer weightGrams;

    /**
     * Canonical combination identifier, e.g. "3|7" (colorId|sizeId).
     * Protected by UNIQUE(product_id, combination_key).
     */
    @Column(name = "combination_key", nullable = false, length = 200)
    private String combinationKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VariantStatus status = VariantStatus.ACTIVE;

    public ProductVariant() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public Size getSize() { return size; }
    public void setSize(Size size) { this.size = size; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    /** Calculate discount percent dynamically on the fly */
    public int getDiscountPercent() {
        if (mrp == null || mrp.compareTo(BigDecimal.ZERO) == 0 || sellingPrice == null || sellingPrice.compareTo(mrp) >= 0) {
            return 0;
        }
        return mrp.subtract(sellingPrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(mrp, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public Integer getWeightGrams() { return weightGrams; }
    public void setWeightGrams(Integer weightGrams) { this.weightGrams = weightGrams; }
    public String getCombinationKey() { return combinationKey; }
    public void setCombinationKey(String combinationKey) { this.combinationKey = combinationKey; }
    public VariantStatus getStatus() { return status; }
    public void setStatus(VariantStatus status) { this.status = status; }
    public boolean isActive() { return status == VariantStatus.ACTIVE; }


    /** Build the canonical combination key from color + size IDs */
    public static String buildCombinationKey(Long colorId, Long sizeId) {
        return colorId + "|" + sizeId;
    }

    public static ProductVariantBuilder builder() { return new ProductVariantBuilder(); }

    public static class ProductVariantBuilder {
        private Long id;
        private Product product;
        private Color color;
        private Size size;
        private String sku;
        private String barcode;
        private BigDecimal mrp;
        private BigDecimal sellingPrice;
        private BigDecimal costPrice;
        private int stockQuantity;
        private Integer weightGrams;
        private String combinationKey;
        private VariantStatus status = VariantStatus.ACTIVE;

        public ProductVariantBuilder id(Long id) { this.id = id; return this; }
        public ProductVariantBuilder product(Product product) { this.product = product; return this; }
        public ProductVariantBuilder color(Color color) { this.color = color; return this; }
        public ProductVariantBuilder size(Size size) { this.size = size; return this; }
        public ProductVariantBuilder sku(String sku) { this.sku = sku; return this; }
        public ProductVariantBuilder barcode(String barcode) { this.barcode = barcode; return this; }
        public ProductVariantBuilder mrp(BigDecimal mrp) { this.mrp = mrp; return this; }
        public ProductVariantBuilder sellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; return this; }
        public ProductVariantBuilder costPrice(BigDecimal costPrice) { this.costPrice = costPrice; return this; }
        public ProductVariantBuilder stockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; return this; }
        public ProductVariantBuilder weightGrams(Integer weightGrams) { this.weightGrams = weightGrams; return this; }
        public ProductVariantBuilder combinationKey(String combinationKey) { this.combinationKey = combinationKey; return this; }
        public ProductVariantBuilder status(VariantStatus status) { this.status = status; return this; }

        public ProductVariant build() {
            ProductVariant v = new ProductVariant();
            v.setId(this.id);
            v.setProduct(this.product);
            v.setColor(this.color);
            v.setSize(this.size);
            v.setSku(this.sku);
            v.setBarcode(this.barcode);
            v.setMrp(this.mrp);
            v.setSellingPrice(this.sellingPrice);
            v.setCostPrice(this.costPrice);
            v.setStockQuantity(this.stockQuantity);
            v.setWeightGrams(this.weightGrams);
            v.setCombinationKey(this.combinationKey);
            v.setStatus(this.status);
            return v;
        }
    }
}