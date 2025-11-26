package com.keneyamuso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO pour la création d'un rappel manuel par l'utilisateur
 */
@Data
public class RappelManuelRequest {
    
    @NotBlank(message = "Le titre/message est obligatoire")
    private String titre;
    
    @NotNull(message = "La date est obligatoire")
    private LocalDate date;
    
    @NotNull(message = "L'heure est obligatoire")
    private LocalTime heure;
}

