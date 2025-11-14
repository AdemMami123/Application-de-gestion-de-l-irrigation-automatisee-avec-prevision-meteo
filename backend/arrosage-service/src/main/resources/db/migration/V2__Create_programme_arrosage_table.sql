-- Create programme_arrosage table
CREATE TABLE programme_arrosage (
    id BIGSERIAL PRIMARY KEY,
    parcelle_id BIGINT NOT NULL,
    date_planifiee TIMESTAMP NOT NULL,
    duree INTEGER NOT NULL CHECK (duree > 0),
    volume_prevu NUMERIC(10, 2) NOT NULL CHECK (volume_prevu > 0),
    statut VARCHAR(20) NOT NULL CHECK (statut IN ('PLANIFIE', 'EN_COURS', 'TERMINE', 'ANNULE')),
    CONSTRAINT fk_programme_parcelle FOREIGN KEY (parcelle_id) 
        REFERENCES parcelle(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_programme_parcelle ON programme_arrosage(parcelle_id);
CREATE INDEX idx_programme_date ON programme_arrosage(date_planifiee);
CREATE INDEX idx_programme_statut ON programme_arrosage(statut);

-- Add comments
COMMENT ON TABLE programme_arrosage IS 'Table des programmes d''arrosage planifiés';
COMMENT ON COLUMN programme_arrosage.parcelle_id IS 'Référence vers la parcelle';
COMMENT ON COLUMN programme_arrosage.date_planifiee IS 'Date et heure planifiées pour l''arrosage';
COMMENT ON COLUMN programme_arrosage.duree IS 'Durée prévue de l''arrosage en minutes';
COMMENT ON COLUMN programme_arrosage.volume_prevu IS 'Volume d''eau prévu en m³';
COMMENT ON COLUMN programme_arrosage.statut IS 'Statut du programme (PLANIFIE, EN_COURS, TERMINE, ANNULE)';
