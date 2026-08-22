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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public static FeaturedProductBuilder builder() { return new FeaturedProductBuilder(); }

    public static class FeaturedProductBuilder {
        private Long id;
        private Product product;
        private int displayOrder;
        private boolean active;

        public FeaturedProductBuilder id(Long id) { this.id = id; return this; }
        public FeaturedProductBuilder product(Product product) { this.product = product; return this; }
        public FeaturedProductBuilder displayOrder(int displayOrder) { this.displayOrder = displayOrder; return this; }
        public FeaturedProductBuilder active(boolean active) { this.active = active; return this; }

        public FeaturedProduct build() {
            FeaturedProduct f = new FeaturedProduct();
            f.setId(this.id);
            f.setProduct(this.product);
            f.setDisplayOrder(this.displayOrder);
            f.setActive(this.active);
            return f;
        }
    }
}