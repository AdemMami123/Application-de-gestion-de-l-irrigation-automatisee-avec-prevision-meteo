import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DashboardService } from './services/dashboard.service';
import { DashboardStats, RecentActivity } from './models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatTooltipModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private router = inject(Router);

  stats = signal<DashboardStats>({
    totalParcelles: 0,
    totalProgrammes: 0,
    totalJournaux: 0,
    parcellesActives: 0,
    programmesEnCours: 0,
    programmesPlanifies: 0,
    programmesTermines: 0,
    volumeEauTotal: 0
  });

  recentActivities = signal<RecentActivity[]>([]);
  loading = signal<boolean>(true);

  // Computed values
  parcellesInactives = computed(() => 
    this.stats().totalParcelles - this.stats().parcellesActives
  );

  tauxActivationParcelles = computed(() => {
    const total = this.stats().totalParcelles;
    return total > 0 ? Math.round((this.stats().parcellesActives / total) * 100) : 0;
  });

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading.set(true);
    
    this.dashboardService.getDashboardStats().subscribe({
      next: (stats) => {
        this.stats.set(stats);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading dashboard stats:', error);
        this.loading.set(false);
      }
    });

    this.dashboardService.getRecentActivities().subscribe({
      next: (activities) => {
        this.recentActivities.set(activities);
      },
      error: (error) => {
        console.error('Error loading recent activities:', error);
      }
    });
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  refresh(): void {
    this.loadDashboard();
  }

  formatDate(date: Date | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getRelativeTime(date: Date): string {
    const now = new Date();
    const diffMs = now.getTime() - new Date(date).getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'À l\'instant';
    if (diffMins < 60) return `Il y a ${diffMins} min`;
    if (diffHours < 24) return `Il y a ${diffHours}h`;
    if (diffDays < 7) return `Il y a ${diffDays}j`;
    
    return new Date(date).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short'
    });
  }
}
