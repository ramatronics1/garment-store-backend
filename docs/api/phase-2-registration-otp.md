# Phase 2 — Registration and OTP APIs

## Endpoints

```http
POST /api/v1/auth/register
POST /api/v1/auth/verify-otp
```

## Covered scenarios

- Valid registration
- Email-only registration
- Mobile-only registration
- Email + mobile registration
- Duplicate email
- Duplicate mobile
- Weak password validation
- Invalid mobile validation
- Invalid email validation
- Missing email/mobile validation
- Valid OTP verification
- Invalid OTP
- Expired OTP
- Maximum OTP attempts exceeded
- Missing OTP identifier
