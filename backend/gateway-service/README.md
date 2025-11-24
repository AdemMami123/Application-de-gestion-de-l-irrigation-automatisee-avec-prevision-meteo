# API Gateway Service

Point d'entrée unique pour l'architecture microservices de gestion d'irrigation.

## 📋 Vue d'ensemble

Le Gateway Service (port 8080) agit comme un reverse proxy et fournit:
- **Routage dynamique** vers les microservices via Eureka
- **Rate limiting** basé sur Redis
- **Circuit breaker** pour la résilience
- **Logging centralisé** des requêtes/réponses
- **CORS** pour les applications frontend
- **Correlation ID** pour le traçage distribué

## 🚀 Démarrage rapide

### Prérequis
- Java 17+
- Maven 3.8+
- Redis (pour rate limiting)
- Eureka Server (pour découverte de services)

### Lancement local
```bash
cd backend/gateway-service
mvn spring-boot:run
```

### Avec Docker
```bash
docker-compose up gateway-service
```

Le gateway sera accessible sur: **http://localhost:8080**

## 🔀 Routes configurées

### Services métier

| Route | Service cible | Port | Description |
|-------|--------------|------|-------------|
| `/api/meteo/**` | meteo-service | 8081 | Prévisions météo |
| `/api/arrosage/**` | arrosage-service | 8082 | Gestion d'arrosage |
| `/eureka/**` | eureka-server | 8761 | Dashboard Eureka |
| `/actuator/**` | gateway-service | 8080 | Monitoring gateway |

### Exemples d'utilisation

#### Avant (accès direct):
```bash
# Ancien - Accès direct aux services
curl http://localhost:8081/meteo/previsions
curl http://localhost:8082/arrosage/zones
```

#### Après (via Gateway):
```bash
# Nouveau - Tout passe par le gateway
curl http://localhost:8080/api/meteo/previsions
curl http://localhost:8080/api/arrosage/zones
```

## 🛡️ Fonctionnalités de sécurité et résilience

### 1. Rate Limiting (Redis)

Le gateway limite les requêtes par IP pour éviter les abus:

```properties
# Configuration dans application.properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

**Résolveurs de clés disponibles:**
- `ipKeyResolver`: Limite par adresse IP (par défaut)
- `userKeyResolver`: Limite par API Key (`X-API-Key` header)
- `pathKeyResolver`: Limite par endpoint

**Test du rate limiting:**
```bash
# Envoyer plusieurs requêtes rapidement
for i in {1..100}; do
  curl http://localhost:8080/api/meteo/previsions
done
```

### 2. Circuit Breaker

Protection contre les services défaillants:

```yaml
Configuration:
- Fenêtre glissante: 10 requêtes
- Seuil d'échec: 50%
- Durée en état ouvert: 10 secondes
- Appels autorisés en semi-ouvert: 5
```

**Endpoints de fallback:**
- `/fallback/meteo` - Réponse par défaut si meteo-service est down
- `/fallback/arrosage` - Réponse par défaut si arrosage-service est down

**Test du circuit breaker:**
```bash
# Arrêter un service et tester
docker stop meteo-service
curl http://localhost:8080/api/meteo/previsions
# Réponse: {"status":"SERVICE_UNAVAILABLE", "message":"..."}
```

### 3. Retry Policy

Réessaie automatique pour les requêtes GET:
- Nombre de tentatives: 3
- Facteur d'attente: exponentiel (2x)
- Méthodes concernées: GET uniquement

## 🔍 Filtres globaux

### CorrelationIdFilter (Priorité: HIGHEST)
- Ajoute un `X-Correlation-Id` unique à chaque requête
- Permet de tracer les requêtes à travers tous les services
- L'ID est propagé dans les headers de réponse

**Exemple:**
```bash
curl -H "X-Correlation-Id: my-custom-id" http://localhost:8080/api/meteo/previsions
# Réponse inclura: X-Correlation-Id: my-custom-id
```

### RequestLoggingFilter (Priorité: HIGHEST + 1)
- Log l'IP du client (supporte X-Forwarded-For)
- Log méthode HTTP, path, ID de requête
- Log status code de la réponse

**Logs générés:**
```
INFO - Incoming Request - ID: abc123, Method: GET, Path: /api/meteo/previsions, Client IP: 192.168.1.100
INFO - Outgoing Response - ID: abc123, Status: 200, Path: /api/meteo/previsions
```

### ResponseTimeFilter (Priorité: HIGHEST + 2)
- Mesure le temps de traitement
- Ajoute header `X-Response-Time` à la réponse
- Alerte si temps > 5 secondes

**Exemple de header de réponse:**
```
X-Response-Time: 234ms
```

## 🌐 Configuration CORS

Origines autorisées par défaut:
- `http://localhost:3000` (React)
- `http://localhost:4200` (Angular)
- `http://localhost:5173` (Vite)
- `http://localhost:8080` (Gateway)

**Méthodes autorisées:** GET, POST, PUT, DELETE, OPTIONS, PATCH

**Headers exposés:** X-Correlation-Id, X-Response-Time

**Modifier la configuration:**
```java
// Fichier: CorsConfig.java
corsConfig.setAllowedOrigins(Arrays.asList(
    "https://mon-frontend.com"
));
```

## 📊 Monitoring et Health Checks

### Endpoints Actuator disponibles

```bash
# Santé globale du gateway
curl http://localhost:8080/actuator/health

# Métriques Prometheus
curl http://localhost:8080/actuator/metrics

# Routes configurées
curl http://localhost:8080/actuator/gateway/routes

# Informations sur l'application
curl http://localhost:8080/actuator/info
```

### Health Check response
```json
{
  "status": "UP",
  "components": {
    "discoveryComposite": {"status": "UP"},
    "redis": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

## 🧪 Tests

### Test de routage
```bash
# Vérifier que les routes fonctionnent
curl -v http://localhost:8080/api/meteo/previsions
curl -v http://localhost:8080/api/arrosage/zones
```

### Test de CORS
```bash
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     http://localhost:8080/api/meteo/previsions
```

### Test de corrélation
```bash
curl -H "X-Correlation-Id: test-123" \
     -v http://localhost:8080/api/meteo/previsions 2>&1 | grep X-Correlation-Id
```

### Test de temps de réponse
```bash
curl -v http://localhost:8080/api/meteo/previsions 2>&1 | grep X-Response-Time
```

## 📦 Dépendances principales

```xml
<dependencies>
    <!-- Gateway -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    
    <!-- Service Discovery -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    
    <!-- Rate Limiting -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
    </dependency>
    
    <!-- Circuit Breaker -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
    </dependency>
</dependencies>
```

## 🔧 Configuration avancée

### Modifier le rate limiting

```properties
# À ajouter dans application.properties
spring.cloud.gateway.routes[0].filters[0]=RequestRateLimiter=10,1,#{@ipKeyResolver}
# 10 requêtes par seconde, 1 burst capacity, par IP
```

### Ajouter une nouvelle route

```java
// Dans GatewayRoutesConfig.java
.route("mon-nouveau-service", r -> r
    .path("/api/nouveau/**")
    .filters(f -> f.stripPrefix(1))
    .uri("lb://nouveau-service"))
```

### Personnaliser le circuit breaker

```properties
resilience4j.circuitbreaker.configs.default.sliding-window-size=20
resilience4j.circuitbreaker.configs.default.failure-rate-threshold=60
```

## 🐛 Troubleshooting

### Le gateway ne démarre pas
```bash
# Vérifier qu'Eureka est accessible
curl http://localhost:8761/eureka/apps

# Vérifier que Redis est actif
redis-cli ping
```

### Les routes ne fonctionnent pas
```bash
# Vérifier les routes enregistrées
curl http://localhost:8080/actuator/gateway/routes | jq

# Vérifier les services enregistrés dans Eureka
curl http://localhost:8761/eureka/apps | grep -i meteo
```

### Rate limiting ne fonctionne pas
```bash
# Vérifier la connexion Redis
redis-cli
> PING
PONG
> KEYS *
```

## 📝 Logs utiles

```bash
# Activer le debug dans application.properties
logging.level.org.springframework.cloud.gateway=DEBUG
logging.level.com.irrigation.gateway=DEBUG

# Logs attendus
DEBUG - Route matched: meteo-service
INFO  - Incoming Request - ID: abc, Method: GET, Path: /api/meteo/previsions
INFO  - Request completed - Method: GET, Path: /api/meteo/previsions, Duration: 156ms
```

## 🚦 Architecture

```
[Client] 
   ↓
[Gateway :8080] ← Correlation ID, Logging, Rate Limit
   ↓
[Eureka :8761] ← Service Discovery
   ↓
[Meteo Service :8081] ou [Arrosage Service :8082]
   ↓
[PostgreSQL / Redis]
```

## 📚 Ressources

- [Spring Cloud Gateway Docs](https://spring.io/projects/spring-cloud-gateway)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Redis Rate Limiting](https://redis.io/docs/manual/patterns/rate-limiter/)

---

**Auteur:** Irrigation Meteo Team  
**Version:** 1.0.0  
**Dernière mise à jour:** Novembre 2025
