-- V11: Drop legacy product columns & drop discount_percent from product_variants

-- 1. Helper procedure to safely drop a column if it exists
DROP PROCEDURE IF EXISTS drop_col_if_exists;
DELIMITER //
CREATE PROCEDURE drop_col_if_exists(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = tbl_name
          AND COLUMN_NAME = col_name
    ) THEN
        SET @s = CONCAT('ALTER TABLE ', tbl_name, ' DROP COLUMN ', col_name);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- Drop legacy columns from products table
CALL drop_col_if_exists('products', 'discount_percent');
CALL drop_col_if_exists('products', 'mrp');
CALL drop_col_if_exists('products', 'selling_price');
CALL drop_col_if_exists('products', 'color');

-- Drop discount_percent, obsolete is_active, size_code, and sku_code from product_variants
CALL drop_col_if_exists('product_variants', 'discount_percent');
CALL drop_col_if_exists('product_variants', 'is_active');
CALL drop_col_if_exists('product_variants', 'size_code');
CALL drop_col_if_exists('product_variants', 'sku_code');


-- Ensure missing convenience columns exist on products table
DROP PROCEDURE IF EXISTS add_col_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_col_if_not_exists(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64),
    IN col_definition VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = tbl_name
          AND COLUMN_NAME = col_name
    ) THEN
        SET @s = CONCAT('ALTER TABLE ', tbl_name, ' ADD COLUMN ', col_name, ' ', col_definition);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL add_col_if_not_exists('products', 'product_code', 'VARCHAR(30) UNIQUE AFTER id');
CALL add_col_if_not_exists('products', 'brand', 'VARCHAR(100) AFTER slug');
CALL add_col_if_not_exists('products', 'fit', 'VARCHAR(80) AFTER fabric_details');
CALL add_col_if_not_exists('products', 'season', 'VARCHAR(80) AFTER fit');

-- Clean up helper procedures
DROP PROCEDURE IF EXISTS drop_col_if_exists;
DROP PROCEDURE IF EXISTS add_col_if_not_exists;
