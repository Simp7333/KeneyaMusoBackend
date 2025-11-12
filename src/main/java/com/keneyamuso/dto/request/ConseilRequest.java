package com.keneyamuso.dto.request;

import com.keneyamuso.model.enums.CategorieConseil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO pour la création/modification d'un conseil
 */
@Data
public class ConseilRequest {
    
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;
    
    private String contenu;
    private String lienMedia;
    
    @NotNull(message = "La catégorie est obligatoire")
    private CategorieConseil categorie;
    
    @NotBlank(message = "La cible est obligatoire")
    private String cible;
}

