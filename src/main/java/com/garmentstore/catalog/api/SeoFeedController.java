package com.garmentstore.catalog.api;

import com.garmentstore.catalog.application.SeoFeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "SEO & Merchant Feeds", description = "Public endpoints for search engines, Google Shopping, and crawlers")
public class SeoFeedController {

    private final SeoFeedService seoFeedService;

    public SeoFeedController(SeoFeedService seoFeedService) {
        this.seoFeedService = seoFeedService;
    }

    @GetMapping(value = {"/sitemap.xml", "/api/v1/feeds/sitemap.xml"}, produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Dynamic XML Sitemap with Google Image metadata")
    public ResponseEntity<String> getSitemap() {
        String xml = seoFeedService.getSitemapXml();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/xml; charset=UTF-8"));
        headers.setCacheControl("public, max-age=43200"); // 12 hours HTTP cache
        return new ResponseEntity<>(xml, headers, HttpStatus.OK);
    }

    @GetMapping(value = {"/feeds/google-merchant.xml", "/api/v1/feeds/google-merchant.xml"}, produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Google Merchant Center & Shopping Graph RSS 2.0 Feed")
    public ResponseEntity<String> getGoogleMerchantFeed() {
        String xml = seoFeedService.getGoogleMerchantXml();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/xml; charset=UTF-8"));
        headers.setCacheControl("public, max-age=43200"); // 12 hours HTTP cache
        return new ResponseEntity<>(xml, headers, HttpStatus.OK);
    }
}
