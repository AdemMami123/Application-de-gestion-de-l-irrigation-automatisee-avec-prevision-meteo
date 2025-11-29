import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { JournalArrosage, JournalCreateDto, JournalUpdateDto } from '../models/journal.model';

@Injectable({
  providedIn: 'root'
})
export class JournalService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/arrosage/journaux`;

  getAllJournaux(): Observable<JournalArrosage[]> {
    return this.http.get<JournalArrosage[]>(this.apiUrl);
  }

  getJournalById(id: number): Observable<JournalArrosage> {
    return this.http.get<JournalArrosage>(`${this.apiUrl}/${id}`);
  }

  getJournauxByProgramme(programmeId: number): Observable<JournalArrosage[]> {
    return this.http.get<JournalArrosage[]>(`${this.apiUrl}/programme/${programmeId}`);
  }

  getJournauxByParcelle(parcelleId: number): Observable<JournalArrosage[]> {
    return this.http.get<JournalArrosage[]>(`${this.apiUrl}/parcelle/${parcelleId}`);
  }

  getJournauxByPeriode(startDate: string, endDate: string): Observable<JournalArrosage[]> {
    return this.http.get<JournalArrosage[]>(`${this.apiUrl}/periode`, {
      params: {
        startDate,
        endDate
      }
    });
  }

  createJournal(journal: JournalCreateDto): Observable<JournalArrosage> {
    return this.http.post<JournalArrosage>(this.apiUrl, journal);
  }

  updateJournal(id: number, journal: JournalUpdateDto): Observable<JournalArrosage> {
    return this.http.put<JournalArrosage>(`${this.apiUrl}/${id}`, journal);
  }

  deleteJournal(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
