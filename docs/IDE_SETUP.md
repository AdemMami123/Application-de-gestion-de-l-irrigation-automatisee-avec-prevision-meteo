# Guide de Configuration de l'Environnement de Développement

Ce guide vous aidera à configurer votre environnement de développement pour le projet d'irrigation automatisée.

## Table des Matières

1. [Prérequis Système](#prérequis-système)
2. [Installation Java 17+](#installation-java-17)
3. [Installation Node.js](#installation-nodejs)
4. [Installation Docker Desktop](#installation-docker-desktop)
5. [Configuration IntelliJ IDEA](#configuration-intellij-idea)
6. [Configuration Visual Studio Code](#configuration-visual-studio-code)
7. [Vérification de l'Installation](#vérification-de-linstallation)

---

## Prérequis Système

### Configuration Minimale
- **OS**: Windows 10/11, macOS 10.15+, Linux (Ubuntu 20.04+)
- **RAM**: 8 GB minimum (16 GB recommandé)
- **Espace Disque**: 20 GB libres
- **Processeur**: Intel Core i5 ou équivalent

---

## Installation Java 17+

### Windows

1. **Télécharger Java JDK 17**
   - Visitez [Adoptium](https://adoptium.net/) ou [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
   - Téléchargez le JDK 17 pour Windows (x64 MSI Installer)

2. **Installer le JDK**
   - Exécutez le fichier `.msi` téléchargé
   - Suivez l'assistant d'installation
   - Notez le chemin d'installation (ex: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x`)

3. **Configurer les Variables d'Environnement**
   ```powershell
   # Ouvrir PowerShell en mode Administrateur
   [System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Eclipse Adoptium\jdk-17.x.x', 'Machine')
   [System.Environment]::SetEnvironmentVariable('Path', $env:Path + ';%JAVA_HOME%\bin', 'Machine')
   ```

4. **Vérifier l'installation**
   ```powershell
   java -version
   javac -version
   ```

### macOS

1. **Utiliser Homebrew**
   ```bash
   brew install openjdk@17
   ```

2. **Configurer JAVA_HOME**
   ```bash
   echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
   source ~/.zshrc
   ```

### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version
```

---

## Installation Node.js

### Windows

1. **Télécharger Node.js**
   - Visitez [nodejs.org](https://nodejs.org/)
   - Téléchargez la version LTS (18.x ou supérieur)

2. **Installer Node.js**
   - Exécutez le fichier `.msi` téléchargé
   - Suivez l'assistant d'installation
   - Assurez-vous que "Add to PATH" est coché

3. **Vérifier l'installation**
   ```powershell
   node --version
   npm --version
   ```

### macOS

```bash
brew install node@18
```

### Linux (Ubuntu/Debian)

```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs
```

### Installation Globale d'Angular CLI

```bash
npm install -g @angular/cli
ng version
```

---

## Installation Docker Desktop

### Windows

1. **Prérequis**
   - Windows 10 64-bit: Pro, Enterprise, ou Education (Build 19041+)
   - Activer WSL 2 (Windows Subsystem for Linux)

2. **Installer WSL 2**
   ```powershell
   # PowerShell en mode Administrateur
   wsl --install
   ```

3. **Télécharger Docker Desktop**
   - Visitez [docker.com](https://www.docker.com/products/docker-desktop/)
   - Téléchargez Docker Desktop pour Windows

4. **Installer Docker Desktop**
   - Exécutez l'installateur
   - Redémarrez l'ordinateur si demandé
   - Lancez Docker Desktop

5. **Vérifier l'installation**
   ```powershell
   docker --version
   docker-compose --version
   ```

### macOS

1. **Télécharger Docker Desktop**
   - Visitez [docker.com](https://www.docker.com/products/docker-desktop/)
   - Choisissez la version selon votre puce (Intel ou Apple Silicon)

2. **Installer et Vérifier**
   ```bash
   docker --version
   docker-compose --version
   ```

### Linux (Ubuntu)

```bash
# Installer Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Ajouter l'utilisateur au groupe docker
sudo usermod -aG docker $USER

# Installer Docker Compose
sudo apt-get install docker-compose-plugin

# Vérifier
docker --version
docker compose version
```

---

## Configuration IntelliJ IDEA

### Installation

1. **Télécharger IntelliJ IDEA**
   - [Version Ultimate](https://www.jetbrains.com/idea/download/) (recommandé, 30 jours gratuits)
   - [Version Community](https://www.jetbrains.com/idea/download/) (gratuit)

2. **Installer IntelliJ IDEA**
   - Exécutez l'installateur
   - Suivez l'assistant d'installation

### Configuration du Projet

1. **Ouvrir le Projet**
   - `File` → `Open`
   - Sélectionnez le dossier racine du projet
   - IntelliJ détectera automatiquement les modules Maven

2. **Configurer le JDK**
   - `File` → `Project Structure` → `Project`
   - `SDK`: Sélectionnez Java 17 (ou `Add SDK` → `Download JDK`)
   - `Language Level`: 17

3. **Configurer Maven**
   - `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven`
   - Vérifiez que le JDK utilisé est Java 17

4. **Plugins Recommandés**
   - Lombok Plugin (pour les annotations Lombok)
   - Docker Plugin
   - Spring Boot Assistant
   - Database Navigator

   **Installation**: `File` → `Settings` → `Plugins` → `Marketplace`

### Configuration d'Exécution

1. **Configuration Spring Boot**
   - IntelliJ détecte automatiquement les applications Spring Boot
   - Clic droit sur la classe principale → `Run 'Application'`

2. **Configuration Docker**
   - Ouvrir `docker-compose.yml`
   - Clic sur l'icône verte à gauche → `Run docker-compose.yml`

---

## Configuration Visual Studio Code

### Installation

1. **Télécharger VS Code**
   - Visitez [code.visualstudio.com](https://code.visualstudio.com/)
   - Téléchargez et installez pour votre OS

### Extensions Essentielles

#### Pour Java/Spring Boot

1. **Extension Pack for Java**
   - ID: `vscjava.vscode-java-pack`
   - Inclut: Language Support, Debugger, Maven, Test Runner

2. **Spring Boot Extension Pack**
   - ID: `pivotal.vscode-boot-dev-pack`
   - Inclut: Spring Boot Tools, Dashboard, Initializr

3. **Lombok Annotations Support**
   - ID: `GabrielBB.vscode-lombok`

#### Pour Angular

1. **Angular Language Service**
   - ID: `Angular.ng-template`

2. **Angular Snippets**
   - ID: `johnpapa.Angular2`

3. **TSLint** ou **ESLint**
   - ID: `dbaeumer.vscode-eslint`

#### Général

1. **Docker**
   - ID: `ms-azuretools.vscode-docker`

2. **GitLens**
   - ID: `eamodio.gitlens`

3. **Thunder Client** (alternative à Postman)
   - ID: `rangav.vscode-thunder-client`

### Installation des Extensions

```bash
# Installation via ligne de commande
code --install-extension vscjava.vscode-java-pack
code --install-extension pivotal.vscode-boot-dev-pack
code --install-extension GabrielBB.vscode-lombok
code --install-extension Angular.ng-template
code --install-extension johnpapa.Angular2
code --install-extension dbaeumer.vscode-eslint
code --install-extension ms-azuretools.vscode-docker
code --install-extension eamodio.gitlens
```

### Configuration de VS Code

1. **Ouvrir le Projet**
   ```bash
   cd irregation_meteo_springboot
   code .
   ```

2. **Configurer Java**
   - Ouvrir `File` → `Preferences` → `Settings`
   - Rechercher "java.home"
   - Définir le chemin vers JDK 17

3. **Configuration Workspace**
   
   Créer `.vscode/settings.json`:
   ```json
   {
     "java.configuration.runtimes": [
       {
         "name": "JavaSE-17",
         "path": "/path/to/jdk-17",
         "default": true
       }
     ],
     "java.jdt.ls.java.home": "/path/to/jdk-17",
     "spring-boot.ls.java.home": "/path/to/jdk-17",
     "files.exclude": {
       "**/.classpath": true,
       "**/.project": true,
       "**/.settings": true,
       "**/.factorypath": true
     }
   }
   ```

4. **Lancer les Services**
   - Utiliser le terminal intégré (`` Ctrl+` ``)
   - Ou utiliser les scripts de lancement

---

## Vérification de l'Installation

### Script de Vérification (PowerShell - Windows)

```powershell
# Créer un fichier verify-setup.ps1
Write-Host "=== Vérification de l'environnement de développement ===" -ForegroundColor Green

# Java
Write-Host "`nJava:" -ForegroundColor Yellow
java -version

# Maven
Write-Host "`nMaven:" -ForegroundColor Yellow
mvn -version

# Node.js
Write-Host "`nNode.js:" -ForegroundColor Yellow
node --version

# npm
Write-Host "`nnpm:" -ForegroundColor Yellow
npm --version

# Angular CLI
Write-Host "`nAngular CLI:" -ForegroundColor Yellow
ng version --minimal

# Docker
Write-Host "`nDocker:" -ForegroundColor Yellow
docker --version

# Docker Compose
Write-Host "`nDocker Compose:" -ForegroundColor Yellow
docker-compose --version

Write-Host "`n=== Vérification terminée ===" -ForegroundColor Green
```

### Script de Vérification (Bash - macOS/Linux)

```bash
#!/bin/bash
echo "=== Vérification de l'environnement de développement ==="

echo -e "\nJava:"
java -version

echo -e "\nMaven:"
mvn -version

echo -e "\nNode.js:"
node --version

echo -e "\nnpm:"
npm --version

echo -e "\nAngular CLI:"
ng version --minimal

echo -e "\nDocker:"
docker --version

echo -e "\nDocker Compose:"
docker-compose --version

echo -e "\n=== Vérification terminée ==="
```

---

## Dépannage Courant

### Problème: Java JDK non reconnu

**Solution Windows**:
```powershell
# Vérifier JAVA_HOME
echo $env:JAVA_HOME
# Si vide, redéfinir
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\...\jdk-17', 'User')
```

### Problème: Docker ne démarre pas sous Windows

**Solution**:
1. Vérifier que la virtualisation est activée dans le BIOS
2. S'assurer que WSL 2 est installé
3. Redémarrer Docker Desktop

### Problème: Port déjà utilisé

**Solution**:
```powershell
# Trouver le processus utilisant le port 8080
netstat -ano | findstr :8080
# Tuer le processus (remplacer PID)
taskkill /PID <PID> /F
```

---

## Support et Ressources

- [Documentation Spring Boot](https://spring.io/projects/spring-boot)
- [Documentation Angular](https://angular.io/docs)
- [Documentation Docker](https://docs.docker.com/)
- [Stack Overflow](https://stackoverflow.com/)

---

**Dernière mise à jour**: Novembre 2025
