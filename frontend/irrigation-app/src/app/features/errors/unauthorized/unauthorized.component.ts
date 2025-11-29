import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [RouterModule, MatButtonModule, MatIconModule],
  template: `
    <div style="text-align: center; padding: 60px 20px;">
      <mat-icon style="font-size: 100px; width: 100px; height: 100px; color: #f44336;">block</mat-icon>
      <h1>Accès refusé</h1>
      <p>Vous n'avez pas les permissions nécessaires pour accéder à cette page.</p>
      <button mat-raised-button color="primary" routerLink="/dashboard">Retour au tableau de bord</button>
    </div>
  `
})
export class UnauthorizedComponent {}
