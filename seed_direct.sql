-- =====================================================
-- DIRECT SEED — Run via MySQL CLI (schema-compliant)
-- All NOT NULL columns included with correct values
-- =====================================================

SET @now = NOW(6);

-- Sub-categories (children of Men=1, Women=2)
INSERT INTO categories (name, slug, parent_category_id, display_order, is_active, created_at, updated_at) VALUES
('Shirts',    'shirts',    1, 4,  TRUE, @now, @now),
('T-Shirts',  't-shirts',  1, 5,  TRUE, @now, @now),
('Jeans',     'jeans',     1, 6,  TRUE, @now, @now),
('Jackets',   'jackets',   1, 7,  TRUE, @now, @now),
('Kurtis',    'kurtis',    2, 8,  TRUE, @now, @now),
('Dresses',   'dresses',   2, 9,  TRUE, @now, @now),
('Tops',      'tops',      2, 10, TRUE, @now, @now);

SET @shirts   = (SELECT id FROM categories WHERE slug = 'shirts');
SET @tshirts  = (SELECT id FROM categories WHERE slug = 't-shirts');
SET @jeans    = (SELECT id FROM categories WHERE slug = 'jeans');
SET @jackets  = (SELECT id FROM categories WHERE slug = 'jackets');
SET @kurtis   = (SELECT id FROM categories WHERE slug = 'kurtis');
SET @dresses  = (SELECT id FROM categories WHERE slug = 'dresses');
SET @tops     = (SELECT id FROM categories WHERE slug = 'tops');

-- ============ MEN'S SHIRTS ============
INSERT INTO products (name, slug, category_id, gender_tag, mrp, selling_price, discount_percent, color, description, fabric_details, care_instructions, country_of_origin, return_policy_enabled, status, created_at, updated_at) VALUES
('Oxford Slim Fit Premium Shirt', 'oxford-slim-fit-premium-shirt', @shirts, 'MEN',
 1399.00, 899.00, 35, 'Blue',
 '100% premium cotton Oxford weave. Slim fit with structured collar. Perfect for office and smart-casual.',
 '100% Cotton', 'Machine wash cold, tumble dry low', 'India', TRUE, 'ACTIVE', @now, @now),

('Classic White Formal Shirt', 'classic-white-formal-shirt', @shirts, 'MEN',
 1199.00, 799.00, 33, 'White',
 'Crisp white formal shirt with a regular fit. Ideal for office, interviews, and formal occasions.',
 '100% Cotton Poplin', 'Machine wash', 'India', TRUE, 'ACTIVE', @now, @now),

('Linen Summer Shirt', 'linen-summer-shirt', @shirts, 'MEN',
 1599.00, 1099.00, 31, 'Beige',
 'Lightweight linen shirt for summer. Relaxed fit with mandarin collar. Breathable and stylish.',
 '100% Linen', 'Hand wash recommended', 'India', TRUE, 'ACTIVE', @now, @now);

-- ============ MEN'S T-SHIRTS ============
INSERT INTO products (name, slug, category_id, gender_tag, mrp, selling_price, discount_percent, color, description, fabric_details, care_instructions, country_of_origin, return_policy_enabled, status, created_at, updated_at) VALUES
('Basic Crew Neck T-Shirt', 'basic-crew-neck-t-shirt', @tshirts, 'MEN',
 799.00, 499.00, 37, 'White',
 'Essential white crew neck t-shirt made with ultra-soft breathable cotton. A wardrobe must-have.',
 '100% Cotton', 'Machine wash', 'India', TRUE, 'ACTIVE', @now, @now),

('Graphic Print Oversized Tee', 'graphic-print-oversized-tee', @tshirts, 'MEN',
 999.00, 649.00, 35, 'Black',
 'Urban-style oversized tee with a bold graphic print. Drop shoulders for a relaxed street-style fit.',
 '100% Cotton Boxy Fit', 'Machine wash cold', 'India', TRUE, 'ACTIVE', @now, @now);

-- ============ MEN'S JEANS ============
INSERT INTO products (name, slug, category_id, gender_tag, mrp, selling_price, discount_percent, color, description, fabric_details, care_instructions, country_of_origin, return_policy_enabled, status, created_at, updated_at) VALUES
('Slim Fit Dark Wash Jeans', 'slim-fit-dark-wash-jeans', @jeans, 'MEN',
 2499.00, 1599.00, 36, 'Dark Blue',
 'Classic slim fit jeans in dark wash denim. Versatile for casual and smart-casual outfits.',
 '98% Cotton, 2% Elastane', 'Machine wash cold', 'India', TRUE, 'ACTIVE', @now, @now),

('Cargo Jogger Pants', 'cargo-jogger-pants', @jeans, 'MEN',
 1899.00, 1199.00, 36, 'Olive',
 'Relaxed cargo jogger pants with multi-pocket design. Comfortable waistband and tapered fit.',
 'Cotton Blend', 'Machine wash', 'India', TRUE, 'ACTIVE', @now, @now);

-- ============ MEN'S JACKETS ============
INSERT INTO products (name, slug, category_id, gender_tag, mrp, selling_price, discount_percent, color, description, fabric_details, care_instructions, country_of_origin, return_policy_enabled, status, created_at, updated_at) VALUES
('Classic Denim Jacket', 'classic-denim-jacket', @jackets, 'MEN',
 2999.00, 1999.00, 33, 'Blue',
 'Timeless denim jacket with button placket and chest pockets. A classic layer for any casual look.',
 'Denim 100% Cotton', 'Machine wash cold', 'India', TRUE, 'ACTIVE', @now, @now),

('Quilted Puffer Jacket', 'quilted-puffer-jacket', @jackets, 'MEN',
 3499.00, 2299.00, 34, 'Black',
 'Warm quilted puffer jacket with stand collar. Lightweight yet highly insulating for winter days.',
 'Polyester Shell, Polyester Fill', 'Machine wash', 'India', TRUE, 'ACTIVE', @now, @now);

-- ============ WOMEN'S KURTIS ============
INSERT INTO products (name, slug, category_id, gender_tag, mrp, selling_price, discount_percent, color, description, fabric_details, care_instructions, country_of_origin, return_policy_enabled, status, created_at, updated_at) VALUES
('Floral Anarkali Kurti', 'floral-anarkali-kurti', @kurtis, 'WOMEN',
 1599.00, 1099.00, 31, 'Pink',
 'Beautiful floral print Anarkali kurti. Comfortable A-line silhouette. Perfect for festive occasions.',
 'Rayon', 'Hand wash', 'India', TRUE, 'ACTIVE', @now, @now),

('Embroidered Cotton Kurti', 'embroidered-cotton-kurti', @kurtis, 'WOMEN',
 1899.00, 1299.00, 31, 'White',
 'Elegantly embroidered cotton kurti with straight fit and three-quarter sleeves. Traditional meets modern.',
 '100% Cotton', 'Hand wash cold', 'India', TRUE, 'ACTIVE', @now, @now),

('Printed Straight Kurti', 'printed-straight-kurti', @kurtis, 'WOMEN',
 1299.00, 849.00, 34, 'Yellow',
 'Vibrant printed straight kurti with comfortable fit. Great for everyday wear and casual outings.',
 'Viscose', 'Machine wash cold', 'India', TRUE, 'ACTIVE', @now, @now);

-- ============ WOMEN'S DRESSES ============
INSERT INTO products (name, slug, category_id, gender_tag, mrp, selling_price, discount_percent, color, description, fabric_details, care_instructions, country_of_origin, return_policy_enabled, status, created_at, updated_at) VALUES
('Summer Floral Maxi Dress', 'summer-floral-maxi-dress', @dresses, 'WOMEN',
 2199.00, 1499.00, 31, 'Yellow',
 'Breezy floral maxi dress with V-neckline and flowy skirt. Perfect for beach outings and summer events.',
 'Viscose Crepe', 'Machine wash cold', 'India', TRUE, 'ACTIVE', @now, @now),

('Off-Shoulder Mini Dress', 'off-shoulder-mini-dress', @dresses, 'WOMEN',
 1799.00, 1199.00, 33, 'Red',
 'Trendy off-shoulder mini dress with fitted bodice and flared skirt. Great for parties and evenings.',
 'Polyester Blend', 'Machine wash', 'India', TRUE, 'ACTIVE', @now, @now);

-- ============ WOMEN'S TOPS ============
INSERT INTO products (name, slug, category_id, gender_tag, mrp, selling_price, discount_percent, color, description, fabric_details, care_instructions, country_of_origin, return_policy_enabled, status, created_at, updated_at) VALUES
('Satin Wrap Top', 'satin-wrap-top', @tops, 'WOMEN',
 1299.00, 849.00, 34, 'Emerald',
 'Luxurious satin wrap top with adjustable tie waist. Effortlessly elegant for work and evenings.',
 'Satin Polyester', 'Hand wash', 'India', TRUE, 'ACTIVE', @now, @now);

-- ============ PRODUCT IMAGES ============
INSERT INTO product_images (product_id, media_url, display_order, is_thumbnail)
SELECT p.id,
       CASE p.slug
         WHEN 'oxford-slim-fit-premium-shirt'  THEN 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600&q=80'
         WHEN 'classic-white-formal-shirt'      THEN 'https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=600&q=80'
         WHEN 'linen-summer-shirt'              THEN 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600&q=80'
         WHEN 'basic-crew-neck-t-shirt'         THEN 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600&q=80'
         WHEN 'graphic-print-oversized-tee'     THEN 'https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=600&q=80'
         WHEN 'slim-fit-dark-wash-jeans'        THEN 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&q=80'
         WHEN 'cargo-jogger-pants'              THEN 'https://images.unsplash.com/photo-1552902865-b72c031ac5ea?w=600&q=80'
         WHEN 'classic-denim-jacket'            THEN 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600&q=80'
         WHEN 'quilted-puffer-jacket'           THEN 'https://images.unsplash.com/photo-1544022613-e87ca75a784a?w=600&q=80'
         WHEN 'floral-anarkali-kurti'           THEN 'https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=600&q=80'
         WHEN 'embroidered-cotton-kurti'        THEN 'https://images.unsplash.com/photo-1583391733981-8498408ee4b6?w=600&q=80'
         WHEN 'printed-straight-kurti'          THEN 'https://images.unsplash.com/photo-1614252369475-531eba835eb1?w=600&q=80'
         WHEN 'summer-floral-maxi-dress'        THEN 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=600&q=80'
         WHEN 'off-shoulder-mini-dress'         THEN 'https://images.unsplash.com/photo-1585487000160-6ebcfceb0d03?w=600&q=80'
         WHEN 'satin-wrap-top'                  THEN 'https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600&q=80'
       END,
       1, TRUE
FROM products p
WHERE p.slug IN (
  'oxford-slim-fit-premium-shirt','classic-white-formal-shirt','linen-summer-shirt',
  'basic-crew-neck-t-shirt','graphic-print-oversized-tee',
  'slim-fit-dark-wash-jeans','cargo-jogger-pants',
  'classic-denim-jacket','quilted-puffer-jacket',
  'floral-anarkali-kurti','embroidered-cotton-kurti','printed-straight-kurti',
  'summer-floral-maxi-dress','off-shoulder-mini-dress','satin-wrap-top'
);

-- ============ PRODUCT VARIANTS (SIZES) ============

-- Men's Shirts: S, M, L, XL
INSERT INTO product_variants (product_id, size_code, sku_code, is_active)
SELECT p.id, s.sz, CONCAT(UPPER(SUBSTR(p.slug,1,3)), '-', UPPER(SUBSTR(p.color,1,3)), '-', s.sz), TRUE
FROM products p
CROSS JOIN (SELECT 'S' sz UNION SELECT 'M' UNION SELECT 'L' UNION SELECT 'XL') s
WHERE p.slug IN ('oxford-slim-fit-premium-shirt','classic-white-formal-shirt','linen-summer-shirt');

-- Men's T-Shirts: S, M, L, XL, XXL
INSERT INTO product_variants (product_id, size_code, sku_code, is_active)
SELECT p.id, s.sz, CONCAT(UPPER(SUBSTR(p.slug,1,3)), '-', UPPER(SUBSTR(p.color,1,3)), '-', s.sz, '-', p.id), TRUE
FROM products p
CROSS JOIN (SELECT 'S' sz UNION SELECT 'M' UNION SELECT 'L' UNION SELECT 'XL' UNION SELECT 'XXL') s
WHERE p.slug IN ('basic-crew-neck-t-shirt','graphic-print-oversized-tee');

-- Men's Bottoms: 30, 32, 34, 36
INSERT INTO product_variants (product_id, size_code, sku_code, is_active)
SELECT p.id, s.sz, CONCAT(UPPER(SUBSTR(p.slug,1,3)), '-', UPPER(SUBSTR(p.color,1,3)), '-', s.sz, '-', p.id), TRUE
FROM products p
CROSS JOIN (SELECT '30' sz UNION SELECT '32' UNION SELECT '34' UNION SELECT '36') s
WHERE p.slug IN ('slim-fit-dark-wash-jeans','cargo-jogger-pants');

-- Men's Jackets: S, M, L, XL
INSERT INTO product_variants (product_id, size_code, sku_code, is_active)
SELECT p.id, s.sz, CONCAT(UPPER(SUBSTR(p.slug,1,3)), '-', UPPER(SUBSTR(p.color,1,3)), '-', s.sz, '-', p.id), TRUE
FROM products p
CROSS JOIN (SELECT 'S' sz UNION SELECT 'M' UNION SELECT 'L' UNION SELECT 'XL') s
WHERE p.slug IN ('classic-denim-jacket','quilted-puffer-jacket');

-- Women's: XS, S, M, L, XL
INSERT INTO product_variants (product_id, size_code, sku_code, is_active)
SELECT p.id, s.sz, CONCAT(UPPER(SUBSTR(p.slug,1,3)), '-', UPPER(SUBSTR(p.color,1,3)), '-', s.sz, '-', p.id), TRUE
FROM products p
CROSS JOIN (SELECT 'XS' sz UNION SELECT 'S' UNION SELECT 'M' UNION SELECT 'L' UNION SELECT 'XL') s
WHERE p.slug IN (
  'floral-anarkali-kurti','embroidered-cotton-kurti','printed-straight-kurti',
  'summer-floral-maxi-dress','off-shoulder-mini-dress','satin-wrap-top'
);

-- ============ VERIFY COUNTS ============
SELECT 'categories' tbl, COUNT(*) cnt FROM categories
UNION ALL SELECT 'products',   COUNT(*) FROM products
UNION ALL SELECT 'images',     COUNT(*) FROM product_images
UNION ALL SELECT 'variants',   COUNT(*) FROM product_variants;
