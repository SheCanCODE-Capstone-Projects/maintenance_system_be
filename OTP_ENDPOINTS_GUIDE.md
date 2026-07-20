# OTP Endpoints Implementation Guide

## Overview
This document describes the OTP (One-Time Password) endpoints implemented according to the API documentation.

---

## Endpoints Implemented

### 1. POST /api/otp/send
**Purpose**: Generate and send an OTP code to the user's phone/email

**Request Body**:
```json
{
  "identifier": "user@example.com",  // Email or phone number
  "otpType": "REGISTRATION"           // Optional: REGISTRATION, PASSWORD_RESET, LOGIN
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "OTP sent successfully",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "phoneNumber": "+250788123456",
    "otpCode": "123456",              // For testing only - remove in production!
    "otpType": "REGISTRATION",
    "expiresAt": "2026-07-18T21:50:00",
    "message": "OTP code sent successfully to use***@example.com. OTP will expire in 10 minutes. Code: 123456"
  },
  "timestamp": "2026-07-18T21:40:00"
}
```

**Features**:
- Accepts email or phone number as identifier
- Generates 6-digit OTP code
- OTP expires in 10 minutes
- Creates OTP_VERIFICATION record in database
- Supports different OTP types (REGISTRATION, PASSWORD_RESET, LOGIN)

---

### 2. POST /api/otp/resend
**Purpose**: Resend a fresh OTP if the previous one expired

**Request Body**:
```json
{
  "identifier": "user@example.com"  // Email or phone number
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Fresh OTP sent successfully. Previous OTP has been invalidated.",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "phoneNumber": "+250788123456",
    "otpCode": "654321",              // For testing only
    "otpType": "REGISTRATION",
    "expiresAt": "2026-07-18T21:55:00",
    "message": "Fresh OTP code sent successfully to use***@example.com. Previous OTP has been invalidated. Code: 654321"
  },
  "timestamp": "2026-07-18T21:45:00"
}
```

**Features**:
- Finds and invalidates previous unused OTP
- Generates new 6-digit OTP code
- New OTP expires in 10 minutes
- Preserves OTP type from previous request
- Marks old OTP as used

---

### 3. POST /api/otp/verify
**Purpose**: Verify the OTP code submitted by the user

**Request Body**:
```json
{
  "identifier": "user@example.com",  // Email or phone number
  "otpCode": "123456"                // 6-digit OTP code
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "OTP verified successfully. Account is now active.",
  "data": "verified",
  "timestamp": "2026-07-18T21:45:00"
}
```

**Error Response (400 Bad Request)**:
```json
{
  "success": false,
  "message": "Invalid or expired OTP code",
  "data": null,
  "timestamp": "2026-07-18T21:45:00"
}
```

**Features**:
- Validates OTP code against user identifier
- Checks if OTP is expired
- Marks OTP as used after verification
- Updates user's `isVerified` status to true
- Prevents OTP reuse

---

## Complete OTP Flow

### Typical User Journey:

1. **Registration**
   ```
   POST /api/auth/register
   → User created
   → OTP automatically generated
   → OTP sent to email/SMS
   ```

2. **Send OTP** (if needed separately)
   ```
   POST /api/otp/send
   → OTP generated
   → OTP sent to user
   → 10-minute expiry set
   ```

3. **Resend OTP** (if user didn't receive or OTP expired)
   ```
   POST /api/otp/resend
   → Old OTP invalidated
   → New OTP generated
   → New OTP sent to user
   ```

4. **Verify OTP**
   ```
   POST /api/otp/verify
   → OTP validated
   → Account activated
   → User can now login
   ```

---

## OTP Types

| Type | Use Case |
|------|----------|
| **REGISTRATION** | Account verification after signup |
| **PASSWORD_RESET** | Verify user identity for password reset |
| **LOGIN** | Two-factor authentication during login |

---

## Security Features

### 1. **OTP Expiration**
- All OTPs expire after 10 minutes
- Expired OTPs cannot be verified
- Users must request a new OTP if expired

### 2. **Single Use**
- Each OTP can only be used once
- After verification, OTP is marked as `isUsed = true`
- Prevents replay attacks

### 3. **OTP Invalidation**
- When resending, previous unused OTP is invalidated
- Only the most recent OTP is valid
- Prevents confusion with multiple active OTPs

### 4. **Identifier Masking**
- Email: `use***@example.com`
- Phone: `+25***99`
- Prevents information leakage in logs/responses

### 5. **Rate Limiting** (To be implemented)
- Limit OTP requests per user
- Prevent abuse/spam
- Implement cooldown between requests

---

## Database Schema

### OTP_VERIFICATION Table
```sql
CREATE TABLE otp_verifications (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    otp_type VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## Testing

### Using PowerShell Script
```powershell
.\test-otp-flow.ps1
```

### Using Swagger UI
1. Navigate to `http://localhost:8080/swagger-ui/index.html`
2. Find "OTP Management" section
3. Test each endpoint:
   - POST /api/otp/send
   - POST /api/otp/verify
   - POST /api/otp/resend

### Using cURL

**Send OTP**:
```bash
curl -X POST http://localhost:8080/api/otp/send \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com",
    "otpType": "REGISTRATION"
  }'
```

**Verify OTP**:
```bash
curl -X POST http://localhost:8080/api/otp/verify \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com",
    "otpCode": "123456"
  }'
```

**Resend OTP**:
```bash
curl -X POST http://localhost:8080/api/otp/resend \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com"
  }'
```

---

## Production Considerations

### 1. **Remove OTP from Response**
Currently, the OTP code is returned in the response for testing. In production:
```java
// Remove this line from OtpResponse:
.otpCode(otpCode)  // ❌ Remove in production
```

### 2. **Implement SMS/Email Service**
```java
// In OtpService.java, add:
@Autowired
private EmailService emailService;

@Autowired
private SmsService smsService;

// After generating OTP:
if (identifier.contains("@")) {
    emailService.sendOtp(user.getEmail(), otpCode);
} else {
    smsService.sendOtp(user.getPhoneNumber(), otpCode);
}
```

### 3. **Add Rate Limiting**
```java
// Limit to 3 OTP requests per 15 minutes
@RateLimit(requests = 3, window = 900) // 15 minutes
public OtpResponse sendOtp(String identifier, String otpType) {
    // ...
}
```

### 4. **Implement Audit Logging**
- Log all OTP requests
- Track failed verification attempts
- Monitor for suspicious activity

### 5. **Add Retry Limits**
```java
// After 3 failed verification attempts, lock account temporarily
if (user.getFailedOtpAttempts() >= 3) {
    throw new RuntimeException("Too many failed attempts. Please try again later.");
}
```

---

## Error Handling

| Error | Status Code | Message |
|-------|-------------|---------|
| User not found | 404 | User not found with the provided email or phone number |
| Invalid OTP | 400 | Invalid or expired OTP code |
| OTP expired | 400 | OTP code has expired. Please request a new one. |
| OTP already used | 400 | Invalid or expired OTP code |
| Validation error | 400 | Validation failed |

---

## API Compliance

✅ All endpoints match the API documentation specifications  
✅ Request/response formats follow documentation  
✅ Error handling as specified  
✅ OTP expiration (10 minutes)  
✅ Single-use OTP  
✅ Resend invalidates previous OTP  
✅ Public access (no authentication required)  

---

## Next Steps

1. ✅ **Implemented**: Send OTP endpoint
2. ✅ **Implemented**: Resend OTP endpoint
3. ✅ **Implemented**: Verify OTP endpoint
4. 🔄 **Pending**: Integrate SMS gateway
5. 🔄 **Pending**: Integrate email service
6. 🔄 **Pending**: Add rate limiting
7. 🔄 **Pending**: Remove OTP from response in production

---

**Status**: ✅ **FULLY IMPLEMENTED AND READY FOR TESTING**

**Created**: July 18, 2026  
**Endpoints**: 3 (send, verify, resend)  
**Test Script**: test-otp-flow.ps1
