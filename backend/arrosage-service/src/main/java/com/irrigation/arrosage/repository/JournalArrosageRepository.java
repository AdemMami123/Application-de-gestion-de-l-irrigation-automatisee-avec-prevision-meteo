package com.irrigation.arrosage.repository;

import com.irrigation.arrosage.entity.JournalArrosage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour l'entité JournalArrosage
 */
@Repository
public interface JournalArrosageRepository extends JpaRepository<JournalArrosage, Long> {
    
    /**
     * Rechercher tous les journaux d'un programme
     */
    List<JournalArrosage> findByProgrammeId(Long programmeId);
    
    /**
     * Rechercher les journaux entre deux dates
     */
    @Query("SELECT j FROM JournalArrosage j WHERE j.dateExecution BETWEEN :startDate AND :endDate ORDER BY j.dateExecution DESC")
    List<JournalArrosage> findByDateExecutionBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Rechercher les journaux d'une parcelle via le programme
     */
    @Query("SELECT j FROM JournalArrosage j WHERE j.programme.parcelle.id = :parcelleId ORDER BY j.dateExecution DESC")
    List<JournalArrosage> findByParcelleId(@Param("parcelleId") Long parcelleId);
}
