# Quick Reference - Kubernetes Commands

## 🚀 Deployment Commands

### Deploy Everything
```powershell
cd k8s
.\deploy-k8s.ps1 -Action deploy
```

### Check Status
```powershell
.\deploy-k8s.ps1 -Action status
```

### Delete Everything
```powershell
.\deploy-k8s.ps1 -Action delete
```

## 📊 Monitoring Commands

### View All Pods
```powershell
kubectl get pods -n irrigation-system
kubectl get pods -n irrigation-system -o wide
```

### View Specific Pod Details
```powershell
kubectl describe pod <pod-name> -n irrigation-system
```

### View Pod Logs
```powershell
# Current logs
kubectl logs <pod-name> -n irrigation-system

# Follow logs
kubectl logs -f <pod-name> -n irrigation-system

# Last 100 lines
kubectl logs <pod-name> -n irrigation-system --tail=100

# Previous container logs (if restarted)
kubectl logs <pod-name> -n irrigation-system --previous
```

### View Logs by Label
```powershell
kubectl logs -l app=meteo-service -n irrigation-system --tail=100 -f
```

### View All Services
```powershell
kubectl get services -n irrigation-system
kubectl get svc -n irrigation-system
```

### View Ingress
```powershell
kubectl get ingress -n irrigation-system
kubectl describe ingress irrigation-ingress -n irrigation-system
```

### View ConfigMaps
```powershell
kubectl get configmaps -n irrigation-system
kubectl describe configmap meteo-service-config -n irrigation-system
```

### View Secrets
```powershell
kubectl get secrets -n irrigation-system
```

### View PVCs
```powershell
kubectl get pvc -n irrigation-system
```

### View Events
```powershell
# Recent events
kubectl get events -n irrigation-system --sort-by='.lastTimestamp'

# Watch events
kubectl get events -n irrigation-system --watch
```

## 🔄 Update & Rollback

### Update Deployment Image
```powershell
kubectl set image deployment/meteo-service meteo-service=your-registry/meteo-service:v2 -n irrigation-system
```

### Restart Deployment
```powershell
kubectl rollout restart deployment/meteo-service -n irrigation-system
```

### View Rollout Status
```powershell
kubectl rollout status deployment/meteo-service -n irrigation-system
```

### View Rollout History
```powershell
kubectl rollout history deployment/meteo-service -n irrigation-system
```

### Rollback to Previous Version
```powershell
kubectl rollout undo deployment/meteo-service -n irrigation-system
```

### Rollback to Specific Revision
```powershell
kubectl rollout undo deployment/meteo-service --to-revision=2 -n irrigation-system
```

## 🔍 Debugging

### Execute Command in Pod
```powershell
kubectl exec -it <pod-name> -n irrigation-system -- sh
kubectl exec -it <pod-name> -n irrigation-system -- bash
```

### Test Connectivity from Pod
```powershell
# Check if port is open
kubectl exec -it <pod-name> -n irrigation-system -- nc -zv postgres-meteo 5432

# Test HTTP endpoint
kubectl exec -it <pod-name> -n irrigation-system -- wget -qO- http://eureka-server:8761/actuator/health
```

### Port Forwarding
```powershell
# Forward local port to pod port
kubectl port-forward <pod-name> 8081:8081 -n irrigation-system

# Forward to service
kubectl port-forward svc/meteo-service 8081:8081 -n irrigation-system
```

### Copy Files to/from Pod
```powershell
# Copy from pod
kubectl cp irrigation-system/<pod-name>:/path/to/file ./local-file

# Copy to pod
kubectl cp ./local-file irrigation-system/<pod-name>:/path/to/file
```

## 📈 Scaling

### Scale Deployment
```powershell
kubectl scale deployment/meteo-service --replicas=3 -n irrigation-system
```

### Autoscale Deployment
```powershell
kubectl autoscale deployment meteo-service --cpu-percent=70 --min=2 --max=5 -n irrigation-system
```

### View HPA Status
```powershell
kubectl get hpa -n irrigation-system
```

## 🔧 Configuration Management

### Apply Configuration Changes
```powershell
kubectl apply -f deployments/meteo-service-deployment.yaml
```

### Edit Resource Directly
```powershell
kubectl edit deployment/meteo-service -n irrigation-system
kubectl edit configmap/meteo-service-config -n irrigation-system
```

### Update ConfigMap and Restart Pods
```powershell
kubectl apply -f configmaps/meteo-service-configmap.yaml
kubectl rollout restart deployment/meteo-service -n irrigation-system
```

## 🗑️ Cleanup

### Delete Specific Resource
```powershell
kubectl delete pod <pod-name> -n irrigation-system
kubectl delete deployment meteo-service -n irrigation-system
kubectl delete service meteo-service -n irrigation-system
```

### Delete by File
```powershell
kubectl delete -f deployments/meteo-service-deployment.yaml
```

### Delete All Resources in Namespace
```powershell
kubectl delete namespace irrigation-system
```

## 🔐 Security

### View Secret Data
```powershell
# Get secret in YAML format
kubectl get secret postgres-meteo-secret -n irrigation-system -o yaml

# Decode secret value
kubectl get secret postgres-meteo-secret -n irrigation-system -o jsonpath='{.data.postgres-password}' | base64 -d
```

### Create Secret from Command Line
```powershell
kubectl create secret generic my-secret --from-literal=key1=value1 -n irrigation-system
```

## 📦 Resource Management

### View Resource Usage
```powershell
kubectl top nodes
kubectl top pods -n irrigation-system
```

### View Resource Limits
```powershell
kubectl describe pod <pod-name> -n irrigation-system | Select-String -Pattern "Limits|Requests"
```

## 🌐 Network

### View Network Policies
```powershell
kubectl get networkpolicies -n irrigation-system
```

### Test Service DNS Resolution
```powershell
kubectl exec -it <pod-name> -n irrigation-system -- nslookup meteo-service
```

## 📝 Useful Aliases (PowerShell)

Add these to your PowerShell profile:

```powershell
# Open profile
notepad $PROFILE

# Add these functions:
function k { kubectl $args }
function kgp { kubectl get pods -n irrigation-system $args }
function kgs { kubectl get services -n irrigation-system $args }
function kgd { kubectl get deployments -n irrigation-system $args }
function kdp { kubectl describe pod $args -n irrigation-system }
function kl { kubectl logs $args -n irrigation-system }
function klf { kubectl logs -f $args -n irrigation-system }
function kex { kubectl exec -it $args -n irrigation-system -- sh }
```

## 🎯 Common Workflows

### Deploy New Version
```powershell
# Build and push image
docker build -t your-registry/meteo-service:v2 .
docker push your-registry/meteo-service:v2

# Update deployment
kubectl set image deployment/meteo-service meteo-service=your-registry/meteo-service:v2 -n irrigation-system

# Watch rollout
kubectl rollout status deployment/meteo-service -n irrigation-system
```

### Troubleshoot Pod Not Starting
```powershell
# Check pod status
kubectl get pod <pod-name> -n irrigation-system

# Check events
kubectl describe pod <pod-name> -n irrigation-system

# Check logs
kubectl logs <pod-name> -n irrigation-system

# Check previous logs if restarted
kubectl logs <pod-name> -n irrigation-system --previous
```

### Update Configuration
```powershell
# Edit configmap
kubectl edit configmap meteo-service-config -n irrigation-system

# Or apply from file
kubectl apply -f configmaps/meteo-service-configmap.yaml

# Restart deployment to pick up changes
kubectl rollout restart deployment/meteo-service -n irrigation-system
```

### Check Service Health
```powershell
# Get all pods
kubectl get pods -n irrigation-system

# Check specific service health
kubectl exec -it <any-pod> -n irrigation-system -- wget -qO- http://meteo-service:8081/actuator/health
```

## 💡 Tips

1. **Set default namespace** to avoid typing `-n irrigation-system` every time:
   ```powershell
   kubectl config set-context --current --namespace=irrigation-system
   ```

2. **Use `-o wide`** for more details:
   ```powershell
   kubectl get pods -o wide
   ```

3. **Use `-w`** to watch for changes:
   ```powershell
   kubectl get pods -w
   ```

4. **Use JSONPath** for specific fields:
   ```powershell
   kubectl get pods -o jsonpath='{.items[*].metadata.name}'
   ```

5. **Use `--dry-run`** to preview changes:
   ```powershell
   kubectl apply -f deployment.yaml --dry-run=client
   ```

## 🆘 Emergency Commands

### Force Delete Stuck Pod
```powershell
kubectl delete pod <pod-name> -n irrigation-system --force --grace-period=0
```

### Get All Resources
```powershell
kubectl get all -n irrigation-system
```

### Describe All Pods
```powershell
kubectl describe pods -n irrigation-system
```

### Get Resource YAML
```powershell
kubectl get deployment meteo-service -n irrigation-system -o yaml > meteo-backup.yaml
```
