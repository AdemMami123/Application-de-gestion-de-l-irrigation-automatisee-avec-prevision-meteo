export interface JournalArrosage {
  id?: number;
  programmeId: number;
  parcelleNom?: string;
  dateExecution: string; // ISO datetime string
  volumeReel: number; // m³
  remarque?: string;
}

export interface JournalCreateDto {
  programmeId: number;
  dateExecution: string;
  volumeReel: number;
  remarque?: string;
}

export interface JournalUpdateDto {
  programmeId: number;
  dateExecution: string;
  volumeReel: number;
  remarque?: string;
}
