-- Create station_meteo table
CREATE TABLE station_meteo (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    latitude NUMERIC(10, 8) NOT NULL,
    longitude NUMERIC(11, 8) NOT NULL,
    fournisseur VARCHAR(100) NOT NULL,
    CONSTRAINT uk_station_nom UNIQUE (nom)
);

-- Create indexes
CREATE INDEX idx_station_fournisseur ON station_meteo(fournisseur);
CREATE INDEX idx_station_coordinates ON station_meteo(latitude, longitude);

-- Add comments
COMMENT ON TABLE station_meteo IS 'Table des stations météorologiques';
COMMENT ON COLUMN station_meteo.nom IS 'Nom de la station météo';
COMMENT ON COLUMN station_meteo.latitude IS 'Latitude de la station';
COMMENT ON COLUMN station_meteo.longitude IS 'Longitude de la station';
COMMENT ON COLUMN station_meteo.fournisseur IS 'Fournisseur des données météo';
