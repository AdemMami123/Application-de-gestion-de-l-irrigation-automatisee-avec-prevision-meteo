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

### 2. Setup du Repository de Configuration

Créez le repository local pour les configurations centralisées:

```powershell
# Windows PowerShell
mkdir $env:USERPROFILE\config-repo
cd $env:USERPROFILE\config-repo
git init
git add .
git commit -m "Initial configuration"
```

```bash
# Linux/macOS
mkdir ~/config-repo
cd ~/config-repo
git init
git add .
git commit -m "Initial configuration"
```

Les fichiers de configuration sont déjà créés dans `~/config-repo/`.

### 3. Démarrer l'Infrastructure (Docker)

```bash
docker-compose up -d
```

Cela démarre dans l'ordre:
1. **Eureka Server** (port 8761) - Service Discovery
2. **Config Server** (port 8888) - Configuration centralisée
3. **PostgreSQL** (meteo-db sur port 5432, arrosage-db sur port 5433)
4. **Kafka + Zookeeper** (Kafka sur port 9092)
5. **Redis** (port 6379)
6. **pgAdmin** (port 5050) - Interface de gestion PostgreSQL
7. **Kafka UI** (port 8090) - Interface de gestion Kafka

**Important**: Les services démarrent avec des health checks. Eureka Server démarre en premier, suivi par Config Server.

### 4. Lancer les Microservices Backend

**Ordre de démarrage recommandé**:
1. Eureka Server (déjà dans Docker)
2. Config Server (déjà dans Docker)
3. API Gateway
4. Meteo Service
5. Arrosage Service
6. Auth Service

#### Option A: Avec Maven

```bash
# Les services Eureka et Config sont déjà dans Docker
# Démarrez les autres services:

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

**Vérification**: Accédez à http://localhost:8761 pour voir tous les services enregistrés dans Eureka.

### 5. Lancer le Frontend Angular

```bash
cd frontend/irrigation-app
npm install
npm start
```

L'application sera accessible sur http://localhost:4200

## 🏗️ Service Discovery & Configuration

### Eureka Server (Service Discovery)

Le serveur Eureka permet aux microservices de s'enregistrer et de se découvrir dynamiquement.

- **Console**: http://localhost:8761
- **Fonction**: Registre de services, load balancing côté client
- **Auto-registration**: Tous les services s'enregistrent automatiquement au démarrage

### Config Server (Configuration Centralisée)

Le Config Server fournit une configuration centralisée pour tous les microservices.

- **API**: http://localhost:8888
- **Source**: Repository Git local (`~/config-repo`)
- **Endpoints**:
  - http://localhost:8888/meteo-service/default
  - http://localhost:8888/arrosage-service/default
  - http://localhost:8888/application/default

**Structure du config-repo**:
```
~/config-repo/
├── application.properties        # Configuration commune
├── meteo-service.properties      # Configuration meteo-service
└── arrosage-service.properties   # Configuration arrosage-service
```

### Ordre de Démarrage

1. **Eureka Server** → Service Discovery (premier à démarrer)
2. **Config Server** → Se connecte à Eureka, fournit les configurations
3. **Autres Services** → Se connectent à Eureka et récupèrent leur config depuis Config Server

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
