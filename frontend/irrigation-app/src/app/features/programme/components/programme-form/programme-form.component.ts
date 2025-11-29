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
import { ProgrammeService } from '../../services/programme.service';
import { ParcelleService } from '../../../parcelle/services/parcelle.service';
import { StatutProgramme, STATUT_LABELS } from '../../models/programme.model';
import { Parcelle } from '../../../parcelle/models/parcelle.model';

@Component({
  selector: 'app-programme-form',
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
  templateUrl: './programme-form.component.html',
  styleUrls: ['./programme-form.component.scss']
})
export class ProgrammeFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private programmeService = inject(ProgrammeService);
  private parcelleService = inject(ParcelleService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  programmeForm!: FormGroup;
  isEditMode = signal(false);
  programmeId = signal<number | null>(null);
  parcelles = signal<Parcelle[]>([]);
  statutOptions = Object.values(StatutProgramme);
  statutLabels = STATUT_LABELS;
  isSubmitting = signal(false);

  ngOnInit() {
    this.initForm();
    this.loadParcelles();
    this.checkEditMode();
  }

  initForm() {
    this.programmeForm = this.fb.group({
      parcelleId: ['', [Validators.required]],
      datePlanifiee: ['', [Validators.required]],
      timePlanifiee: ['', [Validators.required]],
      duree: ['', [Validators.required, Validators.min(1)]],
      volumePrevu: ['', [Validators.required, Validators.min(0.01)]],
      statut: [StatutProgramme.PLANIFIE, [Validators.required]]
    });
  }

  loadParcelles() {
    this.parcelleService.getAllParcelles().subscribe({
      next: (data) => {
        this.parcelles.set(data);
      },
      error: (error) => {
        console.error('Error loading parcelles:', error);
        this.snackBar.open('Erreur lors du chargement des parcelles', 'Fermer', {
          duration: 3000
        });
      }
    });
  }

  checkEditMode() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.programmeId.set(+id);
      this.loadProgramme(+id);
    }
  }

  loadProgramme(id: number) {
    this.programmeService.getProgrammeById(id).subscribe({
      next: (programme) => {
        const datePlanifiee = new Date(programme.datePlanifiee);
        const timeString = datePlanifiee.toTimeString().substring(0, 5);
        
        this.programmeForm.patchValue({
          parcelleId: programme.parcelleId,
          datePlanifiee: datePlanifiee,
          timePlanifiee: timeString,
          duree: programme.duree,
          volumePrevu: programme.volumePrevu,
          statut: programme.statut
        });
      },
      error: (error) => {
        console.error('Error loading programme:', error);
        this.snackBar.open('Erreur lors du chargement du programme', 'Fermer', {
          duration: 3000
        });
        this.router.navigate(['/programme']);
      }
    });
  }

  onSubmit() {
    if (this.programmeForm.valid && !this.isSubmitting()) {
      this.isSubmitting.set(true);

      const formValue = this.programmeForm.value;
      
      // Combine date and time
      const date = new Date(formValue.datePlanifiee);
      const [hours, minutes] = formValue.timePlanifiee.split(':');
      date.setHours(parseInt(hours), parseInt(minutes), 0, 0);

      const programmeData = {
        parcelleId: formValue.parcelleId,
        datePlanifiee: date.toISOString(),
        duree: formValue.duree,
        volumePrevu: formValue.volumePrevu,
        statut: formValue.statut
      };

      const operation = this.isEditMode()
        ? this.programmeService.updateProgramme(this.programmeId()!, programmeData)
        : this.programmeService.createProgramme(programmeData);

      operation.subscribe({
        next: () => {
          this.snackBar.open(
            this.isEditMode() ? 'Programme modifié avec succès' : 'Programme créé avec succès',
            'Fermer',
            { duration: 3000, panelClass: ['success-snackbar'] }
          );
          this.router.navigate(['/programme']);
        },
        error: (error) => {
          console.error('Error saving programme:', error);
          this.snackBar.open(
            'Erreur lors de l\'enregistrement du programme',
            'Fermer',
            { duration: 3000, panelClass: ['error-snackbar'] }
          );
          this.isSubmitting.set(false);
        }
      });
    } else {
      Object.keys(this.programmeForm.controls).forEach(key => {
        this.programmeForm.get(key)?.markAsTouched();
      });
    }
  }

  cancel() {
    this.router.navigate(['/programme']);
  }

  calculateVolume() {
    const parcelleId = this.programmeForm.get('parcelleId')?.value;
    if (parcelleId) {
      const parcelle = this.parcelles().find(p => p.id === parcelleId);
      if (parcelle) {
        // Calculate volume: 5mm water per m² = superficie * 0.005 m³
        const volume = (parcelle.superficie * 0.005).toFixed(2);
        this.programmeForm.patchValue({ volumePrevu: parseFloat(volume) });
      }
    }
  }

  calculateDuration() {
    const volume = this.programmeForm.get('volumePrevu')?.value;
    if (volume) {
      // Calculate duration: volume / flow rate (0.5 m³/min)
      const duration = Math.ceil(volume / 0.5);
      this.programmeForm.patchValue({ duree: duration });
    }
  }
}
