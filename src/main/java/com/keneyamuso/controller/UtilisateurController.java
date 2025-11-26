package com.keneyamuso.controller;

import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.dto.response.PatienteListDto;
import com.keneyamuso.model.entity.Enfant;
import com.keneyamuso.model.entity.Grossesse;
import com.keneyamuso.model.entity.Patiente;
import com.keneyamuso.model.entity.ProfessionnelSante;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.repository.PatienteRepository;
import com.keneyamuso.repository.ProfessionnelSanteRepository;
import com.keneyamuso.repository.UtilisateurRepository;
import com.keneyamuso.service.file.FileStorageService;
import com.keneyamuso.model.enums.RoleUtilisateur;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion des utilisateurs
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "APIs pour la gestion des utilisateurs")
public class UtilisateurController {

    private final ProfessionnelSanteRepository professionnelSanteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PatienteRepository patienteRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

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

    @GetMapping("/admins")
    @Operation(summary = "Récupérer tous les administrateurs", description = "Retourne la liste de tous les administrateurs")
    public ResponseEntity<ApiResponse<List<Utilisateur>>> getAllAdmins() {
        try {
            List<Utilisateur> admins = utilisateurRepository.findAll().stream()
                    .filter(u -> u.getRole() == com.keneyamuso.model.enums.RoleUtilisateur.ADMINISTRATEUR)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success("Liste des administrateurs récupérée avec succès", admins));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans getAllAdmins: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération des administrateurs: " + e.getMessage()));
        }
    }

    @GetMapping("/patientes")
    @Operation(summary = "Récupérer toutes les patientes", description = "Retourne la liste de toutes les patientes avec leurs grossesses et enfants")
    public ResponseEntity<ApiResponse<List<PatienteListDto>>> getAllPatientes() {
        try {
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
        } catch (Exception e) {
            // Logger l'erreur pour le débogage
            System.err.println("❌ Erreur dans getAllPatientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération des patientes: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour le profil utilisateur", description = "Permet de mettre à jour les informations de profil d'un utilisateur. Les admins peuvent modifier n'importe quel utilisateur.")
    public ResponseEntity<ApiResponse<Utilisateur>> updateProfil(
            @PathVariable Long id,
            @RequestBody Map<String, String> updates) {
        try {
            // Vérifier l'authentification
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(
                        ApiResponse.error("Non authentifié"));
            }
            
            // Récupérer l'utilisateur connecté
            String telephone = authentication.getName();
            Utilisateur utilisateurConnecte = utilisateurRepository.findByTelephone(telephone)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));
            
            // Vérifier les permissions : soit l'utilisateur modifie son propre profil, soit c'est un admin
            boolean isAdmin = utilisateurConnecte.getRole() == RoleUtilisateur.ADMINISTRATEUR;
            if (!utilisateurConnecte.getId().equals(id) && !isAdmin) {
                return ResponseEntity.status(403).body(
                        ApiResponse.error("Vous ne pouvez mettre à jour que votre propre profil"));
            }
            
            // Récupérer l'utilisateur à mettre à jour
            Utilisateur utilisateur = utilisateurRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
            
            // Mettre à jour les champs fournis
            if (updates.containsKey("nom") && updates.get("nom") != null && !updates.get("nom").isEmpty()) {
                utilisateur.setNom(updates.get("nom"));
            }
            if (updates.containsKey("prenom") && updates.get("prenom") != null && !updates.get("prenom").isEmpty()) {
                utilisateur.setPrenom(updates.get("prenom"));
            }
            if (updates.containsKey("telephone") && updates.get("telephone") != null && !updates.get("telephone").isEmpty()) {
                // Vérifier que le téléphone n'est pas déjà utilisé par un autre utilisateur
                utilisateurRepository.findByTelephone(updates.get("telephone"))
                        .ifPresent(u -> {
                            if (!u.getId().equals(id)) {
                                throw new RuntimeException("Ce numéro de téléphone est déjà utilisé");
                            }
                        });
                utilisateur.setTelephone(updates.get("telephone"));
            }
            
            // Mettre à jour le mot de passe si fourni (seulement pour les admins ou pour soi-même)
            if (updates.containsKey("motDePasse") && updates.get("motDePasse") != null && !updates.get("motDePasse").isEmpty()) {
                if (updates.get("motDePasse").length() < 6) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Le mot de passe doit contenir au moins 6 caractères"));
                }
                utilisateur.setMotDePasse(passwordEncoder.encode(updates.get("motDePasse")));
            }
            
            utilisateur = utilisateurRepository.save(utilisateur);
            
            return ResponseEntity.ok(ApiResponse.success("Profil mis à jour avec succès", utilisateur));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans updateProfil: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    @PostMapping("/upload/profile-image")
    @Operation(summary = "Uploader une photo de profil", description = "Upload une image de profil et retourne l'URL du fichier")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        try {
            // Vérifier que c'est bien une image
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Le fichier doit être une image"));
            }

            // Stocker le fichier
            String fileName = fileStorageService.storeFile(file);
            
            // Construire l'URL du fichier
            String fileUrl = "/uploads/" + fileName;
            
            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("fileUrl", fileUrl);
            response.put("originalFileName", file.getOriginalFilename());
            
            return ResponseEntity.ok(ApiResponse.success("Photo de profil uploadée avec succès", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de l'upload de la photo: " + e.getMessage()));
        }
    }

    @PutMapping("/profile-photo")
    @Operation(summary = "Mettre à jour la photo de profil de l'utilisateur connecté", 
               description = "Met à jour l'URL de la photo de profil de l'utilisateur actuellement connecté")
    public ResponseEntity<ApiResponse<Utilisateur>> updateProfilePhoto(
            @RequestBody Map<String, String> photoData,
            Authentication authentication) {
        try {
            String telephone = authentication.getName();
            Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));
            
            String photoUrl = photoData.get("photoUrl");
            if (photoUrl == null || photoUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("L'URL de la photo est requise"));
            }
            
            utilisateur.setPhotoProfil(photoUrl);
            utilisateur = utilisateurRepository.save(utilisateur);
            
            return ResponseEntity.ok(ApiResponse.success("Photo de profil mise à jour avec succès", utilisateur));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la mise à jour de la photo: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur. Seuls les administrateurs peuvent supprimer d'autres utilisateurs. ⚠️ Cette opération est irréversible.")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            // Vérifier l'authentification
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(
                        ApiResponse.error("Non authentifié"));
            }
            
            // Récupérer l'utilisateur connecté
            String telephone = authentication.getName();
            Utilisateur utilisateurConnecte = utilisateurRepository.findByTelephone(telephone)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));
            
            // Vérifier les permissions : soit l'utilisateur supprime son propre compte, soit c'est un admin
            boolean isAdmin = utilisateurConnecte.getRole() == RoleUtilisateur.ADMINISTRATEUR;
            if (!utilisateurConnecte.getId().equals(id) && !isAdmin) {
                return ResponseEntity.status(403).body(
                        ApiResponse.error("Vous ne pouvez supprimer que votre propre compte"));
            }
            
            // Récupérer l'utilisateur à supprimer
            Utilisateur utilisateur = utilisateurRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
            
            // Empêcher la suppression du dernier admin
            if (utilisateur.getRole() == RoleUtilisateur.ADMINISTRATEUR) {
                long adminCount = utilisateurRepository.findAll().stream()
                        .filter(u -> u.getRole() == RoleUtilisateur.ADMINISTRATEUR)
                        .count();
                if (adminCount <= 1) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Impossible de supprimer le dernier administrateur"));
                }
            }
            
            // Supprimer l'utilisateur (les relations en cascade seront gérées automatiquement)
            utilisateurRepository.delete(utilisateur);
            
            return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé avec succès", null));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans deleteUser: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    @DeleteMapping("/me")
    @Operation(
            summary = "Supprimer mon compte",
            description = "Supprime définitivement le compte de l'utilisateur connecté. ⚠️ Cette opération est irréversible."
    )
    public ResponseEntity<ApiResponse<String>> deleteMyAccount(Authentication authentication) {
        try {
            // Récupérer l'utilisateur connecté
            String telephone = authentication.getName();
            Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));
            
            // Empêcher la suppression du dernier admin
            if (utilisateur.getRole() == RoleUtilisateur.ADMINISTRATEUR) {
                long adminCount = utilisateurRepository.findAll().stream()
                        .filter(u -> u.getRole() == RoleUtilisateur.ADMINISTRATEUR)
                        .count();
                if (adminCount <= 1) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Impossible de supprimer le dernier administrateur"));
                }
            }
            
            // Supprimer l'utilisateur (les relations en cascade seront gérées automatiquement)
            utilisateurRepository.delete(utilisateur);
            
            return ResponseEntity.ok(ApiResponse.success("Compte supprimé avec succès", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la suppression du compte: " + e.getMessage()));
        }
    }
}

