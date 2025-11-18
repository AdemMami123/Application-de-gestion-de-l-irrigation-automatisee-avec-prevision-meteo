# Scheduled Irrigation - Quick Test Guide

## 🚀 Quick Test (2 Minutes)

### Prerequisites
```powershell
# Ensure services are running
docker-compose up -d postgres-arrosage
cd backend\arrosage-service
mvn spring-boot:run
```

---

## Test Scenario 1: Immediate Execution

### Step 1: Create a Programme (Scheduled for the Past)
```powershell
curl -X POST http://localhost:8082/api/programmes `
  -H "Content-Type: application/json" `
  -d '{
    "parcelleId": 1,
    "datePlanifiee": "2025-11-18T08:00:00",
    "duree": 60,
    "volumePrevu": 25.0,
    "statut": "PLANIFIE"
  }'
```

**Response:**
```json
{
  "id": 1,
  "parcelleId": 1,
  "datePlanifiee": "2025-11-18T08:00:00",
  "duree": 60,
  "volumePrevu": 25.0,
  "statut": "PLANIFIE"
}
```

### Step 2: Wait for Scheduler (max 5 minutes)

**Watch the logs:**
```
[10:00:00] === Starting scheduled irrigation execution check ===
[10:00:00] Found 1 program(s) scheduled for execution
[10:00:01] Starting execution of program 1 for parcelle Parcelle Nord
[10:00:01] Successfully completed program 1 - Volume: 24.3 m³
[10:00:01] Successfully executed 1 irrigation program(s)
```

### Step 3: Verify Execution

**Check Programme Status:**
```powershell
curl http://localhost:8082/api/programmes/1
```

**Expected Response:**
```json
{
  "id": 1,
  "parcelleId": 1,
  "datePlanifiee": "2025-11-18T08:00:00",
  "duree": 60,
  "volumePrevu": 25.0,
  "statut": "TERMINE"  // ← Changed from PLANIFIE
}
```

**Check Journal Entry:**
```powershell
curl http://localhost:8082/api/journaux/programme/1
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "programmeId": 1,
    "dateExecution": "2025-11-18T10:00:01",
    "volumeReel": 24.3,  // ± 10% of 25.0
    "remarque": "Arrosage effectué avec succès, volume conforme aux prévisions"
  }
]
```

---

## Test Scenario 2: Multiple Programmes

### Create Multiple Programmes
```powershell
# Programme 1 - Past
curl -X POST http://localhost:8082/api/programmes `
  -H "Content-Type: application/json" `
  -d '{
    "parcelleId": 1,
    "datePlanifiee": "2025-11-18T06:00:00",
    "duree": 60,
    "volumePrevu": 25.0,
    "statut": "PLANIFIE"
  }'

# Programme 2 - Past
curl -X POST http://localhost:8082/api/programmes `
  -H "Content-Type: application/json" `
  -d '{
    "parcelleId": 2,
    "datePlanifiee": "2025-11-18T07:00:00",
    "duree": 45,
    "volumePrevu": 20.0,
    "statut": "PLANIFIE"
  }'

# Programme 3 - Future (should NOT execute)
curl -X POST http://localhost:8082/api/programmes `
  -H "Content-Type: application/json" `
  -d '{
    "parcelleId": 3,
    "datePlanifiee": "2025-11-20T10:00:00",
    "duree": 90,
    "volumePrevu": 35.0,
    "statut": "PLANIFIE"
  }'
```

### Wait for Scheduler

**Expected Logs:**
```
[10:05:00] Found 2 program(s) scheduled for execution
[10:05:01] Successfully completed program 1 - Volume: 23.7 m³
[10:05:01] Successfully completed program 2 - Volume: 21.5 m³
[10:05:01] Execution summary: 2 succeeded, 0 failed
```

### Verify Results
```powershell
# Check all programmes
curl http://localhost:8082/api/programmes

# Programme 1 & 2 should be TERMINE
# Programme 3 should still be PLANIFIE
```

---

## Test Scenario 3: Volume Variance

### Create Programme and Execute Multiple Times

```powershell
# Create 5 programmes with same parameters
for ($i=1; $i -le 5; $i++) {
    curl -X POST http://localhost:8082/api/programmes `
      -H "Content-Type: application/json" `
      -d "{
        \"parcelleId\": 1,
        \"datePlanifiee\": \"2025-11-18T08:00:00\",
        \"duree\": 60,
        \"volumePrevu\": 25.0,
        \"statut\": \"PLANIFIE\"
      }"
}
```

### After Execution, Check Variance

```powershell
curl http://localhost:8082/api/journaux

# Expected volumes should all be between 22.5 and 27.5 m³
# Examples:
# - 24.3 m³ (97.2% of planned)
# - 26.1 m³ (104.4% of planned)
# - 23.8 m³ (95.2% of planned)
# - 25.5 m³ (102% of planned)
# - 24.9 m³ (99.6% of planned)
```

---

## Test Scenario 4: Manual Trigger (for Testing)

### Create Service Endpoint (Temporary)

Add to `ProgrammeArrosageController.java`:
```java
@PostMapping("/execute-now")
public ResponseEntity<String> executeNow() {
    int count = executionService.executeScheduledPrograms(LocalDateTime.now());
    return ResponseEntity.ok("Executed " + count + " programs");
}
```

### Trigger Manually
```powershell
curl -X POST http://localhost:8082/api/programmes/execute-now
```

**Response:**
```
Executed 3 programs
```

---

## 🔍 Monitoring Commands

### Check Scheduler Configuration
```powershell
# View application properties
curl http://localhost:8082/actuator/env | Select-String "scheduler"
```

### View Scheduled Tasks (via Actuator)
```powershell
# Add to application.properties:
# management.endpoints.web.exposure.include=scheduledtasks

curl http://localhost:8082/actuator/scheduledtasks
```

**Response:**
```json
{
  "cron": [
    {
      "runnable": {
        "target": "IrrigationScheduler.executeScheduledIrrigationPrograms"
      },
      "expression": "0 */5 * * * *"
    },
    {
      "runnable": {
        "target": "IrrigationScheduler.cleanupOldPrograms"
      },
      "expression": "0 0 0 * * *"
    }
  ]
}
```

### Check Database Directly
```powershell
# Connect to PostgreSQL
docker exec -it postgres-arrosage psql -U arrosage_user -d arrosagedb

# Query programmes
SELECT id, parcelle_id, date_planifiee, statut FROM programme_arrosage;

# Query journal entries
SELECT id, programme_id, date_execution, volume_reel FROM journal_arrosage;

# Count by status
SELECT statut, COUNT(*) FROM programme_arrosage GROUP BY statut;
```

---

## ⚙️ Scheduler Control

### Disable Scheduler (application.properties)
```properties
app.scheduler.irrigation.enabled=false
```

### Change Frequency
```properties
# Every minute (testing)
app.scheduler.irrigation.cron=0 * * * * *

# Every 10 seconds (aggressive testing)
app.scheduler.irrigation.cron=*/10 * * * * *

# Every hour
app.scheduler.irrigation.cron=0 0 * * * *

# Production (every 5 minutes)
app.scheduler.irrigation.cron=0 */5 * * * *
```

### Restart to Apply Changes
```powershell
# Stop application (Ctrl+C)
# Restart
mvn spring-boot:run
```

---

## 📊 Expected Logs Breakdown

### Normal Execution (Successful)
```
2025-11-18 10:00:00 - INFO  - === Starting scheduled irrigation execution check at 2025-11-18T10:00:00 ===
2025-11-18 10:00:00 - INFO  - Found 2 program(s) scheduled for execution
2025-11-18 10:00:01 - INFO  - Starting execution of program 5 for parcelle Parcelle Nord
2025-11-18 10:00:01 - DEBUG - Simulated irrigation execution - Planned: 25.0 m³, Actual: 24.3 m³ (3% variance)
2025-11-18 10:00:01 - DEBUG - Created journal entry for program 5
2025-11-18 10:00:01 - INFO  - Successfully completed program 5 - Volume: 24.3 m³, Duration: 60 min
2025-11-18 10:00:01 - INFO  - Starting execution of program 6 for parcelle Parcelle Sud
2025-11-18 10:00:01 - DEBUG - Simulated irrigation execution - Planned: 20.0 m³, Actual: 21.2 m³ (6% variance)
2025-11-18 10:00:01 - DEBUG - Created journal entry for program 6
2025-11-18 10:00:02 - INFO  - Successfully completed program 6 - Volume: 21.2 m³, Duration: 45 min
2025-11-18 10:00:02 - INFO  - Execution summary: 2 succeeded, 0 failed
2025-11-18 10:00:02 - INFO  - Successfully executed 2 irrigation program(s)
2025-11-18 10:00:02 - DEBUG - === Completed scheduled irrigation execution check ===
```

### No Programmes to Execute
```
2025-11-18 10:05:00 - INFO  - === Starting scheduled irrigation execution check at 2025-11-18T10:05:00 ===
2025-11-18 10:05:00 - DEBUG - No programs scheduled for execution at 2025-11-18T10:05:00
2025-11-18 10:05:00 - DEBUG - No irrigation programs to execute at this time
2025-11-18 10:05:00 - DEBUG - === Completed scheduled irrigation execution check ===
```

### Execution Failure
```
2025-11-18 10:00:01 - ERROR - Failed to execute program 7: Foreign key constraint violation
2025-11-18 10:00:01 - WARN  - Marked program 7 as ANNULE due to execution failure
2025-11-18 10:00:01 - INFO  - Execution summary: 1 succeeded, 1 failed
```

---

## 🧪 Run Unit Tests

```powershell
cd backend\arrosage-service

# Run all scheduler tests
mvn test -Dtest=IrrigationSchedulerTest

# Run all execution service tests
mvn test -Dtest=IrrigationExecutionServiceTest

# Run all irrigation-related tests
mvn test -Dtest=*Irrigation*
```

**Expected:**
```
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 🔄 Reset Test Data

```powershell
# Connect to database
docker exec -it postgres-arrosage psql -U arrosage_user -d arrosagedb

# Delete all journal entries
DELETE FROM journal_arrosage;

# Delete all programmes
DELETE FROM programme_arrosage;

# Reset sequences
ALTER SEQUENCE programme_arrosage_id_seq RESTART WITH 1;
ALTER SEQUENCE journal_arrosage_id_seq RESTART WITH 1;

# Verify
SELECT COUNT(*) FROM programme_arrosage;  -- Should be 0
SELECT COUNT(*) FROM journal_arrosage;    -- Should be 0
```

---

## ⏱️ Timing Reference

| Action | Expected Time |
|--------|---------------|
| Create programme (API) | < 100ms |
| Scheduler cycle (no programs) | < 50ms |
| Execute single programme | 50-100ms |
| Execute 10 programmes | 500ms-1s |
| Journal query | < 20ms |

---

## ✅ Success Criteria

After running tests, you should see:

**✅ Database:**
- Programmes status changed from PLANIFIE to TERMINE
- Journal entries created for each execution
- Volume variance within ±10% of planned

**✅ Logs:**
- Regular scheduler execution every 5 minutes
- Clear execution flow messages
- No error messages (unless testing failures)

**✅ API Responses:**
- Programme statut = "TERMINE"
- Journal entries with realistic volumes
- Appropriate remarques

---

## 📖 Additional Resources

- **Full Documentation:** [SCHEDULED-IRRIGATION.md](SCHEDULED-IRRIGATION.md)
- **API Docs:** http://localhost:8082/swagger-ui.html
- **Actuator:** http://localhost:8082/actuator/health

---

**Quick Test Version:** 1.0  
**Last Updated:** November 18, 2025
