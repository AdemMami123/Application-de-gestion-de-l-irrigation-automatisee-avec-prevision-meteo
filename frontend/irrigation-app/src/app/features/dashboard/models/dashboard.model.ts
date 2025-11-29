export interface DashboardStats {
  totalParcelles: number;
  totalProgrammes: number;
  totalJournaux: number;
  parcellesActives: number;
  programmesEnCours: number;
  programmesPlanifies: number;
  programmesTermines: number;
  volumeEauTotal: number;
  dernierArrosage?: Date;
}

export interface RecentActivity {
  id: number;
  type: 'parcelle' | 'programme' | 'journal';
  title: string;
  description: string;
  date: Date;
  icon: string;
  color: string;
}

export interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    backgroundColor?: string | string[];
    borderColor?: string;
    tension?: number;
  }[];
}
