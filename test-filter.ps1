$headers = @{
    "Content-Type" = "application/json"
}

Write-Host "=== Test 1: Mirpur 10 to Motijheel (Has direct metro) ==="
$body1 = '{"origin": "Mirpur 10", "destination": "Motijheel"}'
try {
    $response1 = Invoke-WebRequest -Uri "http://localhost:8080/api/routes/find" -Method POST -Headers $headers -Body $body1 -UseBasicParsing
    $json1 = $response1.Content | ConvertFrom-Json
    Write-Host "Direct Routes: $($json1.directRoutes.Count)"
    Write-Host "Combined Routes: $($json1.combinedRoutes.Count)"
    if ($json1.combinedRoutes.Count -gt 0) {
        foreach ($route in $json1.combinedRoutes) {
            $hasMetro = $false
            foreach ($leg in $route.legs) {
                if ($leg.transportMode -eq "METRO") { $hasMetro = $true }
            }
            Write-Host "  Route with $($route.totalSteps) legs - Has Metro: $hasMetro"
        }
    }
} catch {
    Write-Host "Error: $_"
}

Write-Host "`n=== Test 2: Airport to Mirpur 10 (Complex route) ==="
$body2 = '{"origin": "Airport", "destination": "Mirpur 10"}'
try {
    $response2 = Invoke-WebRequest -Uri "http://localhost:8080/api/routes/find" -Method POST -Headers $headers -Body $body2 -UseBasicParsing
    $json2 = $response2.Content | ConvertFrom-Json
    Write-Host "Direct Routes: $($json2.directRoutes.Count)"
    Write-Host "Combined Routes: $($json2.combinedRoutes.Count)"
    if ($json2.combinedRoutes.Count -gt 0) {
        foreach ($route in $json2.combinedRoutes) {
            $hasMetro = $false
            foreach ($leg in $route.legs) {
                if ($leg.transportMode -eq "METRO") { $hasMetro = $true }
            }
            Write-Host "  Route with $($route.totalSteps) legs - Has Metro: $hasMetro"
        }
    }
} catch {
    Write-Host "Error: $_"
}
