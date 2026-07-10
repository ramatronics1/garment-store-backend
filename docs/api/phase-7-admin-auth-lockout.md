# Phase 7 — Admin Login + Admin Lockout

## Endpoints

```http
POST /api/v1/admin/auth/login
POST /api/v1/admin/auth/refresh-token
POST /api/v1/admin/auth/logout
POST /api/v1/admin/auth/logout-all
GET  /api/v1/admin/auth/me
```

## Local bootstrap admin

Local/dev bootstraps an admin user when `ADMIN_BOOTSTRAP_ENABLED=true`:

```text
email: admin@garmentstore.local
mobile: 9999999999
password: Admin@12345
role: ADMIN
```

Disable this in production after initial setup.

## Admin lockout

Admin login records `ADMIN_LOGIN` attempts in `login_attempts`. After five failed attempts since the last successful admin login, the account status changes to `LOCKED`.

## Implemented scenarios

- Admin login with email/mobile
- Admin access restricted to `UserType.ADMIN` plus `ADMIN` or `SUPER_ADMIN` role
- Customer users cannot use admin login
- Admin refresh token flow
- Admin logout and logout-all
- Admin `/me`
- Lockout after failed attempts
