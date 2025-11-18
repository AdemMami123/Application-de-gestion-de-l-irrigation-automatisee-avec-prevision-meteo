# Quick API Test Script
# Prerequisites: Both services must be running

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Quick API Test Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if services are running
Write-Host "Checking if services are running..." -ForegroundColor Yellow

$meteoRunning = Test-NetConnection -ComputerName localhost -Port 8081 -InformationLevel Quiet -WarningAction SilentlyContinue
$arrosageRunning = Test-NetConnection -ComputerName localhost -Port 8082 -InformationLevel Quiet -WarningAction SilentlyContinue

if (-not $meteoRunning) {
    Write-Host "❌ Meteo Service (port 8081) is NOT running!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please start it manually:" -ForegroundColor Yellow
    Write-Host "  1. Open a new terminal" -ForegroundColor White
    Write-Host "  2. cd backend\meteo-service" -ForegroundColor White
    Write-Host "  3. Run: .\mvnw spring-boot:run  (or mvn spring-boot:run)" -ForegroundColor White
    Write-Host ""
}

if (-not $arrosageRunning) {
    Write-Host "❌ Arrosage Service (port 8082) is NOT running!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please start it manually:" -ForegroundColor Yellow
    Write-Host "  1. Open a new terminal" -ForegroundColor White
    Write-Host "  2. cd backend\arrosage-service" -ForegroundColor White
    Write-Host "  3. Run: .\mvnw spring-boot:run  (or mvn spring-boot:run)" -ForegroundColor White
    Write-Host ""
}

if (-not $meteoRunning -or -not $arrosageRunning) {
    Write-Host "Cannot proceed with tests. Please start the services first." -ForegroundColor Red
    Write-Host "Press any key to exit..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

Write-Host "✓ Meteo Service (port 8081) is running" -ForegroundColor Green
Write-Host "✓ Arrosage Service (port 8082) is running" -ForegroundColor Green
Write-Host ""
Write-Host "Starting API tests..." -ForegroundColor Cyan
Write-Host ""

# Run the full test script
& "$PSScriptRoot\test-all-apis.ps1"
