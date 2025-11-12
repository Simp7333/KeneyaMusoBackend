package com.keneyamuso.repository;

import com.keneyamuso.model.entity.Conseil;
import com.keneyamuso.model.entity.ProfessionnelSante;
import com.keneyamuso.model.enums.CategorieConseil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Conseil
 */
@Repository
public interface ConseilRepository extends JpaRepository<Conseil, Long> {
    
    List<Conseil> findByCategorie(CategorieConseil categorie);
    
    List<Conseil> findByCible(String cible);
    
    List<Conseil> findByActif(Boolean actif);
    
    List<Conseil> findByCategorieAndActif(CategorieConseil categorie, Boolean actif);
    
    @Query("SELECT c FROM Conseil c WHERE c.createur.id = :createurId ORDER BY c.dateCreation DESC")
    List<Conseil> findByCreateurId(@Param("createurId") Long createurId);
}

