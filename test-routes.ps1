$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    origin = "Mirpur 10"
    destination = "Motijheel"
} | ConvertTo-Json

Write-Host "Testing route from Mirpur 10 to Motijheel..."
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/routes/find" -Method POST -Headers $headers -Body $body
$responseJson = $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host $responseJson

Write-Host "`n`nTesting route to metro station (Farmgate)..."
$body2 = @{
    origin = "Mirpur 10"
    destination = "Farmgate"
} | ConvertTo-Json
$response2 = Invoke-WebRequest -Uri "http://localhost:8080/api/routes/find" -Method POST -Headers $headers -Body $body2
$responseJson2 = $response2.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host $responseJson2
