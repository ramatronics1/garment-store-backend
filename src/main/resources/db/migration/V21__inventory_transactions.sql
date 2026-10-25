-- ============================================================
-- V21: Inventory Transactions / Stock Movement Audit Table
-- ============================================================

CREATE TABLE inventory_transactions (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    variant_id          BIGINT          NOT NULL,
    product_id          BIGINT          NOT NULL,
    sku                 VARCHAR(120)    NOT NULL,
    previous_quantity   INT             NOT NULL,
    quantity_change     INT             NOT NULL,
    new_quantity        INT             NOT NULL,
    transaction_type    VARCHAR(50)     NOT NULL COMMENT 'RESTOCK, MANUAL_CORRECTION, ORDER_FULFILLMENT, ORDER_CANCELLED_RESTOCK, DAMAGED, RETURN',
    reason              VARCHAR(255)    NULL,
    reference_id        VARCHAR(100)    NULL COMMENT 'Order number, PO number, or batch reference',
    actor_user_id       BIGINT          NULL COMMENT 'Admin or user who triggered the change (NULL if system)',
    actor_name          VARCHAR(150)    NULL,
    created_at          TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT fk_inv_tx_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_tx_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_tx_user    FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_inv_tx_variant_created (variant_id, created_at DESC),
    INDEX idx_inv_tx_sku_created     (sku, created_at DESC),
    INDEX idx_inv_tx_type            (transaction_type),
    INDEX idx_inv_tx_created_at      (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
