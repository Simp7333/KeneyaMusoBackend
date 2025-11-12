package com.keneyamuso.controller;

import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.model.entity.ProfessionnelSante;
import com.keneyamuso.repository.ProfessionnelSanteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller pour la gestion des utilisateurs
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "APIs pour la gestion des utilisateurs")
public class UtilisateurController {

    private final ProfessionnelSanteRepository professionnelSanteRepository;

    @GetMapping("/medecins")
    @Operation(summary = "Récupérer tous les médecins", description = "Retourne la liste de tous les professionnels de santé (médecins)")
    public ResponseEntity<ApiResponse<List<ProfessionnelSante>>> getAllMedecins() {
        List<ProfessionnelSante> medecins = professionnelSanteRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Liste des médecins récupérée avec succès", medecins));
    }

    @GetMapping("/medecins/{id}")
    @Operation(summary = "Récupérer un médecin par ID", description = "Retourne les informations d'un médecin spécifique")
    public ResponseEntity<ApiResponse<ProfessionnelSante>> getMedecinById(@PathVariable Long id) {
        ProfessionnelSante medecin = professionnelSanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'ID : " + id));
        return ResponseEntity.ok(ApiResponse.success("Médecin récupéré avec succès", medecin));
    }

    @GetMapping("/professionnels")
    @Operation(summary = "Récupérer tous les professionnels de santé", description = "Retourne la liste de tous les professionnels de santé (médecins et sages-femmes)")
    public ResponseEntity<ApiResponse<List<ProfessionnelSante>>> getAllProfessionnels() {
        List<ProfessionnelSante> professionnels = professionnelSanteRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Liste des professionnels récupérée avec succès", professionnels));
    }

    @GetMapping("/professionnels/{id}")
    @Operation(summary = "Récupérer un professionnel par ID", description = "Retourne les informations d'un professionnel de santé spécifique")
    public ResponseEntity<ApiResponse<ProfessionnelSante>> getProfessionnelById(@PathVariable Long id) {
        ProfessionnelSante professionnel = professionnelSanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professionnel non trouvé avec l'ID : " + id));
        return ResponseEntity.ok(ApiResponse.success("Professionnel récupéré avec succès", professionnel));
    }
}

