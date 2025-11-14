package com.irrigation.arrosage.service;

import com.irrigation.arrosage.dto.JournalArrosageDTO;
import com.irrigation.arrosage.entity.JournalArrosage;
import com.irrigation.arrosage.entity.ProgrammeArrosage;
import com.irrigation.arrosage.repository.JournalArrosageRepository;
import com.irrigation.arrosage.repository.ProgrammeArrosageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JournalArrosageService {

    private final JournalArrosageRepository journalRepository;
    private final ProgrammeArrosageRepository programmeRepository;

    public JournalArrosageDTO create(JournalArrosageDTO dto) {
        log.info("Création d'un nouveau journal d'arrosage pour le programme ID: {}", dto.getProgrammeId());
        ProgrammeArrosage programme = programmeRepository.findById(dto.getProgrammeId())
                .orElseThrow(() -> new RuntimeException("Programme non trouvé avec l'ID: " + dto.getProgrammeId()));
        
        JournalArrosage journal = mapToEntity(dto, programme);
        JournalArrosage saved = journalRepository.save(journal);
        
        // Mettre à jour le statut du programme si nécessaire
        if (programme.getStatut() == ProgrammeArrosage.StatutProgramme.PLANIFIE) {
            programme.setStatut(ProgrammeArrosage.StatutProgramme.TERMINE);
            programmeRepository.save(programme);
        }
        
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<JournalArrosageDTO> findAll() {
        return journalRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JournalArrosageDTO findById(Long id) {
        return journalRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Journal non trouvé avec l'ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<JournalArrosageDTO> findByProgrammeId(Long programmeId) {
        return journalRepository.findByProgrammeId(programmeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JournalArrosageDTO> findByParcelleId(Long parcelleId) {
        return journalRepository.findByParcelleId(parcelleId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JournalArrosageDTO> findByPeriode(LocalDateTime startDate, LocalDateTime endDate) {
        return journalRepository.findByDateExecutionBetween(startDate, endDate).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public JournalArrosageDTO update(Long id, JournalArrosageDTO dto) {
        JournalArrosage journal = journalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Journal non trouvé avec l'ID: " + id));
        
        if (dto.getProgrammeId() != null && !dto.getProgrammeId().equals(journal.getProgramme().getId())) {
            ProgrammeArrosage programme = programmeRepository.findById(dto.getProgrammeId())
                    .orElseThrow(() -> new RuntimeException("Programme non trouvé avec l'ID: " + dto.getProgrammeId()));
            journal.setProgramme(programme);
        }
        
        journal.setDateExecution(dto.getDateExecution());
        journal.setVolumeReel(dto.getVolumeReel());
        journal.setRemarque(dto.getRemarque());
        
        JournalArrosage updated = journalRepository.save(journal);
        return mapToDTO(updated);
    }

    public void delete(Long id) {
        if (!journalRepository.existsById(id)) {
            throw new RuntimeException("Journal non trouvé avec l'ID: " + id);
        }
        journalRepository.deleteById(id);
    }

    private JournalArrosage mapToEntity(JournalArrosageDTO dto, ProgrammeArrosage programme) {
        return JournalArrosage.builder()
                .id(dto.getId())
                .programme(programme)
                .dateExecution(dto.getDateExecution())
                .volumeReel(dto.getVolumeReel())
                .remarque(dto.getRemarque())
                .build();
    }

    private JournalArrosageDTO mapToDTO(JournalArrosage entity) {
        return JournalArrosageDTO.builder()
                .id(entity.getId())
                .programmeId(entity.getProgramme().getId())
                .parcelleNom(entity.getProgramme().getParcelle().getNom())
                .dateExecution(entity.getDateExecution())
                .volumeReel(entity.getVolumeReel())
                .remarque(entity.getRemarque())
                .build();
    }
}
