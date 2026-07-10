# Phase 3 — Login, JWT, Refresh Token, Me

## Endpoints

```http
POST /api/v1/auth/login
POST /api/v1/auth/refresh-token
GET  /api/v1/auth/me
```

## Login request

```json
{
  "identifier": "customer@example.com",
  "password": "Password@123"
}
```

`identifier` supports email or mobile.

## Refresh token request

```json
{
  "refreshToken": "raw-refresh-token"
}
```

## Covered scenarios

- Login using email
- Login using mobile
- Invalid identifier/password returns generic invalid credentials
- Pending verification accounts cannot login
- Locked/non-active accounts cannot login
- Login attempts are logged for success/failure
- JWT access token is issued
- Refresh token is generated and stored as SHA-256 hash
- Refresh token rotation revokes old token and creates a new token
- Expired/inactive refresh tokens are rejected
- `/me` returns authenticated user profile summary
