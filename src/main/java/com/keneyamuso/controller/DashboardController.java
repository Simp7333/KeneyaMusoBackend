// src/main/java/com/keneyamuso/controller/DashboardController.java
package com.keneyamuso.controller;

import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.dto.response.DashboardStatsResponse;
import com.keneyamuso.dto.response.PatienteListDto;
import com.keneyamuso.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Statistiques et données pour les professionnels de santé")
public class DashboardController {

    private final DashboardService dashboardService;

    // 1. STATISTIQUES DU MÉDECIN
    @GetMapping("/medecin")
    @Operation(
            summary = "Statistiques du tableau de bord médecin",
            description = "Retourne les indicateurs clés : total patientes, suivis en cours, terminés, alertes actives"
    )
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getMedecinDashboardStats(
            Authentication authentication) {

        String telephone = authentication.getName();
        DashboardStatsResponse stats = dashboardService.getMedecinDashboardStats(telephone);

        return ResponseEntity.ok(
                ApiResponse.success("Statistiques récupérées avec succès", stats)
        );
    }

    // 2. LISTE DES PATIENTES (AVEC FILTRE)
    @GetMapping("/medecin/patientes")
    @Operation(
            summary = "Liste des patientes du médecin",
            description = """
            Retourne la liste des patientes suivies par le médecin connecté.
            Filtre optionnel :
            - PRENATAL : grossesses en cours
            - POSTNATAL : grossesses terminées OU mères avec enfants
            - ENFANTS : uniquement les mères avec enfants
            Sans filtre : toutes les patientes assignées
            """
    )
    public ResponseEntity<ApiResponse<List<PatienteListDto>>> getMedecinPatientes(
            Authentication authentication,
            @RequestParam(required = false) String typeSuivi) {

        String telephone = authentication.getName();
        List<PatienteListDto> patientes = dashboardService.getMedecinPatientes(telephone, typeSuivi);

        String message = patientes.isEmpty()
                ? "Aucune patiente trouvée pour ce filtre"
                : "Liste des patientes récupérée avec succès";

        return ResponseEntity.ok(
                ApiResponse.success(message, patientes)
        );
    }

    // 3. STATISTIQUES ADMIN
    @GetMapping("/admin")
    @Operation(
            summary = "Statistiques du tableau de bord administrateur",
            description = "Retourne les statistiques globales : total patientes, professionnels, grossesses, enfants, rappels"
    )
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminDashboardStats(
            Authentication authentication) {
        
        Map<String, Object> stats = dashboardService.getAdminDashboardStats();
        
        return ResponseEntity.ok(
                ApiResponse.success("Statistiques récupérées avec succès", stats)
        );
    }

    // 4. ASSIGNER UNE PATIENTE
    @PostMapping("/medecin/patientes/{patienteId}/assigner")
    @Operation(
            summary = "Assigner une patiente au médecin",
            description = "Permet au médecin connecté d'ajouter une patiente à sa liste de suivi"
    )
    public ResponseEntity<ApiResponse<Void>> assignerPatiente(
            @PathVariable Long patienteId,
            Authentication authentication) {

        String telephone = authentication.getName();
        dashboardService.assignerPatiente(telephone, patienteId);

        return ResponseEntity.ok(
                ApiResponse.success("Patiente assignée avec succès", null)
        );
    }
}