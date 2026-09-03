-- ============================================================
-- V17: Notification Tables
-- ============================================================
-- notification_log: Stores every notification sent (Email, WhatsApp, In-App).
--   - EMAIL and WHATSAPP rows are audit records of external sends.
--   - IN_APP rows are the "inbox" read by the UI notification bell.
-- notification_preferences: Per-user opt-in/out for channels.
-- ============================================================

CREATE TABLE notification_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    order_id        BIGINT          NULL,
    type            VARCHAR(50)     NOT NULL COMMENT 'ORDER_PLACED, ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_DELIVERED, ORDER_CANCELLED, NEW_ORDER_ADMIN, LOW_STOCK_ADMIN, WELCOME',
    channel         VARCHAR(20)     NOT NULL COMMENT 'EMAIL, WHATSAPP, IN_APP',
    recipient       VARCHAR(255)    NOT NULL COMMENT 'Email address or E.164 phone number',
    subject         VARCHAR(500)    NULL,
    body            TEXT            NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    error_message   TEXT            NULL,
    read_at         DATETIME        NULL     COMMENT 'For IN_APP only: when the user read it',
    sent_at         DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_notif_log_user    FOREIGN KEY (user_id)  REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_log_order   FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,

    INDEX idx_notif_log_user_channel  (user_id, channel),
    INDEX idx_notif_log_user_unread   (user_id, channel, read_at),
    INDEX idx_notif_log_type          (type),
    INDEX idx_notif_log_created_at    (created_at),
    INDEX idx_notif_log_status        (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE notification_preferences (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    user_id                 BIGINT          NOT NULL UNIQUE,
    email_enabled           TINYINT(1)      NOT NULL DEFAULT 1,
    whatsapp_enabled        TINYINT(1)      NOT NULL DEFAULT 1,
    order_updates_enabled   TINYINT(1)      NOT NULL DEFAULT 1,
    promotions_enabled      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_notif_pref_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
