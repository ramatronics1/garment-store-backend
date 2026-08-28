-- V12: Safe drop of legacy size_code and sku_code columns from product_variants

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

CALL drop_col_if_exists('product_variants', 'size_code');
CALL drop_col_if_exists('product_variants', 'sku_code');

DROP PROCEDURE IF EXISTS drop_col_if_exists;
