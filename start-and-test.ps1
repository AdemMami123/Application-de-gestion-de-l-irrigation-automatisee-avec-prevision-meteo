# Simple API Test Runner
# This script will guide you through testing your APIs

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  API Testing Setup & Runner" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check Java
Write-Host "[1/5] Checking Java installation..." -ForegroundColor Yellow
$javaPath = Get-Command java -ErrorAction SilentlyContinue
if ($javaPath) {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "  ✓ Java is installed: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "  ✗ Java is NOT installed!" -ForegroundColor Red
    Write-Host "  Please install Java 17 or higher" -ForegroundColor Yellow
    exit 1
}

# Check Maven
Write-Host ""
Write-Host "[2/5] Checking Maven installation..." -ForegroundColor Yellow
$mavenPath = Get-Command mvn -ErrorAction SilentlyContinue
if ($mavenPath) {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "  ✓ Maven is installed: $mavenVersion" -ForegroundColor Green
    $mavenInstalled = $true
} else {
    Write-Host "  ✗ Maven is NOT installed" -ForegroundColor Red
    Write-Host ""
    Write-Host "  To install Maven, run PowerShell as Administrator and execute:" -ForegroundColor Yellow
    Write-Host "    choco install maven -y" -ForegroundColor White
    Write-Host ""
    Write-Host "  OR download from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    Write-Host ""
    
    $response = Read-Host "Do you want to continue without Maven? (services must be started manually) [y/N]"
    if ($response -ne 'y' -and $response -ne 'Y') {
        exit 1
    }
    $mavenInstalled = $false
}

# Check Docker
Write-Host ""
Write-Host "[3/5] Checking Docker installation..." -ForegroundColor Yellow
$dockerPath = Get-Command docker -ErrorAction SilentlyContinue
if ($dockerPath) {
    Write-Host "  ✓ Docker is installed" -ForegroundColor Green
    
    # Check if Docker is running
    $dockerRunning = docker ps 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Docker is running" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Docker is not running. Please start Docker Desktop" -ForegroundColor Red
        $response = Read-Host "Continue anyway? [y/N]"
        if ($response -ne 'y' -and $response -ne 'Y') {
            exit 1
        }
    }
} else {
    Write-Host "  ✗ Docker is NOT installed" -ForegroundColor Yellow
    Write-Host "  Database services may not be available" -ForegroundColor Yellow
}

# Start databases
Write-Host ""
Write-Host "[4/5] Starting database services..." -ForegroundColor Yellow
if ($dockerPath) {
    Write-Host "  Starting PostgreSQL containers..." -ForegroundColor Gray
    docker-compose up -d 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Database services started" -ForegroundColor Green
        Start-Sleep -Seconds 3
    } else {
        Write-Host "  ✗ Failed to start database services" -ForegroundColor Red
    }
} else {
    Write-Host "  ⊘ Skipping (Docker not available)" -ForegroundColor Yellow
}

# Start services
Write-Host ""
Write-Host "[5/5] Checking/Starting Spring Boot services..." -ForegroundColor Yellow

# Check if services are already running
$meteoRunning = Test-NetConnection -ComputerName localhost -Port 8081 -InformationLevel Quiet -WarningAction SilentlyContinue
$arrosageRunning = Test-NetConnection -ComputerName localhost -Port 8082 -InformationLevel Quiet -WarningAction SilentlyContinue

if ($meteoRunning -and $arrosageRunning) {
    Write-Host "  ✓ Both services are already running" -ForegroundColor Green
} else {
    if ($mavenInstalled) {
        Write-Host ""
        Write-Host "  Services are not running. Starting them now..." -ForegroundColor Yellow
        Write-Host "  This may take 30-60 seconds..." -ForegroundColor Gray
        Write-Host ""
        
        # Start meteo-service
        if (-not $meteoRunning) {
            Write-Host "  Starting Meteo Service (port 8081)..." -ForegroundColor Gray
            $meteoJob = Start-Job -ScriptBlock {
                Set-Location "C:\Users\ademm\OneDrive\Desktop\Personal Projects\irregation_meteo_springboot\backend\meteo-service"
                mvn spring-boot:run 2>&1
            }
            Start-Sleep -Seconds 2
        }
        
        # Start arrosage-service
        if (-not $arrosageRunning) {
            Write-Host "  Starting Arrosage Service (port 8082)..." -ForegroundColor Gray
            $arrosageJob = Start-Job -ScriptBlock {
                Set-Location "C:\Users\ademm\OneDrive\Desktop\Personal Projects\irregation_meteo_springboot\backend\arrosage-service"
                mvn spring-boot:run 2>&1
            }
            Start-Sleep -Seconds 2
        }
        
        # Wait for services to start (max 60 seconds)
        Write-Host "  Waiting for services to start..." -ForegroundColor Gray
        $timeout = 60
        $elapsed = 0
        while ($elapsed -lt $timeout) {
            $meteoRunning = Test-NetConnection -ComputerName localhost -Port 8081 -InformationLevel Quiet -WarningAction SilentlyContinue
            $arrosageRunning = Test-NetConnection -ComputerName localhost -Port 8082 -InformationLevel Quiet -WarningAction SilentlyContinue
            
            if ($meteoRunning -and $arrosageRunning) {
                Write-Host "  ✓ Both services are running" -ForegroundColor Green
                break
            }
            
            Start-Sleep -Seconds 2
            $elapsed += 2
            Write-Host "." -NoNewline -ForegroundColor Gray
        }
        Write-Host ""
        
        if (-not $meteoRunning -or -not $arrosageRunning) {
            Write-Host "  ⚠️ Services did not start in time" -ForegroundColor Yellow
            Write-Host "  Please check the logs and start them manually" -ForegroundColor Yellow
        }
        
    } else {
        Write-Host ""
        Write-Host "  ⚠️ Maven not installed - cannot auto-start services" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "  Please start the services manually:" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "  Terminal 1:" -ForegroundColor Cyan
        Write-Host "    cd backend\meteo-service" -ForegroundColor White
        Write-Host "    mvn spring-boot:run" -ForegroundColor White
        Write-Host ""
        Write-Host "  Terminal 2:" -ForegroundColor Cyan
        Write-Host "    cd backend\arrosage-service" -ForegroundColor White
        Write-Host "    mvn spring-boot:run" -ForegroundColor White
        Write-Host ""
        
        $response = Read-Host "Press Enter when services are running (or 'q' to quit)"
        if ($response -eq 'q' -or $response -eq 'Q') {
            exit 1
        }
        
        # Re-check services
        $meteoRunning = Test-NetConnection -ComputerName localhost -Port 8081 -InformationLevel Quiet -WarningAction SilentlyContinue
        $arrosageRunning = Test-NetConnection -ComputerName localhost -Port 8082 -InformationLevel Quiet -WarningAction SilentlyContinue
    }
}

# Final check
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Service Status Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$meteoRunning = Test-NetConnection -ComputerName localhost -Port 8081 -InformationLevel Quiet -WarningAction SilentlyContinue
$arrosageRunning = Test-NetConnection -ComputerName localhost -Port 8082 -InformationLevel Quiet -WarningAction SilentlyContinue

if ($meteoRunning) {
    Write-Host "✓ Meteo Service (8081): RUNNING" -ForegroundColor Green
} else {
    Write-Host "✗ Meteo Service (8081): NOT RUNNING" -ForegroundColor Red
}

if ($arrosageRunning) {
    Write-Host "✓ Arrosage Service (8082): RUNNING" -ForegroundColor Green
} else {
    Write-Host "✗ Arrosage Service (8082): NOT RUNNING" -ForegroundColor Red
}

Write-Host ""

if (-not $meteoRunning -or -not $arrosageRunning) {
    Write-Host "Cannot proceed with API tests - services are not running!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please refer to API-TESTING-GUIDE.md for manual setup instructions" -ForegroundColor Yellow
    exit 1
}

# Run tests
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Running API Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

& "$PSScriptRoot\test-all-apis.ps1"
