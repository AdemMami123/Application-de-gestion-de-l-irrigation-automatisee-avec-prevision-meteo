-- ============================================
-- SUPABASE MIGRATION SCRIPT
-- Irrigation Management System
-- ============================================
-- This script creates all tables for both:
-- 1. Meteo Service (station_meteo, prevision)
-- 2. Arrosage Service (parcelle, programme_arrosage, journal_arrosage)
-- ============================================

-- ============================================
-- METEO SERVICE TABLES
-- ============================================

-- Create station_meteo table
CREATE TABLE IF NOT EXISTS station_meteo (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    latitude NUMERIC(10, 8) NOT NULL,
    longitude NUMERIC(11, 8) NOT NULL,
    fournisseur VARCHAR(100) NOT NULL,
    CONSTRAINT uk_station_nom UNIQUE (nom)
);

-- Create indexes for station_meteo
CREATE INDEX IF NOT EXISTS idx_station_fournisseur ON station_meteo(fournisseur);
CREATE INDEX IF NOT EXISTS idx_station_coordinates ON station_meteo(latitude, longitude);

-- Add comments
COMMENT ON TABLE station_meteo IS 'Table des stations météorologiques';
COMMENT ON COLUMN station_meteo.nom IS 'Nom de la station météo';
COMMENT ON COLUMN station_meteo.latitude IS 'Latitude de la station';
COMMENT ON COLUMN station_meteo.longitude IS 'Longitude de la station';
COMMENT ON COLUMN station_meteo.fournisseur IS 'Fournisseur des données météo';

-- Create prevision table
CREATE TABLE IF NOT EXISTS prevision (
    id BIGSERIAL PRIMARY KEY,
    station_id BIGINT NOT NULL,
    date DATE NOT NULL,
    temperature_max NUMERIC(5, 2),
    temperature_min NUMERIC(5, 2),
    pluie_prevue NUMERIC(6, 2),
    vent NUMERIC(5, 2),
    CONSTRAINT fk_prevision_station FOREIGN KEY (station_id) 
        REFERENCES station_meteo(id) ON DELETE CASCADE,
    CONSTRAINT uk_station_date UNIQUE (station_id, date)
);

-- Create indexes for prevision
CREATE INDEX IF NOT EXISTS idx_prevision_station ON prevision(station_id);
CREATE INDEX IF NOT EXISTS idx_prevision_date ON prevision(date);
CREATE INDEX IF NOT EXISTS idx_prevision_station_date ON prevision(station_id, date);

-- Add comments
COMMENT ON TABLE prevision IS 'Table des prévisions météorologiques';
COMMENT ON COLUMN prevision.station_id IS 'Référence vers la station météo';
COMMENT ON COLUMN prevision.date IS 'Date de la prévision';
COMMENT ON COLUMN prevision.temperature_max IS 'Température maximale prévue (°C)';
COMMENT ON COLUMN prevision.temperature_min IS 'Température minimale prévue (°C)';
COMMENT ON COLUMN prevision.pluie_prevue IS 'Quantité de pluie prévue (mm)';
COMMENT ON COLUMN prevision.vent IS 'Vitesse du vent prévue (km/h)';

-- ============================================
-- ARROSAGE SERVICE TABLES
-- ============================================

-- Create parcelle table
CREATE TABLE IF NOT EXISTS parcelle (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    superficie NUMERIC(10, 2) NOT NULL CHECK (superficie > 0),
    culture VARCHAR(100) NOT NULL,
    CONSTRAINT uk_parcelle_nom UNIQUE (nom)
);

-- Create indexes for parcelle
CREATE INDEX IF NOT EXISTS idx_parcelle_culture ON parcelle(culture);

-- Add comments
COMMENT ON TABLE parcelle IS 'Table des parcelles agricoles';
COMMENT ON COLUMN parcelle.nom IS 'Nom de la parcelle';
COMMENT ON COLUMN parcelle.superficie IS 'Superficie de la parcelle en m²';
COMMENT ON COLUMN parcelle.culture IS 'Type de culture sur la parcelle';

-- Create programme_arrosage table
CREATE TABLE IF NOT EXISTS programme_arrosage (
    id BIGSERIAL PRIMARY KEY,
    parcelle_id BIGINT NOT NULL,
    date_planifiee TIMESTAMP NOT NULL,
    duree INTEGER NOT NULL CHECK (duree > 0),
    volume_prevu NUMERIC(10, 2) NOT NULL CHECK (volume_prevu > 0),
    statut VARCHAR(20) NOT NULL CHECK (statut IN ('PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE')),
    CONSTRAINT fk_programme_parcelle FOREIGN KEY (parcelle_id) 
        REFERENCES parcelle(id) ON DELETE CASCADE
);

-- Create indexes for programme_arrosage
CREATE INDEX IF NOT EXISTS idx_programme_parcelle ON programme_arrosage(parcelle_id);
CREATE INDEX IF NOT EXISTS idx_programme_date ON programme_arrosage(date_planifiee);
CREATE INDEX IF NOT EXISTS idx_programme_statut ON programme_arrosage(statut);

-- Add comments
COMMENT ON TABLE programme_arrosage IS 'Table des programmes d''arrosage planifiés';
COMMENT ON COLUMN programme_arrosage.parcelle_id IS 'Référence vers la parcelle';
COMMENT ON COLUMN programme_arrosage.date_planifiee IS 'Date et heure planifiées pour l''arrosage';
COMMENT ON COLUMN programme_arrosage.duree IS 'Durée prévue de l''arrosage en minutes';
COMMENT ON COLUMN programme_arrosage.volume_prevu IS 'Volume d''eau prévu en m³';
COMMENT ON COLUMN programme_arrosage.statut IS 'Statut du programme (PLANIFIE, EN_COURS, TERMINE, ANNULE)';

-- Create journal_arrosage table
CREATE TABLE IF NOT EXISTS journal_arrosage (
    id BIGSERIAL PRIMARY KEY,
    programme_id BIGINT NOT NULL,
    date_execution TIMESTAMP NOT NULL,
    volume_reel NUMERIC(10, 2) NOT NULL,
    remarque VARCHAR(500),
    CONSTRAINT fk_journal_programme FOREIGN KEY (programme_id) 
        REFERENCES programme_arrosage(id) ON DELETE CASCADE
);

-- Create indexes for journal_arrosage
CREATE INDEX IF NOT EXISTS idx_journal_programme ON journal_arrosage(programme_id);
CREATE INDEX IF NOT EXISTS idx_journal_date ON journal_arrosage(date_execution);

-- Add comments
COMMENT ON TABLE journal_arrosage IS 'Table du journal d''exécution des arrosages';
COMMENT ON COLUMN journal_arrosage.programme_id IS 'Référence vers le programme d''arrosage';
COMMENT ON COLUMN journal_arrosage.date_execution IS 'Date et heure d''exécution réelle';
COMMENT ON COLUMN journal_arrosage.volume_reel IS 'Volume d''eau réellement utilisé en m³';
COMMENT ON COLUMN journal_arrosage.remarque IS 'Remarques ou observations sur l''exécution';

-- ============================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================

-- Insert sample stations meteo
INSERT INTO station_meteo (nom, latitude, longitude, fournisseur) VALUES
    ('Station Paris Centre', 48.8566, 2.3522, 'MeteoFrance'),
    ('Station Lyon Part-Dieu', 45.7640, 4.8357, 'MeteoFrance'),
    ('Station Marseille Vieux-Port', 43.2965, 5.3698, 'OpenWeatherMap'),
    ('Station Toulouse Capitole', 43.6047, 1.4442, 'MeteoFrance'),
    ('Station Nice Promenade', 43.7102, 7.2620, 'OpenWeatherMap')
ON CONFLICT (nom) DO NOTHING;

-- Insert sample previsions
INSERT INTO prevision (station_id, date, temperature_max, temperature_min, pluie_prevue, vent) VALUES
    (1, CURRENT_DATE, 22.5, 15.3, 0.0, 12.5),
    (1, CURRENT_DATE + 1, 24.0, 16.0, 2.5, 15.0),
    (1, CURRENT_DATE + 2, 20.5, 14.0, 8.5, 20.0),
    (2, CURRENT_DATE, 25.0, 17.0, 0.0, 10.0),
    (2, CURRENT_DATE + 1, 26.5, 18.5, 0.0, 8.5),
    (3, CURRENT_DATE, 28.0, 20.0, 0.0, 15.5),
    (3, CURRENT_DATE + 1, 29.5, 21.0, 0.0, 18.0),
    (4, CURRENT_DATE, 26.0, 18.0, 1.5, 12.0),
    (5, CURRENT_DATE, 27.0, 19.5, 0.0, 14.0)
ON CONFLICT (station_id, date) DO NOTHING;

-- Insert sample parcelles
INSERT INTO parcelle (nom, superficie, culture) VALUES
    ('Parcelle Nord', 5000.00, 'Tomates'),
    ('Parcelle Sud', 7500.00, 'Maïs'),
    ('Parcelle Est', 3000.00, 'Salades'),
    ('Parcelle Ouest', 6000.00, 'Pommes de terre'),
    ('Parcelle Centre', 4500.00, 'Carottes')
ON CONFLICT (nom) DO NOTHING;

-- Insert sample programmes (only if parcelles exist)
INSERT INTO programme_arrosage (parcelle_id, date_planifiee, duree, volume_prevu, statut)
SELECT 
    p.id,
    CURRENT_TIMESTAMP + (row_number() OVER ()) * INTERVAL '1 day',
    60,
    25.00,
    'PLANIFIE'
FROM parcelle p
WHERE p.nom IN ('Parcelle Nord', 'Parcelle Sud', 'Parcelle Est', 'Parcelle Ouest', 'Parcelle Centre')
ON CONFLICT DO NOTHING;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Run these to verify the migration:
-- SELECT * FROM station_meteo;
-- SELECT * FROM prevision;
-- SELECT * FROM parcelle;
-- SELECT * FROM programme_arrosage;
-- SELECT * FROM journal_arrosage;
