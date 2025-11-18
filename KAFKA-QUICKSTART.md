# Kafka Integration - Quick Start Guide

## 🚀 Quick Start (5 Minutes)

### 1. Start Infrastructure
```powershell
# From project root
docker-compose up -d zookeeper kafka kafka-ui
```

**Wait for Kafka to be ready (~30 seconds):**
```powershell
docker-compose logs -f kafka
# Look for: "started (kafka.server.KafkaServer)"
# Press Ctrl+C to exit logs
```

### 2. Start Services

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

### 3. Verify Setup

**Check Kafka UI:** http://localhost:8090
- Topics → Should see `weather-change-events`
- Consumer Groups → Should see `arrosage-service-group`

**Check Services:**
- Meteo Service: http://localhost:8081/swagger-ui.html
- Arrosage Service: http://localhost:8082/swagger-ui.html

---

## 🧪 Test Scenarios

### Scenario 1: High Temperature Change (HIGH Severity)

**Step 1 - Create Initial Prevision:**
```powershell
curl -X POST http://localhost:8081/api/previsions `
  -H "Content-Type: application/json" `
  -d '{
    "stationId": 1,
    "date": "2025-11-20",
    "temperatureMax": 25.0,
    "temperatureMin": 15.0,
    "pluiePrevue": 0.0,
    "vent": 5.0
  }'
```

**Step 2 - Create Programme for Tomorrow:**
```powershell
curl -X POST http://localhost:8082/api/programmes `
  -H "Content-Type: application/json" `
  -d '{
    "parcelleId": 1,
    "datePlanifiee": "2025-11-20T08:00:00",
    "duree": 60,
    "volumePrevu": 25.0,
    "statut": "PLANIFIE"
  }'
```

**Step 3 - Update Prevision (Trigger Event):**
```powershell
curl -X PUT http://localhost:8081/api/previsions/1 `
  -H "Content-Type: application/json" `
  -d '{
    "stationId": 1,
    "date": "2025-11-20",
    "temperatureMax": 35.0,
    "temperatureMin": 22.0,
    "pluiePrevue": 0.0,
    "vent": 5.0
  }'
```

**Expected Results:**
- **Meteo logs:** "Publishing weather change event for station 1 with severity HIGH"
- **Arrosage logs:** "Programme X volume increased by 30% for high temperature"
- **Kafka UI:** Message visible in `weather-change-events` topic
- **Programme updated:** Volume ~32.5 m³, Duration ~72 min

---

### Scenario 2: Heavy Rain Forecast (CRITICAL Severity)

**Step 1 - Create Programme:**
```powershell
curl -X POST http://localhost:8082/api/programmes `
  -H "Content-Type: application/json" `
  -d '{
    "parcelleId": 1,
    "datePlanifiee": "2025-11-21T10:00:00",
    "duree": 90,
    "volumePrevu": 35.0,
    "statut": "PLANIFIE"
  }'
```

**Step 2 - Update Weather with Heavy Rain:**
```powershell
curl -X PUT http://localhost:8081/api/previsions/1 `
  -H "Content-Type: application/json" `
  -d '{
    "stationId": 1,
    "date": "2025-11-21",
    "temperatureMax": 20.0,
    "temperatureMin": 12.0,
    "pluiePrevue": 25.0,
    "vent": 10.0
  }'
```

**Expected Results:**
- **Severity:** CRITICAL
- **Action:** Programme CANCELLED
- **Status change:** PLANIFIE → ANNULE

---

### Scenario 3: High Wind (CRITICAL Severity)

**Step 1 - Update Weather with High Wind:**
```powershell
curl -X PUT http://localhost:8081/api/previsions/1 `
  -H "Content-Type: application/json" `
  -d '{
    "stationId": 1,
    "date": "2025-11-22",
    "temperatureMax": 28.0,
    "temperatureMin": 18.0,
    "pluiePrevue": 2.0,
    "vent": 35.0
  }'
```

**Expected Results:**
- **Severity:** CRITICAL
- **Action:** Duration increased by 40%
- **Reason:** Compensation for wind evaporation

---

## 🔍 Monitoring & Debugging

### View Kafka Messages

**Kafka UI (Recommended):**
1. Go to http://localhost:8090
2. Click "Topics"
3. Click "weather-change-events"
4. Click "Messages" tab
5. Inspect JSON payload

**Command Line:**
```powershell
docker exec -it kafka kafka-console-consumer `
  --bootstrap-server localhost:9093 `
  --topic weather-change-events `
  --from-beginning `
  --property print.key=true `
  --property print.value=true
```

### Check Consumer Lag

```powershell
docker exec -it kafka kafka-consumer-groups `
  --bootstrap-server localhost:9093 `
  --group arrosage-service-group `
  --describe
```

**Expected Output:**
```
GROUP                    TOPIC                  PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
arrosage-service-group   weather-change-events  0          5               5               0
arrosage-service-group   weather-change-events  1          3               3               0
arrosage-service-group   weather-change-events  2          2               2               0
```

**LAG = 0** means consumer is caught up ✅

### Service Logs

**Meteo Service:**
```powershell
# In the terminal running meteo-service, watch for:
Publishing weather change event for station X with severity Y
Successfully published weather change event - Offset: Z
```

**Arrosage Service:**
```powershell
# In the terminal running arrosage-service, watch for:
Received weather change event - Station: X, Severity: Y
Programme Z ADJUSTED/CANCELLED due to...
Successfully processed weather change event for station X
```

---

## 🧪 Run Integration Tests

### Meteo Service (Producer Tests)

```powershell
cd backend\meteo-service
mvn test -Dtest=KafkaWeatherProducerIntegrationTest
```

**Expected:**
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

### Arrosage Service (Consumer Tests)

```powershell
cd backend\arrosage-service
mvn test -Dtest=KafkaWeatherConsumerIntegrationTest
```

**Expected:**
```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

---

## ⚠️ Common Issues & Solutions

### Issue: Kafka Not Starting

**Error:** "Connection refused: localhost:9092"

**Solution:**
```powershell
# Check Kafka status
docker-compose ps

# Restart Kafka
docker-compose restart kafka

# Check logs
docker-compose logs kafka
```

### Issue: Events Not Published

**Symptom:** No logs about publishing events

**Solution:**
1. Check if weather change is significant enough (diff > thresholds)
2. Verify update endpoint was called (not create)
3. Check severity is MEDIUM, HIGH, or CRITICAL (LOW not published)

### Issue: Events Not Consumed

**Symptom:** Messages in Kafka but not processed

**Solution:**
```powershell
# Check consumer group status
docker exec -it kafka kafka-consumer-groups `
  --bootstrap-server localhost:9093 `
  --group arrosage-service-group `
  --describe

# Restart arrosage-service
# Press Ctrl+C in Terminal 2, then:
mvn spring-boot:run
```

### Issue: Programmes Not Adjusted

**Symptom:** Event consumed but no changes

**Solution:**
1. Verify programme `datePlanifiee` is within ±12-36 hours of event date
2. Check programme status is `PLANIFIE` (not EN_COURS or TERMINE)
3. Review business logic thresholds in logs

---

## 📊 Expected Performance

| Metric | Value |
|--------|-------|
| Event Publish Time | < 10ms |
| Event Consumption Time | < 50ms |
| Programme Adjustment Time | < 200ms |
| End-to-End Latency | < 300ms |

---

## 🎯 Severity Calculation Reference

### Temperature Change
- **CRITICAL:** > 10°C difference
- **HIGH:** > 5°C difference
- **MEDIUM:** > 3°C difference
- **LOW:** ≤ 3°C difference

### Rain Change
- **CRITICAL:** > 20mm difference
- **HIGH:** > 10mm difference
- **MEDIUM:** > 5mm difference
- **LOW:** ≤ 5mm difference

### Wind Change
- **CRITICAL:** > 20 km/h difference
- **HIGH:** > 10 km/h difference
- **MEDIUM:** > 5 km/h difference
- **LOW:** ≤ 5 km/h difference

**Final Severity:** Highest of the three calculations

---

## 🔄 Reset & Clean Up

### Reset Kafka Topics

```powershell
# Delete topic
docker exec -it kafka kafka-topics `
  --bootstrap-server localhost:9093 `
  --delete --topic weather-change-events

# Topic will be auto-recreated on next message
```

### Reset Consumer Group Offset

```powershell
# Reset to earliest
docker exec -it kafka kafka-consumer-groups `
  --bootstrap-server localhost:9093 `
  --group arrosage-service-group `
  --reset-offsets --to-earliest `
  --topic weather-change-events `
  --execute
```

### Clean Database

```powershell
# Connect to PostgreSQL
docker exec -it postgres-meteo psql -U meteo_user -d meteodb

# Delete previsions
DELETE FROM prevision;

# Connect to arrosage DB
docker exec -it postgres-arrosage psql -U arrosage_user -d arrosagedb

# Delete programmes
DELETE FROM programme_arrosage;
```

---

## 📖 Additional Resources

- **Full Documentation:** [KAFKA-INTEGRATION.md](KAFKA-INTEGRATION.md)
- **Kafka UI:** http://localhost:8090
- **Meteo API Docs:** http://localhost:8081/swagger-ui.html
- **Arrosage API Docs:** http://localhost:8082/swagger-ui.html

---

**Quick Start Version:** 1.0  
**Last Updated:** November 18, 2025
