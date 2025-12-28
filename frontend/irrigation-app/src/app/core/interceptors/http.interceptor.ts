import { HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError, switchMap, BehaviorSubject, filter, take } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Shared state for token refresh
let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

// Public URLs that don't require authentication
const publicUrls = [
  '/api/auth/login',
  '/api/auth/register',
  '/api/auth/refresh-token',
  '/api/auth/validate'
];

/**
 * Check if URL is public (doesn't require authentication)
 */
const isPublicUrl = (url: string): boolean => {
  return publicUrls.some(publicUrl => url.includes(publicUrl));
};

/**
 * Add token to request
 */
const addTokenToRequest = (req: HttpRequest<unknown>, token: string): HttpRequest<unknown> => {
  return req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
};

/**
 * HTTP Interceptor for adding authentication token to requests
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  
  // Skip auth header for public endpoints
  if (isPublicUrl(req.url)) {
    return next(req);
  }
  
  const token = authService.getToken();

  if (token) {
    req = addTokenToRequest(req, token);
  }

  return next(req);
};

/**
 * HTTP Interceptor for global error handling with token refresh
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Handle 401 with token refresh
      if (error.status === 401 && !isPublicUrl(req.url)) {
        return handle401Error(req, next, authService, router);
      }

      let errorMessage = 'An error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        switch (error.status) {
          case 0:
            errorMessage = 'Unable to connect to server. Please check your network connection.';
            break;
          case 400:
            errorMessage = error.error?.message || 'Invalid request';
            break;
          case 403:
            errorMessage = 'Access denied';
            break;
          case 404:
            errorMessage = 'Resource not found';
            break;
          case 409:
            errorMessage = error.error?.message || 'Conflict - resource already exists';
            break;
          case 500:
            errorMessage = 'Internal server error';
            break;
          default:
            errorMessage = error.error?.message || `Error ${error.status}`;
        }
      }

      console.error('HTTP Error:', errorMessage, error);
      
      return throwError(() => ({
        status: error.status,
        message: errorMessage,
        originalError: error
      }));
    })
  );
};

/**
 * Handle 401 error with token refresh
 */
const handle401Error = (
  req: HttpRequest<unknown>, 
  next: HttpHandlerFn, 
  authService: AuthService,
  router: Router
) => {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    const refreshToken = authService.getRefreshToken();
    
    if (refreshToken) {
      return authService.refreshToken().pipe(
        switchMap((response) => {
          isRefreshing = false;
          refreshTokenSubject.next(response.accessToken);
          return next(addTokenToRequest(req, response.accessToken));
        }),
        catchError((error) => {
          isRefreshing = false;
          authService.logout();
          return throwError(() => ({
            status: 401,
            message: 'Session expired. Please login again.',
            originalError: error
          }));
        })
      );
    } else {
      isRefreshing = false;
      authService.logout();
      return throwError(() => ({
        status: 401,
        message: 'No refresh token available. Please login again.'
      }));
    }
  }

  // Wait for token refresh to complete
  return refreshTokenSubject.pipe(
    filter(token => token !== null),
    take(1),
    switchMap((token) => {
      return next(addTokenToRequest(req, token!));
    })
  );
};
