import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap, finalize } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  // URLs that should not include auth headers
  private readonly publicUrls = [
    '/api/auth/login',
    '/api/auth/register',
    '/api/auth/refresh-token',
    '/api/auth/validate'
  ];

  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Skip auth header for public endpoints
    if (this.isPublicUrl(request.url)) {
      return next.handle(request);
    }

    // Add token to request
    const token = this.authService.getToken();
    if (token) {
      request = this.addTokenToRequest(request, token);
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Handle 401 Unauthorized
        if (error.status === 401 && !this.isPublicUrl(request.url)) {
          return this.handle401Error(request, next);
        }
        
        // Handle 403 Forbidden
        if (error.status === 403) {
          console.error('Access forbidden:', error.message);
        }

        return throwError(() => error);
      })
    );
  }

  /**
   * Add JWT token to request headers
   */
  private addTokenToRequest(request: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  /**
   * Handle 401 error by refreshing token
   */
  private handle401Error(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      const refreshToken = this.authService.getRefreshToken();
      
      if (refreshToken) {
        return this.authService.refreshToken().pipe(
          switchMap((response) => {
            this.refreshTokenSubject.next(response.accessToken);
            return next.handle(this.addTokenToRequest(request, response.accessToken));
          }),
          catchError((error) => {
            // Refresh failed, logout user
            this.authService.logout();
            return throwError(() => error);
          }),
          finalize(() => {
            this.isRefreshing = false;
          })
        );
      } else {
        this.isRefreshing = false;
        this.authService.logout();
        return throwError(() => new Error('No refresh token available'));
      }
    }

    // Wait for token refresh to complete
    return this.refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap((token) => {
        return next.handle(this.addTokenToRequest(request, token!));
      })
    );
  }

  /**
   * Check if URL is public (doesn't require auth)
   */
  private isPublicUrl(url: string): boolean {
    return this.publicUrls.some(publicUrl => url.includes(publicUrl));
  }
}
