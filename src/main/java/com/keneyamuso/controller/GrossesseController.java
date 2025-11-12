package com.keneyamuso.controller;

import com.keneyamuso.dto.request.GrossesseRequest;
import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.model.entity.Grossesse;
import com.keneyamuso.service.GrossesseService;
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
 * Controller pour la gestion des grossesses
 */
@RestController
@RequestMapping("/api/grossesses")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Grossesses", description = "APIs pour la gestion des grossesses")
public class GrossesseController {

    private final GrossesseService grossesseService;

    @PostMapping
    @Operation(summary = "Créer une grossesse", description = "Enregistre une nouvelle grossesse pour une patiente")
    public ResponseEntity<ApiResponse<Grossesse>> createGrossesse(@Valid @RequestBody GrossesseRequest request) {
        Grossesse grossesse = grossesseService.createGrossesse(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Grossesse créée avec succès", grossesse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une grossesse", description = "Récupère les détails d'une grossesse par son ID")
    public ResponseEntity<ApiResponse<Grossesse>> getGrossesse(@PathVariable Long id) {
        Grossesse grossesse = grossesseService.getGrossesseById(id);
        return ResponseEntity.ok(ApiResponse.success("Grossesse trouvée", grossesse));
    }

    @GetMapping("/patiente/{patienteId}")
    @Operation(summary = "Obtenir les grossesses d'une patiente", description = "Récupère toutes les grossesses d'une patiente")
    public ResponseEntity<ApiResponse<List<Grossesse>>> getGrossessesByPatiente(@PathVariable Long patienteId) {
        List<Grossesse> grossesses = grossesseService.getGrossessesByPatiente(patienteId);
        return ResponseEntity.ok(ApiResponse.success("Grossesses trouvées", grossesses));
    }

    @GetMapping
    @Operation(summary = "Obtenir toutes les grossesses", description = "Récupère toutes les grossesses")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<List<Grossesse>>> getAllGrossesses() {
        List<Grossesse> grossesses = grossesseService.getAllGrossesses();
        return ResponseEntity.ok(ApiResponse.success("Liste des grossesses", grossesses));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une grossesse", description = "Modifie les informations d'une grossesse")
    public ResponseEntity<ApiResponse<Grossesse>> updateGrossesse(
            @PathVariable Long id,
            @Valid @RequestBody GrossesseRequest request) {
        Grossesse grossesse = grossesseService.updateGrossesse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Grossesse mise à jour", grossesse));
    }

    @PutMapping("/{id}/terminer")
    @Operation(summary = "Terminer une grossesse", description = "Marque une grossesse comme terminée")
    public ResponseEntity<ApiResponse<String>> terminerGrossesse(@PathVariable Long id) {
        grossesseService.terminerGrossesse(id);
        return ResponseEntity.ok(ApiResponse.success("Grossesse terminée", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une grossesse", description = "Supprime une grossesse")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<String>> deleteGrossesse(@PathVariable Long id) {
        grossesseService.deleteGrossesse(id);
        return ResponseEntity.ok(ApiResponse.success("Grossesse supprimée", null));
    }
}

