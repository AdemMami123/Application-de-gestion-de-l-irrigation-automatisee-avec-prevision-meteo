import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
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
  selector: 'app-login',
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
    <div class="login-container">
      <div class="content-wrapper">
        <div class="brand-section">
          <div class="brand-logo">
            <mat-icon class="water-icon">water_drop</mat-icon>
          </div>
          <h1 class="brand-title">AquaGrow</h1>
          <p class="brand-subtitle">Système de Gestion de l'Irrigation Intelligent</p>
        </div>

        <mat-card class="login-card">
          <mat-card-header>
            <div class="form-header">
              <h2>Bienvenue</h2>
              <p>Connectez-vous pour accéder à votre compte</p>
            </div>
          </mat-card-header>

          <mat-card-content>
            <form [formGroup]="loginForm" (ngSubmit)="onLogin()">
              <div class="input-wrapper">
                <label class="input-label">Nom d'utilisateur</label>
                <div class="input-field">
                  <mat-icon class="field-icon">person</mat-icon>
                  <input matInput 
                    formControlName="username" 
                    placeholder="Entrez votre nom d'utilisateur"
                    class="form-input">
                </div>
              </div>

              <div class="input-wrapper">
                <label class="input-label">Mot de passe</label>
                <div class="input-field">
                  <mat-icon class="field-icon">lock</mat-icon>
                  <input matInput 
                    [type]="hidePassword() ? 'password' : 'text'" 
                    formControlName="password"
                    placeholder="Entrez votre mot de passe"
                    class="form-input">
                  <button mat-icon-button matSuffix type="button" (click)="togglePassword()" class="visibility-toggle">
                    <mat-icon>{{ hidePassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
                  </button>
                </div>
              </div>

              <button 
                mat-raised-button 
                type="submit" 
                class="login-button"
                [disabled]="loginForm.invalid || loading()">
                @if (loading()) {
                  <mat-spinner diameter="20"></mat-spinner>
                } @else {
                  <span>Se connecter</span>
                }
              </button>
            </form>
          </mat-card-content>

          <div class="divider">
            <span>Vous n'avez pas de compte ?</span>
          </div>

          <mat-card-actions class="card-actions">
            <a routerLink="/register" class="register-link">
              <span>Créer un compte</span>
              <mat-icon>arrow_forward</mat-icon>
            </a>
          </mat-card-actions>
        </mat-card>

        <p class="footer-text">Version 1.0.0 © Système d'irrigation automatisé</p>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #1a472a 0%, #2d6b4f 50%, #0f3d26 100%);
      padding: 20px;
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    }

    .content-wrapper {
      width: 100%;
      max-width: 420px;
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .brand-section {
      text-align: center;
      margin-bottom: 40px;
      color: white;
      animation: fadeInDown 0.6s ease-out;
    }

    .brand-logo {
      margin-bottom: 16px;
    }

    .water-icon {
      font-size: 56px;
      width: 56px;
      height: 56px;
      color: #4ade80;
      filter: drop-shadow(0 4px 12px rgba(74, 222, 128, 0.3));
    }

    .brand-title {
      font-size: 32px;
      font-weight: 700;
      margin: 0;
      color: white;
      letter-spacing: -0.5px;
    }

    .brand-subtitle {
      font-size: 13px;
      margin: 8px 0 0;
      color: rgba(255, 255, 255, 0.8);
      font-weight: 300;
      letter-spacing: 0.3px;
    }

    .login-card {
      width: 100%;
      border-radius: 12px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
      background: white;
      overflow: hidden;
      animation: fadeInUp 0.6s ease-out 0.1s both;
    }

    mat-card-header {
      background: linear-gradient(135deg, #f8fafb 0%, #f0f4f8 100%);
      padding: 32px 24px;
      border-bottom: 1px solid #e5e7eb;
    }

    .form-header {
      text-align: center;

      h2 {
        margin: 0 0 8px;
        font-size: 24px;
        font-weight: 600;
        color: #1a472a;
      }

      p {
        margin: 0;
        font-size: 13px;
        color: #6b7280;
      }
    }

    mat-card-content {
      padding: 32px 24px;
    }

    form {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .input-wrapper {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .input-label {
      font-size: 13px;
      font-weight: 600;
      color: #374151;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .input-field {
      position: relative;
      display: flex;
      align-items: center;
      background: #f9fafb;
      border: 2px solid #e5e7eb;
      border-radius: 8px;
      padding: 0 12px;
      transition: all 0.3s ease;

      &:focus-within {
        background: white;
        border-color: #4ade80;
        box-shadow: 0 0 0 3px rgba(74, 222, 128, 0.1);
      }

      &:hover {
        border-color: #d1d5db;
      }
    }

    .field-icon {
      color: #9ca3af;
      font-size: 20px;
      width: 20px;
      height: 20px;
      margin-right: 8px;
      flex-shrink: 0;
    }

    .form-input {
      flex: 1;
      border: none;
      background: transparent;
      outline: none;
      font-size: 14px;
      color: #1f2937;
      padding: 12px 0;

      &::placeholder {
        color: #9ca3af;
      }
    }

    .visibility-toggle {
      margin: 0 -8px 0 4px;
      color: #9ca3af;
      padding: 4px;
      height: 32px;
      width: 32px;

      &:hover {
        color: #4ade80;
      }
    }

    .login-button {
      background: linear-gradient(135deg, #2d6b4f 0%, #1a472a 100%);
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 15px;
      font-weight: 600;
      padding: 12px 24px;
      height: 48px;
      margin-top: 8px;
      transition: all 0.3s ease;
      cursor: pointer;
      text-transform: uppercase;
      letter-spacing: 0.5px;

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(45, 107, 79, 0.3);
      }

      &:active:not(:disabled) {
        transform: translateY(0);
      }

      &:disabled {
        opacity: 0.7;
        cursor: not-allowed;
      }

      mat-spinner {
        margin-right: 8px;
      }
    }

    .divider {
      padding: 0 24px;
      text-align: center;
      font-size: 12px;
      color: #9ca3af;
      margin: 24px 0 0;
      position: relative;

      &::before {
        content: '';
        position: absolute;
        left: 24px;
        right: 24px;
        top: 50%;
        height: 1px;
        background: #e5e7eb;
        transform: translateY(-50%);
      }

      span {
        background: white;
        position: relative;
        padding: 0 12px;
      }
    }

    .card-actions {
      padding: 24px;
      text-align: center;
      background: #f9fafb;
      border-top: 1px solid #e5e7eb;
    }

    .register-link {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      color: #2d6b4f;
      text-decoration: none;
      font-weight: 600;
      font-size: 14px;
      padding: 8px 12px;
      border-radius: 6px;
      transition: all 0.3s ease;

      &:hover {
        background: rgba(45, 107, 79, 0.1);
        color: #1a472a;

        mat-icon {
          transform: translateX(4px);
        }
      }

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
        transition: transform 0.3s ease;
      }
    }

    .footer-text {
      text-align: center;
      font-size: 11px;
      color: rgba(255, 255, 255, 0.6);
      margin-top: 24px;
      animation: fadeIn 1s ease-out 0.3s both;
    }

    @keyframes fadeInDown {
      from {
        opacity: 0;
        transform: translateY(-20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes fadeIn {
      from {
        opacity: 0;
      }
      to {
        opacity: 1;
      }
    }

    @media (max-width: 480px) {
      .content-wrapper {
        padding: 0 16px;
      }

      mat-card-header {
        padding: 24px 16px;
      }

      mat-card-content {
        padding: 24px 16px;
      }

      .brand-title {
        font-size: 28px;
      }

      .water-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
      }
    }
  `]
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = signal(false);
  hidePassword = signal(true);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      username: ['admin', Validators.required],
      password: ['admin', Validators.required]
    });
  }

  togglePassword(): void {
    this.hidePassword.set(!this.hidePassword());
  }

  onLogin(): void {
    if (this.loginForm.invalid) return;

    this.loading.set(true);
    const credentials = this.loginForm.value;

    this.authService.login(credentials).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.snackBar.open(`Welcome, ${response.user.username}!`, 'Close', { duration: 3000 });
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        const message = err.message || 'Invalid credentials';
        this.snackBar.open(message, 'Close', { duration: 5000 });
      }
    });
  }
}
