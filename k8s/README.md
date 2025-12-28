# Kubernetes Deployment Guide - Irrigation Management System

## 📋 Overview

This directory contains Kubernetes manifests for deploying the irrigation management microservices application in a Kubernetes cluster.

## 🏗️ Architecture

The Kubernetes deployment includes:

### Infrastructure Components
- **Zookeeper**: Coordination service for Kafka
- **Kafka**: Message broker for asynchronous communication
- **Redis**: Distributed cache and session storage
- **PostgreSQL (Meteo)**: Database for weather service
- **PostgreSQL (Arrosage)**: Database for irrigation service

### Microservices
- **Eureka Server**: Service discovery and registration
- **Config Server**: Centralized configuration management
- **API Gateway**: Single entry point with routing and load balancing
- **Meteo Service**: Weather forecast management
- **Arrosage Service**: Irrigation system management
- **Auth Service**: Authentication and authorization

## 📁 Directory Structure

```
k8s/
├── namespace.yaml                          # Namespace definition
├── configmaps/                             # Application configurations
│   ├── meteo-service-configmap.yaml
│   ├── arrosage-service-configmap.yaml
│   ├── api-gateway-configmap.yaml
│   └── config-server-configmap.yaml
├── secrets/                                # Sensitive data
│   ├── postgres-secrets.yaml
│   └── app-secrets.yaml
├── persistent-volumes/                     # Storage claims
│   ├── postgres-meteo-pvc.yaml
│   ├── postgres-arrosage-pvc.yaml
│   └── redis-pvc.yaml
├── deployments/                            # Application deployments
│   ├── zookeeper-deployment.yaml
│   ├── kafka-deployment.yaml
│   ├── redis-deployment.yaml
│   ├── postgres-meteo-deployment.yaml
│   ├── postgres-arrosage-deployment.yaml
│   ├── eureka-server-deployment.yaml
│   ├── config-server-deployment.yaml
│   ├── api-gateway-deployment.yaml
│   ├── meteo-service-deployment.yaml
│   ├── arrosage-service-deployment.yaml
│   └── auth-service-deployment.yaml
├── services/                               # Service discovery
│   ├── zookeeper-service.yaml
│   ├── kafka-service.yaml
│   ├── redis-service.yaml
│   ├── postgres-meteo-service.yaml
│   ├── postgres-arrosage-service.yaml
│   ├── eureka-server-service.yaml
│   ├── config-server-service.yaml
│   ├── api-gateway-service.yaml
│   ├── meteo-service-service.yaml
│   ├── arrosage-service-service.yaml
│   └── auth-service-service.yaml
└── ingress/                                # External access
    └── irrigation-ingress.yaml
```

## 🚀 Prerequisites

### 1. Kubernetes Cluster
- **Minikube** (for local development)
- **Docker Desktop Kubernetes** (for local development)
- **Cloud providers**: GKE, EKS, AKS (for production)

### 2. Required Tools
```powershell
# kubectl - Kubernetes CLI
choco install kubernetes-cli

# kubectl verification
kubectl version --client

# For Minikube (optional for local development)
choco install minikube
```

### 3. Docker Images
Build and push your Docker images to a container registry:

```powershell
# Build images
cd backend/eureka-server
docker build -t your-registry/eureka-server:latest .

cd ../config-server
docker build -t your-registry/config-server:latest .

cd ../gateway-service
docker build -t your-registry/gateway-service:latest .

cd ../meteo-service
docker build -t your-registry/meteo-service:latest .

cd ../arrosage-service
docker build -t your-registry/arrosage-service:latest .

cd ../auth-service
docker build -t your-registry/auth-service:latest .

# Push to registry (replace with your registry)
docker push your-registry/eureka-server:latest
docker push your-registry/config-server:latest
docker push your-registry/gateway-service:latest
docker push your-registry/meteo-service:latest
docker push your-registry/arrosage-service:latest
docker push your-registry/auth-service:latest
```

**Important**: Update the `image` fields in all deployment YAML files with your actual registry URLs.

### 4. Ingress Controller
Install NGINX Ingress Controller:

```powershell
# For Minikube
minikube addons enable ingress

# For standard Kubernetes cluster
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
```

## 📦 Deployment Steps

### Step 1: Create Namespace
```powershell
kubectl apply -f namespace.yaml
```

### Step 2: Create Secrets
```powershell
kubectl apply -f secrets/
```

### Step 3: Create Persistent Volume Claims
```powershell
kubectl apply -f persistent-volumes/
```

### Step 4: Deploy Infrastructure Components
Deploy in order to respect dependencies:

```powershell
# Deploy Zookeeper first
kubectl apply -f deployments/zookeeper-deployment.yaml
kubectl apply -f services/zookeeper-service.yaml

# Wait for Zookeeper to be ready
kubectl wait --for=condition=ready pod -l app=zookeeper -n irrigation-system --timeout=120s

# Deploy Kafka
kubectl apply -f deployments/kafka-deployment.yaml
kubectl apply -f services/kafka-service.yaml

# Wait for Kafka to be ready
kubectl wait --for=condition=ready pod -l app=kafka -n irrigation-system --timeout=120s

# Deploy Redis
kubectl apply -f deployments/redis-deployment.yaml
kubectl apply -f services/redis-service.yaml

# Deploy PostgreSQL databases
kubectl apply -f deployments/postgres-meteo-deployment.yaml
kubectl apply -f services/postgres-meteo-service.yaml

kubectl apply -f deployments/postgres-arrosage-deployment.yaml
kubectl apply -f services/postgres-arrosage-service.yaml

# Wait for databases to be ready
kubectl wait --for=condition=ready pod -l app=postgres-meteo -n irrigation-system --timeout=120s
kubectl wait --for=condition=ready pod -l app=postgres-arrosage -n irrigation-system --timeout=120s
```

### Step 5: Deploy Microservices
```powershell
# Deploy Eureka Server
kubectl apply -f deployments/eureka-server-deployment.yaml
kubectl apply -f services/eureka-server-service.yaml

# Wait for Eureka to be ready
kubectl wait --for=condition=ready pod -l app=eureka-server -n irrigation-system --timeout=180s

# Deploy Config Server
kubectl apply -f deployments/config-server-deployment.yaml
kubectl apply -f services/config-server-service.yaml

kubectl wait --for=condition=ready pod -l app=config-server -n irrigation-system --timeout=180s

# Deploy ConfigMaps
kubectl apply -f configmaps/

# Deploy business microservices
kubectl apply -f deployments/meteo-service-deployment.yaml
kubectl apply -f services/meteo-service-service.yaml

kubectl apply -f deployments/arrosage-service-deployment.yaml
kubectl apply -f services/arrosage-service-service.yaml

kubectl apply -f deployments/auth-service-deployment.yaml
kubectl apply -f services/auth-service-service.yaml

# Deploy API Gateway
kubectl apply -f deployments/api-gateway-deployment.yaml
kubectl apply -f services/api-gateway-service.yaml
```

### Step 6: Deploy Ingress
```powershell
kubectl apply -f ingress/irrigation-ingress.yaml
```

### Step 7: Configure DNS (Local Development)
Add to your hosts file (`C:\Windows\System32\drivers\etc\hosts`):
```
127.0.0.1 irrigation.local
```

For Minikube, get the Minikube IP:
```powershell
minikube ip
# Add the output IP to hosts file:
# <minikube-ip> irrigation.local
```

## 🔍 Verification and Monitoring

### Check Deployment Status
```powershell
# Check all pods
kubectl get pods -n irrigation-system

# Check services
kubectl get services -n irrigation-system

# Check ingress
kubectl get ingress -n irrigation-system

# Describe a specific pod
kubectl describe pod <pod-name> -n irrigation-system

# View logs
kubectl logs <pod-name> -n irrigation-system

# Follow logs in real-time
kubectl logs -f <pod-name> -n irrigation-system
```

### Access Services

**Via Ingress:**
- API Gateway: http://irrigation.local/api
- Eureka Dashboard: http://irrigation.local/eureka
- Config Server: http://irrigation.local/config

**Via Port Forwarding (for debugging):**
```powershell
# Eureka Server
kubectl port-forward svc/eureka-server 8761:8761 -n irrigation-system

# API Gateway
kubectl port-forward svc/gateway-service 8080:8080 -n irrigation-system

# Meteo Service
kubectl port-forward svc/meteo-service 8081:8081 -n irrigation-system
```

### Health Checks
```powershell
# Check all service health
kubectl get pods -n irrigation-system -o wide

# Check specific service health
kubectl exec -it <pod-name> -n irrigation-system -- wget -qO- http://localhost:8081/actuator/health
```

## 🛠️ Troubleshooting

### Pod Not Starting
```powershell
# Check pod events
kubectl describe pod <pod-name> -n irrigation-system

# Check logs
kubectl logs <pod-name> -n irrigation-system

# Check previous container logs (if pod restarted)
kubectl logs <pod-name> -n irrigation-system --previous
```

### Service Connection Issues
```powershell
# Test internal connectivity from a pod
kubectl exec -it <pod-name> -n irrigation-system -- sh
# Inside the pod:
nc -zv <service-name> <port>
```

### Database Connection Issues
```powershell
# Check PostgreSQL pod logs
kubectl logs postgres-meteo-<pod-id> -n irrigation-system

# Connect to PostgreSQL pod
kubectl exec -it postgres-meteo-<pod-id> -n irrigation-system -- psql -U meteo_user -d meteodb
```

### Ingress Not Working
```powershell
# Check ingress controller logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx

# Check ingress configuration
kubectl describe ingress irrigation-ingress -n irrigation-system
```

## 🔄 Updates and Rollbacks

### Update a Deployment
```powershell
# Update the image
kubectl set image deployment/meteo-service meteo-service=your-registry/meteo-service:v2 -n irrigation-system

# Or apply updated YAML
kubectl apply -f deployments/meteo-service-deployment.yaml

# Check rollout status
kubectl rollout status deployment/meteo-service -n irrigation-system
```

### Rollback a Deployment
```powershell
# View rollout history
kubectl rollout history deployment/meteo-service -n irrigation-system

# Rollback to previous version
kubectl rollout undo deployment/meteo-service -n irrigation-system

# Rollback to specific revision
kubectl rollout undo deployment/meteo-service --to-revision=2 -n irrigation-system
```

## 📊 Scaling

### Manual Scaling
```powershell
# Scale a deployment
kubectl scale deployment/meteo-service --replicas=3 -n irrigation-system

# Verify scaling
kubectl get pods -n irrigation-system -l app=meteo-service
```

### Horizontal Pod Autoscaling (HPA)
```powershell
# Create HPA for meteo-service
kubectl autoscale deployment meteo-service --cpu-percent=70 --min=2 --max=5 -n irrigation-system

# Check HPA status
kubectl get hpa -n irrigation-system
```

## 🧹 Cleanup

### Delete Specific Resources
```powershell
# Delete a deployment
kubectl delete deployment meteo-service -n irrigation-system

# Delete a service
kubectl delete service meteo-service -n irrigation-system
```

### Delete All Resources in Namespace
```powershell
kubectl delete namespace irrigation-system
```

## 🔐 Security Best Practices

1. **Secrets Management**
   - Never commit secrets to Git
   - Use base64 encoding for secret values
   - Consider using external secret management (HashiCorp Vault, AWS Secrets Manager)

2. **Network Policies**
   - Implement network policies to restrict pod-to-pod communication
   - Only allow necessary ingress and egress traffic

3. **RBAC**
   - Configure Role-Based Access Control
   - Follow principle of least privilege

4. **Resource Limits**
   - Always set resource requests and limits
   - Prevents resource exhaustion

5. **Image Security**
   - Use specific image tags (avoid `latest`)
   - Scan images for vulnerabilities
   - Use private registries for production

## 📈 Production Considerations

1. **High Availability**
   - Run multiple replicas of stateless services
   - Use StatefulSets for stateful applications
   - Configure pod anti-affinity rules

2. **Monitoring**
   - Deploy Prometheus and Grafana
   - Configure service metrics collection
   - Set up alerts for critical metrics

3. **Backup and Recovery**
   - Regular backup of persistent volumes
   - Database backup strategies
   - Disaster recovery plan

4. **CI/CD Integration**
   - Automate deployments with GitOps (ArgoCD, Flux)
   - Implement blue-green or canary deployments
   - Automated testing before deployment

## 🌐 Multi-Environment Deployment

Use Kustomize or Helm for managing multiple environments:

```
k8s/
├── base/              # Base configurations
├── overlays/
│   ├── dev/          # Development overrides
│   ├── staging/      # Staging overrides
│   └── production/   # Production overrides
```

## 📚 Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)
- [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/)

## 💡 Tips

- Use `kubectl config set-context --current --namespace=irrigation-system` to avoid typing `-n irrigation-system` every time
- Use `kubectl get events -n irrigation-system --sort-by='.lastTimestamp'` to see recent events
- Use `stern` tool for aggregated log viewing across multiple pods
- Use k9s for a terminal-based UI for Kubernetes

## 🆘 Support

For issues and questions:
1. Check pod logs: `kubectl logs <pod-name> -n irrigation-system`
2. Check pod events: `kubectl describe pod <pod-name> -n irrigation-system`
3. Verify service connectivity
4. Review this documentation

## 📝 Notes

- All resources are deployed in the `irrigation-system` namespace
- Default storage class is used for PVCs (adjust if needed)
- Ingress is configured for HTTP (add TLS certificates for HTTPS)
- Resource limits are configured for a medium-sized cluster (adjust as needed)
