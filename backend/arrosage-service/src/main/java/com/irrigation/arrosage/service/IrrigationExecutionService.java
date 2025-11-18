package com.irrigation.arrosage.service;

import com.irrigation.arrosage.dto.JournalArrosageDTO;
import com.irrigation.arrosage.entity.JournalArrosage;
import com.irrigation.arrosage.entity.ProgrammeArrosage;
import com.irrigation.arrosage.entity.StatutProgramme;
import com.irrigation.arrosage.repository.ProgrammeArrosageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service pour l'exécution automatique des programmes d'arrosage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IrrigationExecutionService {

    private final ProgrammeArrosageRepository programmeRepository;
    private final JournalArrosageService journalService;
    private final Random random = new Random();

    /**
     * Exécuter tous les programmes planifiés dont l'heure est arrivée
     * 
     * @param executionTime Heure d'exécution de référence
     * @return Nombre de programmes exécutés
     */
    @Transactional
    public int executeScheduledPrograms(LocalDateTime executionTime) {
        // Trouver tous les programmes PLANIFIE dont la date est passée ou actuelle
        List<ProgrammeArrosage> programsToExecute = programmeRepository
                .findByDatePlanifieeBeforeAndStatut(executionTime, StatutProgramme.PLANIFIE);
        
        if (programsToExecute.isEmpty()) {
            log.debug("No programs scheduled for execution at {}", executionTime);
            return 0;
        }
        
        log.info("Found {} program(s) scheduled for execution", programsToExecute.size());
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        programsToExecute.forEach(programme -> {
            try {
                executeSingleProgram(programme, executionTime);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
                log.error("Failed to execute program {}: {}", programme.getId(), e.getMessage(), e);
                handleExecutionFailure(programme, e.getMessage());
            }
        });
        
        log.info("Execution summary: {} succeeded, {} failed", successCount.get(), failureCount.get());
        return successCount.get();
    }

    /**
     * Exécuter un seul programme d'arrosage
     * Utilise une isolation SERIALIZABLE pour éviter les conflits de concurrent execution
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void executeSingleProgram(ProgrammeArrosage programme, LocalDateTime executionTime) {
        log.info("Starting execution of program {} for parcelle {}", 
                programme.getId(), programme.getParcelle().getNom());
        
        // Vérifier que le programme n'a pas déjà été pris par un autre thread
        ProgrammeArrosage freshProgramme = programmeRepository.findById(programme.getId())
                .orElseThrow(() -> new RuntimeException("Programme not found: " + programme.getId()));
        
        if (freshProgramme.getStatut() != StatutProgramme.PLANIFIE) {
            log.warn("Program {} already in status {}, skipping execution", 
                    programme.getId(), freshProgramme.getStatut());
            return;
        }
        
        // Marquer comme EN_COURS
        freshProgramme.setStatut(StatutProgramme.EN_COURS);
        programmeRepository.save(freshProgramme);
        
        try {
            // Simuler l'exécution de l'arrosage
            ExecutionResult result = simulateIrrigationExecution(freshProgramme);
            
            // Créer l'entrée de journal
            createJournalEntry(freshProgramme, result, executionTime);
            
            // Marquer comme TERMINE
            freshProgramme.setStatut(StatutProgramme.TERMINE);
            programmeRepository.save(freshProgramme);
            
            log.info("Successfully completed program {} - Volume: {} m³, Duration: {} min", 
                    freshProgramme.getId(), result.getActualVolume(), freshProgramme.getDuree());
            
        } catch (Exception e) {
            // En cas d'erreur, remettre en PLANIFIE pour retry
            freshProgramme.setStatut(StatutProgramme.PLANIFIE);
            programmeRepository.save(freshProgramme);
            throw e;
        }
    }

    /**
     * Simuler l'exécution de l'arrosage avec variance réaliste
     */
    private ExecutionResult simulateIrrigationExecution(ProgrammeArrosage programme) {
        // Simuler une variance de ±10% sur le volume réel
        double variance = 0.9 + (random.nextDouble() * 0.2); // 0.9 to 1.1
        double actualVolume = programme.getVolumePrevu() * variance;
        
        // Arrondir à 2 décimales
        actualVolume = Math.round(actualVolume * 100.0) / 100.0;
        
        // Générer une remarque basée sur la variance
        String remarque = generateExecutionRemark(programme.getVolumePrevu(), actualVolume);
        
        log.debug("Simulated irrigation execution - Planned: {} m³, Actual: {} m³ ({}% variance)", 
                programme.getVolumePrevu(), actualVolume, Math.round((variance - 1.0) * 100));
        
        return new ExecutionResult(actualVolume, remarque);
    }

    /**
     * Générer une remarque basée sur les résultats de l'exécution
     */
    private String generateExecutionRemark(double plannedVolume, double actualVolume) {
        double difference = actualVolume - plannedVolume;
        double percentDiff = (difference / plannedVolume) * 100;
        
        if (Math.abs(percentDiff) < 2) {
            return "Arrosage effectué avec succès, volume conforme aux prévisions";
        } else if (difference > 0) {
            return String.format("Arrosage effectué avec succès. Volume légèrement supérieur (%.1f%%) aux prévisions", 
                    Math.abs(percentDiff));
        } else {
            return String.format("Arrosage effectué avec succès. Volume légèrement inférieur (%.1f%%) aux prévisions", 
                    Math.abs(percentDiff));
        }
    }

    /**
     * Créer une entrée de journal pour l'exécution
     */
    private void createJournalEntry(ProgrammeArrosage programme, ExecutionResult result, LocalDateTime executionTime) {
        JournalArrosageDTO journalDTO = new JournalArrosageDTO();
        journalDTO.setProgrammeId(programme.getId());
        journalDTO.setDateExecution(executionTime);
        journalDTO.setVolumeReel(result.getActualVolume());
        journalDTO.setRemarque(result.getRemarque());
        
        journalService.createJournal(journalDTO);
        
        log.debug("Created journal entry for program {}", programme.getId());
    }

    /**
     * Gérer les échecs d'exécution
     */
    private void handleExecutionFailure(ProgrammeArrosage programme, String errorMessage) {
        try {
            programme.setStatut(StatutProgramme.ANNULE);
            programmeRepository.save(programme);
            
            // Créer une entrée de journal pour l'échec
            JournalArrosageDTO journalDTO = new JournalArrosageDTO();
            journalDTO.setProgrammeId(programme.getId());
            journalDTO.setDateExecution(LocalDateTime.now());
            journalDTO.setVolumeReel(0.0);
            journalDTO.setRemarque("ÉCHEC D'EXÉCUTION: " + errorMessage);
            
            journalService.createJournal(journalDTO);
            
            log.warn("Marked program {} as ANNULE due to execution failure", programme.getId());
            
        } catch (Exception e) {
            log.error("Failed to handle execution failure for program {}: {}", 
                    programme.getId(), e.getMessage(), e);
        }
    }

    /**
     * Archiver les programmes terminés anciens (soft delete)
     * 
     * @param cutoffDate Date limite - programmes avant cette date seront archivés
     * @return Nombre de programmes archivés
     */
    @Transactional
    public int archiveCompletedPrograms(LocalDateTime cutoffDate) {
        List<ProgrammeArrosage> oldPrograms = programmeRepository
                .findByDatePlanifieeBeforeAndStatut(cutoffDate, StatutProgramme.TERMINE);
        
        if (oldPrograms.isEmpty()) {
            return 0;
        }
        
        log.info("Archiving {} completed programs older than {}", oldPrograms.size(), cutoffDate);
        
        // Pour l'instant, on les laisse en base
        // Dans une vraie application, on pourrait les marquer comme archivés ou les déplacer
        // vers une table d'archives
        
        return oldPrograms.size();
    }

    /**
     * Classe interne pour encapsuler les résultats d'exécution
     */
    private static class ExecutionResult {
        private final double actualVolume;
        private final String remarque;

        public ExecutionResult(double actualVolume, String remarque) {
            this.actualVolume = actualVolume;
            this.remarque = remarque;
        }

        public double getActualVolume() {
            return actualVolume;
        }

        public String getRemarque() {
            return remarque;
        }
    }
}
