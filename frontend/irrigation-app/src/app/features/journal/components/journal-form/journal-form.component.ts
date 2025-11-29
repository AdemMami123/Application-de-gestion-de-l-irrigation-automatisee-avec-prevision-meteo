import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { JournalService } from '../../services/journal.service';
import { ProgrammeService } from '../../../programme/services/programme.service';
import { ProgrammeArrosage } from '../../../programme/models/programme.model';

@Component({
  selector: 'app-journal-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule
  ],
  templateUrl: './journal-form.component.html',
  styleUrls: ['./journal-form.component.scss']
})
export class JournalFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private journalService = inject(JournalService);
  private programmeService = inject(ProgrammeService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  journalForm!: FormGroup;
  isEditMode = signal(false);
  journalId = signal<number | null>(null);
  programmes = signal<ProgrammeArrosage[]>([]);
  isSubmitting = signal(false);

  ngOnInit() {
    this.initForm();
    this.loadProgrammes();
    this.checkEditMode();
  }

  initForm() {
    this.journalForm = this.fb.group({
      programmeId: ['', [Validators.required]],
      dateExecution: ['', [Validators.required]],
      timeExecution: ['', [Validators.required]],
      volumeReel: ['', [Validators.required, Validators.min(0.01)]],
      remarque: ['']
    });
  }

  loadProgrammes() {
    this.programmeService.getAllProgrammes().subscribe({
      next: (data) => {
        this.programmes.set(data);
      },
      error: (error) => {
        console.error('Error loading programmes:', error);
        this.snackBar.open('Erreur lors du chargement des programmes', 'Fermer', {
          duration: 3000
        });
      }
    });
  }

  checkEditMode() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.journalId.set(+id);
      this.loadJournal(+id);
    }
  }

  loadJournal(id: number) {
    this.journalService.getJournalById(id).subscribe({
      next: (journal) => {
        const dateExecution = new Date(journal.dateExecution);
        const timeString = dateExecution.toTimeString().substring(0, 5);
        
        this.journalForm.patchValue({
          programmeId: journal.programmeId,
          dateExecution: dateExecution,
          timeExecution: timeString,
          volumeReel: journal.volumeReel,
          remarque: journal.remarque
        });
      },
      error: (error) => {
        console.error('Error loading journal:', error);
        this.snackBar.open('Erreur lors du chargement du journal', 'Fermer', {
          duration: 3000
        });
        this.router.navigate(['/journal']);
      }
    });
  }

  onProgrammeChange() {
    const programmeId = this.journalForm.get('programmeId')?.value;
    if (programmeId) {
      const programme = this.programmes().find(p => p.id === programmeId);
      if (programme) {
        // Auto-fill volume with programme's planned volume
        this.journalForm.patchValue({ volumeReel: programme.volumePrevu });
      }
    }
  }

  onSubmit() {
    if (this.journalForm.valid && !this.isSubmitting()) {
      this.isSubmitting.set(true);

      const formValue = this.journalForm.value;
      
      // Combine date and time
      const date = new Date(formValue.dateExecution);
      const [hours, minutes] = formValue.timeExecution.split(':');
      date.setHours(parseInt(hours), parseInt(minutes), 0, 0);

      const journalData = {
        programmeId: formValue.programmeId,
        dateExecution: date.toISOString(),
        volumeReel: formValue.volumeReel,
        remarque: formValue.remarque || ''
      };

      const operation = this.isEditMode()
        ? this.journalService.updateJournal(this.journalId()!, journalData)
        : this.journalService.createJournal(journalData);

      operation.subscribe({
        next: () => {
          this.snackBar.open(
            this.isEditMode() ? 'Journal modifié avec succès' : 'Journal créé avec succès',
            'Fermer',
            { duration: 3000, panelClass: ['success-snackbar'] }
          );
          this.router.navigate(['/journal']);
        },
        error: (error) => {
          console.error('Error saving journal:', error);
          this.snackBar.open(
            'Erreur lors de l\'enregistrement du journal',
            'Fermer',
            { duration: 3000, panelClass: ['error-snackbar'] }
          );
          this.isSubmitting.set(false);
        }
      });
    } else {
      Object.keys(this.journalForm.controls).forEach(key => {
        this.journalForm.get(key)?.markAsTouched();
      });
    }
  }

  cancel() {
    this.router.navigate(['/journal']);
  }
}
