import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { JournalService } from '../../services/journal.service';
import { JournalArrosage } from '../../models/journal.model';

@Component({
  selector: 'app-journal-details',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  templateUrl: './journal-details.component.html',
  styleUrls: ['./journal-details.component.scss']
})
export class JournalDetailsComponent implements OnInit {
  private journalService = inject(JournalService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  journal = signal<JournalArrosage | null>(null);
  journalId = signal<number>(0);
  isLoading = signal(true);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.journalId.set(+id);
      this.loadJournal(+id);
    }
  }

  loadJournal(id: number) {
    this.isLoading.set(true);
    this.journalService.getJournalById(id).subscribe({
      next: (data) => {
        this.journal.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading journal:', error);
        this.snackBar.open('Erreur lors du chargement du journal', 'Fermer', {
          duration: 3000
        });
        this.isLoading.set(false);
        this.router.navigate(['/journal']);
      }
    });
  }

  editJournal() {
    this.router.navigate(['/journal/edit', this.journalId()]);
  }

  deleteJournal() {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce journal ?')) {
      this.journalService.deleteJournal(this.journalId()).subscribe({
        next: () => {
          this.snackBar.open('Journal supprimé avec succès', 'Fermer', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.router.navigate(['/journal']);
        },
        error: (error) => {
          console.error('Error deleting journal:', error);
          this.snackBar.open('Erreur lors de la suppression du journal', 'Fermer', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
    }
  }

  goBack() {
    this.router.navigate(['/journal']);
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

  formatVolume(volume: number): string {
    return `${volume.toFixed(2)} m³ (${(volume * 1000).toFixed(0)} litres)`;
  }
}
