package com.keneyamuso.dto.response;

import com.keneyamuso.model.enums.RoleUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour la réponse d'authentification JWT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {
    
    private String token;
    private String type = "Bearer";
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private RoleUtilisateur role;
    private LocalDate dateDeNaissance; // Pour les patientes
}

