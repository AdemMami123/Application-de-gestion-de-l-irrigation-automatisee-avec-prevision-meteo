# Eureka Server

Service de découverte Netflix Eureka pour l'enregistrement et la découverte des microservices.

## Description

Le serveur Eureka agit comme un registre de services où tous les microservices s'enregistrent au démarrage. Il permet la découverte dynamique des services et le load balancing côté client.

## Port

- **8761** - Console Eureka et API

## Configuration

Le serveur est configuré en mode standalone (ne s'enregistre pas lui-même).

## Démarrage

### Avec Maven
```bash
cd backend/eureka-server
./mvnw spring-boot:run
```

### Avec Docker
```bash
cd backend/eureka-server
docker build -t eureka-server .
docker run -p 8761:8761 eureka-server
```

## Accès

Une fois démarré, accédez à la console Eureka:
- http://localhost:8761

## Endpoints

- **Dashboard**: http://localhost:8761
- **Health Check**: http://localhost:8761/actuator/health
- **Info**: http://localhost:8761/actuator/info
