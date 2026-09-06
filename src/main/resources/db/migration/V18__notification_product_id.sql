-- ============================================================
-- V18: Add product_id and sku to notification_log
-- ============================================================
-- Enables notifications (e.g. LOW_STOCK_ADMIN) to link directly
-- to specific products and variants in admin and customer portals.
-- ============================================================

ALTER TABLE notification_log
    ADD COLUMN product_id BIGINT NULL AFTER order_id,
    ADD COLUMN sku VARCHAR(120) NULL AFTER product_id,
    ADD CONSTRAINT fk_notif_log_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    ADD INDEX idx_notif_log_product (product_id);
