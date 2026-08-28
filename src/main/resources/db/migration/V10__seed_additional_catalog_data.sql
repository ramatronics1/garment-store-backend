-- V10: Seed additional master catalog data (Sub-categories, Size Groups, Sizes, Colors, and Attribute Values)

-- -------------------------------------------------------
-- 1. Sub-Categories (linked to Men, Women, Kids)
-- -------------------------------------------------------
-- Men Sub-categories
INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Shirts', 'men-shirts', id, 1, TRUE FROM categories WHERE slug = 'men';

INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'T-Shirts & Polos', 'men-tshirts', id, 2, TRUE FROM categories WHERE slug = 'men';

INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Jeans & Trousers', 'men-jeans-trousers', id, 3, TRUE FROM categories WHERE slug = 'men';

INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Jackets & Sweatshirts', 'men-jackets', id, 4, TRUE FROM categories WHERE slug = 'men';

INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Ethnic Wear', 'men-ethnic-wear', id, 5, TRUE FROM categories WHERE slug = 'men';

-- Women Sub-categories
INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Dresses & Jumpsuits', 'women-dresses', id, 1, TRUE FROM categories WHERE slug = 'women';

INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Tops & Tees', 'women-tops-tees', id, 2, TRUE FROM categories WHERE slug = 'women';

INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Kurtas & Suits', 'women-kurtas-suits', id, 3, TRUE FROM categories WHERE slug = 'women';

INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Sarees', 'women-sarees', id, 4, TRUE FROM categories WHERE slug = 'women';

INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Jeans & Trousers', 'women-jeans-trousers', id, 5, TRUE FROM categories WHERE slug = 'women';

-- Kids Sub-categories
INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Boys Clothing', 'kids-boys', id, 1, TRUE FROM categories WHERE slug = 'kids';

INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Girls Clothing', 'kids-girls', id, 2, TRUE FROM categories WHERE slug = 'kids';

INSERT INTO categories (name, slug, parent_category_id, display_order, is_active)
SELECT 'Infants & Toddlers', 'kids-infants', id, 3, TRUE FROM categories WHERE slug = 'kids';


-- -------------------------------------------------------
-- 2. Size Groups and Sizes
-- -------------------------------------------------------
-- Plus Sizes
INSERT INTO size_groups (name) VALUES ('Plus Size');

INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '3XL', '3XL', 1 FROM size_groups WHERE name = 'Plus Size';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '4XL', '4XL', 2 FROM size_groups WHERE name = 'Plus Size';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '5XL', '5XL', 3 FROM size_groups WHERE name = 'Plus Size';

-- Bottomwear (Waist inches)
INSERT INTO size_groups (name) VALUES ('Bottomwear (Waist)');

INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '28', '28', 1 FROM size_groups WHERE name = 'Bottomwear (Waist)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '30', '30', 2 FROM size_groups WHERE name = 'Bottomwear (Waist)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '32', '32', 3 FROM size_groups WHERE name = 'Bottomwear (Waist)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '34', '34', 4 FROM size_groups WHERE name = 'Bottomwear (Waist)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '36', '36', 5 FROM size_groups WHERE name = 'Bottomwear (Waist)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '38', '38', 6 FROM size_groups WHERE name = 'Bottomwear (Waist)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '40', '40', 7 FROM size_groups WHERE name = 'Bottomwear (Waist)';

-- Kids Age Sizes
INSERT INTO size_groups (name) VALUES ('Kids (Age)');

INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '0-6M',  '0-6M',  1 FROM size_groups WHERE name = 'Kids (Age)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '6-12M', '6-12M', 2 FROM size_groups WHERE name = 'Kids (Age)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '1-2Y',  '1-2Y',  3 FROM size_groups WHERE name = 'Kids (Age)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '2-3Y',  '2-3Y',  4 FROM size_groups WHERE name = 'Kids (Age)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '3-4Y',  '3-4Y',  5 FROM size_groups WHERE name = 'Kids (Age)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '5-6Y',  '5-6Y',  6 FROM size_groups WHERE name = 'Kids (Age)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '7-8Y',  '7-8Y',  7 FROM size_groups WHERE name = 'Kids (Age)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '9-10Y', '9-10Y', 8 FROM size_groups WHERE name = 'Kids (Age)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, '11-12Y','11-12Y',9 FROM size_groups WHERE name = 'Kids (Age)';

-- Footwear (UK Sizes)
INSERT INTO size_groups (name) VALUES ('Footwear (UK)');

INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'UK 5',  'UK 5',  1 FROM size_groups WHERE name = 'Footwear (UK)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'UK 6',  'UK 6',  2 FROM size_groups WHERE name = 'Footwear (UK)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'UK 7',  'UK 7',  3 FROM size_groups WHERE name = 'Footwear (UK)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'UK 8',  'UK 8',  4 FROM size_groups WHERE name = 'Footwear (UK)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'UK 9',  'UK 9',  5 FROM size_groups WHERE name = 'Footwear (UK)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'UK 10', 'UK 10', 6 FROM size_groups WHERE name = 'Footwear (UK)';
INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'UK 11', 'UK 11', 7 FROM size_groups WHERE name = 'Footwear (UK)';

-- Free Size
INSERT INTO size_groups (name) VALUES ('Free Size');

INSERT INTO sizes (size_group_id, name, size_code, sort_order)
SELECT id, 'Free Size', 'FS', 1 FROM size_groups WHERE name = 'Free Size';


-- -------------------------------------------------------
-- 3. Additional Popular Fashion Colors
-- -------------------------------------------------------
INSERT INTO colors (name, code, hex_code, display_order) VALUES
('Charcoal',    'CHARCOAL',    '#36454F', 11),
('Teal',        'TEAL',        '#008080', 12),
('Lavender',    'LAVENDER',    '#E6E6FA', 13),
('Mustard',     'MUSTARD',     '#FFDB58', 14),
('Peach',       'PEACH',       '#FFE5B4', 15),
('Coral',       'CORAL',       '#FF7F50', 16),
('Pink',        'PINK',        '#FFC0CB', 17),
('Burgundy',    'BURGUNDY',    '#800020', 18),
('Brown',       'BROWN',       '#8B4513', 19),
('Mint Green',  'MINT_GREEN',  '#98FF98', 20),
('Khaki',       'KHAKI',       '#C3B091', 21),
('Orange',      'ORANGE',      '#FFA500', 22),
('Purple',      'PURPLE',      '#800080', 23),
('Yellow',      'YELLOW',      '#FFFF00', 24),
('Multi',       'MULTI',       '#CCCCCC', 25);


-- -------------------------------------------------------
-- 4. Additional Attribute Values
-- -------------------------------------------------------

-- Extra Fabric values
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Denim',    5 FROM attributes WHERE name = 'Fabric';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Silk',     6 FROM attributes WHERE name = 'Fabric';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Wool',     7 FROM attributes WHERE name = 'Fabric';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Rayon',    8 FROM attributes WHERE name = 'Fabric';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Satin',    9 FROM attributes WHERE name = 'Fabric';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Corduroy', 10 FROM attributes WHERE name = 'Fabric';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Viscose',  11 FROM attributes WHERE name = 'Fabric';

-- Extra Fit values
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Skinny Fit',   5 FROM attributes WHERE name = 'Fit';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Tapered Fit',  6 FROM attributes WHERE name = 'Fit';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Straight Fit', 7 FROM attributes WHERE name = 'Fit';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Loose Fit',    8 FROM attributes WHERE name = 'Fit';

-- Extra Sleeve values
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, '3/4th Sleeve',   4 FROM attributes WHERE name = 'Sleeve';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Roll-up Sleeve', 5 FROM attributes WHERE name = 'Sleeve';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Cap Sleeve',     6 FROM attributes WHERE name = 'Sleeve';

-- Occasion values
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Casual',             1 FROM attributes WHERE name = 'Occasion';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Formal',             2 FROM attributes WHERE name = 'Occasion';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Party & Festive',    3 FROM attributes WHERE name = 'Occasion';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Work / Office',      4 FROM attributes WHERE name = 'Occasion';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Sports & Activewear',5 FROM attributes WHERE name = 'Occasion';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Lounge / Sleepwear', 6 FROM attributes WHERE name = 'Occasion';

-- Pattern values
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Solid',         1 FROM attributes WHERE name = 'Pattern';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Striped',       2 FROM attributes WHERE name = 'Pattern';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Printed',       3 FROM attributes WHERE name = 'Pattern';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Checked',       4 FROM attributes WHERE name = 'Pattern';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Colorblocked',  5 FROM attributes WHERE name = 'Pattern';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Graphic Print', 6 FROM attributes WHERE name = 'Pattern';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Embroidered',   7 FROM attributes WHERE name = 'Pattern';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Self Design',   8 FROM attributes WHERE name = 'Pattern';

-- Neck values
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Round Neck',              1 FROM attributes WHERE name = 'Neck';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'V-Neck',                  2 FROM attributes WHERE name = 'Neck';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Polo Collar',             3 FROM attributes WHERE name = 'Neck';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Spread Collar',           4 FROM attributes WHERE name = 'Neck';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Mandarin / Band Collar',  5 FROM attributes WHERE name = 'Neck';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Hooded',                  6 FROM attributes WHERE name = 'Neck';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Crew Neck',               7 FROM attributes WHERE name = 'Neck';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Boat Neck',               8 FROM attributes WHERE name = 'Neck';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Turtle Neck / High Neck', 9 FROM attributes WHERE name = 'Neck';

-- Season values
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'All Season',      1 FROM attributes WHERE name = 'Season';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Summer',          2 FROM attributes WHERE name = 'Season';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Winter',          3 FROM attributes WHERE name = 'Season';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Spring / Autumn', 4 FROM attributes WHERE name = 'Season';
INSERT INTO attribute_values (attribute_id, value, display_order)
SELECT id, 'Monsoon',         5 FROM attributes WHERE name = 'Season';
