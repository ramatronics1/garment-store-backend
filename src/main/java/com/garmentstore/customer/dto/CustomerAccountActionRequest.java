package com.garmentstore.customer.dto;

import jakarta.validation.constraints.NotNull;

/**
 * CustomerAccountActionRequest — body for PATCH /api/v1/admin/customers/{userId}/status
 *
 * action:
 *   BAN     — sets status to LOCKED, revokes all sessions
 *   UNBAN   — sets status to ACTIVE (only if currently LOCKED)
 *   DISABLE — sets status to DISABLED, revokes all sessions (soft-delete; non-reversible via this endpoint)
 *
 * reason is mandatory for BAN and DISABLE, optional for UNBAN.
 */
public record CustomerAccountActionRequest(

        @NotNull(message = "action is required")
        Action action,

        String reason
) {
    public enum Action { BAN, UNBAN, DISABLE }
}
