-- V10: Cart Items Table
-- Stores the server-side shadow copy of each user's cart.
-- The primary source of truth for the current session is the browser's localStorage.
-- This table enables:
--   (1) Cross-device cart persistence after login
--   (2) Stock re-validation at checkout
--   (3) Post-login merge (guest cart + server cart)

CREATE TABLE cart_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    product_id  BIGINT      NOT NULL,
    variant_id  BIGINT      NOT NULL,
    quantity    INT         NOT NULL DEFAULT 1,
    added_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_cart_user    FOREIGN KEY (user_id)    REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_cart_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    -- One row per user+variant combination
    UNIQUE KEY uk_cart_user_variant (user_id, variant_id)
);

CREATE INDEX idx_cart_user ON cart_items(user_id);
