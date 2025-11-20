# Redis Cache Implementation - Arrosage Service

## Vue d'ensemble

Cette implémentation ajoute un système de cache Redis au service d'arrosage pour réduire les appels au service météo et améliorer les performances.

## Fonctionnalités

### 1. Configuration Redis

- **Image Docker**: Redis 7-alpine
- **Port**: 6379
- **Persistance**: AOF (Append Only File) activé
- **Health Check**: Ping automatique toutes les 10 secondes

### 2. Configuration du Cache

#### TTL (Time To Live)
- **Prévisions météo**: 2 heures (7200000 ms)
- **Stations météo**: 24 heures (86400000 ms)
- **Données météo actuelles**: 30 minutes (1800000 ms)

#### Préfixes de clés
- Format: `arrosage:{cacheName}:{key}`
- Exemples:
  - `arrosage:forecasts:all`
  - `arrosage:forecasts:station:123`
  - `arrosage:forecasts:station:123:2024-01-01:2024-01-07`

### 3. Méthodes Cachées

#### MeteoServiceClient

1. **getAllPrevisions()**
   - Clé: `'all'`
   - Cache toutes les prévisions

2. **getPrevisionById(Long id)**
   - Clé: `'id:' + #id`
   - Cache par ID de prévision

3. **getPrevisionsByStation(Long stationId)**
   - Clé: `'station:' + #stationId`
   - Cache par station

4. **getPrevisionsByPeriode(Long stationId, LocalDate startDate, LocalDate endDate)**
   - Clé: `'station:' + #stationId + ':' + #startDate + ':' + #endDate`
   - Cache par station et période

### 4. Gestion du Cache

#### API REST Endpoints

##### Statistiques
```http
GET /api/cache/stats
```
Retourne les statistiques du cache (nombre d'entrées par cache).

##### Vider tous les caches
```http
DELETE /api/cache/evict/all
```
Vide tous les caches (forecasts, stations, weather-data).

##### Vider le cache des prévisions
```http
DELETE /api/cache/evict/forecasts
```
Vide uniquement le cache des prévisions météo.

##### Vider le cache d'une station
```http
DELETE /api/cache/evict/forecasts/station/{stationId}
```
Vide le cache pour une station spécifique.

##### Vider le cache d'une période
```http
DELETE /api/cache/evict/forecasts/station/{stationId}/period?startDate=2024-01-01&endDate=2024-01-07
```
Vide le cache pour une station et une période spécifiques.

##### Vérifier la santé de Redis
```http
GET /api/cache/health
```
Vérifie si Redis est disponible.

#### Spring Boot Actuator

Le health check Redis est intégré dans Actuator:
```http
GET /actuator/health
```

Réponse exemple:
```json
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP",
      "details": {
        "status": "Redis is available",
        "response": "PONG"
      }
    }
  }
}
```

### 5. Comportement de Secours (Fallback)

Si Redis n'est pas disponible:
- Les requêtes continuent de fonctionner normalement
- Les appels sont faits directement au service météo
- Les erreurs sont loggées avec des warnings
- Aucune exception n'est levée

### 6. Tests

#### Tests d'intégration (Testcontainers)
- Démarre un conteneur Redis réel pour les tests
- Teste la mise en cache et l'éviction
- Teste les statistiques du cache
- Localisation: `src/test/java/com/irrigation/arrosage/integration/RedisCacheIntegrationTest.java`

#### Tests unitaires (Embedded Redis)
- Utilise un serveur Redis embarqué sur le port 6370
- Teste la configuration du cache
- Teste les opérations de base (put, get, evict, clear)
- Localisation: `src/test/java/com/irrigation/arrosage/config/RedisCacheConfigTest.java`

### 7. Dépendances

```xml
<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- Jedis Client -->
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
</dependency>

<!-- Jackson Java Time -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>

<!-- Tests -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>it.ozimov</groupId>
    <artifactId>embedded-redis</artifactId>
    <version>1.4.3</version>
    <scope>test</scope>
</dependency>
```

## Démarrage

### 1. Démarrer Redis avec Docker Compose

```bash
docker-compose up -d redis
```

### 2. Vérifier que Redis fonctionne

```bash
docker-compose ps redis
docker logs redis-cache
```

### 3. Démarrer le service

```bash
cd backend/arrosage-service
mvn spring-boot:run
```

### 4. Tester le cache

```bash
# Obtenir les statistiques
curl http://localhost:8082/api/cache/stats

# Vérifier la santé de Redis
curl http://localhost:8082/api/cache/health

# Vérifier via Actuator
curl http://localhost:8082/actuator/health
```

## Surveillance

### Logs
Le niveau de log pour le cache est configuré sur DEBUG:
```properties
logging.level.com.irrigation.arrosage=DEBUG
```

### Métriques
Les métriques de cache sont disponibles via Actuator:
```http
GET /actuator/metrics
GET /actuator/caches
```

### Redis CLI
Pour inspecter Redis directement:
```bash
docker exec -it redis-cache redis-cli

# Lister toutes les clés
KEYS arrosage:*

# Voir le contenu d'une clé
GET arrosage:forecasts::all

# Voir le TTL d'une clé
TTL arrosage:forecasts::all

# Voir les statistiques
INFO stats
```

## Configuration Personnalisée

### Modifier le TTL
Dans `application.properties`:
```properties
# Prévisions: 2 heures (en millisecondes)
spring.cache.redis.time-to-live=7200000
```

### Modifier la pool Jedis
```properties
spring.data.redis.jedis.pool.max-active=8
spring.data.redis.jedis.pool.max-idle=8
spring.data.redis.jedis.pool.min-idle=0
```

### Ajouter un nouveau cache
1. Ajouter le nom dans `application.properties`:
```properties
spring.cache.cache-names=forecasts,stations,weather-data,new-cache
```

2. Configurer le TTL dans `RedisCacheConfig.java`:
```java
cacheConfigurationMap.put("new-cache", 
    createCacheConfiguration(Duration.ofHours(1)));
```

3. Utiliser l'annotation `@Cacheable`:
```java
@Cacheable(value = "new-cache", key = "#param")
public Data getData(String param) {
    // ...
}
```

## Dépannage

### Redis n'est pas disponible
- Vérifier que Docker est démarré
- Vérifier les logs: `docker logs redis-cache`
- Vérifier le port: `netstat -an | findstr 6379`

### Le cache ne fonctionne pas
- Vérifier les logs de l'application
- Vérifier que `@EnableCaching` est présent
- Vérifier les clés de cache dans Redis CLI
- Vérifier les statistiques: `GET /api/cache/stats`

### Tests échouent
- Vérifier que le port 6370 est libre (embedded Redis)
- Vérifier que Docker est disponible (Testcontainers)
- Vérifier les logs des tests

## Performance

### Avant le cache
- Chaque requête appelle le service météo
- Latence: ~100-500ms par requête
- Charge réseau élevée

### Après le cache
- Premier appel: ~100-500ms (mise en cache)
- Appels suivants: ~1-5ms (depuis Redis)
- Réduction de 95-99% de la latence
- Réduction de la charge sur le service météo

## Sécurité

### Recommandations pour la production
1. Activer l'authentification Redis:
```properties
spring.data.redis.password=your-secure-password
```

2. Utiliser TLS/SSL:
```properties
spring.data.redis.ssl=true
```

3. Limiter l'accès réseau à Redis
4. Configurer des alertes de surveillance
5. Sauvegarder régulièrement les données Redis

## Références

- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Redis Documentation](https://redis.io/docs/)
- [Testcontainers](https://www.testcontainers.org/)
