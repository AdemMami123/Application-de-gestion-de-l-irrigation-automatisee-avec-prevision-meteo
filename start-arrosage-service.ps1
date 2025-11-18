# Start Arrosage Service  
Write-Host "========================================" -ForegroundColor Green
Write-Host " Starting Arrosage Service on port 8082" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

cd backend\arrosage-service
java -jar target\arrosage-service-0.0.1-SNAPSHOT.jar
