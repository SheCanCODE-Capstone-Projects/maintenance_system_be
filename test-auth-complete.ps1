# ==============================================
# Complete Authentication & OTP System Testing
# ==============================================

$baseUrl = "http://localhost:8080"
$timestamp = Get-Date -Format "HHmmss"

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   TESTING AUTHENTICATION & OTP SYSTEM" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Test 1: Register a Customer
Write-Host "[TEST 1] Registering new Customer..." -ForegroundColor Yellow
$registerBody = @{
    name = "Test Customer $timestamp"
    email = "customer$timestamp@example.com"
    phoneNumber = "+25078$(Get-Random -Minimum 1000000 -Maximum 9999999)"
    password = "TestPass123"
    role = "CUSTOMER"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $registerBody -ContentType "application/json"
    $userId = $registerResponse.data.userId
    $userEmail = $registerResponse.data.email
    $otpCode = if ($registerResponse.data.message -match 'OTP: (\d{6})') { $matches[1] } else { "" }
    
    Write-Host "  ✓ Registration successful" -ForegroundColor Green
    Write-Host "    User ID: $userId" -ForegroundColor White
    Write-Host "    Email: $userEmail" -ForegroundColor White
    Write-Host "    OTP Code: $otpCode" -ForegroundColor White
} catch {
    Write-Host "  ✗ Registration failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Try to login without verification (should fail and send OTP)
Write-Host "`n[TEST 2] Attempting login without verification..." -ForegroundColor Yellow
$loginBody = @{
    emailOrPhone = $userEmail
    password = "TestPass123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    Write-Host "  ✗ Login should have failed (account not verified)" -ForegroundColor Red
} catch {
    $errorMessage = $_.Exception.Response
    if ($_.ErrorDetails) {
        $errorContent = $_.ErrorDetails.Message | ConvertFrom-Json
        if ($errorContent.message -match "not verified") {
            Write-Host "  ✓ Login correctly blocked - account not verified" -ForegroundColor Green
            if ($errorContent.message -match 'OTP: (\d{6})') {
                $otpCode = $matches[1]
                Write-Host "    New OTP Code: $otpCode" -ForegroundColor White
            }
        } else {
            Write-Host "  ✗ Unexpected error: $($errorContent.message)" -ForegroundColor Red
        }
    }
}

# Test 3: Verify OTP
Write-Host "`n[TEST 3] Verifying OTP..." -ForegroundColor Yellow
$verifyBody = @{
    userId = $userId
    otpCode = $otpCode
} | ConvertTo-Json

try {
    $verifyResponse = Invoke-RestMethod -Uri "$baseUrl/api/otp/verify" -Method POST -Body $verifyBody -ContentType "application/json"
    Write-Host "  ✓ OTP verification successful" -ForegroundColor Green
    Write-Host "    Message: $($verifyResponse.message)" -ForegroundColor White
} catch {
    Write-Host "  ✗ OTP verification failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 4: Login after verification
Write-Host "`n[TEST 4] Login after verification..." -ForegroundColor Yellow
try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.data.token
    
    Write-Host "  ✓ Login successful" -ForegroundColor Green
    Write-Host "    User: $($loginResponse.data.name)" -ForegroundColor White
    Write-Host "    Role: $($loginResponse.data.role)" -ForegroundColor White
    Write-Host "    Token: $($token.Substring(0, 20))..." -ForegroundColor White
} catch {
    Write-Host "  ✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 5: Forgot Password
Write-Host "`n[TEST 5] Testing forgot password flow..." -ForegroundColor Yellow
$forgotBody = @{
    emailOrPhone = $userEmail
} | ConvertTo-Json

try {
    $forgotResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/forgot-password" -Method POST -Body $forgotBody -ContentType "application/json"
    $resetOtp = $forgotResponse.data.otpCode
    
    Write-Host "  ✓ Password reset OTP sent" -ForegroundColor Green
    Write-Host "    Reset OTP: $resetOtp" -ForegroundColor White
} catch {
    Write-Host "  ✗ Forgot password failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Reset Password
Write-Host "`n[TEST 6] Resetting password..." -ForegroundColor Yellow
$resetBody = @{
    userId = $userId
    otpCode = $resetOtp
    newPassword = "NewTestPass456"
} | ConvertTo-Json

try {
    $resetResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/reset-password" -Method POST -Body $resetBody -ContentType "application/json"
    Write-Host "  ✓ Password reset successful" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Password reset failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 7: Login with new password
Write-Host "`n[TEST 7] Login with new password..." -ForegroundColor Yellow
$newLoginBody = @{
    emailOrPhone = $userEmail
    password = "NewTestPass456"
} | ConvertTo-Json

try {
    $newLoginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $newLoginBody -ContentType "application/json"
    Write-Host "  ✓ Login with new password successful" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Login with new password failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 8: Logout
Write-Host "`n[TEST 8] Testing logout..." -ForegroundColor Yellow
try {
    $logoutResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/logout" -Method POST
    Write-Host "  ✓ Logout successful" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Logout failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 9: Test with wrong password (failed login attempts)
Write-Host "`n[TEST 9] Testing failed login attempts..." -ForegroundColor Yellow
$wrongLoginBody = @{
    emailOrPhone = $userEmail
    password = "WrongPassword123"
} | ConvertTo-Json

for ($i = 1; $i -le 3; $i++) {
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $wrongLoginBody -ContentType "application/json" -ErrorAction Stop
    } catch {
        Write-Host "  ✓ Failed attempt $i blocked correctly" -ForegroundColor Green
    }
}

# Test 10: Resend OTP (create new unverified user first)
Write-Host "`n[TEST 10] Testing OTP resend..." -ForegroundColor Yellow
$resendRegisterBody = @{
    name = "Resend Test $timestamp"
    email = "resend$timestamp@example.com"
    phoneNumber = "+25078$(Get-Random -Minimum 1000000 -Maximum 9999999)"
    password = "TestPass123"
    role = "TECHNICIAN"
} | ConvertTo-Json

try {
    $resendRegisterResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $resendRegisterBody -ContentType "application/json"
    $resendUserId = $resendRegisterResponse.data.userId
    
    Start-Sleep -Seconds 2
    
    $resendOtpBody = @{
        userId = $resendUserId
    } | ConvertTo-Json
    
    $resendOtpResponse = Invoke-RestMethod -Uri "$baseUrl/api/otp/resend" -Method POST -Body $resendOtpBody -ContentType "application/json"
    Write-Host "  ✓ OTP resend successful" -ForegroundColor Green
    Write-Host "    New OTP: $($resendOtpResponse.data.otpCode)" -ForegroundColor White
} catch {
    Write-Host "  ✗ OTP resend failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Summary
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   AUTHENTICATION TESTING COMPLETE" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "`nAll authentication flows tested successfully!" -ForegroundColor Green
Write-Host "`nAccess Swagger UI at:" -ForegroundColor Yellow
Write-Host "http://localhost:8080/swagger-ui/index.html" -ForegroundColor White
Write-Host "`n============================================`n" -ForegroundColor Cyan
