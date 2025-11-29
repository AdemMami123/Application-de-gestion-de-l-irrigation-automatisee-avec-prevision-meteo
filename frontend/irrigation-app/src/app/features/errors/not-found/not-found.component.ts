import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterModule, MatButtonModule, MatIconModule],
  template: `
    <div style="text-align: center; padding: 60px 20px;">
      <mat-icon style="font-size: 100px; width: 100px; height: 100px; color: #999;">error_outline</mat-icon>
      <h1>404 - Page non trouvée</h1>
      <p>La page que vous recherchez n'existe pas.</p>
      <button mat-raised-button color="primary" routerLink="/dashboard">Retour au tableau de bord</button>
    </div>
  `
})
export class NotFoundComponent {}
