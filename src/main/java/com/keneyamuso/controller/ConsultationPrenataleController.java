package com.keneyamuso.controller;

import com.keneyamuso.dto.request.ConsultationPrenataleRequest;
import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.model.entity.ConsultationPrenatale;
import com.keneyamuso.service.ConsultationPrenataleService;
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
 * Controller pour la gestion des consultations prénatales (CPN)
 */
@RestController
@RequestMapping("/api/consultations-prenatales")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Consultations Prénatales", description = "APIs pour la gestion des CPN")
public class ConsultationPrenataleController {

    private final ConsultationPrenataleService consultationService;

    @PostMapping
    @Operation(summary = "Créer une CPN", description = "Enregistre une nouvelle consultation prénatale")
    public ResponseEntity<ApiResponse<ConsultationPrenatale>> createConsultation(
            @Valid @RequestBody ConsultationPrenataleRequest request) {
        ConsultationPrenatale consultation = consultationService.createConsultation(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consultation prénatale créée", consultation));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une CPN", description = "Récupère les détails d'une consultation prénatale")
    public ResponseEntity<ApiResponse<ConsultationPrenatale>> getConsultation(@PathVariable Long id) {
        ConsultationPrenatale consultation = consultationService.getConsultationById(id);
        return ResponseEntity.ok(ApiResponse.success("Consultation trouvée", consultation));
    }

    @GetMapping("/grossesse/{grossesseId}")
    @Operation(summary = "Obtenir les CPN d'une grossesse", description = "Récupère toutes les CPN d'une grossesse")
    public ResponseEntity<ApiResponse<List<ConsultationPrenatale>>> getConsultationsByGrossesse(
            @PathVariable Long grossesseId) {
        List<ConsultationPrenatale> consultations = consultationService.getConsultationsByGrossesse(grossesseId);
        return ResponseEntity.ok(ApiResponse.success("Consultations trouvées", consultations));
    }

    @GetMapping("/patiente/{patienteId}")
    @Operation(summary = "Obtenir les CPN d'une patiente", description = "Récupère toutes les CPN d'une patiente")
    public ResponseEntity<ApiResponse<List<ConsultationPrenatale>>> getConsultationsByPatiente(
            @PathVariable Long patienteId) {
        List<ConsultationPrenatale> consultations = consultationService.getConsultationsByPatiente(patienteId);
        return ResponseEntity.ok(ApiResponse.success("Consultations trouvées", consultations));
    }

    @GetMapping
    @Operation(summary = "Obtenir toutes les CPN", description = "Récupère toutes les consultations prénatales")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<List<ConsultationPrenatale>>> getAllConsultations() {
        List<ConsultationPrenatale> consultations = consultationService.getAllConsultations();
        return ResponseEntity.ok(ApiResponse.success("Liste des consultations", consultations));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une CPN", description = "Modifie les informations d'une consultation prénatale")
    public ResponseEntity<ApiResponse<ConsultationPrenatale>> updateConsultation(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationPrenataleRequest request) {
        ConsultationPrenatale consultation = consultationService.updateConsultation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Consultation mise à jour", consultation));
    }

    @PutMapping("/{id}/manquee")
    @Operation(summary = "Marquer comme manquée", description = "Marque une consultation comme manquée")
    public ResponseEntity<ApiResponse<String>> marquerCommeManquee(@PathVariable Long id) {
        consultationService.marquerCommeManquee(id);
        return ResponseEntity.ok(ApiResponse.success("Consultation marquée comme manquée", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une CPN", description = "Supprime une consultation prénatale")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<String>> deleteConsultation(@PathVariable Long id) {
        consultationService.deleteConsultation(id);
        return ResponseEntity.ok(ApiResponse.success("Consultation supprimée", null));
    }
}

