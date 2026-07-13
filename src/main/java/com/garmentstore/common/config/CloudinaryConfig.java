package com.garmentstore.common.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cloudinary CDN configuration.
 *
 * Set the CLOUDINARY_URL environment variable (format: cloudinary://api_key:api_secret@cloud_name)
 * and CLOUDINARY_ENABLED=true to activate image uploads.
 *
 * Free tier: 25 GB storage + 25 GB CDN bandwidth/month.
 * Sign up at: https://cloudinary.com
 */
@Configuration
public class CloudinaryConfig {

    @Value("${app.cloudinary.url:}")
    private String cloudinaryUrl;

    /**
     * Cloudinary bean — only created when cloudinary is enabled and URL is configured.
     * When disabled (default), the ImageUploadService will throw a meaningful error
     * instead of a NullPointerException.
     */
    @Bean
    @ConditionalOnProperty(name = "app.cloudinary.enabled", havingValue = "true")
    public Cloudinary cloudinary() {
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            throw new IllegalStateException(
                "CLOUDINARY_URL environment variable is required when app.cloudinary.enabled=true. " +
                "Format: cloudinary://api_key:api_secret@cloud_name"
            );
        }
        return new Cloudinary(cloudinaryUrl);
    }
}
