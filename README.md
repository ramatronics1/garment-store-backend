# Garment Store Backend — Phase 0 + Phase 1 + Phase 2

Production-grade Spring Boot modular monolith foundation with Auth foundation and customer registration with OTP verification.

## Included

- Spring Boot 3, Java 21, Maven
- MySQL + Flyway migrations
- Docker Compose for local MySQL
- Global response and exception handling
- Swagger/OpenAPI and Actuator health
- Spring Security stateless setup
- JWT service foundation and JWT authentication filter
- Auth entities: User, Role, RefreshToken, LoginAttempt, OtpToken
- Registration APIs with OTP verification
- Role seed data: CUSTOMER, ADMIN, SUPER_ADMIN

## Run locally

```bash
docker compose up -d
mvn spring-boot:run
```

## Phase 2 APIs

```http
POST /api/v1/auth/register
POST /api/v1/auth/verify-otp
```

Register:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register       -H "Content-Type: application/json"       -d '{"name":"Ayush Customer","email":"ayush.customer@example.com","mobile":"9876543210","password":"Password@123"}'
```

Verify OTP using `devOtp` from the local response:

```bash
curl -X POST http://localhost:8080/api/v1/auth/verify-otp       -H "Content-Type: application/json"       -d '{"userId":1,"otp":"123456"}'
```

For production, set `OTP_EXPOSE_DEV_OTP=false`.

## Next phase

Phase 3 added login, JWT access token issuing, refresh token generation/rotation, login attempt logging, and `/api/v1/auth/me`.


    ## Phase 3 APIs

    ```http
    POST /api/v1/auth/login
    POST /api/v1/auth/refresh-token
    GET  /api/v1/auth/me
    ```

    Login:

    ```bash
    curl -X POST http://localhost:8080/api/v1/auth/login       -H "Content-Type: application/json"       -d '{"identifier":"ayush.customer@example.com","password":"Password@123"}'
    ```

    Refresh token:

    ```bash
    curl -X POST http://localhost:8080/api/v1/auth/refresh-token       -H "Content-Type: application/json"       -d '{"refreshToken":"<refresh-token>"}'
    ```

    Current user:

    ```bash
    curl http://localhost:8080/api/v1/auth/me       -H "Authorization: Bearer <access-token>"
    ```

    ## Next phase

    Phase 4 will add logout, logout-all, refresh-token revocation APIs, and token reuse-detection foundation.


    ## Phase 4 APIs

    ```http
    POST /api/v1/auth/logout
    POST /api/v1/auth/logout-all
    ```

    Logout current session:

    ```bash
    curl -X POST http://localhost:8080/api/v1/auth/logout       -H "Content-Type: application/json"       -d '{"refreshToken":"<refresh-token>"}'
    ```

    Logout all sessions:

    ```bash
    curl -X POST http://localhost:8080/api/v1/auth/logout-all       -H "Authorization: Bearer <access-token>"
    ```

    Phase 4 also adds refresh-token reuse detection foundation. If a previously rotated refresh token is reused, active sessions for that user are revoked.

    ## Next phase

    Phase 5 will add forgot password and reset password flow.


    ## Phase 5 APIs

    ```http
    POST /api/v1/auth/forgot-password
    POST /api/v1/auth/reset-password
    ```

    Forgot password:

    ```bash
    curl -X POST http://localhost:8080/api/v1/auth/forgot-password       -H "Content-Type: application/json"       -d '{"identifier":"customer@example.com"}'
    ```

    Reset password:

    ```bash
    curl -X POST http://localhost:8080/api/v1/auth/reset-password       -H "Content-Type: application/json"       -d '{"resetToken":"<reset-token>","newPassword":"NewPassword@123"}'
    ```

    For production, set:

    ```text
    PASSWORD_RESET_EXPOSE_DEV_TOKEN=false
    ```

    Phase 5 revokes active refresh tokens after a successful password reset.

    ## Next phase

    Phase 6 will add guest session identity foundation for guest cart and guest checkout.


    ## Phase 6 API

    ```http
    POST /api/v1/auth/guest-session
    ```

    Create guest session:

    ```bash
    curl -X POST http://localhost:8080/api/v1/auth/guest-session       -H "Content-Type: application/json"       -d '{}'
    ```

    Guest session with optional identity:

    ```bash
    curl -X POST http://localhost:8080/api/v1/auth/guest-session       -H "Content-Type: application/json"       -d '{"email":"guest@example.com","mobile":"9876543210"}'
    ```

    Guest sessions are stored as hashes in the database. The raw `guestSessionId` should be stored by the client and passed later to cart/checkout APIs.

    ## Next phase

    Phase 7 will add admin login and admin lockout after failed attempts.


    ## Phase 7 APIs

    ```http
    POST /api/v1/admin/auth/login
    POST /api/v1/admin/auth/refresh-token
    POST /api/v1/admin/auth/logout
    POST /api/v1/admin/auth/logout-all
    GET  /api/v1/admin/auth/me
    ```

    Local bootstrap admin:

    ```text
    email: admin@garmentstore.local
    password: Admin@12345
    ```

    Admin login:

    ```bash
    curl -X POST http://localhost:8080/api/v1/admin/auth/login       -H "Content-Type: application/json"       -d '{"identifier":"admin@garmentstore.local","password":"Admin@12345"}'
    ```

    Admin account locks after five failed login attempts. Set `ADMIN_BOOTSTRAP_ENABLED=false` in production after creating the first admin.

    ## Next phase

    Phase 8 will add auth hardening: security headers, CORS cleanup, audit events, endpoint authorization polish, and integration test expansion.


    ## Phase 8 — Auth Hardening

    Added production hardening for the auth module:

    - JSON 401/403 responses
    - Security headers
    - Correlation ID support
    - CORS configuration cleanup
    - Audit logging foundation
    - Admin method-level authorization polish
    - Hardening integration tests

    Useful hardening test:

    ```bash
    curl http://localhost:8080/api/v1/auth/me -H "X-Correlation-Id: local-test-id"
    ```

    Expected: JSON `AUTHENTICATION_REQUIRED` response and `X-Correlation-Id` response header.

    ## Next phase

    Auth module foundation is now complete. Next recommended backend phase is Customer Profile + Address module.
