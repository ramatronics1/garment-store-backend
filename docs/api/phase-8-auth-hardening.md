# Phase 8 — Auth Hardening

## Added

- JSON authentication entry point for unauthenticated requests
- JSON access-denied handler for forbidden requests
- Security headers:
  - Content Security Policy
  - Frame options deny
  - Referrer policy no-referrer
  - Permissions policy disabling camera/microphone/geolocation
- CORS now uses `app.security.cors.allowed-origins`
- Correlation ID filter using `X-Correlation-Id`
- Audit log JPA entity/repository/service
- Audit events for login, admin lockout, logout, logout-all, and password reset
- Method-level authorization on admin `/me` and `logout-all`
- Expanded hardening integration tests

## Notes

The existing `audit_logs` table from Phase 1 is now mapped with JPA and used by the auth module.
