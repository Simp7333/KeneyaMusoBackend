package com.keneyamuso.repository;

import com.keneyamuso.model.entity.FormulaireCPON;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormulaireCPONRepository extends JpaRepository<FormulaireCPON, Long> {
    List<FormulaireCPON> findByDossierMedicalId(Long dossierMedicalId);
}
