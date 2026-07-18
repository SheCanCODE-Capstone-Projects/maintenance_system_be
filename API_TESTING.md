# Maintenance Hub API Testing Guide

## Application Status
✅ Server is running on: `http://localhost:8080`  
✅ Swagger UI: `http://localhost:8080/swagger-ui.html`  
✅ API Docs: `http://localhost:8080/v3/api-docs`

## Database Configuration
The application uses PostgreSQL database with the following configuration:
- Database: `maintenaincehub_db`
- Host: `localhost:5432`
- Username: `postgres`
- Password: `1234`

## Testing Registration Endpoint

### Endpoint Details
- **URL**: `POST /api/auth/register`
- **Access**: Public (no authentication required)
- **Content-Type**: `application/json`

### Available Roles
<cite index="1-3">The system has three roles: CUSTOMER, TECHNICIAN, and ADMIN</cite>

---

## Test Cases

### 1. Register as Customer
<cite index="1-4">Users with role "customer" additionally have a CUSTOMER profile</cite>

**Request:**
```json
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Jean Pierre Nkurunziza",
  "email": "jeanpierre@example.com",
  "phoneNumber": "+250788123456",
  "password": "SecurePass123",
  "role": "CUSTOMER"
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Registration successful. Please verify your account with OTP.",
  "data": {
    "userId": 1,
    "name": "Jean Pierre Nkurunziza",
    "email": "jeanpierre@example.com",
    "phoneNumber": "+250788123456",
    "role": "CUSTOMER",
    "isVerified": false,
    "createdAt": "2026-07-17T14:56:16.123456",
    "message": "Registration successful. Please verify your account with the OTP sent to your email/phone. OTP: 123456"
  },
  "timestamp": "2026-07-17T14:56:16.123456"
}
```

---

### 2. Register as Technician
<cite index="1-13,1-14,1-15">Technicians register an account and select a technician type (independent) or be linked to a company, upload qualification documents/certifications for one or more service categories (e.g. Plumbing, Electrical), and wait for admin verification before going live</cite>

**Request:**
```json
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Marie Uwase",
  "email": "marie.uwase@example.com",
  "phoneNumber": "+250788654321",
  "password": "TechPass456",
  "role": "TECHNICIAN"
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Registration successful. Please verify your account with OTP.",
  "data": {
    "userId": 2,
    "name": "Marie Uwase",
    "email": "marie.uwase@example.com",
    "phoneNumber": "+250788654321",
    "role": "TECHNICIAN",
    "isVerified": false,
    "createdAt": "2026-07-17T14:56:16.123456",
    "message": "Registration successful. Please verify your account with the OTP sent to your email/phone. OTP: 654321"
  },
  "timestamp": "2026-07-17T14:56:16.123456"
}
```

---

### 3. Register as Admin (Company)

**Request:**
```json
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Admin User",
  "email": "admin@maintenancehub.rw",
  "phoneNumber": "+250788999888",
  "password": "AdminPass789",
  "role": "ADMIN"
}
```

---

### 4. Test Duplicate Email (Error Case)

**Request:**
```json
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Another User",
  "email": "jeanpierre@example.com",
  "phoneNumber": "+250788777666",
  "password": "Password123",
  "role": "CUSTOMER"
}
```

**Expected Response (409 Conflict):**
```json
{
  "success": false,
  "message": "Email already registered",
  "data": null,
  "timestamp": "2026-07-17T14:56:16.123456"
}
```

---

### 5. Test Duplicate Phone Number (Error Case)

**Request:**
```json
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Another User",
  "email": "newuser@example.com",
  "phoneNumber": "+250788123456",
  "password": "Password123",
  "role": "CUSTOMER"
}
```

**Expected Response (409 Conflict):**
```json
{
  "success": false,
  "message": "Phone number already registered",
  "data": null,
  "timestamp": "2026-07-17T14:56:16.123456"
}
```

---

### 6. Test Validation Errors (Error Case)

**Request with missing fields:**
```json
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "",
  "email": "invalid-email",
  "phoneNumber": "123",
  "password": "short"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "timestamp": "2026-07-17T14:56:16.123456",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    "Name is required",
    "Invalid email format",
    "Phone number must be between 10 and 15 characters",
    "Password must be at least 8 characters",
    "Role is required"
  ]
}
```

---

## Using Swagger UI

1. Open your browser and navigate to: `http://localhost:8080/swagger-ui.html`
2. Find the "Authentication" section
3. Click on `POST /api/auth/register`
4. Click "Try it out"
5. Replace the request body with one of the test cases above
6. Click "Execute"
7. View the response below

---

## Using cURL

### Customer Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jean Pierre Nkurunziza",
    "email": "jeanpierre@example.com",
    "phoneNumber": "+250788123456",
    "password": "SecurePass123",
    "role": "CUSTOMER"
  }'
```

### Technician Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Marie Uwase",
    "email": "marie.uwase@example.com",
    "phoneNumber": "+250788654321",
    "password": "TechPass456",
    "role": "TECHNICIAN"
  }'
```

---

## Using PowerShell (Windows)

### Customer Registration
```powershell
$body = @{
    name = "Jean Pierre Nkurunziza"
    email = "jeanpierre@example.com"
    phoneNumber = "+250788123456"
    password = "SecurePass123"
    role = "CUSTOMER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -Body $body -ContentType "application/json"
```

---

## Next Steps

After successful registration:
1. <cite index="1-6">Verify identity via OTP sent by SMS/email</cite>
2. For customers: <cite index="1-7">Set/update a default location (latitude, longitude) used for matching</cite>
3. For technicians: <cite index="1-14,1-15">Upload qualification documents/certifications for one or more service categories and wait for admin verification before going live</cite>

---

## Database Verification

You can verify the registration by checking the database:

```sql
-- View all users
SELECT * FROM users;

-- View customers (only for users with CUSTOMER role)
SELECT * FROM customers;

-- View OTP verifications
SELECT * FROM otp_verifications;
```

---

## Important Notes

1. **OTP Code**: In the current implementation, the OTP code is returned in the response for testing purposes. <cite index="1-6">In production, this should be sent via SMS/email</cite>.

2. <cite index="1-12">**Account Safety**: Accounts may be temporarily blocked after repeated failed login attempts</cite>.

3. **Password Security**: All passwords are encrypted using BCrypt before being stored in the database.

4. **Customer Profile**: When a user registers with the CUSTOMER role, a customer profile is automatically created with null default location values that can be updated later.

5. **Verification Status**: All newly registered users have `isVerified: false` until they verify their account using the OTP.

---

## API Response Structure

All API responses follow this structure:
```json
{
  "success": true/false,
  "message": "Human-readable message",
  "data": { /* Response data */ },
  "timestamp": "ISO 8601 timestamp"
}
```
