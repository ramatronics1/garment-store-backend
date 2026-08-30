package com.garmentstore.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CustomerAccountActionResponse — returned after a successful account action.
 *
 * Includes the updated customer row so the UI can refresh in-place without
 * re-fetching the whole page.
 */
public record CustomerAccountActionResponse(

        @JsonProperty("user_id")
        Long userId,

        @JsonProperty("account_status")
        String accountStatus,

        @JsonProperty("sessions_revoked")
        int sessionsRevoked,

        String message
) {}
