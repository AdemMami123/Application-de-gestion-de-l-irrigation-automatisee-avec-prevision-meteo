# Irrigation App - Frontend

## Description

Application Angular pour la gestion de l'irrigation automatisée avec prévisions météorologiques.

## Prérequis

- Node.js 18.x ou supérieur
- npm 9.x ou supérieur
- Angular CLI 16+

## Installation

```bash
npm install
```

## Développement

Lancer le serveur de développement:

```bash
npm start
```

ou

```bash
ng serve
```

Accédez à `http://localhost:4200/`. L'application se recharge automatiquement si vous modifiez un fichier source.

## Build

```bash
ng build
```

Les artefacts de build seront stockés dans le répertoire `dist/`.

## Tests

### Tests Unitaires

```bash
ng test
```

### Tests E2E

```bash
ng e2e
```

## Structure du Projet

```
irrigation-app/
├── src/
│   ├── app/              # Composants de l'application
│   ├── assets/           # Ressources statiques
│   ├── environments/     # Configurations d'environnement
│   └── styles.scss       # Styles globaux
├── angular.json          # Configuration Angular
├── package.json          # Dépendances npm
└── tsconfig.json         # Configuration TypeScript
```

## Fonctionnalités Prévues

- Dashboard de gestion de l'irrigation
- Visualisation des prévisions météorologiques
- Configuration des zones d'arrosage
- Planification automatique
- Historique des arrosages
- Authentification et autorisation

## Technologies

- Angular 18+
- TypeScript
- SCSS
- RxJS
- Angular Material (à installer si nécessaire)

## Configuration API

L'URL de l'API Gateway peut être configurée dans `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```
