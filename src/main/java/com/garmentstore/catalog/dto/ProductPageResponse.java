package com.garmentstore.catalog.dto;

import java.util.List;

/**
 * UI-aligned product listing page response.
 *
 * Field names exactly match what the UI's useProductListing.js hook destructures:
 *   const { products, total, totalPages, perPage } = response.data;
 *
 * Separate from the generic PageResponse<T> (which uses JPA field names like
 * content/totalElements/size) so both can coexist without breaking admin APIs.
 */
public record ProductPageResponse(
        List<ProductSummaryResponse> products,   // the page of product cards
        long total,                               // total matching products (for "X products found")
        int page,                                 // current page (1-based, matches UI)
        int perPage,                              // items per page (always 12)
        int totalPages                            // total pages (for pagination component)
) {}
