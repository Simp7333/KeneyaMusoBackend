package com.keneyamuso.repository;

import com.keneyamuso.model.entity.DossierMedical;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DossierMedicalRepository extends JpaRepository<DossierMedical, Long> {
    
    Optional<DossierMedical> findByPatienteId(Long patienteId);
    
    /**
     * Récupère le dossier médical avec ses formulaires CPN et CPON en une seule requête.
     * Note: Les collections sont maintenant des Set au lieu de List pour éviter MultipleBagFetchException.
     */
    @EntityGraph(attributePaths = {"formulairesCPN", "formulairesCPON"})
    Optional<DossierMedical> findWithFormulairesByPatienteId(Long patienteId);
}
