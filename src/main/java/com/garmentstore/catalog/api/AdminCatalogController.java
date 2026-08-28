package com.garmentstore.catalog.api;

import com.garmentstore.catalog.application.AdminCatalogService;
import com.garmentstore.catalog.application.BulkProductUploadService;
import com.garmentstore.catalog.domain.Attribute;
import com.garmentstore.catalog.domain.AttributeValue;
import com.garmentstore.catalog.dto.CategoryResponse;
import com.garmentstore.catalog.dto.ProductDetailResponse;
import com.garmentstore.catalog.dto.ProductImageResponse;
import com.garmentstore.catalog.dto.admin.*;
import com.garmentstore.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminCatalogController {

    private static final Logger log = LoggerFactory.getLogger(AdminCatalogController.class);
    private final AdminCatalogService s;

    // -------------------------------------------------------------------------
    // Admin Product List & Detail
    // -------------------------------------------------------------------------

    @GetMapping("/api/v1/admin/products")
    public ApiResponse<AdminProductPageResponse> listProducts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "10")         int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc")      String dir) {
        String catParam = (categoryId != null && !categoryId.isBlank()) ? categoryId : category;
        log.info("Admin products list: status={} category={} gender={} q={} page={} size={} sort={} dir={}",
                status, catParam, gender, q, page, size, sort, dir);
        return ApiResponse.success("Products fetched", s.getAdminProducts(status, catParam, gender, q, page, size, sort, dir));
    }

    @GetMapping("/api/v1/admin/products/{id}")
    public ApiResponse<ProductDetailResponse> getProduct(@PathVariable Long id) {
        log.info("Admin product detail: id={}", id);
        return ApiResponse.success("Product fetched", s.getAdminProductDetail(id));
    }

    // -------------------------------------------------------------------------
    // Category CRUD
    // -------------------------------------------------------------------------

    @PostMapping("/api/v1/admin/catalog/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody AdminCategoryRequest r) {
        log.info("Admin create category: name={}", r.name());
        return ApiResponse.success("Category created", s.createCategory(r));
    }

    @PutMapping("/api/v1/admin/catalog/categories/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody AdminCategoryRequest r) {
        log.info("Admin update category: id={}", id);
        return ApiResponse.success("Category updated", s.updateCategory(id, r));
    }

    @DeleteMapping("/api/v1/admin/catalog/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        log.info("Admin delete category: id={}", id);
        s.deleteCategory(id);
        return ApiResponse.success("Category deactivated", null);
    }

    // -------------------------------------------------------------------------
    // Product CRUD
    // -------------------------------------------------------------------------

    @PostMapping("/api/v1/admin/catalog/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductDetailResponse> createProduct(@Valid @RequestBody AdminProductRequest r) {
        log.info("Admin create product: name={}", r.name());
        return ApiResponse.success("Product created", s.createProduct(r));
    }

    @PutMapping("/api/v1/admin/catalog/products/{id}")
    public ApiResponse<ProductDetailResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody AdminProductRequest r) {
        log.info("Admin update product: id={}", id);
        return ApiResponse.success("Product updated", s.updateProduct(id, r));
    }

    @PatchMapping("/api/v1/admin/catalog/products/{id}/status")
    public ApiResponse<ProductDetailResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody AdminProductStatusRequest r) {
        log.info("Admin update product status: id={} status={}", id, r.status());
        return ApiResponse.success("Product status updated", s.updateStatus(id, r));
    }

    @DeleteMapping("/api/v1/admin/catalog/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        log.info("Admin delete product: id={}", id);
        s.deleteProduct(id);
        return ApiResponse.success("Product deleted", null);
    }

    // -------------------------------------------------------------------------
    // Variant CRUD
    // -------------------------------------------------------------------------

    @PostMapping("/api/v1/admin/catalog/products/{id}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminVariantResponse> addVariant(@PathVariable Long id, @Valid @RequestBody AdminVariantRequest r) {
        log.info("Admin add variant: productId={} colorId={} sizeId={}", id, r.colorId(), r.sizeId());
        return ApiResponse.success("Variant added", s.addVariant(id, r));
    }

    /**
     * POST /api/v1/admin/catalog/products/{id}/variants/generate
     * Auto-generates all Color × Size combinations from the supplied ID lists.
     * Existing combinations are silently skipped (idempotent).
     */
    @PostMapping("/api/v1/admin/catalog/products/{id}/variants/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<AdminVariantResponse>> generateVariants(
            @PathVariable Long id, @Valid @RequestBody AdminVariantGenerateRequest r) {
        log.info("Admin generate variants: productId={} colors={} sizes={}", id, r.colorIds(), r.sizeIds());
        return ApiResponse.success("Variants generated", s.generateVariants(id, r));
    }

    @PutMapping("/api/v1/admin/catalog/variants/{id}")
    public ApiResponse<AdminVariantResponse> updateVariant(@PathVariable Long id, @Valid @RequestBody AdminVariantRequest r) {
        log.info("Admin update variant: id={}", id);
        return ApiResponse.success("Variant updated", s.updateVariant(id, r));
    }

    @DeleteMapping("/api/v1/admin/catalog/variants/{id}")
    public ApiResponse<Void> deleteVariant(@PathVariable Long id) {
        log.info("Admin delete variant: id={}", id);
        s.deleteVariant(id);
        return ApiResponse.success("Variant deleted", null);
    }

    // -------------------------------------------------------------------------
    // Image CRUD
    // -------------------------------------------------------------------------

    @PostMapping("/api/v1/admin/catalog/products/{id}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductImageResponse> addImage(@PathVariable Long id, @Valid @RequestBody AdminImageRequest r) {
        log.info("Admin add image: productId={}", id);
        return ApiResponse.success("Image added", s.addImage(id, r));
    }

    @PutMapping("/api/v1/admin/catalog/images/{id}")
    public ApiResponse<ProductImageResponse> updateImage(@PathVariable Long id, @Valid @RequestBody AdminImageRequest r) {
        log.info("Admin update image: id={}", id);
        return ApiResponse.success("Image updated", s.updateImage(id, r));
    }

    @DeleteMapping("/api/v1/admin/catalog/images/{id}")
    public ApiResponse<Void> deleteImage(@PathVariable Long id) {
        log.info("Admin delete image: id={}", id);
        s.deleteImage(id);
        return ApiResponse.success("Image deleted", null);
    }

    // -------------------------------------------------------------------------
    // Featured / Related
    // -------------------------------------------------------------------------

    @PostMapping("/api/v1/admin/catalog/featured")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> addFeatured(@Valid @RequestBody AdminFeaturedRequest r) {
        return ApiResponse.success("Featured product added", s.addFeatured(r));
    }

    @DeleteMapping("/api/v1/admin/catalog/featured/{id}")
    public ApiResponse<Void> deleteFeatured(@PathVariable Long id) {
        s.deleteFeatured(id);
        return ApiResponse.success("Featured product removed", null);
    }

    @PostMapping("/api/v1/admin/catalog/products/{id}/related")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> addRelated(@PathVariable Long id, @Valid @RequestBody AdminRelatedProductRequest r) {
        return ApiResponse.success("Related product added", s.addRelated(id, r));
    }

    @DeleteMapping("/api/v1/admin/catalog/related/{id}")
    public ApiResponse<Void> deleteRelated(@PathVariable Long id) {
        s.deleteRelated(id);
        return ApiResponse.success("Related product removed", null);
    }

    // -------------------------------------------------------------------------
    // Master Data — Colors
    // -------------------------------------------------------------------------

    @GetMapping("/api/v1/admin/catalog/colors")
    public ApiResponse<List<AdminColorResponse>> listColors(
            @RequestParam(defaultValue = "false") boolean all) {
        return ApiResponse.success("Colors fetched", all ? s.listAllColors() : s.listColors());
    }

    @PostMapping("/api/v1/admin/catalog/colors")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminColorResponse> createColor(@Valid @RequestBody AdminColorRequest r) {
        log.info("Admin create color: name={} code={}", r.name(), r.code());
        return ApiResponse.success("Color created", s.createColor(r));
    }

    @PutMapping("/api/v1/admin/catalog/colors/{id}")
    public ApiResponse<AdminColorResponse> updateColor(@PathVariable Long id, @Valid @RequestBody AdminColorRequest r) {
        log.info("Admin update color: id={}", id);
        return ApiResponse.success("Color updated", s.updateColor(id, r));
    }

    @DeleteMapping("/api/v1/admin/catalog/colors/{id}")
    public ApiResponse<Void> deleteColor(@PathVariable Long id) {
        log.info("Admin delete color: id={}", id);
        s.deleteColor(id);
        return ApiResponse.success("Color deactivated", null);
    }

    // -------------------------------------------------------------------------
    // Master Data — Sizes
    // -------------------------------------------------------------------------

    @GetMapping("/api/v1/admin/catalog/sizes")
    public ApiResponse<List<AdminSizeResponse>> listSizes(
            @RequestParam(required = false) Long sizeGroupId) {
        return ApiResponse.success("Sizes fetched", s.listSizes(sizeGroupId));
    }

    @PostMapping("/api/v1/admin/catalog/sizes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminSizeResponse> createSize(@Valid @RequestBody AdminSizeRequest r) {
        log.info("Admin create size: code={} groupId={}", r.sizeCode(), r.sizeGroupId());
        return ApiResponse.success("Size created", s.createSize(r));
    }

    @PutMapping("/api/v1/admin/catalog/sizes/{id}")
    public ApiResponse<AdminSizeResponse> updateSize(@PathVariable Long id, @Valid @RequestBody AdminSizeRequest r) {
        log.info("Admin update size: id={}", id);
        return ApiResponse.success("Size updated", s.updateSize(id, r));
    }

    @DeleteMapping("/api/v1/admin/catalog/sizes/{id}")
    public ApiResponse<Void> deleteSize(@PathVariable Long id) {
        log.info("Admin delete size: id={}", id);
        s.deleteSize(id);
        return ApiResponse.success("Size deactivated", null);
    }

    // -------------------------------------------------------------------------
    // Master Data — Attributes
    // -------------------------------------------------------------------------

    @GetMapping("/api/v1/admin/catalog/attributes")
    public ApiResponse<List<Attribute>> listAttributes() {
        return ApiResponse.success("Attributes fetched", s.listAttributes());
    }

    @PostMapping("/api/v1/admin/catalog/attributes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Attribute> createAttribute(@Valid @RequestBody AdminAttributeRequest r) {
        return ApiResponse.success("Attribute created", s.createAttribute(r));
    }

    @PutMapping("/api/v1/admin/catalog/attributes/{id}")
    public ApiResponse<Attribute> updateAttribute(@PathVariable Long id, @Valid @RequestBody AdminAttributeRequest r) {
        return ApiResponse.success("Attribute updated", s.updateAttribute(id, r));
    }

    @GetMapping("/api/v1/admin/catalog/attributes/{id}/values")
    public ApiResponse<List<AttributeValue>> listAttributeValues(@PathVariable Long id) {
        return ApiResponse.success("Attribute values fetched", s.listAttributeValues(id));
    }

    @PostMapping("/api/v1/admin/catalog/attribute-values")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AttributeValue> createAttributeValue(@Valid @RequestBody AdminAttributeValueRequest r) {
        return ApiResponse.success("Attribute value created", s.createAttributeValue(r));
    }

    private final BulkProductUploadService bulkUploadService;

    // -------------------------------------------------------------------------
    // Bulk Product Upload & Template Download
    // -------------------------------------------------------------------------

    @PostMapping("/api/v1/admin/products/bulk-upload")
    public ApiResponse<BulkProductUploadResponse> bulkUploadProducts(@RequestParam("file") MultipartFile file) {
        log.info("Bulk product upload request received: filename={}, size={}", file.getOriginalFilename(), file.getSize());
        BulkProductUploadResponse response = bulkUploadService.processBulkUpload(file);
        return ApiResponse.success("Bulk upload processed successfully", response);
    }

    @GetMapping("/api/v1/admin/products/bulk-template")
    public org.springframework.http.ResponseEntity<byte[]> downloadBulkTemplate() {
        byte[] csvData = bulkUploadService.generateSampleTemplateCsv();
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"vastra_products_bulk_template.csv\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(csvData);
    }

    @DeleteMapping("/api/v1/admin/catalog/attribute-values/{id}")
    public ApiResponse<Void> deleteAttributeValue(@PathVariable Long id) {
        s.deleteAttributeValue(id);
        return ApiResponse.success("Attribute value deleted", null);
    }
}
