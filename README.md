# Maintenance Hub Backend

A Spring Boot application for managing maintenance requests, connecting customers with verified technicians.

## 🚀 Quick Start

### Prerequisites
- Java 17
- PostgreSQL 17.6
- Maven 3.x

### Database Setup
1. Create PostgreSQL database:
```sql
CREATE DATABASE databaseName;
```

2. Update credentials in `src/main/resources/application.yml` if needed:
```yaml
spring:
  datasource:
    url_Db
    username:
    password: 
```

### Running the Application

#### Option 1: Using Maven Wrapper (Recommended)
```bash
./mvnw spring-boot:run
```

#### Option 2: Using Maven
```bash
mvn spring-boot:run
```

#### Option 3: Build and Run JAR
```bash
./mvnw clean package
java -jar target/maintainanceHub-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:8080**

---

## 📚 API Documentation

### Swagger UI (Interactive Testing) ✅ WORKING
Once the application is running, access the Swagger UI at:
- **Primary URL**: http://localhost:8080/swagger-ui/index.html
- **Short URL**: http://localhost:8080/
- **Alternative**: http://localhost:8080/swagger

All these URLs will take you to the interactive Swagger UI where you can test all API endpoints!

**OpenAPI Specification:**
- JSON format: http://localhost:8080/v3/api-docs
- YAML format: http://localhost:8080/v3/api-docs.yaml

---

## 🔑 Available Endpoints

### Authentication & Registration

#### Register New User
- **Endpoint**: `POST /api/auth/register`
- **Access**: Public (no authentication required)
- **Content-Type**: `application/json`

**Request Body:**
```json
{
  "name": "Jean Pierre Nkurunziza",
  "email": "user@example.com",
  "phoneNumber": "+250788123456",
  "password": "SecurePass123",
  "role": "CUSTOMER"
}
```

**Roles**: `CUSTOMER`, `TECHNICIAN`, `ADMIN`

**Success Response (201):**
```json
{
  "success": true,
  "message": "Registration successful. Please verify your account with OTP.",
  "data": {
    "userId": 1,
    "name": "Jean Pierre Nkurunziza",
    "email": "user@example.com",
    "phoneNumber": "+250788123456",
    "role": "CUSTOMER",
    "isVerified": false,
    "createdAt": "2026-07-17T15:03:04.332547",
    "message": "Registration successful. Please verify your account with the OTP sent to your email/phone. OTP: 123456"
  },
  "timestamp": "2026-07-17T15:03:04.416744"
}
```

---

## 🧪 Testing

### Using PowerShell Scripts
```powershell
# Test customer registration
.\test-register.ps1

# Test technician registration
.\test-technician-register.ps1

# Test duplicate email validation
.\test-duplicate-email.ps1
```

### Using cURL
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "phoneNumber": "+250788123456",
    "password": "SecurePass123",
    "role": "CUSTOMER"
  }'
```

### Using Swagger UI
1. Navigate to http://localhost:8080/swagger-ui.html
2. Click on "Authentication" section
3. Click "POST /api/auth/register"
4. Click "Try it out"
5. Enter test data
6. Click "Execute"

---

## 📊 Database Schema

### Tables Created Automatically
- **users** - Main user table (all roles)
- **customers** - Customer profile (1:1 with users where role=CUSTOMER)
- **otp_verifications** - OTP codes for verification

### Verify Data
```sql
-- View all users
SELECT * FROM users;

-- View customer profiles
SELECT * FROM customers;

-- View OTP codes
SELECT * FROM otp_verifications;
```

---

## 🔒 Security Features

- ✅ BCrypt password encryption
- ✅ Input validation
- ✅ Unique email and phone constraints
- ✅ OTP verification system
- ✅ Account blocking after failed attempts
- ✅ CORS support

---

## 📝 Documentation Files

- **API_TESTING.md** - Detailed testing guide with examples
- **IMPLEMENTATION_SUMMARY.md** - Complete implementation details
- **README.md** - This file (quick start guide)

---

## 🎯 User Roles

### CUSTOMER
- Submit maintenance requests
- Track request status
- Rate technicians
- View request history

### TECHNICIAN
- Receive job requests
- Accept/reject jobs
- Update job status
- View earnings and ratings

### ADMIN
- Manage users (customers and technicians)
- Verify technician qualifications
- Manage companies and categories
- Generate reports
- Monitor system health

---

## 🛠️ Technology Stack

- **Framework**: Spring Boot 4.1.0
- **Language**: Java 17
- **Database**: PostgreSQL 17.6
- **ORM**: Hibernate/JPA
- **Security**: Spring Security + BCrypt
- **API Docs**: SpringDoc OpenAPI 2.3.0
- **Build Tool**: Maven
- **Dev Tools**: Lombok, Spring DevTools

---

## 📂 Project Structure

```
src/main/java/sheCanCode/maintainanceHub/
├── auth/              # Security & API documentation config
├── controllers/       # REST controllers
├── dto/              # Data Transfer Objects
├── modals/           # JPA entities
├── repositories/     # Data access layer
└── services/         # Business logic
```

---

## 🔧 Configuration

### Application Properties
Location: `src/main/resources/application.yml`

Key configurations:
- Database connection
- Server port (8080)
- Hibernate settings
- JWT configuration
- Swagger settings

---

## 📞 Support

For issues or questions:
1. Check the API_TESTING.md guide
2. Review IMPLEMENTATION_SUMMARY.md
3. Use Swagger UI for interactive testing
4. Check application logs

---

## 🎉 Status

✅ **Registration Backend: FULLY FUNCTIONAL**

Successfully tested:
- Customer registration
- Technician registration
- Duplicate email validation
- OTP generation
- Customer profile auto-creation
- Database integration
- Swagger UI documentation

---

## 📅 Created

July 17, 2026

---

## 📄 License

Capstone Project 2025-2026
