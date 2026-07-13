-- V9: Add stock_quantity to product_variants
-- This replaces the is_active-only approach with real stock tracking.
-- Admin can update stock_quantity directly via the Admin API.

ALTER TABLE product_variants
    ADD COLUMN stock_quantity INT NOT NULL DEFAULT 0;

-- Seed realistic random test stock for all existing variants (testing only)
UPDATE product_variants SET stock_quantity = FLOOR(RAND() * 15 + 1) WHERE is_active = TRUE;

-- Intentionally set one variant out of stock for demo/testing purposes
UPDATE product_variants SET stock_quantity = 0 WHERE sku_code = 'OX-BLU-S';
