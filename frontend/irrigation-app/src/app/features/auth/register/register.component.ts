import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="register-container">
      <mat-card class="register-card">
        <mat-card-header>
          <div class="register-header">
            <mat-icon class="register-icon">water_drop</mat-icon>
            <h1>Create Account</h1>
            <p>Join the Irrigation Management System</p>
          </div>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="registerForm" (ngSubmit)="onRegister()">
            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>First Name</mat-label>
                <input matInput formControlName="firstName" placeholder="John">
                <mat-icon matSuffix>person</mat-icon>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Last Name</mat-label>
                <input matInput formControlName="lastName" placeholder="Doe">
                <mat-icon matSuffix>person</mat-icon>
              </mat-form-field>
            </div>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Username</mat-label>
              <input matInput formControlName="username" placeholder="johndoe">
              <mat-icon matSuffix>account_circle</mat-icon>
              @if (registerForm.get('username')?.hasError('required') && registerForm.get('username')?.touched) {
                <mat-error>Username is required</mat-error>
              }
              @if (registerForm.get('username')?.hasError('minlength')) {
                <mat-error>Username must be at least 3 characters</mat-error>
              }
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput type="email" formControlName="email" placeholder="john.doe@example.com">
              <mat-icon matSuffix>email</mat-icon>
              @if (registerForm.get('email')?.hasError('required') && registerForm.get('email')?.touched) {
                <mat-error>Email is required</mat-error>
              }
              @if (registerForm.get('email')?.hasError('email')) {
                <mat-error>Please enter a valid email</mat-error>
              }
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password">
              <button mat-icon-button matSuffix type="button" (click)="togglePassword()">
                <mat-icon>{{ hidePassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              @if (registerForm.get('password')?.hasError('required') && registerForm.get('password')?.touched) {
                <mat-error>Password is required</mat-error>
              }
              @if (registerForm.get('password')?.hasError('minlength')) {
                <mat-error>Password must be at least 6 characters</mat-error>
              }
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Confirm Password</mat-label>
              <input matInput [type]="hideConfirmPassword() ? 'password' : 'text'" formControlName="confirmPassword">
              <button mat-icon-button matSuffix type="button" (click)="toggleConfirmPassword()">
                <mat-icon>{{ hideConfirmPassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              @if (registerForm.hasError('passwordMismatch')) {
                <mat-error>Passwords do not match</mat-error>
              }
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Farm ID (Optional)</mat-label>
              <input matInput formControlName="farmId" placeholder="FARM001">
              <mat-icon matSuffix>agriculture</mat-icon>
            </mat-form-field>

            <button 
              mat-raised-button 
              color="primary" 
              type="submit" 
              class="full-width"
              [disabled]="registerForm.invalid || loading()">
              @if (loading()) {
                <mat-spinner diameter="20"></mat-spinner>
              }
              Create Account
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions class="actions">
          <p>Already have an account? <a routerLink="/login">Sign in</a></p>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .register-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
    }

    .register-card {
      width: 100%;
      max-width: 500px;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
    }

    .register-header {
      text-align: center;
      padding: 20px 0;

      .register-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: #673ab7;
        margin-bottom: 16px;
      }

      h1 {
        margin: 0 0 8px;
        color: #333;
        font-size: 24px;
      }

      p {
        margin: 0;
        color: #666;
        font-size: 14px;
      }
    }

    form {
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding: 20px 0;
    }

    .form-row {
      display: flex;
      gap: 16px;

      mat-form-field {
        flex: 1;
      }
    }

    .full-width {
      width: 100%;
    }

    .actions {
      text-align: center;
      padding: 16px;

      a {
        color: #673ab7;
        text-decoration: none;
        font-weight: 500;

        &:hover {
          text-decoration: underline;
        }
      }
    }
  `]
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = signal(false);
  hidePassword = signal(true);
  hideConfirmPassword = signal(true);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.registerForm = this.fb.group({
      firstName: [''],
      lastName: [''],
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      farmId: ['']
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    return null;
  }

  togglePassword(): void {
    this.hidePassword.set(!this.hidePassword());
  }

  toggleConfirmPassword(): void {
    this.hideConfirmPassword.set(!this.hideConfirmPassword());
  }

  onRegister(): void {
    if (this.registerForm.invalid) return;

    this.loading.set(true);
    const formValue = this.registerForm.value;
    
    const registerData = {
      username: formValue.username,
      email: formValue.email,
      password: formValue.password,
      firstName: formValue.firstName || undefined,
      lastName: formValue.lastName || undefined,
      farmId: formValue.farmId || undefined
    };

    this.authService.register(registerData).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.snackBar.open('Account created successfully!', 'Close', { duration: 3000 });
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        const message = err.message || 'Registration failed';
        this.snackBar.open(message, 'Close', { duration: 5000 });
      }
    });
  }
}
