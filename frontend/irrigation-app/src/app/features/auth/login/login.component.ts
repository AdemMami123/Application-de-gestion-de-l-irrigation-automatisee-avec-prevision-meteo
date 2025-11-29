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
      <mat-card class="login-card">
        <mat-card-header>
          <div class="login-header">
            <mat-icon class="login-icon">water_drop</mat-icon>
            <h1>Gestion de l'Irrigation</h1>
            <p>Connexion au système</p>
          </div>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="loginForm" (ngSubmit)="onLogin()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Nom d'utilisateur</mat-label>
              <input matInput formControlName="username" placeholder="admin">
              <mat-icon matSuffix>person</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Mot de passe</mat-label>
              <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password">
              <button mat-icon-button matSuffix type="button" (click)="togglePassword()">
                <mat-icon>{{ hidePassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
            </mat-form-field>

            <button 
              mat-raised-button 
              color="primary" 
              type="submit" 
              class="full-width"
              [disabled]="loginForm.invalid || loading()">
              @if (loading()) {
                <mat-spinner diameter="20"></mat-spinner>
              }
              Se connecter
            </button>
          </form>
        </mat-card-content>

        <mat-card-footer>
          <p class="footer-text">Version 1.0.0 - Système d'irrigation automatisé</p>
        </mat-card-footer>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
    }

    .login-card {
      width: 100%;
      max-width: 400px;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
    }

    .login-header {
      text-align: center;
      padding: 20px 0;

      .login-icon {
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
      gap: 16px;
      padding: 20px 0;
    }

    .full-width {
      width: 100%;
    }

    .footer-text {
      text-align: center;
      font-size: 12px;
      color: #999;
      padding: 16px;
      margin: 0;
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

    // TODO: Implement real authentication
    // For now, simulate login
    setTimeout(() => {
      localStorage.setItem('irrigation_auth_token', 'demo-token');
      localStorage.setItem('irrigation_user', JSON.stringify({
        username: credentials.username,
        roles: ['ADMIN']
      }));
      
      this.loading.set(false);
      this.router.navigate(['/dashboard']);
    }, 1000);

    /* Real implementation:
    this.authService.login(credentials).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        this.snackBar.open('Identifiants invalides', 'Fermer', { duration: 3000 });
      }
    });
    */
  }
}
