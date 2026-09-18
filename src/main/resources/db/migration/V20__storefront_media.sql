-- V20: Storefront media management
-- 1. Extend categories with visual/marketing fields
-- 2. store_media table for admin-managed homepage/CLP banners
-- 3. sale_config table for dynamic sale banner (on/off toggle + countdown)

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. Category visual fields
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE categories
    ADD COLUMN thumbnail_url  VARCHAR(700) NULL COMMENT '1:1 card image (400x400 px)',
    ADD COLUMN banner_url     VARCHAR(700) NULL COMMENT 'Hero/CLP banner (1440x600 px)',
    ADD COLUMN banner_alt     VARCHAR(300) NULL,
    ADD COLUMN description    TEXT         NULL COMMENT 'Short marketing copy for CLP',
    ADD COLUMN icon_url       VARCHAR(700) NULL COMMENT 'Small icon/SVG (80x80 px)';

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. store_media — named placement slots for homepage hero slides, sale banners, etc.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE store_media (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    placement_key   VARCHAR(100)  NOT NULL UNIQUE COMMENT 'e.g. hero_banner_1, sale_banner_home',
    media_url       VARCHAR(700)  NOT NULL,
    alt_text        VARCHAR(300)  NULL,
    link_url        VARCHAR(500)  NULL COMMENT 'CTA destination URL',
    title           VARCHAR(200)  NULL COMMENT 'Overlay headline',
    subtitle        VARCHAR(400)  NULL COMMENT 'Overlay subtext',
    badge_text      VARCHAR(80)   NULL COMMENT 'e.g. "Up to 40% Off"',
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    display_order   INT           NOT NULL DEFAULT 0,
    created_at      TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_sm_active_order (is_active, display_order)
);

-- Seed default hero slide placeholders (admin replaces URLs)
INSERT INTO store_media (placement_key, media_url, alt_text, title, subtitle, badge_text, link_url, is_active, display_order) VALUES
('hero_banner_1', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=1440&q=80',
 'New arrivals hero', 'Wear the Season', 'Fresh styles for Men, Women & Kids. Up to 40% off today.', '40% Off', '/products', TRUE, 1),
('hero_banner_2', 'https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=1440&q=80',
 'Festive collection hero', 'Celebrate in Style', 'Exquisite ethnic wear — sarees, kurtis & lehengas for every occasion.', '35% Off', '/products?gender=WOMEN', TRUE, 2),
('hero_banner_3', 'https://images.unsplash.com/photo-1617137968427-85924c800a22?w=1440&q=80',
 'Men essentials hero', 'Dress to Impress', 'Premium shirts, jeans & formals — crafted for the modern man.', '50% Off', '/products?gender=MEN', TRUE, 3);

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. sale_config — single-row master config for the sale banner
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE sale_config (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    is_active   BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'Master on/off toggle',
    title       VARCHAR(200) NULL,
    subtitle    VARCHAR(400) NULL,
    ends_at     DATETIME     NULL COMMENT 'Countdown target (NULL = no timer)',
    badge_text  VARCHAR(80)  NULL,
    link_url    VARCHAR(500) NULL,
    media_url   VARCHAR(700) NULL COMMENT 'Optional sale banner image (1200x400 px)',
    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

-- Seed one default row (starts disabled)
INSERT INTO sale_config (is_active, title, subtitle, badge_text, link_url)
VALUES (FALSE, 'End of Season Sale', 'Up to 60% off on selected styles', 'Up to 60% Off', '/products?minDiscount=20');
