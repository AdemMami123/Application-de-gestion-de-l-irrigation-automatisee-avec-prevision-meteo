package com.irrigation.meteo.repository;

import com.irrigation.meteo.entity.StationMeteo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité StationMeteo
 */
@Repository
public interface StationMeteoRepository extends JpaRepository<StationMeteo, Long> {
    
    /**
     * Rechercher une station par son nom
     */
    Optional<StationMeteo> findByNom(String nom);
    
    /**
     * Rechercher toutes les stations d'un fournisseur
     */
    List<StationMeteo> findByFournisseur(String fournisseur);
}
