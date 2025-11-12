package com.keneyamuso.repository;

import com.keneyamuso.model.entity.ProfessionnelSante;
import com.keneyamuso.model.enums.Specialite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité ProfessionnelSante
 */
@Repository
public interface ProfessionnelSanteRepository extends JpaRepository<ProfessionnelSante, Long> {
    
    Optional<ProfessionnelSante> findByTelephone(String telephone);
    
    Optional<ProfessionnelSante> findByIdentifiantProfessionnel(String identifiantProfessionnel);
    
    List<ProfessionnelSante> findBySpecialite(Specialite specialite);
}

