package com.keneyamuso.controller;

import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.model.enums.RoleUtilisateur;
import com.keneyamuso.repository.UtilisateurRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour l'initialisation du système
 * Permet de créer l'administrateur par défaut
 */
@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
@Tag(name = "Initialisation", description = "APIs pour l'initialisation du système")
public class InitController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create-default-admin")
    @Operation(
        summary = "Créer l'administrateur par défaut",
        description = "Crée un administrateur par défaut si aucun n'existe. " +
                      "Téléphone: +22370123456, Mot de passe: admin123"
    )
    public ResponseEntity<ApiResponse<String>> createDefaultAdmin() {
        // Vérifier si un admin existe déjà
        boolean adminExists = utilisateurRepository.findAll().stream()
            .anyMatch(u -> u.getRole() == RoleUtilisateur.ADMINISTRATEUR);

        if (adminExists) {
            return ResponseEntity.ok(ApiResponse.success(
                "Un administrateur existe déjà dans le système", 
                null
            ));
        }

        // Vérifier si le téléphone est déjà utilisé
        String defaultPhone = "+22370123456";
        if (utilisateurRepository.findByTelephone(defaultPhone).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                "Le téléphone +22370123456 est déjà utilisé"
            ));
        }

        // Créer l'administrateur par défaut
        Utilisateur admin = new Utilisateur();
        admin.setNom("Admin");
        admin.setPrenom("Système");
        admin.setTelephone(defaultPhone);
        admin.setMotDePasse(passwordEncoder.encode("admin123")); // Hash BCrypt automatique
        admin.setRole(RoleUtilisateur.ADMINISTRATEUR);
        admin.setLangue("fr");
        admin.setActif(true);

        utilisateurRepository.save(admin);

        return ResponseEntity.ok(ApiResponse.success(
            "Administrateur par défaut créé avec succès. " +
            "Téléphone: +22370123456, Mot de passe: admin123. " +
            "⚠️ Changez le mot de passe après la première connexion!",
            null
        ));
    }

    @GetMapping("/check-admin")
    @Operation(
        summary = "Vérifier si un administrateur existe",
        description = "Vérifie si au moins un administrateur existe dans le système"
    )
    public ResponseEntity<ApiResponse<Boolean>> checkAdminExists() {
        boolean adminExists = utilisateurRepository.findAll().stream()
            .anyMatch(u -> u.getRole() == RoleUtilisateur.ADMINISTRATEUR);
        
        return ResponseEntity.ok(ApiResponse.success(
            adminExists ? "Un administrateur existe" : "Aucun administrateur trouvé",
            adminExists
        ));
    }
}

