import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ProgrammeArrosage, ProgrammeCreateDto, ProgrammeUpdateDto, StatutProgramme } from '../models/programme.model';

@Injectable({
  providedIn: 'root'
})
export class ProgrammeService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/arrosage/programmes`;

  getAllProgrammes(): Observable<ProgrammeArrosage[]> {
    return this.http.get<ProgrammeArrosage[]>(this.apiUrl);
  }

  getProgrammeById(id: number): Observable<ProgrammeArrosage> {
    return this.http.get<ProgrammeArrosage>(`${this.apiUrl}/${id}`);
  }

  getProgrammesByParcelle(parcelleId: number): Observable<ProgrammeArrosage[]> {
    return this.http.get<ProgrammeArrosage[]>(`${this.apiUrl}/parcelle/${parcelleId}`);
  }

  getProgrammesByStatut(statut: StatutProgramme): Observable<ProgrammeArrosage[]> {
    return this.http.get<ProgrammeArrosage[]>(`${this.apiUrl}/statut/${statut}`);
  }

  createProgramme(programme: ProgrammeCreateDto): Observable<ProgrammeArrosage> {
    return this.http.post<ProgrammeArrosage>(this.apiUrl, programme);
  }

  updateProgramme(id: number, programme: ProgrammeUpdateDto): Observable<ProgrammeArrosage> {
    return this.http.put<ProgrammeArrosage>(`${this.apiUrl}/${id}`, programme);
  }

  deleteProgramme(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  scheduleProgrammeWithWeather(
    parcelleId: number,
    stationId: number,
    datePlanifiee: string
  ): Observable<ProgrammeArrosage> {
    return this.http.post<ProgrammeArrosage>(`${this.apiUrl}/schedule`, null, {
      params: {
        parcelleId: parcelleId.toString(),
        stationId: stationId.toString(),
        datePlanifiee
      }
    });
  }
}
