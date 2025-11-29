import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { DashboardStats, RecentActivity } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  getDashboardStats(): Observable<DashboardStats> {
    return forkJoin({
      parcelles: this.http.get<any[]>(`${this.baseUrl}/api/arrosage/parcelles`),
      programmes: this.http.get<any[]>(`${this.baseUrl}/api/arrosage/programmes`),
      journaux: this.http.get<any[]>(`${this.baseUrl}/api/arrosage/journaux`)
    }).pipe(
      map(({ parcelles, programmes, journaux }) => {
        const parcellesActives = parcelles.filter(p => p.actif).length;
        
        const programmesEnCours = programmes.filter(p => p.statut === 'EN_COURS').length;
        const programmesPlanifies = programmes.filter(p => p.statut === 'PLANIFIE').length;
        const programmesTermines = programmes.filter(p => p.statut === 'TERMINE').length;
        
        const volumeEauTotal = programmes.reduce((sum, p) => sum + (p.volumeEau || 0), 0);
        
        const dernierJournal = journaux.length > 0 
          ? new Date(Math.max(...journaux.map(j => new Date(j.dateExecution).getTime())))
          : undefined;

        return {
          totalParcelles: parcelles.length,
          totalProgrammes: programmes.length,
          totalJournaux: journaux.length,
          parcellesActives,
          programmesEnCours,
          programmesPlanifies,
          programmesTermines,
          volumeEauTotal,
          dernierArrosage: dernierJournal
        };
      })
    );
  }

  getRecentActivities(): Observable<RecentActivity[]> {
    return forkJoin({
      parcelles: this.http.get<any[]>(`${this.baseUrl}/api/arrosage/parcelles`),
      programmes: this.http.get<any[]>(`${this.baseUrl}/api/arrosage/programmes`),
      journaux: this.http.get<any[]>(`${this.baseUrl}/api/arrosage/journaux`)
    }).pipe(
      map(({ parcelles, programmes, journaux }) => {
        const activities: RecentActivity[] = [];

        // Add recent parcelles
        parcelles.slice(0, 3).forEach(p => {
          activities.push({
            id: p.id,
            type: 'parcelle',
            title: `Parcelle créée: ${p.nom}`,
            description: `Superficie: ${p.superficie} m² - ${p.localisation}`,
            date: new Date(p.dateCreation || Date.now()),
            icon: 'terrain',
            color: '#4285f4'
          });
        });

        // Add recent programmes
        programmes.slice(0, 3).forEach(p => {
          activities.push({
            id: p.id,
            type: 'programme',
            title: `Programme: ${p.nom}`,
            description: `Durée: ${p.duree} min - Volume: ${p.volumeEau} L`,
            date: new Date(p.dateCreation || Date.now()),
            icon: 'water_drop',
            color: '#f57c00'
          });
        });

        // Add recent journaux
        journaux.slice(0, 3).forEach(j => {
          activities.push({
            id: j.id,
            type: 'journal',
            title: `Arrosage exécuté`,
            description: `Volume: ${j.volumeReel} L - Durée: ${j.dureeReelle} min`,
            date: new Date(j.dateExecution || Date.now()),
            icon: 'check_circle',
            color: '#4caf50'
          });
        });

        // Sort by date descending and return top 10
        return activities
          .sort((a, b) => b.date.getTime() - a.date.getTime())
          .slice(0, 10);
      })
    );
  }

  getParcelles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/parcelles`);
  }

  getProgrammes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/programmes`);
  }

  getJournaux(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/journaux`);
  }
}
