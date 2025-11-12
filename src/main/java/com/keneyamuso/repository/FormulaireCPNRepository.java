package com.keneyamuso.repository;

import com.keneyamuso.model.entity.FormulaireCPN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormulaireCPNRepository extends JpaRepository<FormulaireCPN, Long> {
}
