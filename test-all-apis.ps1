# API Testing Script for Irrigation Meteo System
# Tests all endpoints for meteo-service and arrosage-service

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  API Testing Script" -ForegroundColor Cyan
Write-Host "  Irrigation & Meteo System" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$meteoBaseUrl = "http://localhost:8081/api"
$arrosageBaseUrl = "http://localhost:8082/api"
$testResults = @()

# Helper function to test API endpoint
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [object]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    
    Write-Host "Testing: $Name" -ForegroundColor Yellow
    Write-Host "  Method: $Method" -ForegroundColor Gray
    Write-Host "  URL: $Url" -ForegroundColor Gray
    
    try {
        $headers = @{
            "Content-Type" = "application/json"
            "Accept" = "application/json"
        }
        
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $headers
            TimeoutSec = 10
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
            Write-Host "  Body: $($params.Body)" -ForegroundColor Gray
        }
        
        $response = Invoke-WebRequest @params -ErrorAction Stop
        
        $success = $response.StatusCode -eq $ExpectedStatus
        
        if ($success) {
            Write-Host "  ✓ SUCCESS - Status: $($response.StatusCode)" -ForegroundColor Green
            
            # Try to parse JSON response
            try {
                $jsonResponse = $response.Content | ConvertFrom-Json
                Write-Host "  Response: $($response.Content.Substring(0, [Math]::Min(200, $response.Content.Length)))..." -ForegroundColor Gray
            } catch {
                Write-Host "  Response: $($response.Content.Substring(0, [Math]::Min(100, $response.Content.Length)))..." -ForegroundColor Gray
            }
        } else {
            Write-Host "  ✗ FAILED - Expected: $ExpectedStatus, Got: $($response.StatusCode)" -ForegroundColor Red
        }
        
        $script:testResults += [PSCustomObject]@{
            Name = $Name
            Status = if ($success) { "PASS" } else { "FAIL" }
            StatusCode = $response.StatusCode
            ResponseTime = $response.Headers['X-Response-Time']
        }
        
        Write-Host ""
        return $response
        
    } catch {
        Write-Host "  ✗ ERROR - $($_.Exception.Message)" -ForegroundColor Red
        
        $script:testResults += [PSCustomObject]@{
            Name = $Name
            Status = "ERROR"
            StatusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.Value__ } else { "N/A" }
            Error = $_.Exception.Message
        }
        
        Write-Host ""
        return $null
    }
}

# Check if services are running
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  STEP 1: Service Health Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Test-Endpoint -Name "Meteo Service Health" -Method "GET" -Url "http://localhost:8081/actuator/health"
Test-Endpoint -Name "Arrosage Service Health" -Method "GET" -Url "http://localhost:8082/actuator/health"

# ========================================
# METEO SERVICE TESTS
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  STEP 2: Meteo Service API Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Get all previsions
$previsions = Test-Endpoint -Name "GET All Previsions" -Method "GET" -Url "$meteoBaseUrl/previsions"

# Test 2: Create a new prevision
$newPrevision = @{
    date = "2025-11-20"
    temperatureMax = 28.5
    temperatureMin = 18.2
    pluiePrevue = 5.0
    vitesseVent = 15.0
    humidite = 65.0
}

$createdPrevision = Test-Endpoint -Name "POST Create Prevision" -Method "POST" -Url "$meteoBaseUrl/previsions" -Body $newPrevision -ExpectedStatus 200

# Extract the created prevision ID
$previsionId = $null
if ($createdPrevision) {
    try {
        $previsionData = $createdPrevision.Content | ConvertFrom-Json
        $previsionId = $previsionData.id
        Write-Host "Created Prevision ID: $previsionId" -ForegroundColor Green
    } catch {
        Write-Host "Could not extract prevision ID" -ForegroundColor Yellow
    }
}

# Test 3: Get prevision by ID (if created)
if ($previsionId) {
    Test-Endpoint -Name "GET Prevision by ID" -Method "GET" -Url "$meteoBaseUrl/previsions/$previsionId"
}

# Test 4: Update prevision (if created)
if ($previsionId) {
    $updatedPrevision = @{
        date = "2025-11-20"
        temperatureMax = 30.0
        temperatureMin = 19.0
        pluiePrevue = 8.0
        vitesseVent = 18.0
        humidite = 70.0
    }
    
    Test-Endpoint -Name "PUT Update Prevision" -Method "PUT" -Url "$meteoBaseUrl/previsions/$previsionId" -Body $updatedPrevision
}

# Test 5: Get previsions by date range
$startDate = "2025-11-18"
$endDate = "2025-11-25"
Test-Endpoint -Name "GET Previsions by Date Range" -Method "GET" -Url "$meteoBaseUrl/previsions/date-range?start=$startDate&end=$endDate"

# Test 6: Get previsions for specific date
Test-Endpoint -Name "GET Prevision by Date" -Method "GET" -Url "$meteoBaseUrl/previsions/date/2025-11-20"

# ========================================
# ARROSAGE SERVICE TESTS
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  STEP 3: Arrosage Service API Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Get all parcelles
$parcelles = Test-Endpoint -Name "GET All Parcelles" -Method "GET" -Url "$arrosageBaseUrl/parcelles"

# Test 2: Create a new parcelle
$newParcelle = @{
    nom = "Test Parcelle"
    superficie = 1.5
    typeSol = "ARGILEUX"
    typeCulture = "LEGUMES"
}

$createdParcelle = Test-Endpoint -Name "POST Create Parcelle" -Method "POST" -Url "$arrosageBaseUrl/parcelles" -Body $newParcelle -ExpectedStatus 200

# Extract parcelle ID
$parcelleId = $null
if ($createdParcelle) {
    try {
        $parcelleData = $createdParcelle.Content | ConvertFrom-Json
        $parcelleId = $parcelleData.id
        Write-Host "Created Parcelle ID: $parcelleId" -ForegroundColor Green
    } catch {
        Write-Host "Could not extract parcelle ID" -ForegroundColor Yellow
    }
}

# Test 3: Get parcelle by ID
if ($parcelleId) {
    Test-Endpoint -Name "GET Parcelle by ID" -Method "GET" -Url "$arrosageBaseUrl/parcelles/$parcelleId"
}

# Test 4: Update parcelle
if ($parcelleId) {
    $updatedParcelle = @{
        nom = "Test Parcelle Updated"
        superficie = 2.0
        typeSol = "ARGILEUX"
        typeCulture = "LEGUMES"
    }
    
    Test-Endpoint -Name "PUT Update Parcelle" -Method "PUT" -Url "$arrosageBaseUrl/parcelles/$parcelleId" -Body $updatedParcelle
}

# Test 5: Get parcelles by type de culture
Test-Endpoint -Name "GET Parcelles by Culture Type" -Method "GET" -Url "$arrosageBaseUrl/parcelles/culture/LEGUMES"

# Test 6: Get parcelles by type de sol
Test-Endpoint -Name "GET Parcelles by Soil Type" -Method "GET" -Url "$arrosageBaseUrl/parcelles/sol/ARGILEUX"

# ========================================
# PROGRAMME ARROSAGE TESTS
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  STEP 4: Programme Arrosage API Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Get all programmes
Test-Endpoint -Name "GET All Programmes" -Method "GET" -Url "$arrosageBaseUrl/programmes"

# Test 2: Create a programme (use parcelleId if available, otherwise use 1)
$programmeParcelleId = if ($parcelleId) { $parcelleId } else { 1 }

$newProgramme = @{
    parcelleId = $programmeParcelleId
    datePlanifiee = "2025-11-18T14:00:00"
    duree = 60
    volumePrevu = 25.0
    statut = "PLANIFIE"
}

$createdProgramme = Test-Endpoint -Name "POST Create Programme" -Method "POST" -Url "$arrosageBaseUrl/programmes" -Body $newProgramme -ExpectedStatus 200

# Extract programme ID
$programmeId = $null
if ($createdProgramme) {
    try {
        $programmeData = $createdProgramme.Content | ConvertFrom-Json
        $programmeId = $programmeData.id
        Write-Host "Created Programme ID: $programmeId" -ForegroundColor Green
    } catch {
        Write-Host "Could not extract programme ID" -ForegroundColor Yellow
    }
}

# Test 3: Get programme by ID
if ($programmeId) {
    Test-Endpoint -Name "GET Programme by ID" -Method "GET" -Url "$arrosageBaseUrl/programmes/$programmeId"
}

# Test 4: Update programme
if ($programmeId) {
    $updatedProgramme = @{
        parcelleId = $programmeParcelleId
        datePlanifiee = "2025-11-18T15:00:00"
        duree = 75
        volumePrevu = 30.0
        statut = "PLANIFIE"
    }
    
    Test-Endpoint -Name "PUT Update Programme" -Method "PUT" -Url "$arrosageBaseUrl/programmes/$programmeId" -Body $updatedProgramme
}

# Test 5: Get programmes by status
Test-Endpoint -Name "GET Programmes by Status (PLANIFIE)" -Method "GET" -Url "$arrosageBaseUrl/programmes/statut/PLANIFIE"

# Test 6: Get programmes by parcelle
if ($parcelleId) {
    Test-Endpoint -Name "GET Programmes by Parcelle" -Method "GET" -Url "$arrosageBaseUrl/programmes/parcelle/$parcelleId"
}

# ========================================
# JOURNAL ARROSAGE TESTS
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  STEP 5: Journal Arrosage API Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Get all journal entries
Test-Endpoint -Name "GET All Journal Entries" -Method "GET" -Url "$arrosageBaseUrl/journaux"

# Test 2: Create journal entry (if programme exists)
if ($programmeId) {
    $newJournal = @{
        programmeId = $programmeId
        dateExecution = "2025-11-18T15:00:00"
        volumeReel = 28.5
        remarque = "Test execution - API test script"
    }
    
    $createdJournal = Test-Endpoint -Name "POST Create Journal Entry" -Method "POST" -Url "$arrosageBaseUrl/journaux" -Body $newJournal -ExpectedStatus 200
    
    # Extract journal ID
    $journalId = $null
    if ($createdJournal) {
        try {
            $journalData = $createdJournal.Content | ConvertFrom-Json
            $journalId = $journalData.id
            Write-Host "Created Journal ID: $journalId" -ForegroundColor Green
        } catch {
            Write-Host "Could not extract journal ID" -ForegroundColor Yellow
        }
    }
    
    # Test 3: Get journal by ID
    if ($journalId) {
        Test-Endpoint -Name "GET Journal by ID" -Method "GET" -Url "$arrosageBaseUrl/journaux/$journalId"
    }
    
    # Test 4: Get journals by programme
    Test-Endpoint -Name "GET Journals by Programme" -Method "GET" -Url "$arrosageBaseUrl/journaux/programme/$programmeId"
}

# Test 5: Get journals by date range
Test-Endpoint -Name "GET Journals by Date Range" -Method "GET" -Url "$arrosageBaseUrl/journaux/date-range?start=2025-11-18&end=2025-11-25"

# ========================================
# CLEANUP (Optional)
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  STEP 6: Cleanup Test Data" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Delete created journal
if ($journalId) {
    Test-Endpoint -Name "DELETE Journal Entry" -Method "DELETE" -Url "$arrosageBaseUrl/journaux/$journalId" -ExpectedStatus 204
}

# Delete created programme
if ($programmeId) {
    Test-Endpoint -Name "DELETE Programme" -Method "DELETE" -Url "$arrosageBaseUrl/programmes/$programmeId" -ExpectedStatus 204
}

# Delete created parcelle
if ($parcelleId) {
    Test-Endpoint -Name "DELETE Parcelle" -Method "DELETE" -Url "$arrosageBaseUrl/parcelles/$parcelleId" -ExpectedStatus 204
}

# Delete created prevision
if ($previsionId) {
    Test-Endpoint -Name "DELETE Prevision" -Method "DELETE" -Url "$meteoBaseUrl/previsions/$previsionId" -ExpectedStatus 204
}

# ========================================
# TEST SUMMARY
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TEST SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$totalTests = $testResults.Count
$passedTests = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failedTests = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$errorTests = ($testResults | Where-Object { $_.Status -eq "ERROR" }).Count

Write-Host "Total Tests: $totalTests" -ForegroundColor White
Write-Host "Passed: $passedTests" -ForegroundColor Green
Write-Host "Failed: $failedTests" -ForegroundColor Red
Write-Host "Errors: $errorTests" -ForegroundColor Yellow
Write-Host ""

# Display detailed results
Write-Host "Detailed Results:" -ForegroundColor Cyan
Write-Host ""
$testResults | Format-Table -Property Name, Status, StatusCode -AutoSize

# Success rate
$successRate = [math]::Round(($passedTests / $totalTests) * 100, 2)
Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 80) { "Green" } elseif ($successRate -ge 50) { "Yellow" } else { "Red" })

# Exit code
if ($errorTests -gt 0 -or $failedTests -gt 0) {
    Write-Host "`n⚠️ Some tests failed or encountered errors!" -ForegroundColor Red
    exit 1
} else {
    Write-Host "`n✓ All tests passed successfully!" -ForegroundColor Green
    exit 0
}
