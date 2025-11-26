package com.keneyamuso.controller;

import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.dto.response.PatienteDetailDto;
import com.keneyamuso.dto.response.PatienteListDto;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.Enfant;
import com.keneyamuso.model.entity.Grossesse;
import com.keneyamuso.model.entity.Patiente;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.model.enums.RoleUtilisateur;
import com.keneyamuso.repository.PatienteRepository;
import com.keneyamuso.repository.UtilisateurRepository;
import com.keneyamuso.service.PatienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patientes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PatienteController {

    private final PatienteService patienteService;
    private final UtilisateurRepository utilisateurRepository;
    private final PatienteRepository patienteRepository;
    
    @GetMapping("/me")
    @Operation(summary = "Mes informations", description = "Récupère les informations complètes de la patiente connectée")
    public ResponseEntity<ApiResponse<PatienteDetailDto>> getMyPatienteDetails(Authentication authentication) {
        String telephone = authentication.getName();
        Long patienteId = utilisateurRepository.findByTelephone(telephone)
                .map(utilisateur -> utilisateur.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));
        
        PatienteDetailDto patiente = patienteService.getPatienteDetailsById(patienteId);
        return ResponseEntity.ok(ApiResponse.success("Informations récupérées", patiente));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une patiente", description = "Récupère les informations complètes d'une patiente")
    public ResponseEntity<ApiResponse<PatienteDetailDto>> getPatienteById(@PathVariable Long id) {
        PatienteDetailDto patiente = patienteService.getPatienteDetailsById(id);
        return ResponseEntity.ok(ApiResponse.success("Patiente trouvée", patiente));
    }

    @GetMapping
    @Operation(summary = "Liste toutes les patientes", description = "Récupère la liste de toutes les patientes (réservé aux administrateurs)")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<ApiResponse<List<PatienteListDto>>> getAllPatientes(Authentication authentication) {
        // Vérifier que l'utilisateur est admin
        String telephone = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));
        
        if (utilisateur.getRole() != RoleUtilisateur.ADMINISTRATEUR) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("Accès refusé. Seuls les administrateurs peuvent accéder à cette ressource")
            );
        }

        // Récupérer toutes les patientes avec leurs relations
        // On charge d'abord toutes les patientes, puis on charge les grossesses et enfants séparément
        // pour éviter MultipleBagFetchException
        List<Patiente> allPatientes = patienteRepository.findAll();
        List<Long> patienteIds = allPatientes.stream().map(Patiente::getId).collect(Collectors.toList());
        
        // Charger les grossesses pour toutes les patientes
        List<Patiente> patientesWithGrossesses = patienteIds.isEmpty() 
            ? new java.util.ArrayList<>() 
            : patienteRepository.findByIdInWithGrossesses(patienteIds);
        
        // Charger les enfants pour toutes les patientes
        List<Patiente> patientesWithEnfants = patienteIds.isEmpty() 
            ? new java.util.ArrayList<>() 
            : patienteRepository.findByIdInWithEnfants(patienteIds);
        
        // Créer un map pour accéder rapidement aux grossesses et enfants
        Map<Long, List<Grossesse>> grossessesMap = new HashMap<>();
        for (Patiente p : patientesWithGrossesses) {
            if (p.getGrossesses() != null && !p.getGrossesses().isEmpty()) {
                grossessesMap.put(p.getId(), p.getGrossesses());
            }
        }
        
        Map<Long, List<Enfant>> enfantsMap = new HashMap<>();
        for (Patiente p : patientesWithEnfants) {
            if (p.getEnfants() != null && !p.getEnfants().isEmpty()) {
                enfantsMap.put(p.getId(), p.getEnfants());
            }
        }
        
        // Mapper vers DTO
        List<PatienteListDto> dtos = allPatientes.stream().map(patiente -> {
            PatienteListDto dto = new PatienteListDto();
            dto.setId(patiente.getId());
            dto.setNom(patiente.getNom());
            dto.setPrenom(patiente.getPrenom());
            dto.setTelephone(patiente.getTelephone());
            dto.setDateDeNaissance(patiente.getDateDeNaissance());
            dto.setAdresse(patiente.getAdresse());
            
            // Grossesses (récupérées depuis le map)
            List<Grossesse> grossesses = grossessesMap.get(patiente.getId());
            if (grossesses != null && !grossesses.isEmpty()) {
                dto.setGrossesses(grossesses.stream().map(g -> {
                    PatienteListDto.GrossesseBrief gb = new PatienteListDto.GrossesseBrief();
                    gb.setId(g.getId());
                    gb.setDateDebut(g.getDateDebut());
                    gb.setDatePrevueAccouchement(g.getDatePrevueAccouchement());
                    gb.setStatut(g.getStatut().name());
                    return gb;
                }).collect(Collectors.toList()));
            }
            
            // Enfants (récupérés depuis le map)
            List<Enfant> enfants = enfantsMap.get(patiente.getId());
            if (enfants != null && !enfants.isEmpty()) {
                dto.setEnfants(enfants.stream().map(e -> {
                    PatienteListDto.EnfantBrief eb = new PatienteListDto.EnfantBrief();
                    eb.setId(e.getId());
                    eb.setNom(e.getNom());
                    eb.setPrenom(e.getPrenom());
                    eb.setDateDeNaissance(e.getDateDeNaissance());
                    eb.setSexe(e.getSexe().name());
                    return eb;
                }).collect(Collectors.toList()));
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Liste des patientes récupérée avec succès", dtos));
    }

    @GetMapping("/medecin/{id}/grossesse-en-cours")
    @Operation(summary = "Patientes avec grossesse en cours", description = "Récupère les patientes avec grossesse en cours pour un médecin")
    public ResponseEntity<ApiResponse<List<PatienteListDto>>> getPatientesGrossesseEnCours(@PathVariable Long id) {
        List<PatienteListDto> patientes = patienteService.getPatientesAvecGrossesseEnCours(id);
        return ResponseEntity.ok(ApiResponse.success("Patientes récupérées", patientes));
    }

    @GetMapping("/medecin/{id}/grossesse-terminee")
    @Operation(summary = "Patientes avec grossesse terminée", description = "Récupère les patientes avec grossesse terminée pour un médecin")
    public ResponseEntity<ApiResponse<List<PatienteListDto>>> getPatientesGrossesseTerminee(@PathVariable Long id) {
        List<PatienteListDto> patientes = patienteService.getPatientesAvecGrossesseTerminee(id);
        return ResponseEntity.ok(ApiResponse.success("Patientes récupérées", patientes));
    }

    @GetMapping("/medecin/{id}/enfants")
    @Operation(summary = "Patientes avec enfants", description = "Récupère les patientes avec enfants pour un médecin")
    public ResponseEntity<ApiResponse<List<PatienteListDto>>> getPatientesAvecEnfants(@PathVariable Long id) {
        List<PatienteListDto> patientes = patienteService.getPatientesAvecEnfants(id);
        return ResponseEntity.ok(ApiResponse.success("Patientes récupérées", patientes));
    }
}
