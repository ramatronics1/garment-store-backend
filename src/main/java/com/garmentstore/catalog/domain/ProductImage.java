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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isThumbnail() { return thumbnail; }
    public void setThumbnail(boolean thumbnail) { this.thumbnail = thumbnail; }

    public static ProductImageBuilder builder() { return new ProductImageBuilder(); }

    public static class ProductImageBuilder {
        private Long id;
        private Product product;
        private String mediaUrl;
        private int displayOrder;
        private boolean thumbnail;

        public ProductImageBuilder id(Long id) { this.id = id; return this; }
        public ProductImageBuilder product(Product product) { this.product = product; return this; }
        public ProductImageBuilder mediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; return this; }
        public ProductImageBuilder displayOrder(int displayOrder) { this.displayOrder = displayOrder; return this; }
        public ProductImageBuilder thumbnail(boolean thumbnail) { this.thumbnail = thumbnail; return this; }

        public ProductImage build() {
            ProductImage i = new ProductImage();
            i.setId(this.id);
            i.setProduct(this.product);
            i.setMediaUrl(this.mediaUrl);
            i.setDisplayOrder(this.displayOrder);
            i.setThumbnail(this.thumbnail);
            return i;
        }
    }
}