package com.keneyamuso.repository;

import com.keneyamuso.model.entity.DossierMedicalSubmission;
import com.keneyamuso.model.enums.SubmissionStatus;
import com.keneyamuso.model.enums.SubmissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DossierMedicalSubmissionRepository extends JpaRepository<DossierMedicalSubmission, Long> {

    List<DossierMedicalSubmission> findByPatienteIdOrderByDateCreationDesc(Long patienteId);

    List<DossierMedicalSubmission> findByProfessionnelSanteIdAndStatusInOrderByDateCreationDesc(Long medecinId, List<SubmissionStatus> statuts);

    long countByProfessionnelSanteIdAndStatus(Long medecinId, SubmissionStatus statut);

    @Query("SELECT s FROM DossierMedicalSubmission s WHERE s.professionnelSante IS NULL AND s.status = :status ORDER BY s.dateCreation DESC")
    List<DossierMedicalSubmission> findByProfessionnelSanteIsNullAndStatusOrderByDateCreationDesc(@Param("status") SubmissionStatus status);
}


