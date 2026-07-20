$body = @{
    name = "Another User"
    email = "testuser@example.com"
    phoneNumber = "+250788777666"
    password = "Password123"
    role = "CUSTOMER"
} | ConvertTo-Json

Write-Host "Testing duplicate email..." -ForegroundColor Yellow
Write-Host "Body: $body" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -Body $body -ContentType "application/json"
    Write-Host "`nUnexpected success!" -ForegroundColor Red
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "`nExpected error received!" -ForegroundColor Green
    Write-Host $_.Exception.Message
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $reader.DiscardBufferedData()
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response: $responseBody" -ForegroundColor Yellow
    }
}
