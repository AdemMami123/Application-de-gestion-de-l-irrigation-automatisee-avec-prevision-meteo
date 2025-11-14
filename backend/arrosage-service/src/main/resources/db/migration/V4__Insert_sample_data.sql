-- Insert sample parcelles
INSERT INTO parcelle (nom, superficie, culture) VALUES
    ('Parcelle Nord', 5000.00, 'Tomates'),
    ('Parcelle Sud', 7500.00, 'Maïs'),
    ('Parcelle Est', 3000.00, 'Salades'),
    ('Parcelle Ouest', 6000.00, 'Pommes de terre'),
    ('Parcelle Centre', 4500.00, 'Carottes');

-- Insert sample programmes
INSERT INTO programme_arrosage (parcelle_id, date_planifiee, duree, volume_prevu, statut) VALUES
    (1, CURRENT_TIMESTAMP + INTERVAL '1 day', 60, 25.00, 'PLANIFIE'),
    (2, CURRENT_TIMESTAMP + INTERVAL '2 days', 90, 37.50, 'PLANIFIE'),
    (3, CURRENT_TIMESTAMP, 45, 15.00, 'EN_COURS'),
    (4, CURRENT_TIMESTAMP - INTERVAL '1 day', 75, 30.00, 'TERMINE'),
    (5, CURRENT_TIMESTAMP + INTERVAL '3 days', 55, 22.50, 'PLANIFIE');

-- Insert sample journal entries
INSERT INTO journal_arrosage (programme_id, date_execution, volume_reel, remarque) VALUES
    (4, CURRENT_TIMESTAMP - INTERVAL '1 day', 29.50, 'Arrosage effectué avec succès'),
    (3, CURRENT_TIMESTAMP, 14.80, 'En cours d''exécution');
