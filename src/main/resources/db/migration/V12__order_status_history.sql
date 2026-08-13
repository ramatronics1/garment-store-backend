-- V12: Create order_status_history table
-- Every time an order's status changes (by admin or by system),
-- a new row is appended here. This powers the Order Timeline on the detail page
-- and is visible to both the customer and the admin.

CREATE TABLE order_status_history (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    status          VARCHAR(50) NOT NULL,
    changed_by_type VARCHAR(30) NOT NULL DEFAULT 'ADMIN',  -- ADMIN | SYSTEM | CUSTOMER
    changed_by_id   BIGINT,                                -- user_id of who made the change (nullable for SYSTEM)
    note            VARCHAR(500),                          -- optional admin note
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_osh_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_osh_order_id (order_id),
    INDEX idx_osh_order_created (order_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Back-fill: create an initial PENDING history entry for every existing order
-- so that the timeline is never empty for existing data.
INSERT INTO order_status_history (order_id, status, changed_by_type, note, created_at)
SELECT id, status, 'SYSTEM', 'Initial status at order creation', created_at
FROM orders;
