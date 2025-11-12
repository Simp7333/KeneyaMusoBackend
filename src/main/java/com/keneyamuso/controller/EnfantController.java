package com.keneyamuso.controller;

import com.keneyamuso.dto.request.EnfantRequest;
import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.model.entity.Enfant;
import com.keneyamuso.service.EnfantService;
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
 * Controller pour la gestion des enfants
 */
@RestController
@RequestMapping("/api/enfants")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Enfants", description = "APIs pour la gestion des enfants")
public class EnfantController {

    private final EnfantService enfantService;

    @PostMapping
    @Operation(summary = "Créer un enfant", description = "Enregistre un nouvel enfant")
    public ResponseEntity<ApiResponse<Enfant>> createEnfant(@Valid @RequestBody EnfantRequest request) {
        Enfant enfant = enfantService.createEnfant(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Enfant créé avec succès", enfant));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un enfant", description = "Récupère les détails d'un enfant")
    public ResponseEntity<ApiResponse<Enfant>> getEnfant(@PathVariable Long id) {
        Enfant enfant = enfantService.getEnfantById(id);
        return ResponseEntity.ok(ApiResponse.success("Enfant trouvé", enfant));
    }

    @GetMapping("/patiente/{patienteId}")
    @Operation(summary = "Obtenir les enfants d'une patiente", description = "Récupère tous les enfants d'une patiente")
    public ResponseEntity<ApiResponse<List<Enfant>>> getEnfantsByPatiente(@PathVariable Long patienteId) {
        List<Enfant> enfants = enfantService.getEnfantsByPatiente(patienteId);
        return ResponseEntity.ok(ApiResponse.success("Enfants trouvés", enfants));
    }

    @GetMapping
    @Operation(summary = "Obtenir tous les enfants", description = "Récupère tous les enfants")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<List<Enfant>>> getAllEnfants() {
        List<Enfant> enfants = enfantService.getAllEnfants();
        return ResponseEntity.ok(ApiResponse.success("Liste des enfants", enfants));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un enfant", description = "Modifie les informations d'un enfant")
    public ResponseEntity<ApiResponse<Enfant>> updateEnfant(
            @PathVariable Long id,
            @Valid @RequestBody EnfantRequest request) {
        Enfant enfant = enfantService.updateEnfant(id, request);
        return ResponseEntity.ok(ApiResponse.success("Enfant mis à jour", enfant));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un enfant", description = "Supprime un enfant")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<String>> deleteEnfant(@PathVariable Long id) {
        enfantService.deleteEnfant(id);
        return ResponseEntity.ok(ApiResponse.success("Enfant supprimé avec succès", null));
    }
}