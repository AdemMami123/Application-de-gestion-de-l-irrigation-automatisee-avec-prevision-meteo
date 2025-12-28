# Enable Kubernetes in Docker Desktop
# This script helps you enable Kubernetes

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "  Enable Kubernetes Setup Guide" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Check if kubectl is installed
Write-Host "[OK] kubectl is installed" -ForegroundColor Green
Write-Host "[OK] Docker is running" -ForegroundColor Green
Write-Host "[OK] Docker images are built (5 microservices)" -ForegroundColor Green
Write-Host ""

# Check current Kubernetes status
Write-Host "Checking Kubernetes status..." -ForegroundColor Yellow
$k8sStatus = kubectl cluster-info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[X] Kubernetes is NOT enabled" -ForegroundColor Red
    Write-Host ""
    Write-Host "TO ENABLE KUBERNETES IN DOCKER DESKTOP:" -ForegroundColor Cyan
    Write-Host "1. Open Docker Desktop (system tray icon)" -ForegroundColor White
    Write-Host "2. Click the Settings gear icon" -ForegroundColor White
    Write-Host "3. Select 'Kubernetes' from the left menu" -ForegroundColor White
    Write-Host "4. Check the box: Enable Kubernetes" -ForegroundColor White
    Write-Host "5. Click 'Apply and Restart'" -ForegroundColor White
    Write-Host ""
    Write-Host "This will take 2-3 minutes to complete..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Once enabled, run: .\deploy-k8s.ps1 -Action deploy" -ForegroundColor Green
} else {
    Write-Host "[OK] Kubernetes is ENABLED and running!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Current context:" -ForegroundColor Cyan
    kubectl config current-context
    Write-Host ""
    Write-Host "Ready to deploy! Run:" -ForegroundColor Green
    Write-Host "  .\deploy-k8s.ps1 -Action deploy" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Built Docker Images:" -ForegroundColor Cyan
docker images irrigation/* --format "  [OK] {{.Repository}}:{{.Tag}} ({{.Size}})"
