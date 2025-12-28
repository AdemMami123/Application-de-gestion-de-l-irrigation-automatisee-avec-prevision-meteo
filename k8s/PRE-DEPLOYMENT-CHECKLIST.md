# Pre-Deployment Checklist

Before deploying to Kubernetes, complete this checklist to ensure a successful deployment.

## ✅ Prerequisites

### 1. Kubernetes Cluster
- [ ] Kubernetes cluster is running (Minikube, Docker Desktop, or cloud provider)
- [ ] kubectl is installed and configured
- [ ] Can connect to cluster: `kubectl cluster-info`
- [ ] Cluster has sufficient resources (4+ CPU cores, 8+ GB RAM)

### 2. Container Registry
- [ ] Have access to a container registry (Docker Hub, GitHub Container Registry, etc.)
- [ ] Registry credentials configured: `docker login`
- [ ] Know your registry URL (e.g., `docker.io/username`, `ghcr.io/username`)

### 3. Ingress Controller
- [ ] NGINX Ingress Controller installed
  - Minikube: `minikube addons enable ingress`
  - Standard K8s: Follow [README.md](README.md) instructions
- [ ] Verify: `kubectl get pods -n ingress-nginx`

## 📦 Build and Push Docker Images

Complete these steps for each service:

### Services to Build:
1. [ ] **Eureka Server**
   ```powershell
   cd backend/eureka-server
   docker build -t YOUR-REGISTRY/eureka-server:latest .
   docker push YOUR-REGISTRY/eureka-server:latest
   ```

2. [ ] **Config Server**
   ```powershell
   cd backend/config-server
   docker build -t YOUR-REGISTRY/config-server:latest .
   docker push YOUR-REGISTRY/config-server:latest
   ```

3. [ ] **Gateway Service**
   ```powershell
   cd backend/gateway-service
   docker build -t YOUR-REGISTRY/gateway-service:latest .
   docker push YOUR-REGISTRY/gateway-service:latest
   ```

4. [ ] **Meteo Service**
   ```powershell
   cd backend/meteo-service
   docker build -t YOUR-REGISTRY/meteo-service:latest .
   docker push YOUR-REGISTRY/meteo-service:latest
   ```

5. [ ] **Arrosage Service**
   ```powershell
   cd backend/arrosage-service
   docker build -t YOUR-REGISTRY/arrosage-service:latest .
   docker push YOUR-REGISTRY/arrosage-service:latest
   ```

6. [ ] **Auth Service**
   ```powershell
   cd backend/auth-service
   docker build -t YOUR-REGISTRY/auth-service:latest .
   docker push YOUR-REGISTRY/auth-service:latest
   ```

### Verify Images
- [ ] All images pushed successfully
- [ ] Can pull images: `docker pull YOUR-REGISTRY/meteo-service:latest`

## 🔧 Update Configuration Files

### Update Image References
Replace `your-docker-registry` with your actual registry in these files:

- [ ] `k8s/deployments/eureka-server-deployment.yaml`
  ```yaml
  image: YOUR-REGISTRY/eureka-server:latest
  ```

- [ ] `k8s/deployments/config-server-deployment.yaml`
  ```yaml
  image: YOUR-REGISTRY/config-server:latest
  ```

- [ ] `k8s/deployments/api-gateway-deployment.yaml`
  ```yaml
  image: YOUR-REGISTRY/gateway-service:latest
  ```

- [ ] `k8s/deployments/meteo-service-deployment.yaml`
  ```yaml
  image: YOUR-REGISTRY/meteo-service:latest
  ```

- [ ] `k8s/deployments/arrosage-service-deployment.yaml`
  ```yaml
  image: YOUR-REGISTRY/arrosage-service:latest
  ```

- [ ] `k8s/deployments/auth-service-deployment.yaml`
  ```yaml
  image: YOUR-REGISTRY/auth-service:latest
  ```

### Tip: PowerShell Replace Script
```powershell
# Replace all occurrences
$files = Get-ChildItem -Path "k8s/deployments" -Filter "*-deployment.yaml"
foreach ($file in $files) {
    (Get-Content $file.FullName) -replace 'your-docker-registry', 'YOUR-ACTUAL-REGISTRY' | Set-Content $file.FullName
}
```

## 🔐 Update Secrets

### Database Passwords
- [ ] Generate strong passwords for databases
- [ ] Encode passwords in base64:
  ```powershell
  # Example
  $password = "your-strong-password-here"
  $bytes = [System.Text.Encoding]::UTF8.GetBytes($password)
  $encoded = [Convert]::ToBase64String($bytes)
  Write-Host $encoded
  ```

- [ ] Update `k8s/secrets/postgres-secrets.yaml` with new encoded passwords

### Application Secrets
- [ ] Generate JWT secret
- [ ] Encode in base64
- [ ] Update `k8s/secrets/app-secrets.yaml`

### Security Notes
- [ ] Passwords are at least 16 characters
- [ ] Passwords contain uppercase, lowercase, numbers, and symbols
- [ ] Secrets are NOT committed to Git with real values

## 🌐 Network Configuration

### Local Development
- [ ] Add to hosts file (`C:\Windows\System32\drivers\etc\hosts`):
  ```
  127.0.0.1 irrigation.local
  ```

### Minikube
- [ ] Get Minikube IP: `minikube ip`
- [ ] Add to hosts file:
  ```
  <MINIKUBE-IP> irrigation.local
  ```

### Cloud/Production
- [ ] DNS records configured
- [ ] SSL/TLS certificates obtained
- [ ] Update ingress with proper domain and TLS config

## 💾 Storage

### Local Development
- [ ] Default storage class available: `kubectl get storageclass`
- [ ] Sufficient disk space for PVCs (~11 GB)

### Production
- [ ] SSD-backed storage class configured
- [ ] Backup strategy defined
- [ ] Volume expansion enabled if needed

## 📊 Monitoring (Optional but Recommended)

- [ ] Prometheus deployed or plan to deploy
- [ ] Grafana deployed or plan to deploy
- [ ] Log aggregation solution ready (ELK, Loki, etc.)
- [ ] Alerting configured

## 🧪 Pre-Deployment Tests

### Local Tests
- [ ] All services run successfully with Docker Compose
- [ ] Can access services locally
- [ ] Database connections work
- [ ] Kafka messaging works
- [ ] Redis caching works

### Configuration Tests
- [ ] All YAML files are valid:
  ```powershell
  kubectl apply -f k8s/namespace.yaml --dry-run=client
  kubectl apply -f k8s/secrets/ --dry-run=client
  kubectl apply -f k8s/deployments/ --dry-run=client
  ```

## 🚀 Ready to Deploy

Once all items are checked:

```powershell
cd k8s
.\deploy-k8s.ps1 -Action deploy
```

## 📝 Post-Deployment Verification

After deployment, verify:

- [ ] All pods are running:
  ```powershell
  kubectl get pods -n irrigation-system
  ```

- [ ] All services are available:
  ```powershell
  kubectl get services -n irrigation-system
  ```

- [ ] Ingress is configured:
  ```powershell
  kubectl get ingress -n irrigation-system
  ```

- [ ] Can access Eureka dashboard:
  ```
  http://irrigation.local/eureka
  ```

- [ ] All services registered in Eureka

- [ ] Can access API Gateway:
  ```
  http://irrigation.local/api
  ```

- [ ] Test API endpoints work

## 🐛 Troubleshooting

If something goes wrong:

1. Check pod status:
   ```powershell
   kubectl get pods -n irrigation-system
   ```

2. Check pod logs:
   ```powershell
   kubectl logs <pod-name> -n irrigation-system
   ```

3. Check events:
   ```powershell
   kubectl get events -n irrigation-system --sort-by='.lastTimestamp'
   ```

4. Describe problematic pod:
   ```powershell
   kubectl describe pod <pod-name> -n irrigation-system
   ```

5. Consult [README.md](README.md) troubleshooting section

## 📚 Documentation to Review

Before deploying, review:
- [ ] [README.md](README.md) - Complete deployment guide
- [ ] [QUICK-REFERENCE.md](QUICK-REFERENCE.md) - Common commands
- [ ] [DEPLOYMENT-SUMMARY.md](DEPLOYMENT-SUMMARY.md) - Overview

## ⚠️ Important Reminders

1. **Never commit secrets with real values to Git**
2. **Always test in development environment first**
3. **Have a rollback plan**
4. **Document any custom changes**
5. **Keep backup of configurations**
6. **Monitor resource usage after deployment**
7. **Set up alerts for critical services**

## 🎯 Development vs Production

### Development Deployment
- Use default storage class
- Single replica for most services
- HTTP (no TLS)
- Relaxed resource limits
- Mock external services if needed

### Production Deployment
- SSD-backed storage
- Multiple replicas for HA
- HTTPS with valid certificates
- Strict resource limits
- Real external service integrations
- Monitoring and alerting
- Backup and disaster recovery
- Security scanning
- Network policies
- Pod security policies

---

**Last Updated**: December 2025

**Need Help?** 
- Check [README.md](README.md) for detailed instructions
- Review [QUICK-REFERENCE.md](QUICK-REFERENCE.md) for commands
- Check pod logs for errors
- Review Kubernetes events
