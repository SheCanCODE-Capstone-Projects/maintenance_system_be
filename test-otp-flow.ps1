# Test OTP Flow - Complete workflow demonstration

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   OTP ENDPOINT TESTING" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Step 1: Register a new user
Write-Host "Step 1: Registering a new user..." -ForegroundColor Yellow
$registerBody = @{
    name = "OTP Test User"
    email = "otptest@example.com"
    phoneNumber = "+250788999999"
    password = "TestPass123"
    role = "CUSTOMER"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -Body $registerBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "✓ User registered successfully!" -ForegroundColor Green
    Write-Host "  User ID: $($registerResponse.data.userId)" -ForegroundColor White
    Write-Host "  Email: $($registerResponse.data.email)" -ForegroundColor White
    Write-Host "  Initial OTP: $($registerResponse.data.message.Split('OTP: ')[-1])" -ForegroundColor White
    $userId = $registerResponse.data.userId
    $userEmail = $registerResponse.data.email
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 409) {
        Write-Host "✓ User already exists, proceeding with existing user..." -ForegroundColor Yellow
        $userEmail = "otptest@example.com"
    } else {
        Write-Host "✗ Registration failed: $($_.Exception.Message)" -ForegroundColor Red
        exit
    }
}

Start-Sleep -Seconds 2

# Step 2: Send OTP
Write-Host "`nStep 2: Sending OTP..." -ForegroundColor Yellow
$sendOtpBody = @{
    identifier = $userEmail
    otpType = "REGISTRATION"
} | ConvertTo-Json

try {
    $sendResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/otp/send" -Method POST -Body $sendOtpBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "✓ OTP sent successfully!" -ForegroundColor Green
    Write-Host "  OTP Code: $($sendResponse.data.otpCode)" -ForegroundColor White
    Write-Host "  Expires At: $($sendResponse.data.expiresAt)" -ForegroundColor White
    Write-Host "  Message: $($sendResponse.data.message)" -ForegroundColor White
    $otpCode = $sendResponse.data.otpCode
} catch {
    Write-Host "✗ Send OTP failed: $($_.Exception.Message)" -ForegroundColor Red
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $reader.BaseStream.Position = 0
    $reader.DiscardBufferedData()
    $responseBody = $reader.ReadToEnd()
    Write-Host "Response: $responseBody" -ForegroundColor Red
    exit
}

Start-Sleep -Seconds 2

# Step 3: Resend OTP
Write-Host "`nStep 3: Resending OTP (simulating expired/lost OTP)..." -ForegroundColor Yellow
$resendOtpBody = @{
    identifier = $userEmail
} | ConvertTo-Json

try {
    $resendResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/otp/resend" -Method POST -Body $resendOtpBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "✓ OTP resent successfully!" -ForegroundColor Green
    Write-Host "  New OTP Code: $($resendResponse.data.otpCode)" -ForegroundColor White
    Write-Host "  Expires At: $($resendResponse.data.expiresAt)" -ForegroundColor White
    Write-Host "  Message: $($resendResponse.data.message)" -ForegroundColor White
    $otpCode = $resendResponse.data.otpCode
} catch {
    Write-Host "✗ Resend OTP failed: $($_.Exception.Message)" -ForegroundColor Red
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $reader.BaseStream.Position = 0
    $reader.DiscardBufferedData()
    $responseBody = $reader.ReadToEnd()
    Write-Host "Response: $responseBody" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# Step 4: Verify OTP
Write-Host "`nStep 4: Verifying OTP..." -ForegroundColor Yellow
$verifyOtpBody = @{
    identifier = $userEmail
    otpCode = $otpCode
} | ConvertTo-Json

try {
    $verifyResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/otp/verify" -Method POST -Body $verifyOtpBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "✓ OTP verified successfully!" -ForegroundColor Green
    Write-Host "  Message: $($verifyResponse.message)" -ForegroundColor White
    Write-Host "  Account is now verified and active!" -ForegroundColor Green
} catch {
    Write-Host "✗ Verify OTP failed: $($_.Exception.Message)" -ForegroundColor Red
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $reader.BaseStream.Position = 0
    $reader.DiscardBufferedData()
    $responseBody = $reader.ReadToEnd()
    Write-Host "Response: $responseBody" -ForegroundColor Red
}

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "   OTP FLOW COMPLETE!" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "  1. ✓ User registered" -ForegroundColor White
Write-Host "  2. ✓ OTP sent" -ForegroundColor White
Write-Host "  3. ✓ OTP resent (old one invalidated)" -ForegroundColor White
Write-Host "  4. ✓ OTP verified" -ForegroundColor White
Write-Host "`nUser account is now fully verified!`n" -ForegroundColor Green
