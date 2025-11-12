package com.keneyamuso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO pour la création/modification d'une vaccination
 */
@Data
public class VaccinationRequest {
    
    @NotBlank(message = "Le nom du vaccin est obligatoire")
    private String nomVaccin;
    
    @NotNull(message = "La date prévue est obligatoire")
    private LocalDate datePrevue;
    
    private LocalDate dateRealisee;
    private String notes;
    
    @NotNull(message = "L'ID de l'enfant est obligatoire")
    private Long enfantId;
}

