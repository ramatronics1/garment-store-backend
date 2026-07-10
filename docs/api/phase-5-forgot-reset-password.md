# Phase 5 — Forgot Password + Reset Password

## Endpoints

```http
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
```

## Forgot password request

```json
{
  "identifier": "customer@example.com"
}
```

The response is intentionally generic to reduce user enumeration risk.

Local/dev response includes `devResetToken` only when `PASSWORD_RESET_EXPOSE_DEV_TOKEN=true`.

## Reset password request

```json
{
  "resetToken": "raw-reset-token",
  "newPassword": "NewPassword@123"
}
```

## Implemented scenarios

- Forgot password by email
- Forgot password by mobile
- Generic response for unknown accounts
- Reset token generated using secure random bytes
- Reset token stored as SHA-256 hash
- Previous active reset tokens revoked before issuing a new one
- Reset token expiry
- Reset token single-use status transition: `ACTIVE -> USED`
- Expired token status transition: `ACTIVE -> EXPIRED`
- New password strength validation
- Old password reuse rejection
- All active refresh tokens revoked after successful password reset
