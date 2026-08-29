-- V13: Add performance indexes for product filter queries
-- Adapted from feature/order_apis V7 to match the V8 variant schema redesign.
-- (The old V7 referenced size_code/is_active which no longer exist on product_variants.)

-- Index on gender_tag — filters by Men/Women/Kids
ALTER TABLE products ADD INDEX idx_gender_tag (gender_tag);

-- Index on selling_price — supports minPrice/maxPrice range filters and price_asc/price_desc sort
-- NOTE: selling_price is now on product_variants, not products (since V8 redesign).
--       Keeping an index on variants for range queries from the API filter layer.
ALTER TABLE product_variants ADD INDEX idx_var_selling_price (selling_price);

-- Index on mrp on variants — used for discount computation sort
ALTER TABLE product_variants ADD INDEX idx_var_mrp (mrp);

-- Index on status on variants — replaces old is_active boolean
ALTER TABLE product_variants ADD INDEX idx_var_status (status);

-- Composite index on product_variants for size+status filter lookups
-- Covers: product_id + status filter (replaces old size_code/is_active combo)
ALTER TABLE product_variants ADD INDEX idx_var_product_status (product_id, status);

-- Composite index on product_variants for color lookups
ALTER TABLE product_variants ADD INDEX idx_var_product_color (product_id, color_id);

-- Index on color_id on variants for color filter
ALTER TABLE product_variants ADD INDEX idx_var_color (color_id);

-- Index on size_id on variants for size filter
ALTER TABLE product_variants ADD INDEX idx_var_size (size_id);

-- Composite index on status + category_id — the most common product filter combination
ALTER TABLE products ADD INDEX idx_status_category (status, category_id);

-- Index on deleted_at — for soft-delete filter (WHERE deleted_at IS NULL)
ALTER TABLE products ADD INDEX idx_deleted_at (deleted_at);
