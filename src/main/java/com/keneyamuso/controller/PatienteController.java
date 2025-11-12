package com.keneyamuso.controller;

import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.dto.response.PatienteDetailDto;
import com.keneyamuso.dto.response.PatienteListDto;
import com.keneyamuso.service.PatienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patientes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PatienteController {

    private final PatienteService patienteService;
    
    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une patiente", description = "Récupère les informations complètes d'une patiente")
    public ResponseEntity<ApiResponse<PatienteDetailDto>> getPatienteById(@PathVariable Long id) {
        PatienteDetailDto patiente = patienteService.getPatienteDetailsById(id);
        return ResponseEntity.ok(ApiResponse.success("Patiente trouvée", patiente));
    }

    @GetMapping("/medecin/{id}/grossesse-en-cours")
    public ResponseEntity<List<PatienteListDto>> getPatientesGrossesseEnCours(@PathVariable Long id) {
        return ResponseEntity.ok(patienteService.getPatientesAvecGrossesseEnCours(id));
    }

    @GetMapping("/medecin/{id}/grossesse-terminee")
    public ResponseEntity<List<PatienteListDto>> getPatientesGrossesseTerminee(@PathVariable Long id) {
        return ResponseEntity.ok(patienteService.getPatientesAvecGrossesseTerminee(id));
    }

    @GetMapping("/medecin/{id}/enfants")
    public ResponseEntity<List<PatienteListDto>> getPatientesAvecEnfants(@PathVariable Long id) {
        return ResponseEntity.ok(patienteService.getPatientesAvecEnfants(id));
    }
}
