# Phase 6 — Guest Session Identity

## Endpoint

```http
POST /api/v1/auth/guest-session
```

## Request

Body is optional. Email and mobile are optional and can be captured later during checkout.

```json
{
  "email": "guest@example.com",
  "mobile": "9876543210"
}
```

Empty request is also valid:

```json
{}
```

## Response

```json
{
  "guestIdentityId": 1,
  "guestSessionId": "gst_xxx",
  "status": "ACTIVE",
  "expiresAt": "2026-07-28T...Z"
}
```

## Important security note

The raw `guestSessionId` is returned only once to the client. The database stores only `guest_session_hash`.

## Implemented scenarios

- Create anonymous guest session
- Create guest session with optional email/mobile
- Validate email/mobile format
- Store only hashed guest session id
- Guest session expiry support
- Future merge support using `merged_to_user_id` and `merged_at`
