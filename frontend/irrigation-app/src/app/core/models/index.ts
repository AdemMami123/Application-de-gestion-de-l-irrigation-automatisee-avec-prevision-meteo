// Parcelle model
export interface Parcelle {
  id?: number;
  nom: string;
  superficie: number;
  localisation: string;
  typeSol: string;
  typeCulture: string;
  capteurId?: number;
  actif: boolean;
  dateCreation?: Date;
  dateModification?: Date;
}

// Programme d'irrigation model
export interface ProgrammeIrrigation {
  id?: number;
  nom: string;
  parcelleId: number;
  parcelleName?: string;
  dateDebut: Date;
  dateFin: Date;
  heureDebut: string;
  duree: number; // en minutes
  quantiteEau: number; // en litres
  frequence: 'DAILY' | 'WEEKLY' | 'CUSTOM';
  joursActifs?: number[]; // 0=Dimanche, 1=Lundi, etc.
  actif: boolean;
  baseSurMeteo: boolean;
  seuilHumidite?: number;
  seuilPluie?: number;
  dateCreation?: Date;
  dateModification?: Date;
}

// Journal d'exécution model
export interface JournalExecution {
  id?: number;
  programmeId: number;
  programmeName?: string;
  parcelleId: number;
  parcelleName?: string;
  dateExecution: Date;
  heureDebut: string;
  heureFin?: string;
  dureeEffective?: number; // en minutes
  quantiteEauUtilisee?: number; // en litres
  statut: 'PLANIFIE' | 'EN_COURS' | 'TERMINE' | 'ANNULE' | 'ERREUR';
  temperature?: number;
  humidite?: number;
  pluie?: number;
  meteoCondition?: string;
  notes?: string;
  dateCreation?: Date;
}

// Capteur model
export interface Capteur {
  id?: number;
  nom: string;
  type: 'HUMIDITE' | 'TEMPERATURE' | 'PLUVIOMETRE';
  parcelleId?: number;
  valeurActuelle?: number;
  unite: string;
  actif: boolean;
  derniereMesure?: Date;
  dateCreation?: Date;
}

// Alerte model
export interface Alerte {
  id?: number;
  type: 'INFO' | 'WARNING' | 'ERROR';
  message: string;
  parcelleId?: number;
  programmeId?: number;
  capteurId?: number;
  dateCreation: Date;
  lu: boolean;
}

// API Response wrapper
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
  timestamp?: Date;
}

// Pagination
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
