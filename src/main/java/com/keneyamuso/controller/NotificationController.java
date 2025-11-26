package com.keneyamuso.controller;

import com.keneyamuso.dto.request.RappelManuelRequest;
import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.Rappel;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.model.enums.StatutRappel;
import com.keneyamuso.repository.RappelRepository;
import com.keneyamuso.repository.UtilisateurRepository;
import com.keneyamuso.service.RappelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion des notifications
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "APIs pour la gestion des notifications")
public class NotificationController {

    private final RappelService rappelService;
    private final RappelRepository rappelRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Obtenir toutes les notifications d'une patiente
     */
    @GetMapping("/patiente/{patienteId}")
    @Operation(summary = "Liste des notifications d'une patiente")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotificationsByPatiente(
            @PathVariable Long patienteId) {
        
        List<Rappel> rappels = rappelService.getRappelsByUtilisateurId(patienteId);
        List<Map<String, Object>> notifications = rappels.stream()
                .map(rappelService::rappelToNotificationMap)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(
                "Notifications de la patiente récupérées", notifications));
    }

    /**
     * Obtenir toutes les notifications d'un médecin
     */
    @GetMapping("/medecin/{medecinId}")
    @Operation(summary = "Liste des notifications d'un médecin")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotificationsByMedecin(
            @PathVariable Long medecinId) {
        
        List<Rappel> rappels = rappelService.getRappelsByUtilisateurId(medecinId);
        List<Map<String, Object>> notifications = rappels.stream()
                .map(rappelService::rappelToNotificationMap)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(
                "Notifications du médecin récupérées", notifications));
    }

    /**
     * Obtenir les notifications de l'utilisateur connecté
     */
    @GetMapping("/me")
    @Operation(summary = "Liste de mes notifications")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyNotifications(
            Authentication authentication) {
        
        String telephone = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));
        
        List<Rappel> rappels = rappelService.getRappelsByUtilisateurId(utilisateur.getId());
        List<Map<String, Object>> notifications = rappels.stream()
                .map(rappelService::rappelToNotificationMap)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(
                "Mes notifications récupérées", notifications));
    }

    /**
     * Marquer une notification comme lue
     */
    @PutMapping("/{notificationId}/lue")
    @Operation(summary = "Marquer une notification comme lue")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> marquerCommeLue(
            @PathVariable Long notificationId) {
        
        Rappel rappel = rappelService.marquerCommeLu(notificationId);
        Map<String, Object> notification = rappelService.rappelToNotificationMap(rappel);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Notification marquée comme lue", notification));
    }

    /**
     * Marquer une notification comme traitée
     */
    @PutMapping("/{notificationId}/traitee")
    @Operation(summary = "Marquer une notification comme traitée")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> marquerCommeTraitee(
            @PathVariable Long notificationId) {
        
        Rappel rappel = rappelRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Rappel", "id", notificationId));
        
        rappel.setStatut(StatutRappel.CONFIRME);
        rappelRepository.save(rappel);
        
        Map<String, Object> notification = rappelService.rappelToNotificationMap(rappel);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Notification marquée comme traitée", notification));
    }

    /**
     * Supprimer une notification
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Supprimer une notification")
    @Transactional
    public ResponseEntity<ApiResponse<String>> supprimerNotification(
            @PathVariable Long notificationId) {
        
        rappelRepository.deleteById(notificationId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Notification supprimée", null));
    }

    /**
     * Obtenir les statistiques de notifications
     */
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques des notifications")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistiques(
            Authentication authentication) {
        
        String telephone = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));
        
        long total = rappelRepository.findByUtilisateurId(utilisateur.getId()).size();
        long nonLues = rappelRepository.findByUtilisateurIdAndStatut(
                utilisateur.getId(), StatutRappel.ENVOYE).size();
        long lues = rappelRepository.findByUtilisateurIdAndStatut(
                utilisateur.getId(), StatutRappel.LU).size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("nonLues", nonLues);
        stats.put("lues", lues);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Statistiques récupérées", stats));
    }

    /**
     * Déclencher manuellement l'envoi des rappels quotidiens (TEST)
     * Utile pour tester le système de rappels sans attendre 8h du matin
     */
    @PostMapping("/envoyer-rappels-manuel")
    @Operation(
            summary = "Envoyer les rappels manuellement (TEST)",
            description = "Déclenche l'envoi des rappels pour les consultations et vaccinations à venir. " +
                         "Normalement exécuté automatiquement chaque jour à 8h."
    )
    public ResponseEntity<ApiResponse<Map<String, Integer>>> envoyerRappelsManuellement() {
        Map<String, Integer> stats = rappelService.envoyerRappelsManuellement();
        
        return ResponseEntity.ok(ApiResponse.success(
                "Rappels envoyés manuellement", stats));
    }

    /**
     * Confirmer un rappel (la patiente confirme sa présence)
     * Marque la consultation/vaccination comme effectuée
     */
    @PostMapping("/{rappelId}/confirmer")
    @Operation(
            summary = "Confirmer un rappel",
            description = "La patiente confirme sa présence. La consultation/vaccination est marquée comme effectuée."
    )
    @Transactional
    public ResponseEntity<ApiResponse<String>> confirmerRappel(@PathVariable Long rappelId) {
        rappelService.confirmerRappel(rappelId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Rappel confirmé. Consultation/vaccination marquée comme effectuée.", null));
    }

    /**
     * Reprogrammer un rappel à une nouvelle date
     * Met à jour la date prévue de la consultation/vaccination
     */
    @PostMapping("/{rappelId}/reprogrammer")
    @Operation(
            summary = "Reprogrammer un rappel",
            description = "La patiente choisit une nouvelle date. La consultation/vaccination est reprogrammée."
    )
    @Transactional
    public ResponseEntity<ApiResponse<String>> reprogrammerRappel(
            @PathVariable Long rappelId,
            @RequestParam String nouvelleDate) {
        
        LocalDate date = LocalDate.parse(nouvelleDate);
        rappelService.reprogrammerRappel(rappelId, date);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Rappel reprogrammé au " + nouvelleDate, null));
    }

    /**
     * Créer un rappel manuel
     * Permet à l'utilisateur de créer un rappel personnalisé avec titre, date et heure
     */
    @PostMapping("/manuel")
    @Operation(
            summary = "Créer un rappel manuel",
            description = "Crée un rappel personnalisé avec un titre, une date et une heure"
    )
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> creerRappelManuel(
            @Valid @RequestBody RappelManuelRequest request,
            Authentication authentication) {
        
        String telephone = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));
        
        Rappel rappel = rappelService.creerRappelManuel(
                utilisateur,
                request.getTitre(),
                request.getDate(),
                request.getHeure()
        );
        
        Map<String, Object> notification = rappelService.rappelToNotificationMap(rappel);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Rappel créé avec succès", notification));
    }
}

