# Quick Start Guide - API Testing

## Current Status

✓ Test scripts created successfully:
- `test-all-apis.ps1` - Comprehensive API test suite
- `run-api-tests.ps1` - Pre-flight check + test runner
- `start-and-test.ps1` - Auto-start services + tests
- `API-TESTING-GUIDE.md` - Complete documentation

## Next Steps to Test Your APIs

### Option 1: Quick Manual Test (Recommended)

1. **Install Maven** (run PowerShell as Administrator):
   ```powershell
   choco install maven -y
   ```
   Then close and reopen your terminal.

2. **Start Database Services**:
   ```powershell
   docker-compose up -d
   ```

3. **Start Meteo Service** (Terminal 1):
   ```powershell
   cd backend\meteo-service
   mvn spring-boot:run
   ```

4. **Start Arrosage Service** (Terminal 2):
   ```powershell
   cd backend\arrosage-service
   mvn spring-boot:run
   ```

5. **Run API Tests** (Terminal 3):
   ```powershell
   .\test-all-apis.ps1
   ```

### Option 2: Use IDE

1. Open the project in IntelliJ IDEA or VS Code
2. Run `MeteoServiceApplication.java`
3. Run `ArrosageServiceApplication.java`
4. In terminal:
   ```powershell
   .\test-all-apis.ps1
   ```

### Option 3: Quick Manual API Check

Without running the full test suite, you can test individual endpoints:

```powershell
# Start services first (see Option 1)

# Test meteo-service health
curl http://localhost:8081/actuator/health

# Test arrosage-service health
curl http://localhost:8082/actuator/health

# Create a prevision
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

# Get all previsions
curl http://localhost:8081/api/previsions
```

## What the Test Script Does

The `test-all-apis.ps1` script will:

1. ✓ Check service health (meteo + arrosage)
2. ✓ Test all Meteo Service endpoints:
   - GET /api/previsions
   - POST /api/previsions
   - GET /api/previsions/{id}
   - PUT /api/previsions/{id}
   - GET /api/previsions/date-range
   - GET /api/previsions/date/{date}
   - DELETE /api/previsions/{id}

3. ✓ Test all Arrosage Service - Parcelles endpoints:
   - GET /api/parcelles
   - POST /api/parcelles
   - GET /api/parcelles/{id}
   - PUT /api/parcelles/{id}
   - GET /api/parcelles/culture/{type}
   - GET /api/parcelles/sol/{type}
   - DELETE /api/parcelles/{id}

4. ✓ Test all Programme Arrosage endpoints:
   - GET /api/programmes
   - POST /api/programmes
   - GET /api/programmes/{id}
   - PUT /api/programmes/{id}
   - GET /api/programmes/statut/{statut}
   - GET /api/programmes/parcelle/{id}
   - DELETE /api/programmes/{id}

5. ✓ Test all Journal Arrosage endpoints:
   - GET /api/journaux
   - POST /api/journaux
   - GET /api/journaux/{id}
   - GET /api/journaux/programme/{id}
   - GET /api/journaux/date-range
   - DELETE /api/journaux/{id}

6. ✓ Generate comprehensive test report with:
   - Total tests run
   - Passed/Failed/Errors count
   - Success rate percentage
   - Detailed results table

## Expected Output

When all tests pass, you'll see:

```
========================================
  TEST SUMMARY
========================================

Total Tests: 38
Passed: 38
Failed: 0
Errors: 0

Detailed Results:

Name                                    Status StatusCode
----                                    ------ ----------
Meteo Service Health                    PASS   200
Arrosage Service Health                 PASS   200
GET All Previsions                      PASS   200
POST Create Prevision                   PASS   200
GET Prevision by ID                     PASS   200
PUT Update Prevision                    PASS   200
... (and so on)

Success Rate: 100%

✓ All tests passed successfully!
```

## Troubleshooting

**Maven not found:**
- Run PowerShell as Administrator
- Execute: `choco install maven -y`
- Close and reopen terminal

**Services won't start:**
- Check if ports 8081/8082 are available: `netstat -ano | findstr :8081`
- Make sure databases are running: `docker ps`
- Check application.properties for correct database URLs

**Tests fail:**
- Verify services are running: `curl http://localhost:8081/actuator/health`
- Check logs in service terminals
- Ensure databases have correct schema

## Files Created

- ✓ `test-all-apis.ps1` - Main test script (400+ lines)
- ✓ `run-api-tests.ps1` - Launcher with service checks
- ✓ `start-and-test.ps1` - Automated setup + tests
- ✓ `API-TESTING-GUIDE.md` - Complete documentation
- ✓ `QUICK-START.md` - This file

## Reference Documents

- Full guide: `API-TESTING-GUIDE.md`
- Kafka testing: `KAFKA-QUICKSTART.md`
- Scheduler testing: `SCHEDULED-IRRIGATION-QUICKTEST.md`

---

**Ready to test!** 🚀

Just start the services and run: `.\test-all-apis.ps1`
