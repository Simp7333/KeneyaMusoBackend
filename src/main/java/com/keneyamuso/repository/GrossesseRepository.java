package com.keneyamuso.repository;

import com.keneyamuso.model.entity.Grossesse;
import com.keneyamuso.model.enums.StatutGrossesse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Grossesse
 */
@Repository
public interface GrossesseRepository extends JpaRepository<Grossesse, Long> {
    
    List<Grossesse> findByPatienteId(Long patienteId);
    
    List<Grossesse> findByStatut(StatutGrossesse statut);
    
    @Query("SELECT g FROM Grossesse g WHERE g.patiente.id = :patienteId AND g.statut = :statut")
    Optional<Grossesse> findActiveGrossesseByPatiente(@Param("patienteId") Long patienteId, 
                                                       @Param("statut") StatutGrossesse statut);
    
    @Query("SELECT g FROM Grossesse g WHERE g.datePrevueAccouchement BETWEEN :startDate AND :endDate")
    List<Grossesse> findGrossessesWithDPAInRange(@Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
}

