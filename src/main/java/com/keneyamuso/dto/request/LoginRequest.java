package com.keneyamuso.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour la requête de connexion
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
}

