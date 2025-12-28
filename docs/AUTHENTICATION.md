# Authentication System Documentation

## Overview

This document describes the complete authentication system implementation for the Irrigation Management Application. The system uses JWT (JSON Web Tokens) for secure authentication with role-based access control (RBAC).

## Architecture

```
+----------------+     +----------------+     +----------------+
|   Angular      | --> | API Gateway    | --> | Auth Service   |
|   Frontend     |     | (JWT Filter)   |     | (Spring Boot)  |
+----------------+     +----------------+     +----------------+
                                                      |
                                              +----------------+
                                              | PostgreSQL     |
                                              | (authdb)       |
                                              +----------------+
```

## Components

### Backend (auth-service)

**Location:** `backend/auth-service/`

#### Package Structure
```
com.irrigation.auth/
├── config/
│   ├── SecurityConfig.java       # Spring Security configuration
│   └── DataInitializer.java      # Creates default admin user
├── controller/
│   └── AuthController.java       # REST API endpoints
├── dto/
│   ├── AuthResponse.java         # Login response with tokens
│   ├── LoginRequest.java         # Login credentials
│   ├── MessageResponse.java      # Generic message response
│   ├── RegisterRequest.java      # Registration data
│   ├── TokenRefreshRequest.java  # Refresh token request
│   ├── TokenRefreshResponse.java # New access token response
│   └── UserDTO.java              # User data transfer object
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── TokenRefreshException.java
│   ├── UserAlreadyExistsException.java
│   └── UserNotFoundException.java
├── model/
│   ├── RefreshToken.java         # Refresh token entity
│   ├── Role.java                 # Role enum
│   └── User.java                 # User entity
├── repository/
│   ├── RefreshTokenRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java     # JWT generation/validation
└── service/
    ├── AuthService.java          # Main authentication logic
    ├── RefreshTokenService.java  # Refresh token management
    └── UserDetailsServiceImpl.java
```

### Frontend (Angular)

**Location:** `frontend/irrigation-app/`

#### Key Files
- `src/app/core/services/auth.service.ts` - Authentication service
- `src/app/core/interceptors/http.interceptor.ts` - JWT interceptor with refresh logic
- `src/app/core/guards/auth.guard.ts` - Route guards
- `src/app/features/auth/login/login.component.ts` - Login page
- `src/app/features/auth/register/register.component.ts` - Registration page

### API Gateway

**Location:** `backend/gateway-service/`

- `JwtAuthenticationFilter.java` - Validates JWT tokens for all requests
- Routes auth requests to auth-service
- Adds user info headers for downstream services

## Roles

The system supports four roles:

| Role | Description | Permissions |
|------|-------------|-------------|
| `ROLE_ADMIN` | System Administrator | Full access to all features |
| `ROLE_MANAGER` | Farm Manager | Manage users, view all farms |
| `ROLE_OPERATOR` | Field Operator | Control irrigation systems |
| `ROLE_VIEWER` | Read-only User | View data only |

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/register` | User registration | No |
| POST | `/api/auth/refresh-token` | Refresh access token | No |
| POST | `/api/auth/logout` | User logout | Yes |
| GET | `/api/auth/validate` | Validate token | No |
| GET | `/api/auth/me` | Get current user | Yes |

### User Management Endpoints (Admin/Manager)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/auth/users` | List all users | ADMIN, MANAGER |
| GET | `/api/auth/users/{id}` | Get user by ID | ADMIN, MANAGER |
| PUT | `/api/auth/users/{id}` | Update user | ADMIN |
| DELETE | `/api/auth/users/{id}` | Delete user | ADMIN |
| PATCH | `/api/auth/users/{id}/toggle-status` | Enable/disable user | ADMIN |

## JWT Token Structure

### Access Token Claims
```json
{
  "sub": "username",
  "roles": ["ROLE_ADMIN"],
  "iat": 1234567890,
  "exp": 1234654290
}
```

### Token Lifetimes
- **Access Token:** 24 hours (configurable via `jwt.expiration`)
- **Refresh Token:** 7 days (configurable via `jwt.refresh-expiration`)

## Configuration

### Backend Configuration (application.yml)
```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: ${JWT_EXPIRATION:86400000}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000}

app:
  admin:
    username: ${APP_ADMIN_USERNAME:admin}
    password: ${APP_ADMIN_PASSWORD:admin123}
    email: ${APP_ADMIN_EMAIL:admin@irrigation.local}
```

### Frontend Configuration (environment.ts)
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  authServiceUrl: 'http://localhost:8083'
};
```

## Usage Examples

### Login (cURL)
```bash
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### Login Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@irrigation.local",
    "roles": ["ROLE_ADMIN"],
    "enabled": true
  }
}
```

### Authenticated Request
```bash
curl -X GET http://localhost:8083/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Refresh Token
```bash
curl -X POST http://localhost:8083/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "550e8400-e29b-41d4-a716-446655440000"}'
```

## Security Best Practices

1. **JWT Secret:** Use a strong, unique secret key (min 256 bits) in production
2. **HTTPS:** Always use HTTPS in production
3. **Token Storage:** Store tokens securely (localStorage with proper XSS protection)
4. **Token Rotation:** Refresh tokens are rotated on each use
5. **Password Hashing:** BCrypt is used for password hashing
6. **CORS:** Configured to allow only trusted origins

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    farm_id VARCHAR(50),
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### User Roles Table
```sql
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id),
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role)
);
```

### Refresh Tokens Table
```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL
);
```

## Building and Running

### Build Auth Service
```bash
cd backend/auth-service
mvn clean package -DskipTests
```

### Run with Docker
```bash
# Build image
docker build -t irrigation/auth-service:latest .

# Run container
docker run -p 8083:8083 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/authdb \
  -e JWT_SECRET=your-secret-key \
  irrigation/auth-service:latest
```

### Run with Kubernetes
```bash
# Apply auth-service deployments
kubectl apply -f k8s/deployments/postgres-auth-deployment.yaml
kubectl apply -f k8s/services/postgres-auth-service.yaml
kubectl apply -f k8s/persistent-volumes/postgres-auth-pvc.yaml
kubectl apply -f k8s/deployments/auth-service-deployment.yaml
kubectl apply -f k8s/services/auth-service-service.yaml
```

## Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Check if token is expired
   - Verify token is being sent in Authorization header
   - Ensure JWT secret matches between services

2. **403 Forbidden**
   - User doesn't have required role
   - Check role names (ROLE_ADMIN vs ADMIN)

3. **Database Connection**
   - Verify PostgreSQL is running
   - Check connection string and credentials

4. **Token Refresh Failing**
   - Refresh token may be expired (7 days)
   - Token may have been invalidated on logout

## Testing

### Unit Tests
```bash
cd backend/auth-service
mvn test
```

### Integration Tests
```bash
# Login test
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Should return access token and user info
```

## Version History

- **1.0.0** - Initial implementation with JWT auth, role-based access, refresh tokens
