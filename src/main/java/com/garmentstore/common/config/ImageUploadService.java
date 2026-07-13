package com.garmentstore.common.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.garmentstore.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for uploading product images to Cloudinary CDN.
 *
 * Why Cloudinary:
 *  - Free tier: 25 GB storage + 25 GB CDN bandwidth (Akamai-backed)
 *  - Low latency globally via CDN edge nodes
 *  - On-the-fly transformations via URL params (resize, format, quality)
 *  - The returned secure_url is stored in product_images.media_url — no schema change needed
 *
 * Usage (Admin flow):
 *  1. Admin calls POST /api/v1/admin/catalog/images/upload with a file
 *  2. This service uploads to Cloudinary, returns { url: "https://res.cloudinary.com/..." }
 *  3. Admin then calls POST /api/v1/admin/catalog/products/{id}/images with that URL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final Optional<Cloudinary> cloudinary; // Optional — absent when disabled

    @Value("${app.cloudinary.folder:vastra/products}")
    private String uploadFolder;

    @Value("${app.cloudinary.enabled:false}")
    private boolean enabled;

    /**
     * Uploads an image file to Cloudinary and returns its CDN URL.
     *
     * @param file The multipart image file (JPEG, PNG, WebP)
     * @return The secure CDN URL (https://res.cloudinary.com/...)
     * @throws BusinessException if Cloudinary is not configured or upload fails
     */
    @SuppressWarnings("unchecked")
    public String upload(MultipartFile file) {
        if (!enabled || cloudinary.isEmpty()) {
            throw new BusinessException(
                "CLOUDINARY_NOT_CONFIGURED",
                "Image upload via Cloudinary is not enabled. Set CLOUDINARY_ENABLED=true and CLOUDINARY_URL env vars.",
                HttpStatus.SERVICE_UNAVAILABLE
            );
        }

        validateFile(file);

        try {
            String publicId = uploadFolder + "/" + UUID.randomUUID();
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "public_id", publicId,
                "overwrite", true,
                "resource_type", "image",
                // Auto-convert to WebP for best compression + quality
                "format", "webp",
                // Preserve aspect ratio, max 1200px wide
                "transformation", "w_1200,c_limit,q_auto,f_webp"
            );

            Map<String, Object> result = cloudinary.get().uploader().upload(file.getBytes(), uploadParams);
            String url = (String) result.get("secure_url");
            log.info("Image uploaded successfully: publicId={}, url={}", publicId, url);
            return url;

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new BusinessException(
                "IMAGE_UPLOAD_FAILED",
                "Failed to upload image. Please try again.",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("INVALID_FILE", "No file provided", HttpStatus.BAD_REQUEST);
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new BusinessException("INVALID_FILE_TYPE", "Only image files are allowed (JPEG, PNG, WebP)", HttpStatus.BAD_REQUEST);
        }
        // 10 MB max
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("FILE_TOO_LARGE", "Image must be smaller than 10 MB", HttpStatus.BAD_REQUEST);
        }
    }
}
