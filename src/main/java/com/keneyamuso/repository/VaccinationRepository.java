package com.keneyamuso.repository;

import com.keneyamuso.model.entity.Vaccination;
import com.keneyamuso.model.enums.StatutVaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour l'entité Vaccination
 */
@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {

    @Query("SELECT v FROM Vaccination v WHERE v.enfant.id = :enfantId")
    List<Vaccination> findByEnfantId(@Param("enfantId") Long enfantId);

    List<Vaccination> findByStatut(StatutVaccination statut);

    @Query("SELECT v FROM Vaccination v WHERE v.datePrevue BETWEEN :startDate AND :endDate")
    List<Vaccination> findVaccinationsByDateRange(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT v FROM Vaccination v WHERE v.enfant.patiente.id = :patienteId")
    List<Vaccination> findByPatienteId(@Param("patienteId") Long patienteId);
    
    // === MÉTHODE POUR LE SYSTÈME DE RAPPELS ===
    @Query("SELECT v FROM Vaccination v WHERE v.datePrevue = :datePrevue AND v.statut = :statut")
    List<Vaccination> findByDatePrevueAndStatut(@Param("datePrevue") LocalDate datePrevue, 
                                                @Param("statut") StatutVaccination statut);
}