package com.garmentstore.catalog.application;

import com.garmentstore.catalog.domain.Category;
import com.garmentstore.catalog.domain.Color;
import com.garmentstore.catalog.domain.GenderTag;
import com.garmentstore.catalog.domain.Size;
import com.garmentstore.catalog.dto.admin.BulkProductUploadResponse;
import com.garmentstore.catalog.infrastructure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkProductUploadServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private ProductImageRepository imageRepository;
    @Mock private ColorRepository colorRepository;
    @Mock private SizeRepository sizeRepository;

    @InjectMocks
    private BulkProductUploadService bulkProductUploadService;

    @BeforeEach
    void setUp() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Shirts");

        Color color = new Color();
        color.setId(1L);
        color.setName("Black");

        Size size = new Size();
        size.setId(1L);
        size.setName("M");

        lenient().when(categoryRepository.findAll()).thenReturn(List.of(cat));
        lenient().when(colorRepository.findAll()).thenReturn(List.of(color));
        lenient().when(sizeRepository.findAll()).thenReturn(List.of(size));
    }

    @Test
    @DisplayName("Should generate template CSV byte content")
    void testGenerateSampleTemplateCsv() {
        byte[] csv = bulkProductUploadService.generateSampleTemplateCsv();
        assertNotNull(csv);
        String text = new String(csv, StandardCharsets.UTF_8);
        assertTrue(text.contains("product_code,product_name,category_name,gender_tag"));
        assertTrue(text.contains("SHRT001"));
    }

    @Test
    @DisplayName("Should parse and process valid bulk CSV file")
    void testProcessValidBulkUpload() {
        String csvData = """
product_code,product_name,category_name,gender_tag,brand,description,fabric_details,fit,season,care_instructions,country_of_origin,variant_sku,barcode,color_name,size_code,mrp,selling_price,cost_price,stock_quantity,weight_grams,image_urls
TSHRT001,Cotton Crew T-Shirt,Shirts,MEN,Vastra,Desc,100% Cotton,Regular,Summer,Wash,India,TSHRT001-BLK-M,123456,Black,M,999.00,799.00,400.00,100,200,https://cdn.example.com/1.jpg
""".trim();

        MockMultipartFile file = new MockMultipartFile("file", "products.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));

        when(productRepository.existsBySlug(any())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BulkProductUploadResponse response = bulkProductUploadService.processBulkUpload(file);

        assertNotNull(response);
        assertEquals(1, response.totalRowsProcessed());
        assertEquals(1, response.productsCreated());
        assertEquals(1, response.variantsCreated());
        assertEquals(0, response.failedRowsCount());
    }

    @Test
    @DisplayName("Should collect validation error when category or size is invalid")
    void testProcessInvalidCategoryAndSize() {
        String csvData = """
product_code,product_name,category_name,gender_tag,brand,description,fabric_details,fit,season,care_instructions,country_of_origin,variant_sku,barcode,color_name,size_code,mrp,selling_price,cost_price,stock_quantity,weight_grams,image_urls
TSHRT002,Cotton Crew T-Shirt,NonExistentCategory,MEN,Vastra,Desc,100% Cotton,Regular,Summer,Wash,India,TSHRT002-BLK-XXL,123456,Black,XXL,999.00,1200.00,400.00,100,200,https://cdn.example.com/1.jpg
""".trim();

        MockMultipartFile file = new MockMultipartFile("file", "invalid_products.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));

        BulkProductUploadResponse response = bulkProductUploadService.processBulkUpload(file);

        assertNotNull(response);
        assertEquals(1, response.totalRowsProcessed());
        assertEquals(0, response.productsCreated());
        assertTrue(response.failedRowsCount() > 0);
    }

    @Test
    @DisplayName("Should auto-derive SKU and default selling_price to MRP when left blank in CSV")
    void testProcessBulkUploadWithDerivedSkuAndSellingPrice() {
        String csvData = """
product_code,product_name,category_name,gender_tag,brand,description,fabric_details,fit,season,care_instructions,country_of_origin,variant_sku,barcode,color_name,size_code,mrp,selling_price,cost_price,stock_quantity,weight_grams,image_urls
TSHRT003,Cotton Crew T-Shirt,Shirts,MEN,Vastra,Desc,100% Cotton,Regular,Summer,Wash,India,,123456,Black,M,999.00,,400.00,100,200,https://cdn.example.com/1.jpg
""".trim();

        MockMultipartFile file = new MockMultipartFile("file", "derived_products.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));

        when(productRepository.existsBySlug(any())).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BulkProductUploadResponse response = bulkProductUploadService.processBulkUpload(file);

        assertNotNull(response);
        assertEquals(1, response.totalRowsProcessed());
        assertEquals(1, response.productsCreated());
        assertEquals(1, response.variantsCreated());
        assertEquals(0, response.failedRowsCount());

        verify(variantRepository).save(argThat(variant -> 
            "TSHRT003-BLACK-M".equals(variant.getSku()) && 
            variant.getSellingPrice().compareTo(new java.math.BigDecimal("999.00")) == 0
        ));
    }
}

