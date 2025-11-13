package com.irrigation.meteo.repository;

import com.irrigation.meteo.entity.Prevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour l'entité Prevision
 */
@Repository
public interface PrevisionRepository extends JpaRepository<Prevision, Long> {
    
    /**
     * Rechercher toutes les prévisions d'une station
     */
    List<Prevision> findByStationId(Long stationId);
    
    /**
     * Rechercher les prévisions d'une station pour une date spécifique
     */
    List<Prevision> findByStationIdAndDate(Long stationId, LocalDate date);
    
    /**
     * Rechercher les prévisions entre deux dates
     */
    @Query("SELECT p FROM Prevision p WHERE p.station.id = :stationId AND p.date BETWEEN :startDate AND :endDate ORDER BY p.date")
    List<Prevision> findByStationIdAndDateBetween(
            @Param("stationId") Long stationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
