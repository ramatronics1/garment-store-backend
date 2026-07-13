-- Final clean Cloudinary URLs for all 15 products
-- Cloud: niwb76bb | Folder: vastra/products
-- All images optimized to WebP by Cloudinary CDN

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/oxford-shirt.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'oxford-slim-fit-premium-shirt');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/white-formal-shirt.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'classic-white-formal-shirt');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/linen-shirt.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'linen-summer-shirt');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/crew-neck-tee.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'basic-crew-neck-t-shirt');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/graphic-tee.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'graphic-print-oversized-tee');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/dark-jeans.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'slim-fit-dark-wash-jeans');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/cargo-jogger.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'cargo-jogger-pants');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/denim-jacket.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'classic-denim-jacket');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/puffer-jacket.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'quilted-puffer-jacket');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/anarkali-kurti.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'floral-anarkali-kurti');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/embroidered-kurti.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'embroidered-cotton-kurti');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/printed-kurti.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'printed-straight-kurti');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/maxi-dress.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'summer-floral-maxi-dress');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/mini-dress.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'off-shoulder-mini-dress');

UPDATE product_images SET media_url = 'https://res.cloudinary.com/niwb76bb/image/upload/vastra/products/satin-top.webp'
  WHERE product_id = (SELECT id FROM products WHERE slug = 'satin-wrap-top');

-- Verify
SELECT p.slug, pi.media_url
FROM products p JOIN product_images pi ON pi.product_id = p.id
ORDER BY p.id;
