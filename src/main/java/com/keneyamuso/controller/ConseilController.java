package com.keneyamuso.controller;

import com.keneyamuso.dto.request.ConseilRequest;
import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.model.entity.Conseil;
import com.keneyamuso.service.ConseilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    @Operation(summary = "Créer un conseil", description = "Ajoute un nouveau contenu éducatif")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<Conseil>> createConseil(@Valid @RequestBody ConseilRequest request) {
        Conseil conseil = conseilService.createConseil(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conseil créé avec succès", conseil));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un conseil", description = "Récupère les détails d'un conseil")
    public ResponseEntity<ApiResponse<Conseil>> getConseil(@PathVariable Long id) {
        Conseil conseil = conseilService.getConseilById(id);
        return ResponseEntity.ok(ApiResponse.success("Conseil trouvé", conseil));
    }

    @GetMapping
    @Operation(
            summary = "Obtenir les conseils",
            description = "Récupère les conseils actifs avec filtres optionnels: type (video|audio|article), catégorie, cible"
    )
    public ResponseEntity<ApiResponse<List<Conseil>>> getAllConseils(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "categorie", required = false) com.keneyamuso.model.enums.CategorieConseil categorie,
            @RequestParam(value = "cible", required = false) String cible
    ) {
        List<Conseil> conseils;
        if (type != null || categorie != null || cible != null) {
            conseils = conseilService.getConseilsActifsFiltres(type, categorie, cible);
        } else {
            conseils = conseilService.getConseilsActifs();
        }
        return ResponseEntity.ok(ApiResponse.success("Liste des conseils", conseils));
    }

    @GetMapping("/mes-conseils")
    @Operation(summary = "Obtenir mes conseils", description = "Récupère les conseils créés par le médecin connecté")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<List<Conseil>>> getMesConseils() {
        List<Conseil> conseils = conseilService.getMesConseils();
        return ResponseEntity.ok(ApiResponse.success("Vos conseils", conseils));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un conseil", description = "Modifie les informations d'un conseil")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<Conseil>> updateConseil(
            @PathVariable Long id,
            @Valid @RequestBody ConseilRequest request) {
        Conseil conseil = conseilService.updateConseil(id, request);
        return ResponseEntity.ok(ApiResponse.success("Conseil mis à jour", conseil));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un conseil", description = "Supprime un conseil (le médecin peut supprimer ses propres conseils)")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<String>> deleteConseil(@PathVariable Long id) {
        conseilService.deleteConseil(id);
        return ResponseEntity.ok(ApiResponse.success("Conseil supprimé", null));
    }
}

