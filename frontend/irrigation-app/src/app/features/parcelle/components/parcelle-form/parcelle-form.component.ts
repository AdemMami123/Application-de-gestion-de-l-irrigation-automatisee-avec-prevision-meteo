import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ParcelleService } from '../../services/parcelle.service';
import { Parcelle, ParcelleCreateDto, ParcelleUpdateDto } from '../../models/parcelle.model';

@Component({
  selector: 'app-parcelle-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './parcelle-form.component.html',
  styleUrls: ['./parcelle-form.component.scss']
})
export class ParcelleFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private parcelleService = inject(ParcelleService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  parcelleForm!: FormGroup;
  loading = signal<boolean>(false);
  isEditMode = signal<boolean>(false);
  parcelleId: number | null = null;

  typesCulture: string[] = [
    'Céréales',
    'Légumes',
    'Fruits',
    'Légumineuses',
    'Cultures fourragères',
    'Plantes aromatiques',
    'Viticulture',
    'Oléagineux',
    'Cultures maraîchères',
    'Tubercules'
  ];

  ngOnInit(): void {
    this.initializeForm();
    this.checkEditMode();
  }

  initializeForm(): void {
    this.parcelleForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      superficie: ['', [Validators.required, Validators.min(1), Validators.max(1000000)]],
      culture: ['', Validators.required]
    });
  }

  checkEditMode(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode.set(true);
      this.parcelleId = +id;
      this.loadParcelle(this.parcelleId);
    }
  }

  loadParcelle(id: number): void {
    this.loading.set(true);
    this.parcelleService.getParcelleById(id).subscribe({
      next: (parcelle: Parcelle) => {
        this.parcelleForm.patchValue({
          nom: parcelle.nom,
          superficie: parcelle.superficie,
          culture: parcelle.culture
        });
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

  onSubmit(): void {
    if (this.parcelleForm.invalid) {
      this.parcelleForm.markAllAsTouched();
      this.snackBar.open('Veuillez remplir tous les champs requis', 'Fermer', {
        duration: 3000
      });
      return;
    }

    this.loading.set(true);
    const formValue = this.parcelleForm.value;

    if (this.isEditMode() && this.parcelleId) {
      this.updateParcelle(this.parcelleId, formValue);
    } else {
      this.createParcelle(formValue);
    }
  }

  createParcelle(data: ParcelleCreateDto): void {
    this.parcelleService.createParcelle(data).subscribe({
      next: (parcelle: Parcelle) => {
        this.snackBar.open('Parcelle créée avec succès', 'Fermer', {
          duration: 3000
        });
        this.loading.set(false);
        this.router.navigate(['/parcelles', parcelle.id]);
      },
      error: (error: any) => {
        console.error('Error creating parcelle:', error);
        this.snackBar.open('Erreur lors de la création de la parcelle', 'Fermer', {
          duration: 3000
        });
        this.loading.set(false);
      }
    });
  }

  updateParcelle(id: number, data: ParcelleUpdateDto): void {
    this.parcelleService.updateParcelle(id, data).subscribe({
      next: (parcelle: Parcelle) => {
        this.snackBar.open('Parcelle mise à jour avec succès', 'Fermer', {
          duration: 3000
        });
        this.loading.set(false);
        this.router.navigate(['/parcelles', parcelle.id]);
      },
      error: (error: any) => {
        console.error('Error updating parcelle:', error);
        this.snackBar.open('Erreur lors de la mise à jour de la parcelle', 'Fermer', {
          duration: 3000
        });
        this.loading.set(false);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/parcelles']);
  }

  getErrorMessage(fieldName: string): string {
    const control = this.parcelleForm.get(fieldName);
    if (!control || !control.errors || !control.touched) return '';

    if (control.errors['required']) return 'Ce champ est requis';
    if (control.errors['minlength']) return `Minimum ${control.errors['minlength'].requiredLength} caractères`;
    if (control.errors['maxlength']) return `Maximum ${control.errors['maxlength'].requiredLength} caractères`;
    if (control.errors['min']) return `La valeur minimum est ${control.errors['min'].min}`;
    if (control.errors['max']) return `La valeur maximum est ${control.errors['max'].max}`;

    return '';
  }
}
