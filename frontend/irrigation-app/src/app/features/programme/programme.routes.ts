import { Routes } from '@angular/router';

export const PROGRAMME_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/programme-list/programme-list.component')
      .then(m => m.ProgrammeListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./components/programme-form/programme-form.component')
      .then(m => m.ProgrammeFormComponent)
  },
  {
    path: 'edit/:id',
    loadComponent: () => import('./components/programme-form/programme-form.component')
      .then(m => m.ProgrammeFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./components/programme-details/programme-details.component')
      .then(m => m.ProgrammeDetailsComponent)
  }
];
