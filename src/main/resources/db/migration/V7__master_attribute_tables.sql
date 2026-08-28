-- V7: Master attribute tables for flexible product/variant modelling
-- Additive only — no existing tables altered in this migration

-- -------------------------------------------------------
-- Color master
-- -------------------------------------------------------
CREATE TABLE colors (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    code          VARCHAR(50)  NOT NULL UNIQUE,
    hex_code      VARCHAR(10),
    display_order INT          NOT NULL DEFAULT 0,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6)
);

-- -------------------------------------------------------
-- Size groups (e.g. "Apparel", "Footwear", "Kids")
-- -------------------------------------------------------
CREATE TABLE size_groups (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6)
);

-- -------------------------------------------------------
-- Sizes (each belongs to a size group)
-- -------------------------------------------------------
CREATE TABLE sizes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    size_group_id   BIGINT       NOT NULL,
    name            VARCHAR(50)  NOT NULL,
    size_code       VARCHAR(30)  NOT NULL,
    sort_order      INT          NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_size_group_code (size_group_id, size_code),
    CONSTRAINT fk_size_group FOREIGN KEY (size_group_id) REFERENCES size_groups (id)
);

-- -------------------------------------------------------
-- Attribute definitions (Color, Size are variant-level; Fabric, Fit are product-level)
-- -------------------------------------------------------
CREATE TABLE attributes (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    scope      ENUM('PRODUCT','VARIANT','BOTH') NOT NULL DEFAULT 'PRODUCT',
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6)
);

-- -------------------------------------------------------
-- Attribute values (e.g. attribute "Fit" → "Slim Fit", "Regular Fit")
-- -------------------------------------------------------
CREATE TABLE attribute_values (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    attribute_id  BIGINT       NOT NULL,
    value         VARCHAR(200) NOT NULL,
    display_order INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_attr_value (attribute_id, value),
    CONSTRAINT fk_attr_val_attr FOREIGN KEY (attribute_id) REFERENCES attributes (id)
);

-- -------------------------------------------------------
-- Seed: default size group + common sizes
-- -------------------------------------------------------
INSERT INTO size_groups (name) VALUES ('Apparel');

INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'XS',  'XS',  1 FROM size_groups WHERE name = 'Apparel';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'S',   'S',   2 FROM size_groups WHERE name = 'Apparel';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'M',   'M',   3 FROM size_groups WHERE name = 'Apparel';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'L',   'L',   4 FROM size_groups WHERE name = 'Apparel';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'XL',  'XL',  5 FROM size_groups WHERE name = 'Apparel';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'XXL', 'XXL', 6 FROM size_groups WHERE name = 'Apparel';

-- -------------------------------------------------------
-- Seed: common colors
-- -------------------------------------------------------
INSERT INTO colors (name, code, hex_code, display_order) VALUES
('Black',      'BLACK',      '#000000', 1),
('White',      'WHITE',      '#FFFFFF', 2),
('Navy Blue',  'NAVY',       '#1B2A6B', 3),
('Sky Blue',   'SKY_BLUE',   '#87CEEB', 4),
('Red',        'RED',        '#CC0000', 5),
('Green',      'GREEN',      '#2D6A2D', 6),
('Grey',       'GREY',       '#808080', 7),
('Olive',      'OLIVE',      '#6B6B2D', 8),
('Maroon',     'MAROON',     '#800000', 9),
('Beige',      'BEIGE',      '#F5F5DC', 10);

-- -------------------------------------------------------
-- Seed: common product-level attributes
-- -------------------------------------------------------
INSERT INTO attributes (name, scope) VALUES
('Fabric',     'PRODUCT'),
('Fit',        'PRODUCT'),
('Sleeve',     'PRODUCT'),
('Occasion',   'PRODUCT'),
('Pattern',    'PRODUCT'),
('Neck',       'PRODUCT'),
('Season',     'PRODUCT');

INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, '100% Cotton', 1 FROM attributes WHERE name = 'Fabric';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Cotton Blend', 2 FROM attributes WHERE name = 'Fabric';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Polyester', 3 FROM attributes WHERE name = 'Fabric';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Linen', 4 FROM attributes WHERE name = 'Fabric';

INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Slim Fit',    1 FROM attributes WHERE name = 'Fit';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Regular Fit', 2 FROM attributes WHERE name = 'Fit';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Relaxed Fit', 3 FROM attributes WHERE name = 'Fit';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Oversized',   4 FROM attributes WHERE name = 'Fit';

INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Full Sleeve',  1 FROM attributes WHERE name = 'Sleeve';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Half Sleeve',  2 FROM attributes WHERE name = 'Sleeve';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Sleeveless',   3 FROM attributes WHERE name = 'Sleeve';
