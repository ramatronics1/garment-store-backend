package com.garmentstore.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "related_products")
public class RelatedProductMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_product_id")
    private Product relatedProduct;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Product getRelatedProduct() { return relatedProduct; }
    public void setRelatedProduct(Product relatedProduct) { this.relatedProduct = relatedProduct; }

    public static RelatedProductMappingBuilder builder() { return new RelatedProductMappingBuilder(); }

    public static class RelatedProductMappingBuilder {
        private Long id;
        private Product product;
        private Product relatedProduct;

        public RelatedProductMappingBuilder id(Long id) { this.id = id; return this; }
        public RelatedProductMappingBuilder product(Product product) { this.product = product; return this; }
        public RelatedProductMappingBuilder relatedProduct(Product relatedProduct) { this.relatedProduct = relatedProduct; return this; }

        public RelatedProductMapping build() {
            RelatedProductMapping r = new RelatedProductMapping();
            r.setId(this.id);
            r.setProduct(this.product);
            r.setRelatedProduct(this.relatedProduct);
            return r;
        }
    }
}