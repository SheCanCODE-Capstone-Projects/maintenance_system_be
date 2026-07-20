$body = @{
    name = "Jean Pierre Nkurunziza"
    email = "testuser@example.com"
    phoneNumber = "+250788123456"
    password = "SecurePass123"
    role = "CUSTOMER"
} | ConvertTo-Json

Write-Host "Sending registration request..." -ForegroundColor Yellow
Write-Host "Body: $body" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -Body $body -ContentType "application/json"
    Write-Host "`nRegistration successful!" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "`nRegistration failed!" -ForegroundColor Red
    Write-Host $_.Exception.Message
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $reader.DiscardBufferedData()
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response: $responseBody" -ForegroundColor Red
    }
}
