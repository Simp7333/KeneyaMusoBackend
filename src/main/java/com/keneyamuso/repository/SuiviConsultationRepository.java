package com.keneyamuso.repository;

import com.keneyamuso.model.entity.SuiviConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuiviConsultationRepository extends JpaRepository<SuiviConsultation, Long> {
}
