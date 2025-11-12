package com.keneyamuso.repository;

import com.keneyamuso.model.entity.ConsultationPostnatale;
import com.keneyamuso.model.enums.StatutConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour l'entité ConsultationPostnatale
 */
@Repository
public interface ConsultationPostnataleRepository extends JpaRepository<ConsultationPostnatale, Long> {
    
    @Query("SELECT c FROM ConsultationPostnatale c WHERE c.patiente.id = :patienteId")
    List<ConsultationPostnatale> findByPatienteId(@Param("patienteId") Long patienteId);
    
    @Query("SELECT c FROM ConsultationPostnatale c WHERE c.enfant.id = :enfantId")
    List<ConsultationPostnatale> findByEnfantId(@Param("enfantId") Long enfantId);
    
    List<ConsultationPostnatale> findByStatut(StatutConsultation statut);
    
    @Query("SELECT c FROM ConsultationPostnatale c WHERE c.datePrevue BETWEEN :startDate AND :endDate")
    List<ConsultationPostnatale> findConsultationsByDateRange(@Param("startDate") LocalDate startDate, 
                                                                @Param("endDate") LocalDate endDate);
    
    // === MÉTHODE POUR LE SYSTÈME DE RAPPELS ===
    @Query("SELECT c FROM ConsultationPostnatale c WHERE c.datePrevue = :datePrevue AND c.statut = :statut")
    List<ConsultationPostnatale> findByDatePrevueAndStatut(@Param("datePrevue") LocalDate datePrevue, 
                                                            @Param("statut") StatutConsultation statut);
}

