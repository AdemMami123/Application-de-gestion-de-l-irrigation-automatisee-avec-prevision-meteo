# Application de Gestion de l'Irrigation Automatisée avec Prévisions Météo

## 📋 Description

Système de gestion d'irrigation automatisée intégrant des prévisions météorologiques pour optimiser l'utilisation de l'eau. Le système est basé sur une architecture microservices permettant une scalabilité et une maintenance facilitée.

## 🏗️ Architecture

### Microservices Backend

- **eureka-server**: Service de découverte et registre des microservices
- **config-server**: Serveur de configuration centralisée
- **api-gateway**: Point d'entrée unique pour tous les services (routing, load balancing)
- **meteo-service**: Gestion des prévisions météorologiques
- **arrosage-service**: Gestion des systèmes d'arrosage et planification
- **auth-service**: Authentification et autorisation

### Frontend

- **irrigation-app**: Application Angular pour l'interface utilisateur

### Infrastructure

- **PostgreSQL**: Bases de données pour meteo-service et arrosage-service
- **Kafka/Zookeeper**: Messagerie asynchrone entre microservices
- **Redis**: Cache distribué et session management

## 🚀 Prérequis

- **Java**: 17 ou supérieur
- **Node.js**: 18.x ou supérieur
- **Docker Desktop**: Pour containerisation et orchestration
- **Maven**: 3.8+ (ou utiliser Maven Wrapper inclus)
- **Git**: Pour la gestion de version

### IDEs Recommandés

- **IntelliJ IDEA** (Ultimate ou Community Edition)
- **Visual Studio Code** avec extensions Java et Angular

## 📦 Installation et Configuration

### 1. Cloner le Repository

```bash
git clone <repository-url>
cd irregation_meteo_springboot
```

### 2. Démarrer l'Infrastructure (Docker)

```bash
docker-compose up -d
```

Cela démarre:
- PostgreSQL (meteo-db sur port 5432, arrosage-db sur port 5433)
- Kafka + Zookeeper (Kafka sur port 9092)
- Redis (port 6379)

### 3. Lancer les Microservices Backend

#### Option A: Avec Maven

```bash
# Eureka Server
cd backend/eureka-server
./mvnw spring-boot:run

# Config Server
cd backend/config-server
./mvnw spring-boot:run

# API Gateway
cd backend/api-gateway
./mvnw spring-boot:run

# Meteo Service
cd backend/meteo-service
./mvnw spring-boot:run

# Arrosage Service
cd backend/arrosage-service
./mvnw spring-boot:run

# Auth Service
cd backend/auth-service
./mvnw spring-boot:run
```

#### Option B: Avec IDE (IntelliJ IDEA)

1. Ouvrir le projet dans IntelliJ
2. Attendre l'indexation et le téléchargement des dépendances
3. Exécuter chaque application Spring Boot via la classe principale

### 4. Lancer le Frontend Angular

```bash
cd frontend/irrigation-app
npm install
npm start
```

L'application sera accessible sur http://localhost:4200

## 🔧 Ports par Défaut

| Service | Port |
|---------|------|
| Eureka Server | 8761 |
| Config Server | 8888 |
| API Gateway | 8080 |
| Meteo Service | 8081 |
| Arrosage Service | 8082 |
| Auth Service | 8083 |
| Angular App | 4200 |
| PostgreSQL (meteo) | 5432 |
| PostgreSQL (arrosage) | 5433 |
| Kafka | 9092 |
| Zookeeper | 2181 |
| Redis | 6379 |

## 🛠️ Technologies Utilisées

### Backend
- Spring Boot 3.x
- Spring Cloud (Eureka, Config, Gateway)
- Spring Data JPA
- PostgreSQL
- Kafka
- Redis
- Lombok

### Frontend
- Angular 16+
- TypeScript
- SCSS
- Angular Material (optionnel)

### DevOps
- Docker & Docker Compose
- Maven
- Git

## 📚 Documentation Supplémentaire

- [Guide de Configuration IDE](./docs/IDE_SETUP.md)
- [Architecture Détaillée](./docs/ARCHITECTURE.md) (à venir)
- [Guide de Développement](./docs/DEVELOPMENT.md) (à venir)
- [Guide de Déploiement](./docs/DEPLOYMENT.md) (à venir)

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📝 License

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👥 Auteurs

- Votre Nom - *Travail initial*

## 🙏 Remerciements

- Spring Boot Community
- Angular Team
- Open Source Contributors
