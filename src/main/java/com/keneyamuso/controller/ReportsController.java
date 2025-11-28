package com.keneyamuso.controller;

import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.dto.response.ReportsStatsDto;
import com.keneyamuso.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour les rapports et statistiques détaillées
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reports", description = "APIs pour les rapports et statistiques détaillées")
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/stats")
    @Operation(
            summary = "Obtenir les statistiques de rapports",
            description = "Retourne toutes les statistiques détaillées pour les rapports : stats principales, graphiques, listes de patientes et consultations. " +
                    "Paramètre period: 'week', 'month', 'quarter', 'year' (défaut: 'month')"
    )
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<ReportsStatsDto>> getReportsStats(
            @RequestParam(value = "period", required = false, defaultValue = "month") String period) {
        
        ReportsStatsDto stats = reportsService.getReportsStats(period);
        
        return ResponseEntity.ok(
                ApiResponse.success("Statistiques de rapports récupérées avec succès", stats)
        );
    }
}

