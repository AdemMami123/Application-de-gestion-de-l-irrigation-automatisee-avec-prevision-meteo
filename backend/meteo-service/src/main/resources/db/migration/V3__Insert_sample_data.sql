-- Insert sample data for testing
INSERT INTO station_meteo (nom, latitude, longitude, fournisseur) VALUES
    ('Station Paris Centre', 48.8566, 2.3522, 'MeteoFrance'),
    ('Station Lyon Part-Dieu', 45.7640, 4.8357, 'MeteoFrance'),
    ('Station Marseille Vieux-Port', 43.2965, 5.3698, 'OpenWeatherMap'),
    ('Station Toulouse Capitole', 43.6047, 1.4442, 'MeteoFrance'),
    ('Station Nice Promenade', 43.7102, 7.2620, 'OpenWeatherMap');

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
    (5, CURRENT_DATE, 27.0, 19.5, 0.0, 14.0);
