package com.keneyamuso.dto.request;

import com.keneyamuso.model.enums.Sexe;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO pour la création/modification d'un enfant
 */
@Data
public class EnfantRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotNull(message = "La date de naissance est obligatoire")
    private LocalDate dateDeNaissance;

    @NotNull(message = "Le sexe est obligatoire")
    private Sexe sexe;

    @NotNull(message = "L'ID de la patiente est obligatoire")
    private Long patienteId;
}