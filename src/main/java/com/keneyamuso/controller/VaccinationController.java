package com.keneyamuso.controller;

import com.keneyamuso.dto.request.VaccinationRequest;
import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.model.entity.Vaccination;
import com.keneyamuso.service.VaccinationService;
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
 * Controller pour la gestion des vaccinations
 */
@RestController
@RequestMapping("/api/vaccinations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Vaccinations", description = "APIs pour la gestion du calendrier vaccinal")
public class VaccinationController {

    private final VaccinationService vaccinationService;

    @PostMapping
    @Operation(summary = "Créer une vaccination", description = "Enregistre une nouvelle vaccination")
    public ResponseEntity<ApiResponse<Vaccination>> createVaccination(@Valid @RequestBody VaccinationRequest request) {
        Vaccination vaccination = vaccinationService.createVaccination(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vaccination créée avec succès", vaccination));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une vaccination", description = "Récupère les détails d'une vaccination")
    public ResponseEntity<ApiResponse<Vaccination>> getVaccination(@PathVariable Long id) {
        Vaccination vaccination = vaccinationService.getVaccinationById(id);
        return ResponseEntity.ok(ApiResponse.success("Vaccination trouvée", vaccination));
    }

    @GetMapping("/enfant/{enfantId}")
    @Operation(summary = "Obtenir le calendrier vaccinal d'un enfant", 
               description = "Récupère toutes les vaccinations d'un enfant")
    public ResponseEntity<ApiResponse<List<Vaccination>>> getVaccinationsByEnfant(@PathVariable Long enfantId) {
        List<Vaccination> vaccinations = vaccinationService.getVaccinationsByEnfant(enfantId);
        return ResponseEntity.ok(ApiResponse.success("Vaccinations trouvées", vaccinations));
    }

    @GetMapping
    @Operation(summary = "Obtenir toutes les vaccinations", description = "Récupère toutes les vaccinations")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<List<Vaccination>>> getAllVaccinations() {
        List<Vaccination> vaccinations = vaccinationService.getAllVaccinations();
        return ResponseEntity.ok(ApiResponse.success("Liste des vaccinations", vaccinations));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une vaccination", description = "Modifie les informations d'une vaccination")
    public ResponseEntity<ApiResponse<Vaccination>> updateVaccination(
            @PathVariable Long id,
            @Valid @RequestBody VaccinationRequest request) {
        Vaccination vaccination = vaccinationService.updateVaccination(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vaccination mise à jour", vaccination));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une vaccination", description = "Supprime une vaccination")
    @PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<String>> deleteVaccination(@PathVariable Long id) {
        vaccinationService.deleteVaccination(id);
        return ResponseEntity.ok(ApiResponse.success("Vaccination supprimée", null));
    }
}

