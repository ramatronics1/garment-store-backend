package com.garmentstore.catalog.api;

import com.garmentstore.catalog.application.CatalogService;
import com.garmentstore.catalog.dto.SaleConfigResponse;
import com.garmentstore.catalog.dto.StoreMediaResponse;
import com.garmentstore.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public (no-auth) endpoints for storefront media:
 *   - Hero banner slides
 *   - Individual placement slots
 *   - Sale configuration (frontend reads this to show/hide the sale banner)
 */
@RestController
@RequestMapping("/api/v1/store-media")
@RequiredArgsConstructor
public class StoreMediaController {

    private final CatalogService s;

    /**
     * GET /api/v1/store-media/hero-slides
     * Returns all active hero_banner_* slots ordered by display_order.
     */
    @GetMapping("/hero-slides")
    public ApiResponse<List<StoreMediaResponse>> heroSlides() {
        return ApiResponse.success("Hero slides fetched", s.getHeroSlides());
    }

    /**
     * GET /api/v1/store-media/{key}
     * Returns a single active slot by placement key (e.g. "category_hero_WOMEN").
     * Returns 200 with null data if the slot is missing or inactive (not a 404).
     */
    @GetMapping("/{key}")
    public ApiResponse<StoreMediaResponse> one(@PathVariable String key) {
        return ApiResponse.success("Store media fetched", s.getStoreMedia(key));
    }

    /**
     * GET /api/v1/store-media/sale-config
     * Returns the current sale config.
     * When {@code active = false} the frontend hides the sale banner entirely.
     */
    @GetMapping("/sale-config")
    public ApiResponse<SaleConfigResponse> saleConfig() {
        return ApiResponse.success("Sale config fetched", s.getSaleConfig());
    }
}
