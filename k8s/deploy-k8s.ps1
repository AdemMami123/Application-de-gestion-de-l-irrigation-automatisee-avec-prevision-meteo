# Kubernetes Deployment Script for Irrigation Management System
# This script deploys all components in the correct order with proper health checks

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('deploy', 'delete', 'status', 'logs')]
    [string]$Action = 'deploy',
    
    [Parameter(Mandatory=$false)]
    [string]$Namespace = 'irrigation-system'
)

$ErrorActionPreference = "Stop"

# Color output functions
function Write-Success {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Cyan
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-ErrorMsg {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Write-Step {
    param([string]$Message)
    Write-Host "`n==> $Message" -ForegroundColor Magenta
    Write-Host "================================================" -ForegroundColor Magenta
}

# Check if kubectl is installed
function Test-KubectlInstalled {
    try {
        kubectl version --client | Out-Null
        return $true
    } catch {
        Write-ErrorMsg "kubectl is not installed or not in PATH"
        Write-Info "Install kubectl: choco install kubernetes-cli"
        return $false
    }
}

# Wait for pods to be ready
function Wait-ForPods {
    param(
        [string]$Label,
        [string]$Namespace,
        [int]$TimeoutSeconds = 180
    )
    
    Write-Info "Waiting for pods with label '$Label' to be ready (timeout: ${TimeoutSeconds}s)..."
    
    try {
        kubectl wait --for=condition=ready pod -l $Label -n $Namespace --timeout="${TimeoutSeconds}s"
        Write-Success "Pods with label '$Label' are ready"
        return $true
    } catch {
        Write-Warning "Timeout waiting for pods with label '$Label'"
        return $false
    }
}

# Deploy function
function Deploy-IrrigationSystem {
    Write-Step "Starting Irrigation Management System Deployment"
    
    # Check kubectl
    if (-not (Test-KubectlInstalled)) {
        exit 1
    }
    
    # Step 1: Create Namespace
    Write-Step "Step 1: Creating Namespace"
    kubectl apply -f namespace.yaml
    Write-Success "Namespace created"
    
    # Step 2: Create Secrets
    Write-Step "Step 2: Creating Secrets"
    kubectl apply -f secrets/
    Write-Success "Secrets created"
    
    # Step 3: Create Persistent Volume Claims
    Write-Step "Step 3: Creating Persistent Volume Claims"
    kubectl apply -f persistent-volumes/
    Write-Success "PVCs created"
    
    # Step 4: Deploy Infrastructure - Zookeeper
    Write-Step "Step 4: Deploying Zookeeper"
    kubectl apply -f deployments/zookeeper-deployment.yaml
    kubectl apply -f services/zookeeper-service.yaml
    Start-Sleep -Seconds 5
    Wait-ForPods -Label "app=zookeeper" -Namespace $Namespace -TimeoutSeconds 120
    
    # Step 5: Deploy Kafka
    Write-Step "Step 5: Deploying Kafka"
    kubectl apply -f deployments/kafka-deployment.yaml
    kubectl apply -f services/kafka-service.yaml
    Start-Sleep -Seconds 10
    Wait-ForPods -Label "app=kafka" -Namespace $Namespace -TimeoutSeconds 120
    
    # Step 6: Deploy Redis
    Write-Step "Step 6: Deploying Redis"
    kubectl apply -f deployments/redis-deployment.yaml
    kubectl apply -f services/redis-service.yaml
    Start-Sleep -Seconds 5
    Wait-ForPods -Label "app=redis" -Namespace $Namespace -TimeoutSeconds 120
    
    # Step 7: Deploy PostgreSQL Databases
    Write-Step "Step 7: Deploying PostgreSQL Databases"
    kubectl apply -f deployments/postgres-meteo-deployment.yaml
    kubectl apply -f services/postgres-meteo-service.yaml
    kubectl apply -f deployments/postgres-arrosage-deployment.yaml
    kubectl apply -f services/postgres-arrosage-service.yaml
    Start-Sleep -Seconds 5
    Wait-ForPods -Label "app=postgres-meteo" -Namespace $Namespace -TimeoutSeconds 120
    Wait-ForPods -Label "app=postgres-arrosage" -Namespace $Namespace -TimeoutSeconds 120
    
    # Step 8: Deploy Eureka Server
    Write-Step "Step 8: Deploying Eureka Server"
    kubectl apply -f deployments/eureka-server-deployment.yaml
    kubectl apply -f services/eureka-server-service.yaml
    Start-Sleep -Seconds 10
    Wait-ForPods -Label "app=eureka-server" -Namespace $Namespace -TimeoutSeconds 180
    
    # Step 9: Deploy Config Server
    Write-Step "Step 9: Deploying Config Server"
    kubectl apply -f deployments/config-server-deployment.yaml
    kubectl apply -f services/config-server-service.yaml
    Start-Sleep -Seconds 10
    Wait-ForPods -Label "app=config-server" -Namespace $Namespace -TimeoutSeconds 180
    
    # Step 10: Apply ConfigMaps
    Write-Step "Step 10: Creating ConfigMaps"
    kubectl apply -f configmaps/
    Write-Success "ConfigMaps created"
    
    # Step 11: Deploy Meteo Service
    Write-Step "Step 11: Deploying Meteo Service"
    kubectl apply -f deployments/meteo-service-deployment.yaml
    kubectl apply -f services/meteo-service-service.yaml
    Start-Sleep -Seconds 10
    Wait-ForPods -Label "app=meteo-service" -Namespace $Namespace -TimeoutSeconds 180
    
    # Step 12: Deploy Arrosage Service
    Write-Step "Step 12: Deploying Arrosage Service"
    kubectl apply -f deployments/arrosage-service-deployment.yaml
    kubectl apply -f services/arrosage-service-service.yaml
    Start-Sleep -Seconds 10
    Wait-ForPods -Label "app=arrosage-service" -Namespace $Namespace -TimeoutSeconds 180
    
    # Step 13: Deploy Auth Service (SKIPPED - Still in development)
    # Write-Step "Step 13: Deploying Auth Service"
    # kubectl apply -f deployments/auth-service-deployment.yaml
    # kubectl apply -f services/auth-service-service.yaml
    # Start-Sleep -Seconds 10
    # Wait-ForPods -Label "app=auth-service" -Namespace $Namespace -TimeoutSeconds 180
    
    # Step 14: Deploy API Gateway
    Write-Step "Step 14: Deploying API Gateway"
    kubectl apply -f deployments/api-gateway-deployment.yaml
    kubectl apply -f services/api-gateway-service.yaml
    Start-Sleep -Seconds 10
    Wait-ForPods -Label "app=gateway-service" -Namespace $Namespace -TimeoutSeconds 180
    
    # Step 15: Deploy Ingress
    Write-Step "Step 15: Deploying Ingress"
    kubectl apply -f ingress/irrigation-ingress.yaml
    Write-Success "Ingress created"
    
    # Final Status
    Write-Step "Deployment Complete!"
    Write-Success "All components deployed successfully"
    
    Write-Host "`n"
    Show-DeploymentStatus
    
    Write-Host "`n"
    Write-Info "Access the application:"
    Write-Host "  • API Gateway: http://irrigation.local/api" -ForegroundColor Yellow
    Write-Host "  • Eureka Dashboard: http://irrigation.local/eureka" -ForegroundColor Yellow
    Write-Host "  • Config Server: http://irrigation.local/config" -ForegroundColor Yellow
    Write-Host "`n"
    Write-Info "Don't forget to add '127.0.0.1 irrigation.local' to your hosts file!"
    Write-Info "Hosts file location: C:\Windows\System32\drivers\etc\hosts"
}

# Show deployment status
function Show-DeploymentStatus {
    Write-Step "Deployment Status"
    
    Write-Host "`nPods:" -ForegroundColor Cyan
    kubectl get pods -n $Namespace -o wide
    
    Write-Host "`nServices:" -ForegroundColor Cyan
    kubectl get services -n $Namespace
    
    Write-Host "`nIngress:" -ForegroundColor Cyan
    kubectl get ingress -n $Namespace
    
    Write-Host "`nPersistent Volume Claims:" -ForegroundColor Cyan
    kubectl get pvc -n $Namespace
}

# Show logs
function Show-Logs {
    param([string]$Service)
    
    if ($Service) {
        Write-Info "Showing logs for $Service..."
        kubectl logs -l app=$Service -n $Namespace --tail=100 -f
    } else {
        Write-Info "Available services:"
        kubectl get pods -n $Namespace -o jsonpath='{range .items[*]}{.metadata.labels.app}{"\n"}{end}' | Sort-Object -Unique
        Write-Host "`nUsage: .\deploy-k8s.ps1 -Action logs -Service <service-name>" -ForegroundColor Yellow
    }
}

# Delete all resources
function Remove-IrrigationSystem {
    Write-Step "Deleting Irrigation Management System"
    
    $confirmation = Read-Host "Are you sure you want to delete the entire namespace '$Namespace'? (yes/no)"
    
    if ($confirmation -eq 'yes') {
        Write-Info "Deleting namespace and all resources..."
        kubectl delete namespace $Namespace
        Write-Success "All resources deleted"
    } else {
        Write-Info "Deletion cancelled"
    }
}

# Main execution
try {
    Set-Location $PSScriptRoot
    
    switch ($Action) {
        'deploy' {
            Deploy-IrrigationSystem
        }
        'delete' {
            Remove-IrrigationSystem
        }
        'status' {
            Show-DeploymentStatus
        }
        'logs' {
            Show-Logs
        }
        default {
            Write-ErrorMsg "Invalid action: $Action"
            Write-Info "Valid actions: deploy, delete, status, logs"
            exit 1
        }
    }
} catch {
    Write-ErrorMsg "Deployment failed: $_"
    Write-Info "Check logs with: kubectl get events -n $Namespace --sort-by='.lastTimestamp'"
    exit 1
}
