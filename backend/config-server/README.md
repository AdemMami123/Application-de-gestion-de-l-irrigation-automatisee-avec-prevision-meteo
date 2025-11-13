# Config Server

Serveur de configuration centralisée utilisant Spring Cloud Config pour gérer les configurations de tous les microservices.

## Description

Le Config Server fournit une gestion centralisée des configurations externes pour les microservices. Il supporte plusieurs backends (Git, système de fichiers local, Vault, etc.).

## Port

- **8888** - API de configuration

## Configuration

Le serveur est configuré pour lire les configurations depuis un dépôt Git local situé dans `~/config-repo`.

## Setup du Repository de Configuration

### 1. Créer le Repository Local

```bash
# Windows PowerShell
mkdir $env:USERPROFILE\config-repo
cd $env:USERPROFILE\config-repo
git init
```

```bash
# Linux/macOS
mkdir ~/config-repo
cd ~/config-repo
git init
```

### 2. Créer des Fichiers de Configuration

Créez des fichiers de configuration pour chaque service:

**application.properties** (configuration globale)
```properties
# Common properties for all services
management.endpoints.web.exposure.include=health,info,metrics
```

**meteo-service.properties**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/meteodb
spring.datasource.username=meteo_user
spring.datasource.password=meteo_pass
```

**arrosage-service.properties**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/arrosagedb
spring.datasource.username=arrosage_user
spring.datasource.password=arrosage_pass
```

### 3. Commit les Configurations

```bash
git add .
git commit -m "Initial configuration"
```

## Démarrage

### Avec Maven
```bash
cd backend/config-server
./mvnw spring-boot:run
```

### Avec Docker
```bash
cd backend/config-server
docker build -t config-server .
docker run -p 8888:8888 config-server
```

## Endpoints de Configuration

Une fois démarré, les configurations sont accessibles via:

- **Application globale**: http://localhost:8888/application/default
- **Meteo Service**: http://localhost:8888/meteo-service/default
- **Arrosage Service**: http://localhost:8888/arrosage-service/default
- **Health Check**: http://localhost:8888/actuator/health

## Format des URLs

```
http://localhost:8888/{application}/{profile}[/{label}]
```

- `{application}`: Nom du service (ex: meteo-service)
- `{profile}`: Profil (dev, prod, etc.)
- `{label}`: Branche Git (optionnel, default: main)

## Utilisation dans les Microservices

Les microservices doivent ajouter cette dépendance:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

Et configurer dans `application.properties`:

```properties
spring.config.import=optional:configserver:http://localhost:8888
spring.cloud.config.name=meteo-service
```
