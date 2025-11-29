import { Routes } from '@angular/router';

export const JOURNAL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/journal-list/journal-list.component')
      .then(m => m.JournalListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./components/journal-form/journal-form.component')
      .then(m => m.JournalFormComponent)
  },
  {
    path: 'edit/:id',
    loadComponent: () => import('./components/journal-form/journal-form.component')
      .then(m => m.JournalFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./components/journal-details/journal-details.component')
      .then(m => m.JournalDetailsComponent)
  }
];
