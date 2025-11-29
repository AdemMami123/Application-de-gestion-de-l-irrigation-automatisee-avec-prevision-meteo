import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ParcelleService } from '../../services/parcelle.service';
import { Parcelle } from '../../models/parcelle.model';

@Component({
  selector: 'app-confirm-delete-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="confirm-dialog">
      <div class="dialog-header">
        <mat-icon class="warning-icon">warning</mat-icon>
        <h2>Confirmer la suppression</h2>
      </div>
      <mat-dialog-content>
        <p>Êtes-vous sûr de vouloir supprimer cette parcelle ?</p>
        <p class="warning-text">Cette action est irréversible et supprimera également tous les programmes et journaux associés.</p>
      </mat-dialog-content>
      <mat-dialog-actions>
        <button mat-stroked-button mat-dialog-close>Annuler</button>
        <button mat-raised-button color="warn" [mat-dialog-close]="true">
          <mat-icon>delete</mat-icon>
          Supprimer
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .confirm-dialog {
      .dialog-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 16px;

        .warning-icon {
          color: #ff9800;
          font-size: 32px;
          width: 32px;
          height: 32px;
        }

        h2 {
          margin: 0;
          font-size: 20px;
          font-weight: 600;
        }
      }

      mat-dialog-content {
        p {
          margin: 0 0 12px 0;
          color: #666;
        }

        .warning-text {
          color: #f44336;
          font-size: 13px;
          font-weight: 500;
        }
      }

      mat-dialog-actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
        padding: 16px 0 0 0;
      }
    }
  `]
})
export class ConfirmDeleteDialogComponent {}

@Component({
  selector: 'app-parcelle-details',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './parcelle-details.component.html',
  styleUrls: ['./parcelle-details.component.scss']
})
export class ParcelleDetailsComponent implements OnInit {
  private parcelleService = inject(ParcelleService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  parcelle = signal<Parcelle | null>(null);
  loading = signal<boolean>(false);
  parcelleId: number | null = null;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.parcelleId = +id;
      this.loadParcelle(this.parcelleId);
    } else {
      this.router.navigate(['/parcelles']);
    }
  }

  loadParcelle(id: number): void {
    this.loading.set(true);
    this.parcelleService.getParcelleById(id).subscribe({
      next: (parcelle: Parcelle) => {
        this.parcelle.set(parcelle);
        this.loading.set(false);
      },
      error: (error: any) => {
        console.error('Error loading parcelle:', error);
        this.snackBar.open('Erreur lors du chargement de la parcelle', 'Fermer', {
          duration: 3000
        });
        this.loading.set(false);
        this.router.navigate(['/parcelles']);
      }
    });
  }

  onEdit(): void {
    if (this.parcelleId) {
      this.router.navigate(['/parcelles', this.parcelleId, 'edit']);
    }
  }

  onDelete(): void {
    const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
      width: '450px',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.parcelleId) {
        this.deleteParcelle(this.parcelleId);
      }
    });
  }

  deleteParcelle(id: number): void {
    this.loading.set(true);
    this.parcelleService.deleteParcelle(id).subscribe({
      next: () => {
        this.snackBar.open('Parcelle supprimée avec succès', 'Fermer', {
          duration: 3000
        });
        this.router.navigate(['/parcelles']);
      },
      error: (error: any) => {
        console.error('Error deleting parcelle:', error);
        this.snackBar.open('Erreur lors de la suppression de la parcelle', 'Fermer', {
          duration: 3000
        });
        this.loading.set(false);
      }
    });
  }

  onBack(): void {
    this.router.navigate(['/parcelles']);
  }

  refresh(): void {
    if (this.parcelleId) {
      this.loadParcelle(this.parcelleId);
    }
  }
}
