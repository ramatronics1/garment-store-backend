-- V8: Seed Dummy Products for UI Display
-- Adds some categories, products, images, and variants so the frontend has data to show.

-- 1. Add some concrete product categories
INSERT INTO categories(id, name, slug, parent_category_id, display_order, is_active) VALUES 
(4, 'Shirts', 'shirts', 1, 4, TRUE),
(5, 'Kurtis', 'kurtis', 2, 5, TRUE),
(6, 'T-Shirts', 't-shirts', 1, 6, TRUE),
(7, 'Jeans', 'jeans', 1, 7, TRUE),
(8, 'Dresses', 'dresses', 2, 8, TRUE);

-- 2. Add products
INSERT INTO products (id, name, slug, category_id, gender_tag, mrp, selling_price, discount_percent, color, description, fabric_details, care_instructions, country_of_origin, status) VALUES 
(1, 'Oxford Slim Fit Premium Shirt', 'oxford-slim-fit-premium-shirt', 4, 'MEN', 1399.00, 899.00, 35, 'Blue', '100% premium cotton Oxford weave. Machine washable. Slim fit with structured collar.', '100% Cotton', 'Machine wash cold, tumble dry low', 'India', 'ACTIVE'),
(2, 'Floral Anarkali Kurti', 'floral-anarkali-kurti', 5, 'WOMEN', 1599.00, 1199.00, 25, 'Pink', 'Beautiful floral print Anarkali kurti. Perfect for festive and casual occasions.', 'Cotton Blend', 'Hand wash', 'India', 'ACTIVE'),
(3, 'Classic Denim Jacket', 'classic-denim-jacket', 4, 'MEN', 2499.00, 1799.00, 28, 'Blue', 'Timeless denim jacket with a comfortable fit and classic styling.', 'Denim', 'Machine wash', 'India', 'ACTIVE'),
(4, 'Summer Maxi Dress', 'summer-maxi-dress', 8, 'WOMEN', 1999.00, 1499.00, 25, 'Yellow', 'Breezy and bright yellow maxi dress for summer outings and beach walks.', 'Viscose', 'Machine wash cold', 'India', 'ACTIVE'),
(5, 'Basic Crew Neck T-Shirt', 'basic-crew-neck-t-shirt', 6, 'MEN', 799.00, 499.00, 37, 'White', 'Essential white crew neck t-shirt made with ultra-soft breathable cotton.', '100% Cotton', 'Machine wash', 'India', 'ACTIVE');

-- 3. Add images (using Unsplash for dummy purposes)
INSERT INTO product_images (product_id, media_url, display_order, is_thumbnail) VALUES 
(1, 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=400&q=80', 1, TRUE),
(1, 'https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=400&q=80', 2, FALSE),
(2, 'https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=400&q=80', 1, TRUE),
(3, 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&q=80', 1, TRUE),
(4, 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=400&q=80', 1, TRUE),
(5, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&q=80', 1, TRUE);

-- 4. Add variants (sizes)
INSERT INTO product_variants (product_id, size_code, sku_code, is_active) VALUES 
(1, 'S', 'OX-BLU-S', TRUE),
(1, 'M', 'OX-BLU-M', TRUE),
(1, 'L', 'OX-BLU-L', TRUE),
(2, 'M', 'FL-PNK-M', TRUE),
(2, 'L', 'FL-PNK-L', TRUE),
(3, 'M', 'DJ-BLU-M', TRUE),
(3, 'L', 'DJ-BLU-L', TRUE),
(3, 'XL', 'DJ-BLU-XL', TRUE),
(4, 'S', 'MD-YEL-S', TRUE),
(4, 'M', 'MD-YEL-M', TRUE),
(5, 'S', 'TS-WHT-S', TRUE),
(5, 'M', 'TS-WHT-M', TRUE),
(5, 'L', 'TS-WHT-L', TRUE),
(5, 'XL', 'TS-WHT-XL', TRUE);

-- 5. Add featured products for the home page (if it gets re-enabled)
INSERT INTO featured_products (product_id, display_order, is_active) VALUES 
(1, 1, TRUE),
(2, 2, TRUE),
(3, 3, TRUE),
(4, 4, TRUE);
