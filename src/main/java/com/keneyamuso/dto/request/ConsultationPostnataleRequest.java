package com.keneyamuso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO pour la création/modification d'une consultation postnatale
 */
@Data
public class ConsultationPostnataleRequest {
    
    @NotBlank(message = "Le type de consultation est obligatoire")
    private String type;
    
    @NotNull(message = "La date prévue est obligatoire")
    private LocalDate datePrevue;
    
    private LocalDate dateRealisee;
    private String notesMere;
    private String notesNouveauNe;
    
    @NotNull(message = "L'ID de la patiente est obligatoire")
    private Long patienteId;
    
    private Long enfantId;
}

