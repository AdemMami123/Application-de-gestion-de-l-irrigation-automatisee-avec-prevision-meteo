import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Auth Guard - Protects routes from unauthorized access
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Store the attempted URL for redirecting after login
  router.navigate(['/login'], { 
    queryParams: { returnUrl: state.url }
  });
  
  return false;
};

/**
 * Role Guard - Checks if user has required role
 */
export const roleGuard = (requiredRole: string): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/login'], { 
        queryParams: { returnUrl: state.url }
      });
      return false;
    }

    if (authService.hasRole(requiredRole)) {
      return true;
    }

    router.navigate(['/unauthorized']);
    return false;
  };
};

/**
 * Roles Guard - Checks if user has any of the required roles
 */
export const rolesGuard = (requiredRoles: string[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/login'], { 
        queryParams: { returnUrl: state.url }
      });
      return false;
    }

    if (authService.hasAnyRole(requiredRoles)) {
      return true;
    }

    router.navigate(['/unauthorized']);
    return false;
  };
};

/**
 * Admin Guard - Shorthand for admin role check
 */
export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login'], { 
      queryParams: { returnUrl: state.url }
    });
    return false;
  }

  if (authService.isAdmin()) {
    return true;
  }

  router.navigate(['/unauthorized']);
  return false;
};

/**
 * Manager Guard - Allows Admin and Manager roles
 */
export const managerGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login'], { 
      queryParams: { returnUrl: state.url }
    });
    return false;
  }

  if (authService.isManager()) {
    return true;
  }

  router.navigate(['/unauthorized']);
  return false;
};

/**
 * Dynamic Role Guard - Uses route data to determine required roles
 * Usage in route: { path: 'admin', canActivate: [dynamicRoleGuard], data: { roles: ['ADMIN'] } }
 */
export const dynamicRoleGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (!authService.isAuthenticated()) {
    router.navigate(['/login'], { 
      queryParams: { returnUrl: state.url }
    });
    return false;
  }

  const requiredRoles = route.data['roles'] as string[];
  
  if (!requiredRoles || requiredRoles.length === 0) {
    // No roles specified, just check authentication
    return true;
  }

  if (authService.hasAnyRole(requiredRoles)) {
    return true;
  }

  router.navigate(['/unauthorized']);
  return false;
};
