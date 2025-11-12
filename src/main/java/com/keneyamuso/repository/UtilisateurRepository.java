package com.keneyamuso.repository;

import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.model.enums.RoleUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Utilisateur
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByTelephone(String telephone);
    
    Boolean existsByTelephone(String telephone);
    
    List<Utilisateur> findByRole(RoleUtilisateur role);
    
    List<Utilisateur> findByActif(Boolean actif);
}

