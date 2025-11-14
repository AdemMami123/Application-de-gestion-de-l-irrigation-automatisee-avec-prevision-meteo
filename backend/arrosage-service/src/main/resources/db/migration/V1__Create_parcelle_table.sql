-- Create parcelle table
CREATE TABLE parcelle (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    superficie NUMERIC(10, 2) NOT NULL CHECK (superficie > 0),
    culture VARCHAR(100) NOT NULL,
    CONSTRAINT uk_parcelle_nom UNIQUE (nom)
);

-- Create indexes
CREATE INDEX idx_parcelle_culture ON parcelle(culture);

-- Add comments
COMMENT ON TABLE parcelle IS 'Table des parcelles agricoles';
COMMENT ON COLUMN parcelle.nom IS 'Nom de la parcelle';
COMMENT ON COLUMN parcelle.superficie IS 'Superficie de la parcelle en m²';
COMMENT ON COLUMN parcelle.culture IS 'Type de culture sur la parcelle';
