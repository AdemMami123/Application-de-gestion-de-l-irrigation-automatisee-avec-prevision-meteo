package com.irrigation.arrosage.repository;

import com.irrigation.arrosage.entity.Parcelle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Parcelle
 */
@Repository
public interface ParcelleRepository extends JpaRepository<Parcelle, Long> {
    
    /**
     * Rechercher une parcelle par son nom
     */
    Optional<Parcelle> findByNom(String nom);
    
    /**
     * Rechercher toutes les parcelles d'une culture spécifique
     */
    List<Parcelle> findByCulture(String culture);
}
