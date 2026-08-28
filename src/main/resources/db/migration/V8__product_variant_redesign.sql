-- V8: Redesign product_variants table and update products table
-- Prices and color move from products → product_variants

-- -------------------------------------------------------
-- 1. Drop the old product_variants table (dev DB — no data to preserve)
-- -------------------------------------------------------
ALTER TABLE product_variants DROP FOREIGN KEY fk_var_prod;
DROP TABLE product_variants;

-- -------------------------------------------------------
-- 2. Recreate product_variants with full schema
-- -------------------------------------------------------
CREATE TABLE product_variants (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT          NOT NULL,
    color_id        BIGINT          NOT NULL,
    size_id         BIGINT          NOT NULL,
    sku             VARCHAR(120)    NOT NULL UNIQUE,
    barcode         VARCHAR(100),
    mrp             DECIMAL(12,2)   NOT NULL,
    selling_price   DECIMAL(12,2)   NOT NULL,
    cost_price      DECIMAL(12,2),
    discount_percent INT            NOT NULL DEFAULT 0,
    stock_quantity  INT             NOT NULL DEFAULT 0,
    weight_grams    INT,
    combination_key VARCHAR(200)    NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP(6)    DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6)    DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_product_combination (product_id, combination_key),
    CONSTRAINT fk_var_prod  FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_var_color FOREIGN KEY (color_id)   REFERENCES colors (id),
    CONSTRAINT fk_var_size  FOREIGN KEY (size_id)    REFERENCES sizes (id)
);

-- -------------------------------------------------------
-- 3. Create product_variant_attributes junction table
--    (for extra attributes beyond color+size on a variant)
-- -------------------------------------------------------
CREATE TABLE product_variant_attributes (
    variant_id          BIGINT NOT NULL,
    attribute_id        BIGINT NOT NULL,
    attribute_value_id  BIGINT NOT NULL,
    PRIMARY KEY (variant_id, attribute_id),
    CONSTRAINT fk_pva_variant FOREIGN KEY (variant_id)         REFERENCES product_variants (id) ON DELETE CASCADE,
    CONSTRAINT fk_pva_attr    FOREIGN KEY (attribute_id)       REFERENCES attributes (id),
    CONSTRAINT fk_pva_val     FOREIGN KEY (attribute_value_id) REFERENCES attribute_values (id)
);

-- -------------------------------------------------------
-- 4. Create product_attributes table
--    (product-level attributes like Fabric, Fit, Sleeve)
-- -------------------------------------------------------
CREATE TABLE product_attributes (
    product_id          BIGINT NOT NULL,
    attribute_id        BIGINT NOT NULL,
    attribute_value_id  BIGINT NOT NULL,
    PRIMARY KEY (product_id, attribute_id),
    CONSTRAINT fk_pa_product FOREIGN KEY (product_id)          REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_pa_attr    FOREIGN KEY (attribute_id)        REFERENCES attributes (id),
    CONSTRAINT fk_pa_val     FOREIGN KEY (attribute_value_id)  REFERENCES attribute_values (id)
);

-- -------------------------------------------------------
-- 5. Alter products table:
--    - Remove flat price/color columns (they live on variants now)
--    - Add product_code (immutable business identifier)
--    - Add brand, fit, season convenience columns (searchable fields)
-- -------------------------------------------------------
ALTER TABLE products
    DROP COLUMN color,
    DROP COLUMN mrp,
    DROP COLUMN selling_price,
    DROP COLUMN discount_percent,
    ADD COLUMN product_code VARCHAR(30) UNIQUE AFTER id,
    ADD COLUMN brand        VARCHAR(100) AFTER slug,
    ADD COLUMN fit          VARCHAR(80)  AFTER fabric_details,
    ADD COLUMN season       VARCHAR(80)  AFTER fit;
