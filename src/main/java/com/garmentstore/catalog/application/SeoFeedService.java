package com.garmentstore.catalog.application;

import com.garmentstore.catalog.domain.*;
import com.garmentstore.catalog.infrastructure.CategoryRepository;
import com.garmentstore.catalog.infrastructure.ProductImageRepository;
import com.garmentstore.catalog.infrastructure.ProductRepository;
import com.garmentstore.catalog.infrastructure.ProductVariantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SeoFeedService {

    private static final Logger log = LoggerFactory.getLogger(SeoFeedService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
    private static final long CACHE_TTL_MS = 12 * 60 * 60 * 1000L; // 12 hours

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final String baseUrl;

    private String cachedSitemapXml;
    private long sitemapLastGenerated = 0;

    private String cachedMerchantXml;
    private long merchantLastGenerated = 0;

    public SeoFeedService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductImageRepository productImageRepository,
            ProductVariantRepository productVariantRepository,
            @Value("${app.site.base-url:https://selection.com}") String baseUrl) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /**
     * Generates or returns cached XML Sitemap with Image metadata adhering to sitemaps.org specification.
     */
    @Transactional(readOnly = true)
    public synchronized String getSitemapXml() {
        long now = System.currentTimeMillis();
        if (cachedSitemapXml != null && (now - sitemapLastGenerated < CACHE_TTL_MS)) {
            return cachedSitemapXml;
        }

        log.info("Generating dynamic XML Sitemap for base URL: {}", baseUrl);
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n");
        sb.append("        xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\">\n");

        String today = DATE_FORMATTER.format(Instant.now());

        // 1. Core Primary URLs
        addUrl(sb, baseUrl + "/", today, "daily", "1.0");
        addUrl(sb, baseUrl + "/shop/men", today, "daily", "0.9");
        addUrl(sb, baseUrl + "/shop/women", today, "daily", "0.9");
        addUrl(sb, baseUrl + "/shop/kids", today, "daily", "0.8");
        addUrl(sb, baseUrl + "/products", today, "daily", "0.9");
        addUrl(sb, baseUrl + "/journal", today, "daily", "0.8");

        // 2. Static & Policy URLs
        addUrl(sb, baseUrl + "/about", today, "monthly", "0.6");
        addUrl(sb, baseUrl + "/contact", today, "monthly", "0.5");
        addUrl(sb, baseUrl + "/faq", today, "weekly", "0.7");
        addUrl(sb, baseUrl + "/shipping", today, "monthly", "0.5");
        addUrl(sb, baseUrl + "/returns", today, "monthly", "0.5");
        addUrl(sb, baseUrl + "/size-guide", today, "monthly", "0.6");
        addUrl(sb, baseUrl + "/sustainability", today, "monthly", "0.5");
        addUrl(sb, baseUrl + "/careers", today, "weekly", "0.5");
        addUrl(sb, baseUrl + "/privacy", today, "monthly", "0.3");
        addUrl(sb, baseUrl + "/terms", today, "monthly", "0.3");
        addUrl(sb, baseUrl + "/grievance", today, "monthly", "0.3");

        // 3. Dynamic Categories
        List<Category> categories = categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc();
        Map<Long, Category> categoryMap = new HashMap<>();
        for (Category c : categories) {
            categoryMap.put(c.getId(), c);
        }

        for (Category c : categories) {
            String path = buildCategoryPath(c, categoryMap);
            if (path != null && !path.isEmpty()) {
                addUrl(sb, baseUrl + path, today, "daily", "0.8");
            }
        }

        // 4. Dynamic Products with Image Sitemaps
        List<Product> products = productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE);
        for (Product p : products) {
            String productSlug = (p.getSlug() != null && !p.getSlug().isBlank()) ? p.getSlug() : String.valueOf(p.getId());
            String productUrl = baseUrl + "/products/" + productSlug;
            Instant modTime = p.getUpdatedAt() != null ? p.getUpdatedAt() : (p.getCreatedAt() != null ? p.getCreatedAt() : Instant.now());
            String lastMod = DATE_FORMATTER.format(modTime);

            sb.append("  <url>\n");
            sb.append("    <loc>").append(escapeXml(productUrl)).append("</loc>\n");
            sb.append("    <lastmod>").append(lastMod).append("</lastmod>\n");
            sb.append("    <changefreq>weekly</changefreq>\n");
            sb.append("    <priority>0.8</priority>\n");

            // Attach product images for Google Image Search
            List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(p.getId());
            for (ProductImage img : images) {
                if (img.getMediaUrl() != null && !img.getMediaUrl().isBlank()) {
                    sb.append("    <image:image>\n");
                    sb.append("      <image:loc>").append(escapeXml(img.getMediaUrl())).append("</image:loc>\n");
                    sb.append("      <image:title>").append(escapeXml(p.getName())).append("</image:title>\n");
                    sb.append("    </image:image>\n");
                }
            }
            sb.append("  </url>\n");
        }

        sb.append("</urlset>\n");

        cachedSitemapXml = sb.toString();
        sitemapLastGenerated = now;
        return cachedSitemapXml;
    }

    /**
     * Generates or returns cached Google Merchant Center Product Feed XML (RSS 2.0).
     */
    @Transactional(readOnly = true)
    public synchronized String getGoogleMerchantXml() {
        long now = System.currentTimeMillis();
        if (cachedMerchantXml != null && (now - merchantLastGenerated < CACHE_TTL_MS)) {
            return cachedMerchantXml;
        }

        log.info("Generating Google Merchant Center Feed XML");
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<rss version=\"2.0\" xmlns:g=\"http://base.google.com/ns/1.0\">\n");
        sb.append("  <channel>\n");
        sb.append("    <title>Selection Luxury &amp; Handcrafted Apparel</title>\n");
        sb.append("    <link>").append(escapeXml(baseUrl)).append("</link>\n");
        sb.append("    <description>Curated handcrafted ethnic and contemporary apparel store</description>\n");

        List<Product> products = productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE);

        for (Product p : products) {
            String productSlug = (p.getSlug() != null && !p.getSlug().isBlank()) ? p.getSlug() : String.valueOf(p.getId());
            String productUrl = baseUrl + "/products/" + productSlug;
            String brand = (p.getBrand() != null && !p.getBrand().isBlank()) ? p.getBrand() : "Selection";
            String gender = p.getGenderTag() != null ? p.getGenderTag().name().toLowerCase() : "unisex";

            List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(p.getId());
            String mainImage = images.isEmpty() ? baseUrl + "/favicon.svg" : images.get(0).getMediaUrl();

            List<ProductVariant> variants = productVariantRepository.findByProductIdOrderByColorDisplayOrderAscSizeSortOrderAsc(p.getId());

            if (variants.isEmpty()) {
                // Standalone product item
                sb.append("    <item>\n");
                sb.append("      <g:id>SEL-").append(p.getId()).append("</g:id>\n");
                sb.append("      <g:title>").append(escapeXml(p.getName())).append("</g:title>\n");
                sb.append("      <g:description>").append(escapeXml(p.getDescription() != null ? p.getDescription() : p.getName())).append("</g:description>\n");
                sb.append("      <g:link>").append(escapeXml(productUrl)).append("</g:link>\n");
                sb.append("      <g:image_link>").append(escapeXml(mainImage)).append("</g:image_link>\n");
                sb.append("      <g:brand>").append(escapeXml(brand)).append("</g:brand>\n");
                sb.append("      <g:condition>new</g:condition>\n");
                sb.append("      <g:availability>in_stock</g:availability>\n");
                sb.append("      <g:price>1499.00 INR</g:price>\n");
                sb.append("      <g:google_product_category>Apparel &amp; Accessories &gt; Clothing</g:google_product_category>\n");
                sb.append("      <g:gender>").append(escapeXml(gender)).append("</g:gender>\n");
                sb.append("      <g:age_group>adult</g:age_group>\n");
                sb.append("      <g:shipping><g:country>IN</g:country><g:service>Standard</g:service><g:price>0.00 INR</g:price></g:shipping>\n");
                sb.append("    </item>\n");
            } else {
                for (ProductVariant v : variants) {
                    BigDecimal price = v.getSellingPrice() != null ? v.getSellingPrice() : (v.getMrp() != null ? v.getMrp() : BigDecimal.ZERO);
                    boolean inStock = v.getStockQuantity() > 0;
                    String sku = v.getSku() != null ? v.getSku() : ("SEL-" + p.getId() + "-" + v.getId());

                    sb.append("    <item>\n");
                    sb.append("      <g:id>").append(escapeXml(sku)).append("</g:id>\n");
                    sb.append("      <g:item_group_id>SEL-").append(p.getId()).append("</g:item_group_id>\n");
                    sb.append("      <g:title>").append(escapeXml(p.getName())).append("</g:title>\n");
                    sb.append("      <g:description>").append(escapeXml(p.getDescription() != null ? p.getDescription() : p.getName())).append("</g:description>\n");
                    sb.append("      <g:link>").append(escapeXml(productUrl)).append("</g:link>\n");
                    sb.append("      <g:image_link>").append(escapeXml(mainImage)).append("</g:image_link>\n");
                    sb.append("      <g:brand>").append(escapeXml(brand)).append("</g:brand>\n");
                    sb.append("      <g:condition>new</g:condition>\n");
                    sb.append("      <g:availability>").append(inStock ? "in_stock" : "out_of_stock").append("</g:availability>\n");
                    sb.append("      <g:price>").append(price.setScale(2, java.math.RoundingMode.HALF_UP)).append(" INR</g:price>\n");
                    sb.append("      <g:google_product_category>Apparel &amp; Accessories &gt; Clothing</g:google_product_category>\n");
                    sb.append("      <g:gender>").append(escapeXml(gender)).append("</g:gender>\n");
                    sb.append("      <g:age_group>adult</g:age_group>\n");
                    if (v.getColor() != null && v.getColor().getName() != null) {
                        sb.append("      <g:color>").append(escapeXml(v.getColor().getName())).append("</g:color>\n");
                    }
                    if (v.getSize() != null && v.getSize().getName() != null) {
                        sb.append("      <g:size>").append(escapeXml(v.getSize().getName())).append("</g:size>\n");
                    }
                    sb.append("      <g:shipping><g:country>IN</g:country><g:service>Standard</g:service><g:price>0.00 INR</g:price></g:shipping>\n");
                    sb.append("    </item>\n");
                }
            }
        }

        sb.append("  </channel>\n");
        sb.append("</rss>\n");

        cachedMerchantXml = sb.toString();
        merchantLastGenerated = now;
        return cachedMerchantXml;
    }

    public synchronized void invalidateCache() {
        this.cachedSitemapXml = null;
        this.cachedMerchantXml = null;
        this.sitemapLastGenerated = 0;
        this.merchantLastGenerated = 0;
        log.info("SEO and Merchant XML caches invalidated");
    }

    private void addUrl(StringBuilder sb, String loc, String lastmod, String changefreq, String priority) {
        sb.append("  <url>\n");
        sb.append("    <loc>").append(escapeXml(loc)).append("</loc>\n");
        sb.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        sb.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        sb.append("    <priority>").append(priority).append("</priority>\n");
        sb.append("  </url>\n");
    }

    private String buildCategoryPath(Category c, Map<Long, Category> map) {
        if (c.getSlug() == null || c.getSlug().isBlank()) return null;
        if (c.getParentCategory() == null) {
            // Level 1 root category (Men / Women / Kids)
            return "/shop/" + c.getSlug();
        }

        Category parent = map.get(c.getParentCategory().getId());
        if (parent == null) return "/shop/" + c.getSlug();

        if (parent.getParentCategory() == null) {
            // Level 2 category
            return "/shop/" + parent.getSlug() + "/" + c.getSlug();
        }

        // Level 3 category
        Category grandParent = map.get(parent.getParentCategory().getId());
        if (grandParent != null) {
            return "/shop/" + grandParent.getSlug() + "/" + parent.getSlug() + "/" + c.getSlug();
        }

        return "/shop/" + parent.getSlug() + "/" + c.getSlug();
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
