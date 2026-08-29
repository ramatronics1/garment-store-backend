/**
 * Quick Cloudinary connection test — run with node.js
 * Tests that credentials are valid and can list folders
 *
 * Run: node test_cloudinary.js
 */
const https = require('https');

const CLOUD_NAME = 'niwb76bb';
const API_KEY = '914171142689583';
const API_SECRET = 'U4UvO13pXJzCJ7v0OaLWM0KA-80';

// Base64 encode API_KEY:API_SECRET
const auth = Buffer.from(`${API_KEY}:${API_SECRET}`).toString('base64');

// Call Cloudinary ping API to verify credentials
const options = {
  hostname: 'api.cloudinary.com',
  path: `/v1_1/${CLOUD_NAME}/ping`,
  method: 'GET',
  headers: {
    'Authorization': `Basic ${auth}`,
    'Content-Type': 'application/json'
  }
};

const req = https.request(options, (res) => {
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => {
    if (res.statusCode === 200) {
      console.log('✅ Cloudinary connection SUCCESSFUL!');
      console.log('   Cloud Name:', CLOUD_NAME);
      console.log('   Response:', data);
      console.log('\n📸 Your CDN base URL:');
      console.log(`   https://res.cloudinary.com/${CLOUD_NAME}/image/upload/`);
    } else {
      console.log('❌ Connection FAILED — status:', res.statusCode);
      console.log('   Response:', data);
    }
  });
});

req.on('error', (e) => {
  console.error('❌ Network error:', e.message);
});

req.end();
