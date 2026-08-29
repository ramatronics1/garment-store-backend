package com.garmentstore.catalog.application;

import com.garmentstore.catalog.dto.DeliveryCheckResponse;
import com.garmentstore.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pincode / Delivery Check Service.
 *
 * Business Rule: Delivery is only available within Karnataka, India.
 * Uses the free api.postalpincode.in to resolve pincode → state.
 * Results are cached in-memory (bounded ConcurrentHashMap) to avoid
 * hammering the external API on repeated checks.
 *
 * Delivery ETA logic:
 *   Bengaluru (560xxx) → 3 business days
 *   Rest of Karnataka → 5 business days
 */
@Slf4j
@Service
public class PincodeService {

    private static final String KARNATAKA = "Karnataka";
    private static final String POSTAL_API = "https://api.postalpincode.in/pincode/";

    // Simple bounded cache: pincode → resolved state name (or "ERROR")
    private final Map<String, String> stateCache = new ConcurrentHashMap<>(512);

    private final RestClient restClient;

    public PincodeService() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);

        this.restClient = RestClient.builder()
                .baseUrl(POSTAL_API)
                .requestFactory(factory)
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    /**
     * Check delivery availability for a given 6-digit pincode.
     *
     * @param pincode 6-digit Indian postal code
     * @return DeliveryCheckResponse with availability, COD, ETA details
     */
    public DeliveryCheckResponse checkDelivery(String pincode) {
        // ── Format validation ─────────────────────────────────────────────────
        if (pincode == null || !pincode.matches("^[1-9][0-9]{5}$")) {
            throw new BusinessException("INVALID_PINCODE", "Please enter a valid 6-digit pincode", HttpStatus.BAD_REQUEST);
        }

        // ── Resolve state (cached) ─────────────────────────────────────────────
        String state = stateCache.computeIfAbsent(pincode, this::resolveState);

        // ── Business rule: Karnataka only ──────────────────────────────────────
        if ("ERROR".equals(state) || "NOT_FOUND".equals(state)) {
            return new DeliveryCheckResponse(pincode, false, false, null, null,
                    "Could not verify delivery for this pincode. Please try again.");
        }

        if (!KARNATAKA.equalsIgnoreCase(state)) {
            return new DeliveryCheckResponse(pincode, false, false, null, state,
                    "Sorry, we currently deliver only within Karnataka. We're expanding soon!");
        }

        // ── Karnataka delivery is available ───────────────────────────────────
        int etaDays = pincode.startsWith("560") ? 3 : 5;  // Bengaluru vs rest of KA
        String eta = LocalDate.now().plusDays(etaDays)
                .format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH));

        return new DeliveryCheckResponse(pincode, true, true, eta, state,
                "Delivery available by " + eta);
    }

    /**
     * Calls api.postalpincode.in and extracts the state name.
     * Returns "NOT_FOUND" if the pincode is not in the postal database.
     * Returns "ERROR" if the external API is unavailable (graceful degradation).
     *
     * Response shape from api.postalpincode.in:
     * [{
     *   "Status": "Success",
     *   "PostOffice": [{ "State": "Karnataka", "District": "...", "Name": "..." }]
     * }]
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private String resolveState(String pincode) {
        try {
            List response = restClient.get()
                    .uri(pincode)
                    .retrieve()
                    .body(List.class);

            if (response == null || response.isEmpty()) return "NOT_FOUND";

            Map<String, Object> first = (Map<String, Object>) response.get(0);
            String status = (String) first.get("Status");

            if (!"Success".equals(status)) return "NOT_FOUND";

            List<Map<String, Object>> postOffices = (List<Map<String, Object>>) first.get("PostOffice");
            if (postOffices == null || postOffices.isEmpty()) return "NOT_FOUND";

            String state = (String) postOffices.get(0).get("State");
            return state != null ? state : "NOT_FOUND";

        } catch (RestClientException e) {
            log.warn("[PincodeService] External postal API unavailable for pincode={}: {}", pincode, e.getMessage());
            return "ERROR";
        } catch (Exception e) {
            log.error("[PincodeService] Unexpected error resolving pincode={}", pincode, e);
            return "ERROR";
        }
    }
}
