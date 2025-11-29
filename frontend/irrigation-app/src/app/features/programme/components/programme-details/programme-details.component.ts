import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ProgrammeService } from '../../services/programme.service';
import { ProgrammeArrosage, STATUT_LABELS, StatutProgramme } from '../../models/programme.model';

@Component({
  selector: 'app-programme-details',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  templateUrl: './programme-details.component.html',
  styleUrls: ['./programme-details.component.scss']
})
export class ProgrammeDetailsComponent implements OnInit {
  private programmeService = inject(ProgrammeService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  programme = signal<ProgrammeArrosage | null>(null);
  programmeId = signal<number>(0);
  statutLabels = STATUT_LABELS;
  isLoading = signal(true);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.programmeId.set(+id);
      this.loadProgramme(+id);
    }
  }

  loadProgramme(id: number) {
    this.isLoading.set(true);
    this.programmeService.getProgrammeById(id).subscribe({
      next: (data) => {
        this.programme.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading programme:', error);
        this.snackBar.open('Erreur lors du chargement du programme', 'Fermer', {
          duration: 3000
        });
        this.isLoading.set(false);
        this.router.navigate(['/programme']);
      }
    });
  }

  editProgramme() {
    this.router.navigate(['/programme/edit', this.programmeId()]);
  }

  deleteProgramme() {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce programme ?')) {
      this.programmeService.deleteProgramme(this.programmeId()).subscribe({
        next: () => {
          this.snackBar.open('Programme supprimé avec succès', 'Fermer', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.router.navigate(['/programme']);
        },
        error: (error) => {
          console.error('Error deleting programme:', error);
          this.snackBar.open('Erreur lors de la suppression du programme', 'Fermer', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
    }
  }

  goBack() {
    this.router.navigate(['/programme']);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('fr-FR', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getStatutColor(statut: StatutProgramme): string {
    switch (statut) {
      case StatutProgramme.PLANIFIE:
        return 'primary';
      case StatutProgramme.EN_COURS:
        return 'accent';
      case StatutProgramme.TERMINE:
        return 'success';
      case StatutProgramme.ANNULE:
        return 'warn';
      default:
        return '';
    }
  }

  calculateDebit(): number {
    const prog = this.programme();
    if (prog && prog.duree > 0) {
      return prog.volumePrevu / prog.duree;
    }
    return 0;
  }

  viewParcelle() {
    const prog = this.programme();
    if (prog?.parcelleId) {
      this.router.navigate(['/parcelle', prog.parcelleId]);
    }
  }
}
