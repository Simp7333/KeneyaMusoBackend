package com.keneyamuso.dto.request;

import com.keneyamuso.model.enums.RoleUtilisateur;
import com.keneyamuso.model.enums.Specialite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO pour la requête d'inscription avec profil complet.
 * 
 * Gère l'inscription différenciée selon le rôle :
 * - PATIENTE : date de naissance, adresse, professionnel assigné
 * - MEDECIN : spécialité, identifiant professionnel
 * - ADMINISTRATEUR : informations de base uniquement
 */
@Data
public class RegisterRequest {
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    
    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Format de téléphone invalide")
    private String telephone;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
    
    @NotNull(message = "Le rôle est obligatoire")
    private RoleUtilisateur role;
    
    private String langue = "fr";
    
    private String photoProfil; // URL de la photo de profil (optionnel)
    
    // Champs spécifiques pour PATIENTE
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateDeNaissance;
    
    private String adresse;
    
    private Long professionnelSanteId; // ID du médecin assigné (optionnel)
    
    // Champs spécifiques pour MEDECIN
    private Specialite specialite;
    
    private String identifiantProfessionnel;
}

