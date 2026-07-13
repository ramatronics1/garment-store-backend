package com.garmentstore.catalog.api;

import com.garmentstore.catalog.application.PincodeService;
import com.garmentstore.catalog.dto.DeliveryCheckResponse;
import com.garmentstore.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Public delivery check API.
 *
 * GET /api/v1/delivery/check?pincode=560001
 *
 * No authentication required — this is shown on the product detail page
 * before the user logs in.
 *
 * Business rule: Delivery only within Karnataka, India.
 * External data: api.postalpincode.in (free, no API key needed).
 */
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final PincodeService pincodeService;

    @GetMapping("/check")
    public ApiResponse<DeliveryCheckResponse> check(@RequestParam String pincode) {
        DeliveryCheckResponse result = pincodeService.checkDelivery(pincode.trim());
        String message = result.available()
                ? "Delivery available"
                : "Delivery not available for this pincode";
        return ApiResponse.success(message, result);
    }
}
