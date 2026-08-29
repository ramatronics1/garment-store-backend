/**
 * Auto-fetch all 15 product images from Unsplash into Cloudinary
 * This uses Cloudinary's "upload from URL" API — no download needed
 *
 * Run: node import_images_to_cloudinary.js
 */
const https = require('https');
const crypto = require('crypto');

const CLOUD_NAME = 'niwb76bb';
const API_KEY = '914171142689583';
const API_SECRET = 'U4UvO13pXJzCJ7v0OaLWM0KA-80';
const FOLDER = 'vastra/products';

// Products mapped to their Unsplash image URLs
const PRODUCTS = [
  { slug: 'oxford-slim-fit-premium-shirt',  publicId: 'oxford-shirt',       url: 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600&q=80' },
  { slug: 'classic-white-formal-shirt',      publicId: 'white-formal-shirt',  url: 'https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=600&q=80' },
  { slug: 'linen-summer-shirt',              publicId: 'linen-shirt',          url: 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600&q=80' },
  { slug: 'basic-crew-neck-t-shirt',         publicId: 'crew-neck-tee',        url: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600&q=80' },
  { slug: 'graphic-print-oversized-tee',     publicId: 'graphic-tee',          url: 'https://images.unsplash.com/photo-1503341504253-dff4815485f1?w=600&q=80' },
  { slug: 'slim-fit-dark-wash-jeans',        publicId: 'dark-jeans',           url: 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&q=80' },
  { slug: 'cargo-jogger-pants',              publicId: 'cargo-jogger',         url: 'https://images.unsplash.com/photo-1552902865-b72c031ac5ea?w=600&q=80' },
  { slug: 'classic-denim-jacket',            publicId: 'denim-jacket',         url: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600&q=80' },
  { slug: 'quilted-puffer-jacket',           publicId: 'puffer-jacket',        url: 'https://images.unsplash.com/photo-1544022613-e87ca75a784a?w=600&q=80' },
  { slug: 'floral-anarkali-kurti',           publicId: 'anarkali-kurti',       url: 'https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=600&q=80' },
  { slug: 'embroidered-cotton-kurti',        publicId: 'embroidered-kurti',    url: 'https://images.unsplash.com/photo-1583391733981-8498408ee4b6?w=600&q=80' },
  { slug: 'printed-straight-kurti',          publicId: 'printed-kurti',        url: 'https://images.unsplash.com/photo-1614252369475-531eba835eb1?w=600&q=80' },
  { slug: 'summer-floral-maxi-dress',        publicId: 'maxi-dress',           url: 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=600&q=80' },
  { slug: 'off-shoulder-mini-dress',         publicId: 'mini-dress',           url: 'https://images.unsplash.com/photo-1585487000160-6ebcfceb0d03?w=600&q=80' },
  { slug: 'satin-wrap-top',                  publicId: 'satin-top',            url: 'https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600&q=80' },
];

function generateSignature(params) {
  const sortedParams = Object.keys(params)
    .sort()
    .map(k => `${k}=${params[k]}`)
    .join('&');
  return crypto.createHash('sha1').update(sortedParams + API_SECRET).digest('hex');
}

function uploadImage(product) {
  return new Promise((resolve, reject) => {
    const timestamp = Math.floor(Date.now() / 1000);
    const publicId = `${FOLDER}/${product.publicId}`;

    const params = {
      public_id: publicId,
      folder: FOLDER,
      timestamp: timestamp,
      overwrite: 'true',
      format: 'webp',
      transformation: 'w_800,c_limit,q_auto,f_webp',
    };

    const signature = generateSignature(params);

    const postData = new URLSearchParams({
      ...params,
      file: product.url,
      api_key: API_KEY,
      signature: signature,
    }).toString();

    const options = {
      hostname: 'api.cloudinary.com',
      path: `/v1_1/${CLOUD_NAME}/image/upload`,
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': Buffer.byteLength(postData),
      }
    };

    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          const json = JSON.parse(data);
          if (json.secure_url) {
            resolve({ slug: product.slug, url: json.secure_url, publicId: json.public_id });
          } else {
            reject(new Error(`Upload failed for ${product.publicId}: ${data}`));
          }
        } catch (e) {
          reject(new Error(`Parse error for ${product.publicId}: ${data}`));
        }
      });
    });

    req.on('error', reject);
    req.write(postData);
    req.end();
  });
}

async function main() {
  console.log(`🚀 Uploading ${PRODUCTS.length} product images to Cloudinary...\n`);
  
  const sqlLines = [];
  const results = [];

  for (const product of PRODUCTS) {
    try {
      process.stdout.write(`  ⏳ ${product.publicId}...`);
      const result = await uploadImage(product);
      results.push(result);
      console.log(` ✅`);
      sqlLines.push(
        `UPDATE product_images SET media_url = '${result.url}'\n` +
        `  WHERE product_id = (SELECT id FROM products WHERE slug = '${result.slug}');`
      );
    } catch (e) {
      console.log(` ❌ ${e.message}`);
    }
  }

  console.log(`\n✅ Uploaded ${results.length}/${PRODUCTS.length} images successfully!\n`);
  
  // Write the SQL update file automatically
  const sqlContent = `-- Auto-generated by import_images_to_cloudinary.js\n-- Run this to update all product image URLs in your DB\n\n` + sqlLines.join('\n\n') + '\n';
  require('fs').writeFileSync('update_cloudinary_urls_generated.sql', sqlContent);
  
  console.log('📄 SQL update file written: update_cloudinary_urls_generated.sql');
  console.log('📌 Run it with:');
  console.log('   Get-Content update_cloudinary_urls_generated.sql | & "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe" -u root -proot garment_stores\n');
  
  console.log('🔗 Uploaded URLs:');
  results.forEach(r => console.log(`   ${r.slug}: ${r.url}`));
}

main().catch(console.error);
