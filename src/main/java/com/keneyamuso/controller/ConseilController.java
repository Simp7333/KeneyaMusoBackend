package com.keneyamuso.controller;

import com.keneyamuso.dto.request.ConseilRequest;
import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.Conseil;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.model.enums.RoleUtilisateur;
import com.keneyamuso.repository.UtilisateurRepository;
import com.keneyamuso.service.ConseilService;
import com.keneyamuso.service.file.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller pour la gestion des conseils éducatifs
 */
@RestController
@RequestMapping("/api/conseils")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Conseils", description = "APIs pour la gestion des contenus éducatifs")
public class ConseilController {

    private final ConseilService conseilService;
    private final UtilisateurRepository utilisateurRepository;
    private final FileStorageService fileStorageService;

    @PostMapping("/upload/video")
    @Operation(summary = "Uploader une vidéo", description = "Upload une vidéo et retourne l'URL du fichier")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            // Vérifier que c'est bien une vidéo
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Le fichier doit être une vidéo"));
            }

            // Vérifier la taille du fichier (max 100MB)
            long maxSize = 100 * 1024 * 1024; // 100MB
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("La vidéo ne doit pas dépasser 100MB"));
            }

            // Stocker le fichier
            String fileName = fileStorageService.storeFile(file);
            
            // Construire l'URL du fichier
            String fileUrl = "/uploads/" + fileName;
            
            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("fileUrl", fileUrl);
            response.put("originalFileName", file.getOriginalFilename());
            
            return ResponseEntity.ok(ApiResponse.success("Vidéo uploadée avec succès", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de l'upload de la vidéo: " + e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Créer un conseil", description = "Ajoute un nouveau contenu éducatif")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<Conseil>> createConseil(@Valid @RequestBody ConseilRequest request) {
        Conseil conseil = conseilService.createConseil(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conseil créé avec succès", conseil));
    }

    // Mappings spécifiques doivent être déclarés AVANT les mappings avec variables de chemin
    @GetMapping(value = "/pour-patiente", produces = "application/json")
    @Operation(
            summary = "Obtenir les conseils pour une patiente",
            description = "Récupère uniquement les conseils pertinents pour la patiente connectée selon son type de suivi (prenatal/postnatal)"
    )
    public ResponseEntity<ApiResponse<List<Conseil>>> getConseilsPourPatiente(
            org.springframework.security.core.Authentication authentication,
            @RequestParam(value = "typeSuivi", required = false) String typeSuivi
    ) {
        String telephone = authentication.getName();
        List<Conseil> conseils;
        
        if (typeSuivi != null && !typeSuivi.isBlank()) {
            conseils = conseilService.getConseilsPourPatiente(telephone, typeSuivi);
        } else {
            conseils = conseilService.getConseilsPourPatiente(telephone);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Conseils pertinents pour la patiente", conseils));
    }

    @GetMapping("/mes-conseils")
    @Operation(summary = "Obtenir mes conseils", description = "Récupère les conseils créés par le médecin connecté")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<List<Conseil>>> getMesConseils() {
        List<Conseil> conseils = conseilService.getMesConseils();
        return ResponseEntity.ok(ApiResponse.success("Vos conseils", conseils));
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Obtenir un conseil", description = "Récupère les détails d'un conseil")
    public ResponseEntity<ApiResponse<Conseil>> getConseil(@PathVariable Long id) {
        Conseil conseil = conseilService.getConseilById(id);
        return ResponseEntity.ok(ApiResponse.success("Conseil trouvé", conseil));
    }

    @GetMapping
    @Operation(
            summary = "Obtenir les conseils",
            description = "Récupère les conseils. Pour les admins: tous les conseils (actifs et inactifs). Pour les autres: conseils actifs uniquement. Filtres optionnels: type (video|audio|article), catégorie, cible"
    )
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<List<Conseil>>> getAllConseils(
            org.springframework.security.core.Authentication authentication,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "categorie", required = false) com.keneyamuso.model.enums.CategorieConseil categorie,
            @RequestParam(value = "cible", required = false) String cible,
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean all
    ) {
        // Vérifier si l'utilisateur est admin pour retourner tous les conseils
        String telephone = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));
        
        boolean isAdmin = utilisateur.getRole() == RoleUtilisateur.ADMINISTRATEUR;
        
        List<Conseil> conseils;
        
        // Si admin et paramètre all=true, retourner tous les conseils
        if (isAdmin && all) {
            conseils = conseilService.getAllConseils();
        } else if (type != null || categorie != null || cible != null) {
            // Sinon, utiliser les filtres sur les conseils actifs
            conseils = conseilService.getConseilsActifsFiltres(type, categorie, cible);
        } else {
            // Par défaut, conseils actifs
            conseils = conseilService.getConseilsActifs();
        }
        
        return ResponseEntity.ok(ApiResponse.success("Liste des conseils", conseils));
    }

    @PutMapping("/id/{id}")
    @Operation(summary = "Mettre à jour un conseil", description = "Modifie les informations d'un conseil")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<Conseil>> updateConseil(
            @PathVariable Long id,
            @Valid @RequestBody ConseilRequest request) {
        Conseil conseil = conseilService.updateConseil(id, request);
        return ResponseEntity.ok(ApiResponse.success("Conseil mis à jour", conseil));
    }

    @DeleteMapping("/id/{id}")
    @Operation(summary = "Supprimer un conseil", description = "Supprime un conseil (le médecin peut supprimer ses propres conseils)")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<String>> deleteConseil(@PathVariable Long id) {
        conseilService.deleteConseil(id);
        return ResponseEntity.ok(ApiResponse.success("Conseil supprimé", null));
    }
}

