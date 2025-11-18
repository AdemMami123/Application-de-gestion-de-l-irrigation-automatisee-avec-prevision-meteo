# Start Meteo Service
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Starting Meteo Service on port 8081" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

cd backend\meteo-service
java -jar target\meteo-service-0.0.1-SNAPSHOT.jar
