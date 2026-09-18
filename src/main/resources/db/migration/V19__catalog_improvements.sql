-- V19: Catalog improvements
-- 1. Add created_at / updated_at to featured_products (fixes BaseEntity JPA mapping)
-- 2. Add performance index on display_order for ordered featured queries

ALTER TABLE featured_products
    ADD COLUMN created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

ALTER TABLE featured_products
    ADD INDEX idx_feat_display_order (display_order);

-- 3. Unique constraint to prevent duplicate featured entries for the same product
ALTER TABLE featured_products
    ADD CONSTRAINT uk_feat_product UNIQUE (product_id);
