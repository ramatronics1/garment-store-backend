-- V9: Add/fix created_at & updated_at on product_variants
-- V8 was applied before the BaseEntity columns were included in the DDL.
-- Flyway won't re-run V8, so we fix it here.
-- Dev DB — safe to be destructive.

-- Drop dependents first (FK references product_variants)
DROP TABLE IF EXISTS product_variant_attributes;

-- Drop and recreate product_variants with the correct, complete schema
DROP TABLE IF EXISTS product_variants;

CREATE TABLE product_variants (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY,
    product_id       BIGINT          NOT NULL,
    color_id         BIGINT          NOT NULL,
    size_id          BIGINT          NOT NULL,
    sku              VARCHAR(120)    NOT NULL UNIQUE,
    barcode          VARCHAR(100),
    mrp              DECIMAL(12,2)   NOT NULL,
    selling_price    DECIMAL(12,2)   NOT NULL,
    cost_price       DECIMAL(12,2),
    discount_percent INT             NOT NULL DEFAULT 0,
    stock_quantity   INT             NOT NULL DEFAULT 0,
    weight_grams     INT,
    combination_key  VARCHAR(200)    NOT NULL,
    status           VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    created_at       DATETIME(6)     NOT NULL DEFAULT '2026-01-01 00:00:00.000000',
    updated_at       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_product_combination (product_id, combination_key),
    CONSTRAINT fk_var_prod  FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_var_color FOREIGN KEY (color_id)   REFERENCES colors (id),
    CONSTRAINT fk_var_size  FOREIGN KEY (size_id)    REFERENCES sizes (id)
);

-- Recreate product_variant_attributes junction table
CREATE TABLE product_variant_attributes (
    variant_id          BIGINT NOT NULL,
    attribute_id        BIGINT NOT NULL,
    attribute_value_id  BIGINT NOT NULL,
    PRIMARY KEY (variant_id, attribute_id),
    CONSTRAINT fk_pva_variant FOREIGN KEY (variant_id)         REFERENCES product_variants (id) ON DELETE CASCADE,
    CONSTRAINT fk_pva_attr    FOREIGN KEY (attribute_id)       REFERENCES attributes (id),
    CONSTRAINT fk_pva_val     FOREIGN KEY (attribute_value_id) REFERENCES attribute_values (id)
);
