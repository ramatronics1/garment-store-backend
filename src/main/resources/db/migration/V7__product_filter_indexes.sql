-- V7: Add performance indexes for product filter queries
-- These indexes make the product listing API fast even at scale
-- (thousands of products with multiple concurrent filter combinations)

-- Index on gender_tag — filters by Men/Women/Kids
ALTER TABLE products ADD INDEX idx_gender_tag (gender_tag);

-- Index on selling_price — supports minPrice/maxPrice range filters and price_asc/price_desc sort
ALTER TABLE products ADD INDEX idx_selling_price (selling_price);

-- Index on discount_percent — supports minDiscount filter and discount sort
ALTER TABLE products ADD INDEX idx_discount_percent (discount_percent);

-- Index on color — supports color filter (case-insensitive IN query)
ALTER TABLE products ADD INDEX idx_color (color);

-- Composite index on status + category_id — the most common filter combination
-- (almost every query filters by status=ACTIVE and optionally category_id)
ALTER TABLE products ADD INDEX idx_status_category (status, category_id);

-- Composite index on product_variants for size filter lookups
-- Covers: product_id IN (:ids) AND size_code IN (:sizes) AND is_active = true
ALTER TABLE product_variants ADD INDEX idx_var_size_active (product_id, size_code, is_active);

-- Index on deleted_at — for soft-delete filter (WHERE deleted_at IS NULL)
ALTER TABLE products ADD INDEX idx_deleted_at (deleted_at);
