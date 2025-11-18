# SUPABASE MIGRATION GUIDE

## Configuration Summary

**Supabase Project:** uphsmjapfljujaeaocux
**Database URL:** `aws-0-eu-central-1.pooler.supabase.com:6543`
**Database:** `postgres`
**Username:** `postgres.uphsmjapfljujaeaocux`

## Step 1: Run SQL Migration on Supabase

1. **Open Supabase Dashboard:**
   - Go to: https://app.supabase.com/project/uphsmjapfljujaeaocux
   - Login to your Supabase account

2. **Navigate to SQL Editor:**
   - Click on "SQL Editor" in the left sidebar
   - Click "New Query"

3. **Run the Migration Script:**
   - Open the file: `supabase-migration.sql`
   - Copy the entire content
   - Paste it into the Supabase SQL Editor
   - Click "Run" or press `Ctrl+Enter`

4. **Verify Tables Created:**
   - Go to "Table Editor" in Supabase
   - You should see 5 tables:
     * `station_meteo` (Meteo Service)
     * `prevision` (Meteo Service)
     * `parcelle` (Arrosage Service)
     * `programme_arrosage` (Arrosage Service)
     * `journal_arrosage` (Arrosage Service)

## Step 2: Configuration Files Updated

### Meteo Service
**File:** `backend/meteo-service/src/main/resources/application.properties`

```properties
# Supabase Database Configuration
spring.datasource.url=jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require
spring.datasource.username=postgres.uphsmjapfljujaeaocux
spring.datasource.password=Ademdem@123
spring.datasource.driver-class-name=org.postgresql.Driver

# Flyway disabled (migration done manually on Supabase)
spring.flyway.enabled=false
```

### Arrosage Service
**File:** `backend/arrosage-service/src/main/resources/application.properties`

```properties
# Supabase Database Configuration  
spring.datasource.url=jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require
spring.datasource.username=postgres.uphsmjapfljujaeaocux
spring.datasource.password=Ademdem@123
spring.datasource.driver-class-name=org.postgresql.Driver

# Flyway disabled (migration done manually on Supabase)
spring.flyway.enabled=false
```

## Step 3: Build Services

```powershell
# Build meteo-service
cd backend\meteo-service
mvn clean package -DskipTests

# Build arrosage-service
cd ..\arrosage-service
mvn clean package -DskipTests
```

## Step 4: Start Services

### Terminal 1 - Meteo Service
```powershell
cd backend\meteo-service
java -jar target\meteo-service-0.0.1-SNAPSHOT.jar
```

### Terminal 2 - Arrosage Service
```powershell
cd backend\arrosage-service
java -jar target\arrosage-service-0.0.1-SNAPSHOT.jar
```

## Step 5: Test Database Connection

Run the test script:
```powershell
.\test-supabase-connection.ps1
```

Or test manually:
```powershell
# Test meteo-service health
curl http://localhost:8081/actuator/health

# Test arrosage-service health  
curl http://localhost:8082/actuator/health

# Get all stations meteo
curl http://localhost:8081/api/stations

# Get all parcelles
curl http://localhost:8082/api/parcelles
```

## Troubleshooting

### Connection Timeout
If you see "Connection timed out":
- Check your internet connection
- Verify Supabase project is active
- Check firewall settings

### Authentication Failed
If you see "password authentication failed":
- Verify the password in application.properties: `Ademdem@123`
- Check the username is correct: `postgres.uphsmjapfljujaeaocux`

### SSL/TLS Errors
If you see SSL errors:
- The connection string includes `?sslmode=require`
- Ensure your JDK supports TLS 1.2+

### Tables Not Found
If you see "relation does not exist":
- Go back to Step 1 and run the migration script
- Verify tables exist in Supabase Table Editor

## Verification Queries

Run these in Supabase SQL Editor to verify:

```sql
-- Check all tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public'
ORDER BY table_name;

-- Count records in each table
SELECT 'station_meteo' as table_name, COUNT(*) as count FROM station_meteo
UNION ALL
SELECT 'prevision', COUNT(*) FROM prevision
UNION ALL
SELECT 'parcelle', COUNT(*) FROM parcelle
UNION ALL
SELECT 'programme_arrosage', COUNT(*) FROM programme_arrosage
UNION ALL
SELECT 'journal_arrosage', COUNT(*) FROM journal_arrosage;

-- View sample data
SELECT * FROM station_meteo LIMIT 5;
SELECT * FROM prevision LIMIT 5;
SELECT * FROM parcelle LIMIT 5;
SELECT * FROM programme_arrosage LIMIT 5;
SELECT * FROM journal_arrosage LIMIT 5;
```

## Environment Variables (Optional)

For better security, you can use environment variables instead of hardcoding credentials:

```properties
# In application.properties
spring.datasource.url=${SUPABASE_DB_URL}
spring.datasource.username=${SUPABASE_DB_USER}
spring.datasource.password=${SUPABASE_DB_PASSWORD}
```

Then set them before running:
```powershell
$env:SUPABASE_DB_URL="jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require"
$env:SUPABASE_DB_USER="postgres.uphsmjapfljujaeaocux"
$env:SUPABASE_DB_PASSWORD="Ademdem@123"
```

## Supabase Dashboard Quick Links

- **Dashboard:** https://app.supabase.com/project/uphsmjapfljujaeaocux
- **SQL Editor:** https://app.supabase.com/project/uphsmjapfljujaeaocux/sql
- **Table Editor:** https://app.supabase.com/project/uphsmjapfljujaeaocux/editor
- **API Docs:** https://app.supabase.com/project/uphsmjapfljujaeaocux/api

## Next Steps

1. ✅ Run migration on Supabase
2. ✅ Build both services
3. ✅ Start both services
4. ✅ Test database connection
5. ✅ Run API tests: `.\test-all-apis.ps1`
