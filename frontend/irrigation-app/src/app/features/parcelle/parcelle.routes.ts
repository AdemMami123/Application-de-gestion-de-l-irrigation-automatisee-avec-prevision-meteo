import { Routes } from '@angular/router';

export const parcelleRoutes: Routes = [
  {
    path: '',
    loadComponent: () => 
      import('./components/parcelle-list/parcelle-list.component').then(m => m.ParcelleListComponent)
  },
  {
    path: 'new',
    loadComponent: () => 
      import('./components/parcelle-form/parcelle-form.component').then(m => m.ParcelleFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => 
      import('./components/parcelle-details/parcelle-details.component').then(m => m.ParcelleDetailsComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () => 
      import('./components/parcelle-form/parcelle-form.component').then(m => m.ParcelleFormComponent)
  }
];
