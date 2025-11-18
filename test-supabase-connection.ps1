# Supabase Connection Test Script
# Tests database connection and verifies services can connect

Write-Host "======================================" -ForegroundColor Cyan
Write-Host " SUPABASE CONNECTION TEST" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

$supabaseUrl = "aws-0-eu-central-1.pooler.supabase.com"
$supabasePort = 6543
$meteoServiceUrl = "http://localhost:8081"
$arrosageServiceUrl = "http://localhost:8082"

# Test 1: Check if Supabase endpoint is reachable
Write-Host "[1/6] Testing Supabase endpoint connectivity..." -ForegroundColor Yellow
try {
    $tcpTest = Test-NetConnection -ComputerName $supabaseUrl -Port $supabasePort -InformationLevel Quiet -WarningAction SilentlyContinue
    if ($tcpTest) {
        Write-Host "  PASS - Supabase endpoint is reachable" -ForegroundColor Green
    } else {
        Write-Host "  FAIL - Cannot reach Supabase endpoint" -ForegroundColor Red
        Write-Host "  Check your internet connection and firewall settings" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  FAIL - Error testing connectivity: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Check if meteo-service is running
Write-Host "[2/6] Testing Meteo Service status..." -ForegroundColor Yellow
$meteoRunning = Test-NetConnection -ComputerName localhost -Port 8081 -InformationLevel Quiet -WarningAction SilentlyContinue
if ($meteoRunning) {
    Write-Host "  PASS - Meteo Service is running on port 8081" -ForegroundColor Green
    
    # Test health endpoint
    try {
        $health = Invoke-RestMethod -Uri "$meteoServiceUrl/actuator/health" -Method Get -ErrorAction Stop
        if ($health.status -eq "UP") {
            Write-Host "  PASS - Meteo Service health check: UP" -ForegroundColor Green
        } else {
            Write-Host "  WARN - Meteo Service health check: $($health.status)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  FAIL - Cannot access health endpoint: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "  FAIL - Meteo Service is not running" -ForegroundColor Red
    Write-Host "  Start with: cd backend\meteo-service; java -jar target\meteo-service-0.0.1-SNAPSHOT.jar" -ForegroundColor Yellow
}
Write-Host ""

# Test 3: Check if arrosage-service is running
Write-Host "[3/6] Testing Arrosage Service status..." -ForegroundColor Yellow
$arrosageRunning = Test-NetConnection -ComputerName localhost -Port 8082 -InformationLevel Quiet -WarningAction SilentlyContinue
if ($arrosageRunning) {
    Write-Host "  PASS - Arrosage Service is running on port 8082" -ForegroundColor Green
    
    # Test health endpoint
    try {
        $health = Invoke-RestMethod -Uri "$arrosageServiceUrl/actuator/health" -Method Get -ErrorAction Stop
        if ($health.status -eq "UP") {
            Write-Host "  PASS - Arrosage Service health check: UP" -ForegroundColor Green
        } else {
            Write-Host "  WARN - Arrosage Service health check: $($health.status)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  FAIL - Cannot access health endpoint: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "  FAIL - Arrosage Service is not running" -ForegroundColor Red
    Write-Host "  Start with: cd backend\arrosage-service; java -jar target\arrosage-service-0.0.1-SNAPSHOT.jar" -ForegroundColor Yellow
}
Write-Host ""

# Test 4: Test database connectivity through meteo-service
if ($meteoRunning) {
    Write-Host "[4/6] Testing Meteo Service database operations..." -ForegroundColor Yellow
    try {
        $stations = Invoke-RestMethod -Uri "$meteoServiceUrl/api/stations" -Method Get -ErrorAction Stop
        $stationCount = ($stations | Measure-Object).Count
        Write-Host "  PASS - Retrieved $stationCount station(s) from database" -ForegroundColor Green
        
        if ($stationCount -gt 0) {
            Write-Host "  Sample data:" -ForegroundColor Gray
            $stations | Select-Object -First 3 | ForEach-Object {
                Write-Host "    - $($_.nom) ($($_.fournisseur))" -ForegroundColor Gray
            }
        }
    } catch {
        Write-Host "  FAIL - Cannot retrieve stations: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Message -like "*connection*") {
            Write-Host "  This might indicate a database connection issue" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "[4/6] SKIP - Meteo Service not running" -ForegroundColor Yellow
}
Write-Host ""

# Test 5: Test database connectivity through arrosage-service
if ($arrosageRunning) {
    Write-Host "[5/6] Testing Arrosage Service database operations..." -ForegroundColor Yellow
    try {
        $parcelles = Invoke-RestMethod -Uri "$arrosageServiceUrl/api/parcelles" -Method Get -ErrorAction Stop
        $parcelleCount = ($parcelles | Measure-Object).Count
        Write-Host "  PASS - Retrieved $parcelleCount parcelle(s) from database" -ForegroundColor Green
        
        if ($parcelleCount -gt 0) {
            Write-Host "  Sample data:" -ForegroundColor Gray
            $parcelles | Select-Object -First 3 | ForEach-Object {
                Write-Host "    - $($_.nom): $($_.superficie) m² ($($_.culture))" -ForegroundColor Gray
            }
        }
    } catch {
        Write-Host "  FAIL - Cannot retrieve parcelles: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Message -like "*connection*") {
            Write-Host "  This might indicate a database connection issue" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "[5/6] SKIP - Arrosage Service not running" -ForegroundColor Yellow
}
Write-Host ""

# Test 6: Create test data to verify write operations
if ($meteoRunning) {
    Write-Host "[6/6] Testing database write operations..." -ForegroundColor Yellow
    try {
        $testPrevision = @{
            stationId = 1
            date = (Get-Date).AddDays(5).ToString("yyyy-MM-dd")
            temperatureMax = 25.5
            temperatureMin = 18.0
            pluiePrevue = 0.0
            vitesseVent = 12.0
            humidite = 65.0
        } | ConvertTo-Json
        
        $headers = @{
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$meteoServiceUrl/api/previsions" -Method Post -Body $testPrevision -Headers $headers -ErrorAction Stop
        Write-Host "  PASS - Successfully created test prevision (ID: $($response.id))" -ForegroundColor Green
        
        # Cleanup - delete the test record
        try {
            Invoke-RestMethod -Uri "$meteoServiceUrl/api/previsions/$($response.id)" -Method Delete -ErrorAction SilentlyContinue | Out-Null
            Write-Host "  PASS - Cleanup successful" -ForegroundColor Green
        } catch {
            Write-Host "  WARN - Could not cleanup test data" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  FAIL - Cannot write to database: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "[6/6] SKIP - Meteo Service not running" -ForegroundColor Yellow
}
Write-Host ""

# Summary
Write-Host "======================================" -ForegroundColor Cyan
Write-Host " TEST SUMMARY" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

if ($meteoRunning -and $arrosageRunning) {
    Write-Host " STATUS: ALL SERVICES RUNNING" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor White
    Write-Host "  1. Run full API tests: .\test-all-apis.ps1" -ForegroundColor White
    Write-Host "  2. Check Supabase dashboard for data" -ForegroundColor White
    Write-Host "  3. View logs for any errors" -ForegroundColor White
} else {
    Write-Host " STATUS: SOME SERVICES NOT RUNNING" -ForegroundColor Yellow
    Write-Host ""
    if (-not $meteoRunning) {
        Write-Host "Start Meteo Service:" -ForegroundColor White
        Write-Host "  cd backend\meteo-service" -ForegroundColor Gray
        Write-Host "  java -jar target\meteo-service-0.0.1-SNAPSHOT.jar" -ForegroundColor Gray
        Write-Host ""
    }
    if (-not $arrosageRunning) {
        Write-Host "Start Arrosage Service:" -ForegroundColor White
        Write-Host "  cd backend\arrosage-service" -ForegroundColor Gray
        Write-Host "  java -jar target\arrosage-service-0.0.1-SNAPSHOT.jar" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "Supabase Dashboard: https://app.supabase.com/project/uphsmjapfljujaeaocux" -ForegroundColor Cyan
Write-Host ""
