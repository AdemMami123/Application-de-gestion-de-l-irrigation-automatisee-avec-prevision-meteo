import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ProgrammeService } from '../../services/programme.service';
import { ProgrammeArrosage, StatutProgramme, STATUT_LABELS } from '../../models/programme.model';

@Component({
  selector: 'app-programme-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatCardModule,
    MatChipsModule,
    MatSelectModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './programme-list.component.html',
  styleUrls: ['./programme-list.component.scss']
})
export class ProgrammeListComponent implements OnInit {
  private programmeService = inject(ProgrammeService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  programmes = signal<ProgrammeArrosage[]>([]);
  filteredProgrammes = signal<ProgrammeArrosage[]>([]);
  searchTerm = signal('');
  selectedStatut = signal<StatutProgramme | 'ALL'>('ALL');
  
  displayedColumns: string[] = ['id', 'parcelleNom', 'datePlanifiee', 'duree', 'volumePrevu', 'statut', 'actions'];
  statutOptions = Object.values(StatutProgramme);
  statutLabels = STATUT_LABELS;
  StatutProgramme = StatutProgramme; // Expose enum to template

  ngOnInit() {
    this.loadProgrammes();
  }

  loadProgrammes() {
    this.programmeService.getAllProgrammes().subscribe({
      next: (data) => {
        this.programmes.set(data);
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading programmes:', error);
        this.snackBar.open('Erreur lors du chargement des programmes', 'Fermer', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  applyFilters() {
    let filtered = [...this.programmes()];

    // Filter by search term
    const search = this.searchTerm().toLowerCase();
    if (search) {
      filtered = filtered.filter(p => 
        p.parcelleNom?.toLowerCase().includes(search) ||
        p.id?.toString().includes(search)
      );
    }

    // Filter by statut
    if (this.selectedStatut() !== 'ALL') {
      filtered = filtered.filter(p => p.statut === this.selectedStatut());
    }

    this.filteredProgrammes.set(filtered);
  }

  onSearchChange() {
    this.applyFilters();
  }

  onStatutChange() {
    this.applyFilters();
  }

  viewDetails(id: number) {
    this.router.navigate(['/programme', id]);
  }

  editProgramme(id: number) {
    this.router.navigate(['/programme/edit', id]);
  }

  deleteProgramme(id: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce programme ?')) {
      this.programmeService.deleteProgramme(id).subscribe({
        next: () => {
          this.snackBar.open('Programme supprimé avec succès', 'Fermer', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.loadProgrammes();
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

  createProgramme() {
    this.router.navigate(['/programme/new']);
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

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getStatutLabel(statut: StatutProgramme): string {
    return this.statutLabels[statut];
  }
}
