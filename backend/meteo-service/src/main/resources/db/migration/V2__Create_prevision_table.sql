-- Create prevision table
CREATE TABLE prevision (
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

-- Create indexes
CREATE INDEX idx_prevision_station ON prevision(station_id);
CREATE INDEX idx_prevision_date ON prevision(date);
CREATE INDEX idx_prevision_station_date ON prevision(station_id, date);

-- Add comments
COMMENT ON TABLE prevision IS 'Table des prévisions météorologiques';
COMMENT ON COLUMN prevision.station_id IS 'Référence vers la station météo';
COMMENT ON COLUMN prevision.date IS 'Date de la prévision';
COMMENT ON COLUMN prevision.temperature_max IS 'Température maximale prévue (°C)';
COMMENT ON COLUMN prevision.temperature_min IS 'Température minimale prévue (°C)';
COMMENT ON COLUMN prevision.pluie_prevue IS 'Quantité de pluie prévue (mm)';
COMMENT ON COLUMN prevision.vent IS 'Vitesse du vent prévue (km/h)';
