-- Create journal_arrosage table
CREATE TABLE journal_arrosage (
    id BIGSERIAL PRIMARY KEY,
    programme_id BIGINT NOT NULL,
    date_execution TIMESTAMP NOT NULL,
    volume_reel NUMERIC(10, 2) NOT NULL,
    remarque VARCHAR(500),
    CONSTRAINT fk_journal_programme FOREIGN KEY (programme_id) 
        REFERENCES programme_arrosage(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_journal_programme ON journal_arrosage(programme_id);
CREATE INDEX idx_journal_date ON journal_arrosage(date_execution);

-- Add comments
COMMENT ON TABLE journal_arrosage IS 'Table du journal d''exécution des arrosages';
COMMENT ON COLUMN journal_arrosage.programme_id IS 'Référence vers le programme d''arrosage';
COMMENT ON COLUMN journal_arrosage.date_execution IS 'Date et heure d''exécution réelle';
COMMENT ON COLUMN journal_arrosage.volume_reel IS 'Volume d''eau réellement utilisé en m³';
COMMENT ON COLUMN journal_arrosage.remarque IS 'Remarques ou observations sur l''exécution';
