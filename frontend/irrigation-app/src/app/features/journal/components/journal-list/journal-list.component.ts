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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { JournalService } from '../../services/journal.service';
import { JournalArrosage } from '../../models/journal.model';

@Component({
  selector: 'app-journal-list',
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
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule
  ],
  templateUrl: './journal-list.component.html',
  styleUrls: ['./journal-list.component.scss']
})
export class JournalListComponent implements OnInit {
  private journalService = inject(JournalService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  journaux = signal<JournalArrosage[]>([]);
  filteredJournaux = signal<JournalArrosage[]>([]);
  searchTerm = signal('');
  startDate = signal<Date | null>(null);
  endDate = signal<Date | null>(null);
  
  displayedColumns: string[] = ['id', 'parcelleNom', 'dateExecution', 'volumeReel', 'remarque', 'actions'];

  ngOnInit() {
    this.loadJournaux();
  }

  loadJournaux() {
    this.journalService.getAllJournaux().subscribe({
      next: (data) => {
        this.journaux.set(data);
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading journaux:', error);
        this.snackBar.open('Erreur lors du chargement des journaux', 'Fermer', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  applyFilters() {
    let filtered = [...this.journaux()];

    // Filter by search term
    const search = this.searchTerm().toLowerCase();
    if (search) {
      filtered = filtered.filter(j => 
        j.parcelleNom?.toLowerCase().includes(search) ||
        j.id?.toString().includes(search) ||
        j.remarque?.toLowerCase().includes(search)
      );
    }

    // Filter by date range
    if (this.startDate() && this.endDate()) {
      const start = this.startDate()!.getTime();
      const end = this.endDate()!.getTime();
      filtered = filtered.filter(j => {
        const execDate = new Date(j.dateExecution).getTime();
        return execDate >= start && execDate <= end;
      });
    }

    this.filteredJournaux.set(filtered);
  }

  onSearchChange() {
    this.applyFilters();
  }

  onDateRangeChange() {
    this.applyFilters();
  }

  clearDateFilter() {
    this.startDate.set(null);
    this.endDate.set(null);
    this.applyFilters();
  }

  viewDetails(id: number) {
    this.router.navigate(['/journal', id]);
  }

  editJournal(id: number) {
    this.router.navigate(['/journal/edit', id]);
  }

  deleteJournal(id: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce journal ?')) {
      this.journalService.deleteJournal(id).subscribe({
        next: () => {
          this.snackBar.open('Journal supprimé avec succès', 'Fermer', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.loadJournaux();
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

  createJournal() {
    this.router.navigate(['/journal/new']);
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

  getTotalVolume(): number {
    return this.filteredJournaux().reduce((sum, j) => sum + j.volumeReel, 0);
  }
}
