import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  // Redirect root to dashboard
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },

  // Dashboard (lazy loaded)
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component')
      .then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },

  // Parcelles (lazy loaded)
  {
    path: 'parcelles',
    loadChildren: () => import('./features/parcelle/parcelle.routes')
      .then(m => m.parcelleRoutes),
    canActivate: [authGuard]
  },

  // Programmes (lazy loaded)
  {
    path: 'programme',
    loadChildren: () => import('./features/programme/programme.routes')
      .then(m => m.PROGRAMME_ROUTES),
    canActivate: [authGuard]
  },

  // Journal (lazy loaded)
  {
    path: 'journal',
    loadChildren: () => import('./features/journal/journal.routes')
      .then(m => m.JOURNAL_ROUTES),
    canActivate: [authGuard]
  },

  // Authentication routes (no guard)
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component')
      .then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component')
      .then(m => m.RegisterComponent)
  },

  // Error pages
  {
    path: 'unauthorized',
    loadComponent: () => import('./features/errors/unauthorized/unauthorized.component')
      .then(m => m.UnauthorizedComponent)
  },
  {
    path: '**',
    loadComponent: () => import('./features/errors/not-found/not-found.component')
      .then(m => m.NotFoundComponent)
  }
];
