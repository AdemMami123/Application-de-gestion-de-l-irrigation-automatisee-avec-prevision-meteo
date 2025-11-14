package com.irrigation.arrosage.repository;

import com.irrigation.arrosage.entity.ProgrammeArrosage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour l'entité ProgrammeArrosage
 */
@Repository
public interface ProgrammeArrosageRepository extends JpaRepository<ProgrammeArrosage, Long> {
    
    /**
     * Rechercher tous les programmes d'une parcelle
     */
    List<ProgrammeArrosage> findByParcelleId(Long parcelleId);
    
    /**
     * Rechercher les programmes par statut
     */
    List<ProgrammeArrosage> findByStatut(ProgrammeArrosage.StatutProgramme statut);
    
    /**
     * Rechercher les programmes entre deux dates
     */
    @Query("SELECT p FROM ProgrammeArrosage p WHERE p.datePlanifiee BETWEEN :startDate AND :endDate ORDER BY p.datePlanifiee")
    List<ProgrammeArrosage> findByDatePlanifieeBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Rechercher les programmes planifiés d'une parcelle
     */
    List<ProgrammeArrosage> findByParcelleIdAndStatut(Long parcelleId, ProgrammeArrosage.StatutProgramme statut);
}
