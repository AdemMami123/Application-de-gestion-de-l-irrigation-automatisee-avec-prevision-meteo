# Meteo Service - Microservice Météo

Service de gestion des stations météorologiques et des prévisions météo pour le système d'irrigation automatisée.

## 🎯 Fonctionnalités

- **Gestion des Stations Météo**: CRUD complet pour les stations météorologiques
- **Gestion des Prévisions**: CRUD complet pour les prévisions météorologiques
- **Recherche Avancée**: Filtrage par fournisseur, période, station
- **API REST**: Endpoints RESTful avec validation
- **Documentation**: Swagger/OpenAPI intégré
- **Migration BDD**: Flyway pour la gestion des schémas
- **Service Discovery**: Enregistrement automatique dans Eureka
- **Configuration Centralisée**: Intégration avec Config Server

## 📦 Technologies

- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- Flyway
- Lombok
- SpringDoc OpenAPI (Swagger)
- JUnit 5 & Mockito

## 🏗️ Structure du Projet

```
meteo-service/
├── src/main/java/com/irrigation/meteo/
│   ├── config/          # Configuration (OpenAPI)
│   ├── controller/      # REST Controllers
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # JPA Entities
│   ├── exception/       # Gestion des exceptions
│   ├── repository/      # JPA Repositories
│   └── service/         # Business Logic
├── src/main/resources/
│   ├── db/migration/    # Scripts Flyway
│   └── application.properties
└── src/test/           # Tests unitaires
```

## 🚀 Démarrage

### Prérequis

- Java 17+
- PostgreSQL (ou Docker)
- Eureka Server (port 8761)
- Config Server (port 8888)

### Avec Maven

```bash
cd backend/meteo-service
./mvnw spring-boot:run
```

### Avec Docker

```bash
docker-compose up postgres-meteo eureka-server config-server
cd backend/meteo-service
./mvnw spring-boot:run
```

Le service démarre sur **http://localhost:8081**

## 📚 API Documentation

Une fois le service démarré, accédez à la documentation Swagger:

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/api-docs

## 🔌 Endpoints Principaux

### Stations Météo

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/stations` | Liste toutes les stations |
| GET | `/api/stations/{id}` | Récupère une station |
| POST | `/api/stations` | Crée une station |
| PUT | `/api/stations/{id}` | Met à jour une station |
| DELETE | `/api/stations/{id}` | Supprime une station |
| GET | `/api/stations/fournisseur/{fournisseur}` | Recherche par fournisseur |

### Prévisions Météo

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/previsions` | Liste toutes les prévisions |
| GET | `/api/previsions/{id}` | Récupère une prévision |
| POST | `/api/previsions` | Crée une prévision |
| PUT | `/api/previsions/{id}` | Met à jour une prévision |
| DELETE | `/api/previsions/{id}` | Supprime une prévision |
| GET | `/api/previsions/station/{stationId}` | Prévisions par station |
| GET | `/api/previsions/station/{stationId}/periode?startDate=...&endDate=...` | Prévisions par période |

## 📊 Modèle de Données

### StationMeteo

```json
{
  "id": 1,
  "nom": "Station Paris Centre",
  "latitude": 48.8566,
  "longitude": 2.3522,
  "fournisseur": "MeteoFrance"
}
```

### Prevision

```json
{
  "id": 1,
  "stationId": 1,
  "stationNom": "Station Paris Centre",
  "date": "2025-11-15",
  "temperatureMax": 22.5,
  "temperatureMin": 15.3,
  "pluiePrevue": 0.0,
  "vent": 12.5
}
```

## 🗄️ Base de Données

### Migration Flyway

Les migrations sont dans `src/main/resources/db/migration/`:

- `V1__Create_station_meteo_table.sql`: Table des stations
- `V2__Create_prevision_table.sql`: Table des prévisions
- `V3__Insert_sample_data.sql`: Données de test

### Configuration

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/meteodb
spring.datasource.username=meteo_user
spring.datasource.password=meteo_pass
```

## 🧪 Tests

Exécuter les tests unitaires:

```bash
./mvnw test
```

Les tests utilisent H2 en mémoire et Mockito pour les mocks.

## 🔧 Configuration

### Eureka Client

Le service s'enregistre automatiquement dans Eureka:

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

### Config Server

Le service récupère sa configuration depuis le Config Server:

```properties
spring.config.import=optional:configserver:http://localhost:8888
```

## 🎯 Exemples d'Utilisation

### Créer une Station

```bash
curl -X POST http://localhost:8081/api/stations \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Ma Station",
    "latitude": 48.8566,
    "longitude": 2.3522,
    "fournisseur": "MeteoFrance"
  }'
```

### Créer une Prévision

```bash
curl -X POST http://localhost:8081/api/previsions \
  -H "Content-Type: application/json" \
  -d '{
    "stationId": 1,
    "date": "2025-11-15",
    "temperatureMax": 25.0,
    "temperatureMin": 18.0,
    "pluiePrevue": 2.5,
    "vent": 15.0
  }'
```

### Récupérer les Prévisions d'une Station

```bash
curl http://localhost:8081/api/previsions/station/1
```

## 📈 Health Check

```bash
curl http://localhost:8081/actuator/health
```

## 🐛 Dépannage

### Port déjà utilisé

```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8081 | xargs kill -9
```

### Base de données non accessible

Vérifiez que PostgreSQL est démarré:

```bash
docker-compose ps postgres-meteo
```

### Erreur de migration Flyway

Réinitialiser Flyway:

```bash
./mvnw flyway:clean flyway:migrate
```

## 📝 Logs

Les logs sont configurés dans `application.properties`:

```properties
logging.level.com.irrigation.meteo=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

## 🤝 Contribution

1. Créer une branche feature
2. Implémenter les changements
3. Écrire les tests
4. Soumettre une PR

## 📄 License

MIT License
