# API Testing - Complete Guide

## Prerequisites

Before running the API tests, you need to:

### 1. Install Maven (if not already installed)

**Check if Maven is installed:**
```powershell
mvn -version
```

**If not installed, download and install:**
- Download from: https://maven.apache.org/download.cgi
- Or use Chocolatey: `choco install maven`
- Or use Scoop: `scoop install maven`

### 2. Start PostgreSQL Databases

```powershell
docker-compose up -d
```

This starts:
- PostgreSQL for meteo-service (port 5433)
- PostgreSQL for arrosage-service (port 5434)
- Kafka and Zookeeper

## Running the Services

### Option 1: Using Maven (Recommended)

**Terminal 1 - Meteo Service:**
```powershell
cd backend\meteo-service
mvn spring-boot:run
```

**Terminal 2 - Arrosage Service:**
```powershell
cd backend\arrosage-service
mvn spring-boot:run
```

### Option 2: Using IDE
- Open both projects in IntelliJ IDEA or VS Code
- Run `MeteoServiceApplication.java`
- Run `ArrosageServiceApplication.java`

### Option 3: Build and Run JAR Files

```powershell
# Build meteo-service
cd backend\meteo-service
mvn clean package -DskipTests
java -jar target\meteo-service-0.0.1-SNAPSHOT.jar

# Build arrosage-service (in new terminal)
cd backend\arrosage-service
mvn clean package -DskipTests
java -jar target\arrosage-service-0.0.1-SNAPSHOT.jar
```

## Verify Services are Running

```powershell
# Check meteo-service
curl http://localhost:8081/actuator/health

# Check arrosage-service
curl http://localhost:8082/actuator/health
```

Both should return: `{"status":"UP"}`

## Running the API Tests

### Full Test Suite
```powershell
.\test-all-apis.ps1
```

### Quick Test with Auto-Check
```powershell
.\run-api-tests.ps1
```

## Manual API Testing

### Meteo Service (Port 8081)

**Create Prevision:**
```powershell
curl -X POST http://localhost:8081/api/previsions `
  -H "Content-Type: application/json" `
  -d '{
    "date": "2025-11-20",
    "temperatureMax": 28.5,
    "temperatureMin": 18.2,
    "pluiePrevue": 5.0,
    "vitesseVent": 15.0,
    "humidite": 65.0
  }'
```

**Get All Previsions:**
```powershell
curl http://localhost:8081/api/previsions
```

### Arrosage Service (Port 8082)

**Create Parcelle:**
```powershell
curl -X POST http://localhost:8082/api/parcelles `
  -H "Content-Type: application/json" `
  -d '{
    "nom": "Parcelle Test",
    "superficie": 1.5,
    "typeSol": "ARGILEUX",
    "typeCulture": "LEGUMES"
  }'
```

**Create Programme:**
```powershell
curl -X POST http://localhost:8082/api/programmes `
  -H "Content-Type: application/json" `
  -d '{
    "parcelleId": 1,
    "datePlanifiee": "2025-11-18T14:00:00",
    "duree": 60,
    "volumePrevu": 25.0,
    "statut": "PLANIFIE"
  }'
```

**Get All Programmes:**
```powershell
curl http://localhost:8082/api/programmes
```

## Troubleshooting

### Service Won't Start

**Check if port is in use:**
```powershell
netstat -ano | findstr :8081
netstat -ano | findstr :8082
```

**Kill process if needed:**
```powershell
Stop-Process -Id <PID> -Force
```

### Database Connection Issues

**Check if PostgreSQL is running:**
```powershell
docker ps | Select-String postgres
```

**Restart databases:**
```powershell
docker-compose restart
```

### Maven Issues

**Clear Maven cache:**
```powershell
mvn clean
rm -r ~/.m2/repository  # Or manually delete the folder
mvn clean install
```

## Expected Test Results

When running `test-all-apis.ps1`, you should see:

```
========================================
  TEST SUMMARY
========================================

Total Tests: 35-40
Passed: 35-40
Failed: 0
Errors: 0

Success Rate: 100%

✓ All tests passed successfully!
```

## Test Coverage

The test script covers:

**Meteo Service:**
- ✓ Health check
- ✓ GET all previsions
- ✓ POST create prevision
- ✓ GET prevision by ID
- ✓ PUT update prevision
- ✓ GET previsions by date range
- ✓ GET prevision by date
- ✓ DELETE prevision

**Arrosage Service - Parcelles:**
- ✓ GET all parcelles
- ✓ POST create parcelle
- ✓ GET parcelle by ID
- ✓ PUT update parcelle
- ✓ GET parcelles by culture type
- ✓ GET parcelles by soil type
- ✓ DELETE parcelle

**Arrosage Service - Programmes:**
- ✓ GET all programmes
- ✓ POST create programme
- ✓ GET programme by ID
- ✓ PUT update programme
- ✓ GET programmes by status
- ✓ GET programmes by parcelle
- ✓ DELETE programme

**Arrosage Service - Journaux:**
- ✓ GET all journal entries
- ✓ POST create journal entry
- ✓ GET journal by ID
- ✓ GET journals by programme
- ✓ GET journals by date range
- ✓ DELETE journal entry

## Quick Commands Reference

```powershell
# Start everything
docker-compose up -d
cd backend\meteo-service; mvn spring-boot:run    # Terminal 1
cd backend\arrosage-service; mvn spring-boot:run # Terminal 2

# Run tests
.\test-all-apis.ps1

# Stop everything
# Ctrl+C in each terminal
docker-compose down
```
