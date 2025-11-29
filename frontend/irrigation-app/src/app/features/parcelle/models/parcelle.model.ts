export interface Parcelle {
  id?: number;
  nom: string;
  superficie: number;
  culture: string;
}

export interface ParcelleCreateDto {
  nom: string;
  superficie: number;
  culture: string;
}

export interface ParcelleUpdateDto {
  nom: string;
  superficie: number;
  culture: string;
}
