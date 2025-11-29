# Irrigation Management Frontend

Angular 17 application for managing agricultural irrigation systems with weather-based scheduling.

## 🚀 Features

### Implemented
- ✅ **Parcelle Management** (Complete CRUD)
  - List parcelles with pagination, search, and filtering
  - Create/Edit parcelle forms with validation
  - Toggle activation status
  - Delete parcelles
  - Responsive table design

- ✅ **Authentication System**
  - Login component with form validation
  - Auth service with token management
  - HTTP interceptors for authentication
  - Route guards for protected pages
  - User session management

- ✅ **Core Infrastructure**
  - Angular Material UI components
  - Reactive forms with validation
  - HTTP client with error handling
  - Lazy-loaded routes for performance
  - Responsive navigation layout
  - Environment configuration (dev/prod)

### In Progress / Planned
- 🚧 **Programme Management**
  - Service layer complete
  - Components structure created
  - Full CRUD implementation pending

- 🚧 **Journal/Execution History**
  - Service layer complete
  - Components structure created  
  - Filtering and statistics pending

- 📋 **Additional Features**
  - Weather integration dashboard
  - Real-time notifications
  - Sensor data visualization
  - Analytics and reports

## 📁 Project Structure

```
frontend/irrigation-app/
├── src/
│   ├── app/
│   │   ├── core/                      # Core functionality
│   │   │   ├── models/                # TypeScript interfaces
│   │   │   │   └── index.ts          # Parcelle, Programme, Journal models
│   │   │   ├── services/
│   │   │   │   └── auth.service.ts   # Authentication service
│   │   │   ├── guards/
│   │   │   │   └── auth.guard.ts     # Route protection
│   │   │   └── interceptors/
│   │   │       └── http.interceptor.ts  # Auth & error handling
│   │   │
│   │   ├── features/                  # Feature modules
│   │   │   ├── parcelle/             # Parcelle management
│   │   │   │   ├── components/
│   │   │   │   │   ├── parcelle-list/         # List view
│   │   │   │   │   ├── parcelle-form/         # Create/Edit form
│   │   │   │   │   └── parcelle-detail/       # Detail view
│   │   │   │   └── services/
│   │   │   │       └── parcelle.service.ts    # API integration
│   │   │   │
│   │   │   ├── programme/            # Programme management
│   │   │   │   ├── components/
│   │   │   │   │   ├── programme-list/
│   │   │   │   │   └── programme-form/
│   │   │   │   └── services/
│   │   │   │       └── programme.service.ts
│   │   │   │
│   │   │   ├── journal/              # Execution journal
│   │   │   │   ├── components/
│   │   │   │   │   └── journal-list/
│   │   │   │   └── services/
│   │   │   │       └── journal.service.ts
│   │   │   │
│   │   │   ├── auth/                 # Authentication
│   │   │   │   └── login/
│   │   │   │       └── login.component.ts
│   │   │   │
│   │   │   ├── dashboard/            # Dashboard
│   │   │   │   └── dashboard.component.ts
│   │   │   │
│   │   │   └── errors/               # Error pages
│   │   │       ├── not-found/
│   │   │       └── unauthorized/
│   │   │
│   │   ├── app.ts                    # Root component
│   │   ├── app.html                  # App template with navigation
│   │   ├── app.scss                  # App styles
│   │   ├── app.routes.ts             # Route configuration
│   │   └── app.config.ts             # App configuration
│   │
│   ├── environments/                  # Environment configs
│   │   ├── environment.ts            # Development
│   │   └── environment.prod.ts       # Production
│   │
│   ├── index.html                    # HTML entry point
│   ├── main.ts                       # Bootstrap file
│   └── styles.scss                   # Global styles
│
├── angular.json                      # Angular CLI config
├── package.json                      # Dependencies
├── tsconfig.json                     # TypeScript config
├── proxy.conf.json                   # Dev proxy config
├── Dockerfile                        # Docker build config
├── nginx.conf                        # Nginx server config
└── README.md                         # This file
```

## 🛠️ Technologies

- **Angular 17** - Latest Angular with standalone components
- **Angular Material** - Material Design components
- **RxJS** - Reactive programming
- **TypeScript** - Type-safe development
- **Nginx** - Production web server
- **Docker** - Containerization

## 📦 Installation

### Prerequisites
- Node.js 20.x or higher
- npm 10.x or higher
- Angular CLI 17.x

### Setup

1. **Install dependencies**
   ```bash
   cd frontend/irrigation-app
   npm install
   ```

2. **Configure environment**
   Edit `src/environments/environment.ts`:
   ```typescript
   export const environment = {
     production: false,
     apiUrl: '/api/arrosage',
     gatewayUrl: 'http://localhost:8080'
   };
   ```

## 🚀 Development

### Start development server
```bash
npm start
# OR
ng serve --proxy-config proxy.conf.json
```

Application runs at `http://localhost:4200`

### Build for production
```bash
npm run build:prod
# OR
ng build --configuration production
```

Output: `dist/irrigation-app/`

### Run tests
```bash
npm test
```

### Lint code
```bash
npm run lint
```

## 🐳 Docker Deployment

### Build Docker image
```bash
docker build -t irrigation-frontend:latest .
```

### Run container
```bash
docker run -d \
  -p 80:80 \
  --name irrigation-frontend \
  irrigation-frontend:latest
```

### Docker Compose (with backend services)
Add to root `docker-compose.yml`:

```yaml
services:
  frontend:
    build: ./frontend/irrigation-app
    container_name: irrigation-frontend
    ports:
      - "4200:80"
    depends_on:
      - gateway-service
    networks:
      - irrigation-network
```

Run:
```bash
docker-compose up -d frontend
```

## 🔌 API Integration

### API Base URL
- **Development:** `http://localhost:8080/api/arrosage` (via proxy)
- **Production:** `/api/arrosage` (same origin)

### Proxy Configuration
`proxy.conf.json` proxies `/api` requests to gateway:
```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

### API Endpoints Used

**Parcelles:**
- `GET /api/arrosage/parcelles` - List with pagination
- `GET /api/arrosage/parcelles/{id}` - Get by ID
- `POST /api/arrosage/parcelles` - Create
- `PUT /api/arrosage/parcelles/{id}` - Update
- `DELETE /api/arrosage/parcelles/{id}` - Delete
- `PATCH /api/arrosage/parcelles/{id}/toggle-activation` - Toggle status

**Programmes:**
- `GET /api/arrosage/programmes` - List programmes
- `POST /api/arrosage/programmes` - Create programme
- `PUT /api/arrosage/programmes/{id}` - Update
- `POST /api/arrosage/programmes/{id}/execute` - Manual execution

**Journal:**
- `GET /api/arrosage/journal` - List executions with filters
- `GET /api/arrosage/journal/stats` - Statistics

## 🎨 UI Components

### Material Design
All UI components use Angular Material:
- Tables with sorting/pagination
- Forms with validation
- Cards and layouts
- Icons and buttons
- Snackbar notifications
- Navigation drawer

### Responsive Design
- Mobile-first approach
- Adaptive layouts for tablets and desktops
- Touch-friendly controls

## 🔐 Authentication

### Login Flow
1. User enters credentials on `/login`
2. `AuthService.login()` calls backend API
3. Token stored in localStorage
4. `authInterceptor` adds token to requests
5. `authGuard` protects routes

### Mock Authentication
For development, login with:
- Username: `admin`
- Password: `admin`

### Session Management
- Token: `localStorage.getItem('irrigation_auth_token')`
- User: `localStorage.getItem('irrigation_user')`
- Auto-logout on 401 responses

## 🛣️ Routing

```
/                       → Redirect to /dashboard
/dashboard              → Dashboard (protected)
/parcelles              → List parcelles (protected)
/parcelles/new          → Create parcelle (protected)
/parcelles/edit/:id     → Edit parcelle (protected)
/parcelles/:id          → View parcelle details (protected)
/programmes             → List programmes (protected)
/programmes/new         → Create programme (protected)
/programmes/edit/:id    → Edit programme (protected)
/journal                → Execution history (protected)
/login                  → Login page (public)
/unauthorized           → 403 error page
/**                     → 404 not found page
```

## 📊 Models

### Parcelle
```typescript
interface Parcelle {
  id?: number;
  nom: string;
  superficie: number;
  localisation: string;
  typeSol: string;
  typeCulture: string;
  capteurId?: number;
  actif: boolean;
  dateCreation?: Date;
  dateModification?: Date;
}
```

### Programme
```typescript
interface ProgrammeIrrigation {
  id?: number;
  nom: string;
  parcelleId: number;
  dateDebut: Date;
  dateFin: Date;
  heureDebut: string;
  duree: number;
  quantiteEau: number;
  frequence: 'DAILY' | 'WEEKLY' | 'CUSTOM';
  actif: boolean;
  baseSurMeteo: boolean;
  seuilHumidite?: number;
  seuilPluie?: number;
}
```

### Journal
```typescript
interface JournalExecution {
  id?: number;
  programmeId: number;
  parcelleId: number;
  dateExecution: Date;
  statut: 'PLANIFIE' | 'EN_COURS' | 'TERMINE' | 'ANNULE' | 'ERREUR';
  dureeEffective?: number;
  quantiteEauUtilisee?: number;
  temperature?: number;
  humidite?: number;
  notes?: string;
}
```

## 🧪 Testing

### Unit Tests
```bash
npm test
```

### E2E Tests
```bash
npm run e2e
```

### Test Coverage
```bash
npm run test:coverage
```

## 📝 Code Style

- Angular style guide compliance
- ESLint for linting
- Prettier for formatting
- Standalone components (Angular 17)
- Signal-based state management

## 🚧 Next Steps

1. **Complete Programme Module**
   - Implement full CRUD UI
   - Add weather-based scheduling form
   - Programme execution visualization

2. **Complete Journal Module**
   - Execution history table
   - Date range filters
   - Statistics dashboard
   - Export functionality

3. **Enhanced Features**
   - Real-time updates (WebSocket)
   - Weather forecast integration
   - Sensor data charts
   - Notification system
   - Mobile app (PWA)

4. **Performance Optimization**
   - Virtual scrolling for large lists
   - Image lazy loading
   - Bundle size optimization
   - Service worker caching

## 📞 Support

For issues or questions, contact the development team or open an issue in the repository.

## 📄 License

Copyright © 2025 Irrigation Management System
