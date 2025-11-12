package com.keneyamuso.repository;

import com.keneyamuso.model.entity.Rappel;
import com.keneyamuso.model.enums.StatutRappel;
import com.keneyamuso.model.enums.TypeRappel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RappelRepository extends JpaRepository<Rappel, Long> {

    @Query("SELECT r FROM Rappel r WHERE r.utilisateur.id = :utilisateurId")
    List<Rappel> findByUtilisateurId(@Param("utilisateurId") Long utilisateurId);

    List<Rappel> findByType(TypeRappel type);

    List<Rappel> findByStatut(StatutRappel statut);

    @Query("SELECT r FROM Rappel r WHERE r.utilisateur.id = :utilisateurId AND r.statut = :statut")
    List<Rappel> findByUtilisateurIdAndStatut(@Param("utilisateurId") Long utilisateurId,
                                              @Param("statut") StatutRappel statut);

    @Query("SELECT r FROM Rappel r WHERE r.dateEnvoi BETWEEN :startDate AND :endDate")
    List<Rappel> findRappelsByDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // ✅ Méthode corrigée pour compter les rappels par professionnel et statut
    @Query("""
        SELECT COUNT(r)
        FROM Rappel r
        WHERE r.utilisateur.professionnelSanteAssigne.id = :professionnelId
        AND r.statut = :statut
    """)
    long countByProfessionnelIdAndStatut(@Param("professionnelId") Long professionnelId,
                                         @Param("statut") StatutRappel statut);
    
    // === MÉTHODES POUR VÉRIFIER L'EXISTENCE DES RAPPELS ===
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Rappel r WHERE r.consultationPrenatale.id = :consultationId AND r.type = :type")
    boolean existsByConsultationPrenataleIdAndType(@Param("consultationId") Long consultationId, 
                                                   @Param("type") TypeRappel type);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Rappel r WHERE r.consultationPostnatale.id = :consultationId AND r.type = :type")
    boolean existsByConsultationPostnataleIdAndType(@Param("consultationId") Long consultationId, 
                                                    @Param("type") TypeRappel type);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Rappel r WHERE r.vaccination.id = :vaccinationId AND r.type = :type")
    boolean existsByVaccinationIdAndType(@Param("vaccinationId") Long vaccinationId, 
                                        @Param("type") TypeRappel type);
}
