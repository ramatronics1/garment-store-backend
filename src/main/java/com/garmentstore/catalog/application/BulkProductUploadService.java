package com.garmentstore.catalog.application;

import com.garmentstore.catalog.domain.*;
import com.garmentstore.catalog.dto.admin.BulkProductRowDTO;
import com.garmentstore.catalog.dto.admin.BulkProductUploadResponse;
import com.garmentstore.catalog.dto.admin.BulkUploadRowError;
import com.garmentstore.catalog.infrastructure.*;
import com.garmentstore.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BulkProductUploadService {

    private static final Logger log = LoggerFactory.getLogger(BulkProductUploadService.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;

    public byte[] generateSampleTemplateCsv() {
        String csvContent = """
product_code,product_name,category_name,gender_tag,brand,description,fabric_details,fit,season,care_instructions,country_of_origin,variant_sku,barcode,color_name,size_code,mrp,selling_price,cost_price,stock_quantity,weight_grams,image_urls
SHRT001,Classic Slim Fit Cotton Shirt,Shirts,MEN,Vastra,100% Breathable Cotton Shirt for casual wear,100% Organic Cotton,Slim Fit,Summer 2026,Machine wash cold,India,SHRT001-BLK-M,8901234567891,Black,M,1999.00,1499.00,800.00,50,350,https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf
SHRT001,Classic Slim Fit Cotton Shirt,Shirts,MEN,Vastra,100% Breathable Cotton Shirt for casual wear,100% Organic Cotton,Slim Fit,Summer 2026,Machine wash cold,India,,8901234567892,Black,L,1999.00,,800.00,30,360,https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf
SHRT001,Classic Slim Fit Cotton Shirt,Shirts,MEN,Vastra,100% Breathable Cotton Shirt for casual wear,100% Organic Cotton,Slim Fit,Summer 2026,Machine wash cold,India,SHRT001-BLU-M,8901234567893,Blue,M,1999.00,1499.00,800.00,25,350,https://images.unsplash.com/photo-1596755094514-f87e34085b2c
""".trim();
        return csvContent.getBytes(StandardCharsets.UTF_8);
    }

    public BulkProductUploadResponse processBulkUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("EMPTY_FILE", "Please select a non-empty CSV or Excel file.", HttpStatus.BAD_REQUEST);
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        List<BulkProductRowDTO> rawRows;

        try {
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                rawRows = parseExcel(file.getInputStream());
            } else if (filename.endsWith(".csv") || file.getContentType() != null && file.getContentType().contains("csv")) {
                rawRows = parseCsv(file.getInputStream());
            } else {
                // Fallback attempt to parse as CSV
                rawRows = parseCsv(file.getInputStream());
            }
        } catch (Exception e) {
            log.error("Failed to parse bulk upload file", e);
            throw new BusinessException("FILE_PARSE_ERROR", "Failed to parse file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (rawRows.isEmpty()) {
            throw new BusinessException("NO_DATA_FOUND", "No data rows found in spreadsheet.", HttpStatus.BAD_REQUEST);
        }

        return validateAndSave(rawRows);
    }

    private List<BulkProductRowDTO> parseCsv(InputStream inputStream) throws Exception {
        List<BulkProductRowDTO> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build())) {

            int rowNum = 2; // Row 1 is header
            for (CSVRecord record : csvParser) {
                BulkProductRowDTO row = new BulkProductRowDTO(
                        rowNum++,
                        getVal(record, "product_code"),
                        getVal(record, "product_name"),
                        getVal(record, "category_name"),
                        getVal(record, "gender_tag"),
                        getVal(record, "brand"),
                        getVal(record, "description"),
                        getVal(record, "fabric_details"),
                        getVal(record, "fit"),
                        getVal(record, "season"),
                        getVal(record, "care_instructions"),
                        getVal(record, "country_of_origin"),
                        getVal(record, "variant_sku"),
                        getVal(record, "barcode"),
                        getVal(record, "color_name"),
                        getVal(record, "size_code"),
                        parseDecimal(getVal(record, "mrp")),
                        parseDecimal(getVal(record, "selling_price")),
                        parseDecimal(getVal(record, "cost_price")),
                        parseInt(getVal(record, "stock_quantity")),
                        parseInt(getVal(record, "weight_grams")),
                        getVal(record, "image_urls")
                );
                rows.add(row);
            }
        }
        return rows;
    }

    private List<BulkProductRowDTO> parseExcel(InputStream inputStream) throws Exception {
        List<BulkProductRowDTO> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) return rows;

            // Read header row
            Row headerRow = rowIterator.next();
            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String headerName = cell.getStringCellValue().trim().toLowerCase().replaceAll("\\s+", "_");
                headerMap.put(headerName, cell.getColumnIndex());
            }

            int rowNum = 2;
            DataFormatter formatter = new DataFormatter();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) continue;

                BulkProductRowDTO dto = new BulkProductRowDTO(
                        rowNum++,
                        getCellVal(row, headerMap, "product_code", formatter),
                        getCellVal(row, headerMap, "product_name", formatter),
                        getCellVal(row, headerMap, "category_name", formatter),
                        getCellVal(row, headerMap, "gender_tag", formatter),
                        getCellVal(row, headerMap, "brand", formatter),
                        getCellVal(row, headerMap, "description", formatter),
                        getCellVal(row, headerMap, "fabric_details", formatter),
                        getCellVal(row, headerMap, "fit", formatter),
                        getCellVal(row, headerMap, "season", formatter),
                        getCellVal(row, headerMap, "care_instructions", formatter),
                        getCellVal(row, headerMap, "country_of_origin", formatter),
                        getCellVal(row, headerMap, "variant_sku", formatter),
                        getCellVal(row, headerMap, "barcode", formatter),
                        getCellVal(row, headerMap, "color_name", formatter),
                        getCellVal(row, headerMap, "size_code", formatter),
                        parseDecimal(getCellVal(row, headerMap, "mrp", formatter)),
                        parseDecimal(getCellVal(row, headerMap, "selling_price", formatter)),
                        parseDecimal(getCellVal(row, headerMap, "cost_price", formatter)),
                        parseInt(getCellVal(row, headerMap, "stock_quantity", formatter)),
                        parseInt(getCellVal(row, headerMap, "weight_grams", formatter)),
                        getCellVal(row, headerMap, "image_urls", formatter)
                );
                rows.add(dto);
            }
        }
        return rows;
    }

    @Transactional
    public BulkProductUploadResponse validateAndSave(List<BulkProductRowDTO> rawRows) {
        List<BulkUploadRowError> errors = new ArrayList<>();
        Set<String> processedSkusInBatch = new HashSet<>();
        Set<String> processedCombinationsInBatch = new HashSet<>();

        // Pre-fetch Master Caches
        Map<String, Category> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(c -> c.getName().trim().toLowerCase(), c -> c, (c1, c2) -> c1));
        
        Map<String, Color> colorMap = colorRepository.findAll().stream()
                .collect(Collectors.toMap(c -> c.getName().trim().toLowerCase(), c -> c, (c1, c2) -> c1));
        
        Map<String, Size> sizeMap = sizeRepository.findAll().stream()
                .collect(Collectors.toMap(s -> s.getName().trim().toLowerCase(), s -> s, (s1, s2) -> s1));

        // Group rows by Product Code (or Product Name if code missing)
        Map<String, List<BulkProductRowDTO>> productGroups = new LinkedHashMap<>();
        for (BulkProductRowDTO row : rawRows) {
            String pCode = row.productCode() != null && !row.productCode().isBlank() 
                    ? row.productCode().trim().toUpperCase() 
                    : (row.productName() != null ? slugify(row.productName().trim()) : "ROW_" + row.rowNumber());
            productGroups.computeIfAbsent(pCode, k -> new ArrayList<>()).add(row);
        }

        int totalRowsProcessed = rawRows.size();
        int productsCreatedCount = 0;
        int variantsCreatedCount = 0;

        for (Map.Entry<String, List<BulkProductRowDTO>> entry : productGroups.entrySet()) {
            String pCode = entry.getKey();
            List<BulkProductRowDTO> rows = entry.getValue();

            // Validate parent info from the first row
            BulkProductRowDTO firstRow = rows.get(0);

            // Syntactic Checks for Parent
            boolean productHasErrors = false;

            if (isBlank(firstRow.productName())) {
                errors.add(new BulkUploadRowError(firstRow.rowNumber(), pCode, "", "product_name", "Product Name is required"));
                productHasErrors = true;
            }
            if (isBlank(firstRow.categoryName())) {
                errors.add(new BulkUploadRowError(firstRow.rowNumber(), pCode, "", "category_name", "Category Name is required"));
                productHasErrors = true;
            }
            if (isBlank(firstRow.genderTag())) {
                errors.add(new BulkUploadRowError(firstRow.rowNumber(), pCode, "", "gender_tag", "Gender Tag is required"));
                productHasErrors = true;
            }

            Category category = null;
            if (!isBlank(firstRow.categoryName())) {
                category = categoryMap.get(firstRow.categoryName().trim().toLowerCase());
                if (category == null) {
                    errors.add(new BulkUploadRowError(firstRow.rowNumber(), pCode, "", "category_name", 
                            "Category '" + firstRow.categoryName() + "' not found in master catalog."));
                    productHasErrors = true;
                }
            }

            GenderTag genderTag = null;
            if (!isBlank(firstRow.genderTag())) {
                try {
                    genderTag = GenderTag.valueOf(firstRow.genderTag().trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    errors.add(new BulkUploadRowError(firstRow.rowNumber(), pCode, "", "gender_tag", 
                            "Invalid Gender Tag '" + firstRow.genderTag() + "'. Allowed: MEN, WOMEN, UNISEX, BOYS, GIRLS"));
                    productHasErrors = true;
                }
            }

            if (productHasErrors) {
                // Skip creating product if parent validation failed
                continue;
            }

            // Validate each variant row for this product
            List<BulkProductRowDTO> validVariantRows = new ArrayList<>();

            for (BulkProductRowDTO vRow : rows) {
                boolean rowHasErrors = false;

                // Color check
                if (isBlank(vRow.colorName())) {
                    errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, vRow.variantSku(), "color_name", "Color Name is required"));
                    rowHasErrors = true;
                } else if (!colorMap.containsKey(vRow.colorName().trim().toLowerCase())) {
                    errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, vRow.variantSku(), "color_name", 
                            "Color '" + vRow.colorName() + "' not found in master catalog."));
                    rowHasErrors = true;
                }

                // Size check
                if (isBlank(vRow.sizeCode())) {
                    errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, vRow.variantSku(), "size_code", "Size Code is required"));
                    rowHasErrors = true;
                } else if (!sizeMap.containsKey(vRow.sizeCode().trim().toLowerCase())) {
                    errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, vRow.variantSku(), "size_code", 
                            "Size '" + vRow.sizeCode() + "' not found in master catalog."));
                    rowHasErrors = true;
                }

                // SKU check (Use provided SKU or auto-derive as PRODUCT_CODE-COLOR-SIZE)
                String effectiveSku = null;
                if (!isBlank(vRow.variantSku())) {
                    effectiveSku = vRow.variantSku().trim().toUpperCase();
                } else if (!isBlank(vRow.colorName()) && !isBlank(vRow.sizeCode())) {
                    String colorTag = vRow.colorName().trim().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                    String sizeTag = vRow.sizeCode().trim().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                    effectiveSku = pCode + "-" + colorTag + "-" + sizeTag;
                }

                if (isBlank(effectiveSku)) {
                    errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, "", "variant_sku", "Variant SKU could not be derived (Variant SKU, Color, or Size missing)."));
                    rowHasErrors = true;
                } else {
                    if (!processedSkusInBatch.add(effectiveSku)) {
                        errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, effectiveSku, "variant_sku", "Duplicate SKU '" + effectiveSku + "' within spreadsheet."));
                        rowHasErrors = true;
                    } else if (variantRepository.existsBySku(effectiveSku)) {
                        errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, effectiveSku, "variant_sku", "SKU '" + effectiveSku + "' already exists in database."));
                        rowHasErrors = true;
                    }
                }

                if (vRow.mrp() == null || vRow.mrp().compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, effectiveSku != null ? effectiveSku : "", "mrp", "MRP must be greater than 0"));
                    rowHasErrors = true;
                }

                // Selling Price check (Defaults to MRP if omitted)
                BigDecimal effectiveSellingPrice = vRow.sellingPrice();
                if (effectiveSellingPrice == null && vRow.mrp() != null) {
                    effectiveSellingPrice = vRow.mrp();
                }

                if (effectiveSellingPrice == null || effectiveSellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, effectiveSku != null ? effectiveSku : "", "selling_price", "Selling Price must be greater than 0"));
                    rowHasErrors = true;
                } else if (vRow.mrp() != null && effectiveSellingPrice.compareTo(vRow.mrp()) > 0) {
                    errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, effectiveSku != null ? effectiveSku : "", "selling_price", "Selling Price cannot be greater than MRP"));
                    rowHasErrors = true;
                }

                if (vRow.stockQuantity() == null || vRow.stockQuantity() < 0) {
                    errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, effectiveSku != null ? effectiveSku : "", "stock_quantity", "Stock Quantity cannot be negative"));
                    rowHasErrors = true;
                }

                // Combination key check (Color + Size combination uniqueness per product)
                if (!isBlank(vRow.colorName()) && !isBlank(vRow.sizeCode()) 
                        && colorMap.containsKey(vRow.colorName().trim().toLowerCase()) 
                        && sizeMap.containsKey(vRow.sizeCode().trim().toLowerCase())) {
                    
                    Color color = colorMap.get(vRow.colorName().trim().toLowerCase());
                    Size size = sizeMap.get(vRow.sizeCode().trim().toLowerCase());
                    String comboKey = pCode + ":" + ProductVariant.buildCombinationKey(color.getId(), size.getId());

                    if (!processedCombinationsInBatch.add(comboKey)) {
                        errors.add(new BulkUploadRowError(vRow.rowNumber(), pCode, effectiveSku != null ? effectiveSku : "", "combination", 
                                "Duplicate Color (" + color.getName() + ") + Size (" + size.getName() + ") combination for product " + pCode));
                        rowHasErrors = true;
                    }
                }

                if (!rowHasErrors) {
                    validVariantRows.add(vRow);
                }
            }

            if (validVariantRows.isEmpty()) {
                continue; // Skip creating product if no valid variants exist
            }

            // Create Product
            String baseSlug = slugify(firstRow.productName());
            String finalSlug = baseSlug;
            int counter = 1;
            while (productRepository.existsBySlug(finalSlug)) {
                finalSlug = baseSlug + "-" + counter++;
            }

            Product product = Product.builder()
                    .productCode(pCode)
                    .name(firstRow.productName().trim())
                    .slug(finalSlug)
                    .category(category)
                    .genderTag(genderTag)
                    .brand(cleanString(firstRow.brand()))
                    .description(cleanString(firstRow.description()))
                    .fabricDetails(cleanString(firstRow.fabricDetails()))
                    .fit(cleanString(firstRow.fit()))
                    .season(cleanString(firstRow.season()))
                    .careInstructions(cleanString(firstRow.careInstructions()))
                    .countryOfOrigin(cleanString(firstRow.countryOfOrigin()))
                    .returnPolicyEnabled(true)
                    .status(ProductStatus.ACTIVE)
                    .build();

            product = productRepository.save(product);
            productsCreatedCount++;

            // Process Images
            if (!isBlank(firstRow.imageUrls())) {
                String[] urls = firstRow.imageUrls().split(",");
                int displayOrder = 0;
                for (String url : urls) {
                    if (!url.trim().isBlank()) {
                        ProductImage img = ProductImage.builder()
                                .product(product)
                                .mediaUrl(url.trim())
                                .displayOrder(displayOrder)
                                .thumbnail(displayOrder == 0)
                                .build();
                        imageRepository.save(img);
                        displayOrder++;
                    }
                }
            }

            // Process Variants
            for (BulkProductRowDTO vRow : validVariantRows) {
                Color color = colorMap.get(vRow.colorName().trim().toLowerCase());
                Size size = sizeMap.get(vRow.sizeCode().trim().toLowerCase());
                
                String sku = !isBlank(vRow.variantSku())
                        ? vRow.variantSku().trim().toUpperCase()
                        : (pCode + "-" + vRow.colorName().trim().replaceAll("[^a-zA-Z0-9]", "").toUpperCase() + "-" + vRow.sizeCode().trim().replaceAll("[^a-zA-Z0-9]", "").toUpperCase());

                BigDecimal sellingPrice = vRow.sellingPrice() != null ? vRow.sellingPrice() : vRow.mrp();

                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .color(color)
                        .size(size)
                        .sku(sku)
                        .barcode(cleanString(vRow.barcode()))
                        .mrp(vRow.mrp())
                        .sellingPrice(sellingPrice)
                        .costPrice(vRow.costPrice())
                        .stockQuantity(vRow.stockQuantity())
                        .weightGrams(vRow.weightGrams())
                        .combinationKey(ProductVariant.buildCombinationKey(color.getId(), size.getId()))
                        .status(VariantStatus.ACTIVE)
                        .build();

                variantRepository.save(variant);
                variantsCreatedCount++;
            }
        }

        return new BulkProductUploadResponse(
                totalRowsProcessed,
                productsCreatedCount,
                variantsCreatedCount,
                errors.size(),
                errors
        );
    }

    // --- Helper Parsing Methods ---

    private String getVal(CSVRecord record, String column) {
        try {
            if (record.isMapped(column)) {
                String val = record.get(column);
                return val != null ? val.trim() : null;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getCellVal(Row row, Map<String, Integer> headerMap, String key, DataFormatter formatter) {
        Integer colIdx = headerMap.get(key);
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        String val = formatter.formatCellValue(cell);
        return val != null ? val.trim() : null;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private BigDecimal parseDecimal(String str) {
        if (isBlank(str)) return null;
        try {
            return new BigDecimal(str.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInt(String str) {
        if (isBlank(str)) return null;
        try {
            return Integer.parseInt(str.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String cleanString(String s) {
        return isBlank(s) ? null : s.trim();
    }

    private String slugify(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
