import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Parcelle, ParcelleCreateDto, ParcelleUpdateDto } from '../models/parcelle.model';

@Injectable({
  providedIn: 'root'
})
export class ParcelleService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/arrosage/parcelles`;

  getAllParcelles(): Observable<Parcelle[]> {
    return this.http.get<Parcelle[]>(this.apiUrl);
  }

  getParcelleById(id: number): Observable<Parcelle> {
    return this.http.get<Parcelle>(`${this.apiUrl}/${id}`);
  }

  createParcelle(parcelle: ParcelleCreateDto): Observable<Parcelle> {
    return this.http.post<Parcelle>(this.apiUrl, parcelle);
  }

  updateParcelle(id: number, parcelle: ParcelleUpdateDto): Observable<Parcelle> {
    return this.http.put<Parcelle>(`${this.apiUrl}/${id}`, parcelle);
  }

  deleteParcelle(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getParcellesByCulture(culture: string): Observable<Parcelle[]> {
    return this.http.get<Parcelle[]>(`${this.apiUrl}/culture/${culture}`);
  }
}
