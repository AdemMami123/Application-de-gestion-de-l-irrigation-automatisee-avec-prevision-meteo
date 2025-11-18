package com.irrigation.arrosage.scheduler;

import com.irrigation.arrosage.service.IrrigationExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduler pour l'exécution automatique des programmes d'arrosage planifiés
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "app.scheduler.irrigation.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class IrrigationScheduler {

    private final IrrigationExecutionService executionService;

    /**
     * Tâche planifiée pour exécuter les programmes d'arrosage
     * Exécutée toutes les 5 minutes par défaut
     */
    @Scheduled(cron = "${app.scheduler.irrigation.cron:0 */5 * * * *}")
    public void executeScheduledIrrigationPrograms() {
        LocalDateTime executionTime = LocalDateTime.now();
        
        log.info("=== Starting scheduled irrigation execution check at {} ===", executionTime);
        
        try {
            int executedCount = executionService.executeScheduledPrograms(executionTime);
            
            if (executedCount > 0) {
                log.info("Successfully executed {} irrigation program(s)", executedCount);
            } else {
                log.debug("No irrigation programs to execute at this time");
            }
            
        } catch (Exception e) {
            log.error("Error during scheduled irrigation execution: {}", e.getMessage(), e);
            // Don't rethrow - we want the scheduler to continue running
        }
        
        log.debug("=== Completed scheduled irrigation execution check ===");
    }

    /**
     * Tâche de nettoyage pour archiver les anciens programmes terminés
     * Exécutée tous les jours à minuit
     */
    @Scheduled(cron = "${app.scheduler.cleanup.cron:0 0 0 * * *}")
    public void cleanupOldPrograms() {
        log.info("=== Starting cleanup of old irrigation programs ===");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int archivedCount = executionService.archiveCompletedPrograms(cutoffDate);
            
            if (archivedCount > 0) {
                log.info("Archived {} old irrigation program(s)", archivedCount);
            } else {
                log.debug("No old programs to archive");
            }
            
        } catch (Exception e) {
            log.error("Error during program cleanup: {}", e.getMessage(), e);
        }
        
        log.debug("=== Completed cleanup task ===");
    }
}
