package com.keneyamuso.repository;

import com.keneyamuso.model.entity.ConsultationPrenatale;
import com.keneyamuso.model.enums.StatutConsultation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour l'entité ConsultationPrenatale
 */
@Repository
public interface ConsultationPrenataleRepository extends JpaRepository<ConsultationPrenatale, Long> {
    
    List<ConsultationPrenatale> findByGrossesseId(Long grossesseId);
    
    List<ConsultationPrenatale> findByStatut(StatutConsultation statut);
    
    @Query("SELECT c FROM ConsultationPrenatale c WHERE c.datePrevue BETWEEN :startDate AND :endDate")
    List<ConsultationPrenatale> findConsultationsByDateRange(@Param("startDate") LocalDate startDate, 
                                                               @Param("endDate") LocalDate endDate);
    
    @Query("SELECT c FROM ConsultationPrenatale c JOIN FETCH c.grossesse g WHERE g.patiente.id = :patienteId")
    @EntityGraph(attributePaths = {"grossesse"})
    List<ConsultationPrenatale> findByPatienteId(@Param("patienteId") Long patienteId);
    
    // === MÉTHODE POUR LE SYSTÈME DE RAPPELS ===
    @Query("SELECT c FROM ConsultationPrenatale c WHERE c.datePrevue = :datePrevue AND c.statut = :statut")
    List<ConsultationPrenatale> findByDatePrevueAndStatut(@Param("datePrevue") LocalDate datePrevue, 
                                                           @Param("statut") StatutConsultation statut);
}

