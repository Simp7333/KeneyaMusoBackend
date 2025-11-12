package com.keneyamuso.repository;

import com.keneyamuso.model.entity.Enfant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Enfant
 */
@Repository
public interface EnfantRepository extends JpaRepository<Enfant, Long> {

    // Recherche par identifiant de la patiente
    List<Enfant> findByPatiente_Id(Long patienteId);

    // Recherche par téléphone de la patiente
    List<Enfant> findByPatiente_Telephone(String telephone);
}
