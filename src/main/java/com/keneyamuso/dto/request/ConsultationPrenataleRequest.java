package com.keneyamuso.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO pour la création/modification d'une consultation prénatale
 */
@Data
public class ConsultationPrenataleRequest {
    
    @NotNull(message = "La date prévue est obligatoire")
    private LocalDate datePrevue;
    
    private LocalDate dateRealisee;
    private String notes;
    private Double poids;
    private String tensionArterielle;
    private Double hauteurUterine;
    
    @NotNull(message = "L'ID de la grossesse est obligatoire")
    private Long grossesseId;
}

