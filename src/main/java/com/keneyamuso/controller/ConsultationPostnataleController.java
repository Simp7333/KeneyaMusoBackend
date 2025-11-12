package com.keneyamuso.controller;

import com.keneyamuso.dto.request.ConsultationPostnataleRequest;
import com.keneyamuso.dto.request.CponDeclarationRequest;
import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.model.entity.ConsultationPostnatale;
import com.keneyamuso.service.ConsultationPostnataleService;
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
 * Controller pour la gestion des consultations postnatales (CPoN)
 */
@RestController
@RequestMapping("/api/consultations-postnatales")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Consultations Postnatales", description = "APIs pour la gestion des CPoN")
public class ConsultationPostnataleController {

    private final ConsultationPostnataleService consultationService;

    @PostMapping("/declarer")
    @Operation(summary = "Déclarer des CPoN directement", description = "Génère les consultations postnatales pour une patiente sans grossesse suivie dans l'app, en se basant sur la date d'accouchement.")
    public ResponseEntity<ApiResponse<List<ConsultationPostnatale>>> declarerCpon(
            @Valid @RequestBody CponDeclarationRequest request) {
        List<ConsultationPostnatale> consultations = consultationService.declarerCpon(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consultations postnatales créées avec succès", consultations));
    }

    @PostMapping
    @Operation(summary = "Créer une CPoN manuellement", description = "Enregistre une nouvelle consultation postnatale manuellement")
    public ResponseEntity<ApiResponse<ConsultationPostnatale>> createConsultation(
            @Valid @RequestBody ConsultationPostnataleRequest request) {
        ConsultationPostnatale consultation = consultationService.createConsultation(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consultation postnatale créée", consultation));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une CPoN", description = "Récupère les détails d'une consultation postnatale")
    public ResponseEntity<ApiResponse<ConsultationPostnatale>> getConsultation(@PathVariable Long id) {
        ConsultationPostnatale consultation = consultationService.getConsultationById(id);
        return ResponseEntity.ok(ApiResponse.success("Consultation trouvée", consultation));
    }

    @GetMapping("/patiente/{patienteId}")
    @Operation(summary = "Obtenir les CPoN d'une patiente", description = "Récupère toutes les CPoN d'une patiente")
    public ResponseEntity<ApiResponse<List<ConsultationPostnatale>>> getConsultationsByPatiente(
            @PathVariable Long patienteId) {
        List<ConsultationPostnatale> consultations = consultationService.getConsultationsByPatiente(patienteId);
        return ResponseEntity.ok(ApiResponse.success("Consultations trouvées", consultations));
    }
    
    @GetMapping("/enfant/{enfantId}")
    @Operation(summary = "Obtenir les CPoN d'un enfant", description = "Récupère toutes les CPoN d'un enfant")
    public ResponseEntity<ApiResponse<List<ConsultationPostnatale>>> getConsultationsByEnfant(
            @PathVariable Long enfantId) {
        List<ConsultationPostnatale> consultations = consultationService.getConsultationsByEnfant(enfantId);
        return ResponseEntity.ok(ApiResponse.success("Consultations trouvées", consultations));
    }

    @GetMapping
    @Operation(summary = "Obtenir toutes les CPoN", description = "Récupère toutes les consultations postnatales")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<List<ConsultationPostnatale>>> getAllConsultations() {
        List<ConsultationPostnatale> consultations = consultationService.getAllConsultations();
        return ResponseEntity.ok(ApiResponse.success("Liste des consultations", consultations));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une CPoN", description = "Modifie les informations d'une consultation postnatale")
    public ResponseEntity<ApiResponse<ConsultationPostnatale>> updateConsultation(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationPostnataleRequest request) {
        ConsultationPostnatale consultation = consultationService.updateConsultation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Consultation mise à jour", consultation));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une CPoN", description = "Supprime une consultation postnatale")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<String>> deleteConsultation(@PathVariable Long id) {
        consultationService.deleteConsultation(id);
        return ResponseEntity.ok(ApiResponse.success("Consultation supprimée", null));
    }
}

