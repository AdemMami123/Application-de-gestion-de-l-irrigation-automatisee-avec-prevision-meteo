import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterModule, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterModule,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('Gestion de l\'Irrigation');
  isSidenavOpen = signal(true);

  menuItems = [
    { path: '/dashboard', icon: 'dashboard', label: 'Tableau de bord' },
    { path: '/parcelles', icon: 'agriculture', label: 'Parcelles' },
    { path: '/programme', icon: 'schedule', label: 'Programmes' },
    { path: '/journal', icon: 'history', label: 'Journal' }
  ];

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  toggleSidenav(): void {
    this.isSidenavOpen.set(!this.isSidenavOpen());
  }

  logout(): void {
    this.authService.logout();
  }

  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  get currentUser() {
    return this.authService.getCurrentUser();
  }
}
