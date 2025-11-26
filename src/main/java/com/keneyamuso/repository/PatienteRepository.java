package com.keneyamuso.repository;

import com.keneyamuso.model.entity.Patiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Patiente
 */
@Repository
public interface PatienteRepository extends JpaRepository<Patiente, Long> {

    Optional<Patiente> findByTelephone(String telephone);

    @Query("SELECT p FROM Patiente p WHERE p.professionnelSanteAssigne.id = :professionnelId")
    List<Patiente> findByProfessionnelSanteId(@Param("professionnelId") Long professionnelId);

    // === MÉTHODES POUR STATS DASHBOARD ===
    @Query("SELECT p.id FROM Patiente p WHERE p.professionnelSanteAssigne.id = :medecinId")
    List<Long> findIdsByProfessionnelSanteId(@Param("medecinId") Long medecinId);

    @Query("SELECT COUNT(g) FROM Grossesse g WHERE g.patiente.professionnelSanteAssigne.id = :medecinId AND g.statut = 'TERMINEE'")
    long countGrossessesTermineesByMedecinId(@Param("medecinId") Long medecinId);

    @Query("SELECT COUNT(g) FROM Grossesse g WHERE g.patiente.professionnelSanteAssigne.id = :medecinId AND g.statut = 'EN_COURS'")
    long countGrossessesEnCoursByMedecinId(@Param("medecinId") Long medecinId);

    // === MÉTHODES POUR LISTE PATIENTES ===
    @Query("SELECT DISTINCT p FROM Patiente p JOIN FETCH p.grossesses g WHERE p.professionnelSanteAssigne.id = :medecinId AND g.statut = 'EN_COURS'")
    List<Patiente> findPatientesWithGrossesseEnCours(@Param("medecinId") Long medecinId);

    @Query("SELECT DISTINCT p FROM Patiente p JOIN FETCH p.grossesses g WHERE p.professionnelSanteAssigne.id = :medecinId AND g.statut = 'TERMINEE'")
    List<Patiente> findPatientesWithGrossesseTerminee(@Param("medecinId") Long medecinId);

    @Query("SELECT DISTINCT p FROM Patiente p JOIN FETCH p.enfants e WHERE p.professionnelSanteAssigne.id = :medecinId")
    List<Patiente> findPatientesWithEnfants(@Param("medecinId") Long medecinId);

    // === MÉTHODES POUR POSTNATAL SANS StackOverflow ===
    @Query("SELECT p.id FROM Patiente p JOIN p.grossesses g WHERE p.professionnelSanteAssigne.id = :medecinId AND g.statut = 'TERMINEE'")
    List<Long> findIdsWithGrossesseTerminee(@Param("medecinId") Long medecinId);

    @Query("SELECT p.id FROM Patiente p JOIN p.enfants e WHERE p.professionnelSanteAssigne.id = :medecinId")
    List<Long> findIdsWithEnfants(@Param("medecinId") Long medecinId);

    @Query("SELECT p FROM Patiente p WHERE p.id IN :ids")
    List<Patiente> findByIdIn(@Param("ids") List<Long> ids);
    
    // Charger les grossesses séparément (évite MultipleBagFetchException)
    @Query("SELECT DISTINCT p FROM Patiente p LEFT JOIN FETCH p.grossesses WHERE p.id IN :ids")
    List<Patiente> findByIdInWithGrossesses(@Param("ids") List<Long> ids);
    
    // Charger les enfants séparément (évite MultipleBagFetchException)
    @Query("SELECT DISTINCT p FROM Patiente p LEFT JOIN FETCH p.enfants WHERE p.id IN :ids")
    List<Patiente> findByIdInWithEnfants(@Param("ids") List<Long> ids);
    
    // === MÉTHODE POUR ADMIN : TOUTES LES PATIENTES AVEC RELATIONS ===
    // Charger toutes les patientes avec grossesses (pour admin)
    @Query("SELECT DISTINCT p FROM Patiente p LEFT JOIN FETCH p.grossesses")
    List<Patiente> findAllWithGrossesses();
    
    // Charger toutes les patientes avec enfants (pour admin)
    @Query("SELECT DISTINCT p FROM Patiente p LEFT JOIN FETCH p.enfants")
    List<Patiente> findAllWithEnfants();
    // ================================================
}