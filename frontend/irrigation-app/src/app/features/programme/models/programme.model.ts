export interface ProgrammeArrosage {
  id?: number;
  parcelleId: number;
  parcelleNom?: string;
  datePlanifiee: string; // ISO datetime string
  duree: number; // minutes
  volumePrevu: number; // m³
  statut: StatutProgramme;
}

export interface ProgrammeCreateDto {
  parcelleId: number;
  datePlanifiee: string;
  duree: number;
  volumePrevu: number;
  statut: StatutProgramme;
}

export interface ProgrammeUpdateDto {
  parcelleId: number;
  datePlanifiee: string;
  duree: number;
  volumePrevu: number;
  statut: StatutProgramme;
}

export enum StatutProgramme {
  PLANIFIE = 'PLANIFIE',
  EN_COURS = 'EN_COURS',
  TERMINE = 'TERMINE',
  ANNULE = 'ANNULE'
}

export const STATUT_LABELS: Record<StatutProgramme, string> = {
  [StatutProgramme.PLANIFIE]: 'Planifié',
  [StatutProgramme.EN_COURS]: 'En cours',
  [StatutProgramme.TERMINE]: 'Terminé',
  [StatutProgramme.ANNULE]: 'Annulé'
};
