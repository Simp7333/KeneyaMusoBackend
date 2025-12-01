package com.keneyamuso.controller;

import com.keneyamuso.dto.request.DossierSubmissionRequest;
import com.keneyamuso.dto.request.SubmissionApprovalRequest;
import com.keneyamuso.dto.request.SubmissionRejectionRequest;
import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.dto.response.DossierSubmissionResponse;
import com.keneyamuso.model.entity.DossierMedicalSubmission;
import com.keneyamuso.model.enums.SubmissionStatus;
import com.keneyamuso.service.DossierMedicalSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dossiers/submissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Soumissions dossier médical", description = "Gestion des demandes de validation de dossiers médicaux")
public class DossierMedicalSubmissionController {

    private final DossierMedicalSubmissionService submissionService;

    @PostMapping
    @Operation(summary = "Soumettre un dossier médical", description = "Permet à une patiente de soumettre son dossier médical au médecin assigné ou spécifié")
    public ResponseEntity<ApiResponse<DossierSubmissionResponse>> createSubmission(
            @Valid @RequestBody DossierSubmissionRequest request,
            Authentication authentication) {
        String telephone = authentication.getName();

        DossierMedicalSubmission submission = submissionService.createSubmissionForTelephone(
                telephone,
                request.getType(),
                request.getData(),
                request.getMedecinTelephone()); // Passer le téléphone du médecin si fourni

        DossierSubmissionResponse response = submissionService.mapToResponse(submission);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Soumission enregistrée", response));
    }

    @GetMapping("/patiente")
    @Operation(summary = "Lister les soumissions d'une patiente")
    public ResponseEntity<ApiResponse<List<DossierSubmissionResponse>>> getSubmissionsForPatiente(Authentication authentication) {
        String telephone = authentication.getName();
        Long patienteId = submissionService.getPatienteIdFromTelephone(telephone);
        List<DossierSubmissionResponse> responses = submissionService.mapToResponses(
                submissionService.getSubmissionsForPatiente(patienteId));
        return ResponseEntity.ok(ApiResponse.success("Soumissions récupérées", responses));
    }

    @GetMapping("/medecin")
    @Operation(summary = "Lister les soumissions en attente pour un médecin")
    public ResponseEntity<ApiResponse<List<DossierSubmissionResponse>>> getPendingForMedecin(Authentication authentication) {
        String telephone = authentication.getName();
        System.out.println("🔍 Récupération des alertes pour le médecin avec téléphone: " + telephone);
        
        Long medecinId = submissionService.getMedecinIdFromTelephone(telephone);
        System.out.println("✅ Médecin ID trouvé: " + medecinId);
        
        List<DossierSubmissionResponse> responses = submissionService.mapToResponses(
                submissionService.getPendingSubmissionsForMedecin(medecinId));
        
        System.out.println("📋 Nombre d'alertes retournées: " + responses.size());
        
        return ResponseEntity.ok(ApiResponse.success("Soumissions en attente", responses));
    }

    @PostMapping("/{submissionId}/approve")
    @Operation(summary = "Approuver une soumission")
    public ResponseEntity<ApiResponse<String>> approveSubmission(
            @PathVariable Long submissionId,
            @RequestBody(required = false) SubmissionApprovalRequest request,
            Authentication authentication) {
        try {
            Long medecinId = submissionService.getMedecinIdFromTelephone(authentication.getName());
            String commentaire = request != null ? request.getCommentaire() : null;
            submissionService.approveSubmission(submissionId, medecinId, commentaire);
            return ResponseEntity.ok(ApiResponse.success("Soumission approuvée avec succès", null));
        } catch (Exception e) {
            // Les exceptions seront gérées par le GlobalExceptionHandler
            // On les relance pour qu'elles soient capturées
            throw e;
        }
    }

    @PostMapping("/{submissionId}/reject")
    @Operation(summary = "Rejeter une soumission")
    public ResponseEntity<ApiResponse<String>> rejectSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody SubmissionRejectionRequest request,
            Authentication authentication) {
        Long medecinId = submissionService.getMedecinIdFromTelephone(authentication.getName());
        submissionService.rejectSubmission(submissionId, medecinId, request.getRaison());
        return ResponseEntity.ok(ApiResponse.success("Soumission rejetée", null));
    }

    @GetMapping("/medecin/statut")
    @Operation(summary = "Statistiques rapides des soumissions par statut")
    public ResponseEntity<ApiResponse<Long>> getPendingCount(Authentication authentication) {
        Long medecinId = submissionService.getMedecinIdFromTelephone(authentication.getName());
        long count = submissionService.countPendingForMedecin(medecinId, SubmissionStatus.EN_ATTENTE);
        return ResponseEntity.ok(ApiResponse.success("Nombre de soumissions en attente", count));
    }
}
