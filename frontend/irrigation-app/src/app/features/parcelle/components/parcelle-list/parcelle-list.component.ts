import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ParcelleService } from '../../services/parcelle.service';
import { Parcelle } from '../../models/parcelle.model';

@Component({
  selector: 'app-parcelle-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './parcelle-list.component.html',
  styleUrls: ['./parcelle-list.component.scss']
})
export class ParcelleListComponent implements OnInit {
  private parcelleService = inject(ParcelleService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  parcelles = signal<Parcelle[]>([]);
  filteredParcelles = signal<Parcelle[]>([]);
  loading = signal<boolean>(false);
  searchQuery = signal<string>('');

  displayedColumns: string[] = ['nom', 'superficie', 'culture', 'actions'];

  ngOnInit(): void {
    this.loadParcelles();
  }

  loadParcelles(): void {
    this.loading.set(true);
    this.parcelleService.getAllParcelles().subscribe({
      next: (parcelles: Parcelle[]) => {
        this.parcelles.set(parcelles);
        this.filteredParcelles.set(parcelles);
        this.loading.set(false);
      },
      error: (error: any) => {
        console.error('Error loading parcelles:', error);
        this.snackBar.open('Erreur lors du chargement des parcelles', 'Fermer', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        this.loading.set(false);
      }
    });
  }

  onSearch(): void {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) {
      this.filteredParcelles.set(this.parcelles());
      return;
    }

    const filtered = this.parcelles().filter(p =>
      p.nom.toLowerCase().includes(query) ||
      p.culture.toLowerCase().includes(query) ||
      p.superficie.toString().includes(query)
    );
    this.filteredParcelles.set(filtered);
  }

  onClearSearch(): void {
    this.searchQuery.set('');
    this.filteredParcelles.set(this.parcelles());
  }

  navigateToCreate(): void {
    this.router.navigate(['/parcelles/new']);
  }

  navigateToView(id: number): void {
    this.router.navigate(['/parcelles', id]);
  }

  navigateToEdit(id: number): void {
    this.router.navigate(['/parcelles', id, 'edit']);
  }

  deleteParcelle(parcelle: Parcelle): void {
    if (!parcelle.id) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirmer la suppression',
        message: `Êtes-vous sûr de vouloir supprimer la parcelle "${parcelle.nom}" ?`
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && parcelle.id) {
        this.parcelleService.deleteParcelle(parcelle.id).subscribe({
          next: () => {
            this.snackBar.open('Parcelle supprimée avec succès', 'Fermer', {
              duration: 3000
            });
            this.loadParcelles();
          },
          error: (error: any) => {
            console.error('Error deleting parcelle:', error);
            this.snackBar.open('Erreur lors de la suppression', 'Fermer', {
              duration: 3000,
              panelClass: ['error-snackbar']
            });
          }
        });
      }
    });
  }

  refresh(): void {
    this.loadParcelles();
  }
}

// Confirmation Dialog Component
@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p>{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">Annuler</button>
      <button mat-raised-button color="warn" [mat-dialog-close]="true">Supprimer</button>
    </mat-dialog-actions>
  `
})
export class ConfirmDialogComponent {
  data = inject<any>(MAT_DIALOG_DATA);
}
