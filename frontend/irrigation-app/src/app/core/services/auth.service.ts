import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError, throwError, of } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

// Request DTOs
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  farmId?: number;
  roles?: string[];
}

export interface TokenRefreshRequest {
  refreshToken: string;
}

// Response DTOs
export interface UserDTO {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  farmId?: number;
  roles: string[];
  enabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserDTO;
}

export interface TokenRefreshResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

// Legacy User interface for backward compatibility
export interface User {
  id?: number;
  username: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
  farmId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly ACCESS_TOKEN_KEY = 'irrigation_access_token';
  private readonly REFRESH_TOKEN_KEY = 'irrigation_refresh_token';
  private readonly USER_KEY = 'irrigation_user';
  private readonly TOKEN_EXPIRY_KEY = 'irrigation_token_expiry';
  
  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();
  
  private refreshTokenInProgress = false;
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    // Check token validity on service initialization
    this.checkTokenExpiry();
  }

  /**
   * Login user
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.authServiceUrl}/api/auth/login`, credentials)
      .pipe(
        tap(response => {
          this.setSession(response);
        }),
        catchError(error => {
          console.error('Login failed:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Register new user
   */
  register(registerData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.authServiceUrl}/api/auth/register`, registerData)
      .pipe(
        tap(response => {
          this.setSession(response);
        }),
        catchError(error => {
          console.error('Registration failed:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Refresh access token
   */
  refreshToken(): Observable<TokenRefreshResponse> {
    const refreshToken = this.getRefreshToken();
    
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    if (this.refreshTokenInProgress) {
      return new Observable(observer => {
        this.refreshTokenSubject.subscribe(token => {
          if (token) {
            observer.next({ 
              accessToken: token, 
              refreshToken: refreshToken, 
              tokenType: 'Bearer' 
            });
            observer.complete();
          }
        });
      });
    }

    this.refreshTokenInProgress = true;

    return this.http.post<TokenRefreshResponse>(
      `${environment.authServiceUrl}/api/auth/refresh-token`, 
      { refreshToken }
    ).pipe(
      tap(response => {
        this.refreshTokenInProgress = false;
        this.updateTokens(response);
        this.refreshTokenSubject.next(response.accessToken);
      }),
      catchError(error => {
        this.refreshTokenInProgress = false;
        this.logout();
        return throwError(() => error);
      })
    );
  }

  /**
   * Logout user
   */
  logout(): void {
    const refreshToken = this.getRefreshToken();
    
    // Call backend logout endpoint
    if (refreshToken) {
      this.http.post(`${environment.authServiceUrl}/api/auth/logout`, { refreshToken })
        .pipe(catchError(() => of(null)))
        .subscribe();
    }

    // Clear local storage
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.TOKEN_EXPIRY_KEY);
    
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  /**
   * Get current access token
   */
  getToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  /**
   * Get refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    
    // Check token expiration
    if (this.isTokenExpired()) {
      return false;
    }
    
    return true;
  }

  /**
   * Check if access token is expired
   */
  isTokenExpired(): boolean {
    const expiryStr = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
    if (!expiryStr) {
      return true;
    }
    
    const expiry = parseInt(expiryStr, 10);
    const now = new Date().getTime();
    
    // Consider expired if less than 1 minute remaining
    return now >= (expiry - 60000);
  }

  /**
   * Get current user
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Get current user profile from server
   */
  getUserProfile(): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${environment.authServiceUrl}/api/auth/me`);
  }

  /**
   * Check if user has specific role
   */
  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    if (!user?.roles) return false;
    
    // Handle both 'ADMIN' and 'ROLE_ADMIN' formats
    const normalizedRole = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
    return user.roles.some(r => 
      r === role || r === normalizedRole || 
      r === role.replace('ROLE_', '') || 
      r.replace('ROLE_', '') === role.replace('ROLE_', '')
    );
  }

  /**
   * Check if user has any of the specified roles
   */
  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  /**
   * Check if user is admin
   */
  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  /**
   * Check if user is manager
   */
  isManager(): boolean {
    return this.hasAnyRole(['ADMIN', 'MANAGER']);
  }

  /**
   * Set authentication session from response
   */
  private setSession(authResponse: AuthResponse): void {
    // Store tokens
    localStorage.setItem(this.ACCESS_TOKEN_KEY, authResponse.accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, authResponse.refreshToken);
    
    // Calculate and store expiry time
    const expiryTime = new Date().getTime() + authResponse.expiresIn;
    localStorage.setItem(this.TOKEN_EXPIRY_KEY, expiryTime.toString());
    
    // Map UserDTO to User for local storage
    const user: User = {
      id: authResponse.user.id,
      username: authResponse.user.username,
      email: authResponse.user.email,
      firstName: authResponse.user.firstName,
      lastName: authResponse.user.lastName,
      roles: authResponse.user.roles,
      farmId: authResponse.user.farmId
    };
    
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  /**
   * Update tokens after refresh
   */
  private updateTokens(response: TokenRefreshResponse): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
    
    // Reset expiry (assume same expiry period as initial token)
    // This should ideally come from the server
    const expiryTime = new Date().getTime() + (24 * 60 * 60 * 1000); // 24 hours
    localStorage.setItem(this.TOKEN_EXPIRY_KEY, expiryTime.toString());
  }

  /**
   * Get user from local storage
   */
  private getUserFromStorage(): User | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (!userJson) {
      return null;
    }
    
    try {
      return JSON.parse(userJson);
    } catch {
      return null;
    }
  }

  /**
   * Check token expiry on startup
   */
  private checkTokenExpiry(): void {
    if (this.getToken() && this.isTokenExpired()) {
      const refreshToken = this.getRefreshToken();
      if (refreshToken) {
        // Try to refresh the token
        this.refreshToken().subscribe({
          error: () => this.logout()
        });
      } else {
        this.logout();
      }
    }
  }
}
