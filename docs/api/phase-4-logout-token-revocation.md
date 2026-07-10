# Phase 4 — Logout, Logout-All, Token Revocation

## Endpoints

```http
POST /api/v1/auth/logout
POST /api/v1/auth/logout-all
```

## Logout current session

```json
{
  "refreshToken": "raw-refresh-token"
}
```

Revokes the matching active refresh token by hashing the provided token and updating the stored token status to `REVOKED`.

## Logout all sessions

Requires access token authentication.

Revokes all active refresh tokens for the authenticated user.

## Token reuse detection foundation

If a refresh token that was already rotated is reused, all active refresh tokens for that user are revoked and the API returns `REFRESH_TOKEN_REUSE_DETECTED`.

## Covered scenarios

- Logout revokes current refresh token
- Logout with invalid refresh token returns unauthorized
- Logout-all revokes every active refresh token for current user
- Reusing revoked rotated refresh token revokes all sessions
- Already revoked logout token returns success with `revokedTokenCount = 0`
