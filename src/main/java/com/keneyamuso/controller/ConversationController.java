package com.keneyamuso.controller;

import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.model.entity.Conversation;
import com.keneyamuso.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Conversations", description = "APIs pour la gestion des conversations de chat")
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * Obtient ou crée une conversation entre une patiente et son médecin assigné
     */
    @GetMapping("/patiente/{patienteId}/medecin")
    @Operation(
            summary = "Obtenir la conversation patiente-médecin",
            description = "Retourne la conversation existante ou en crée une nouvelle entre la patiente et son médecin assigné"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrCreateConversationWithMedecin(
            @PathVariable Long patienteId) {
        
        Conversation conversation = conversationService.getOrCreateConversationWithMedecin(patienteId);
        
        // Mapper vers un format simple pour le frontend
        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("id", conversation.getId());
        conversationData.put("titre", conversation.getTitre());
        conversationData.put("active", conversation.getActive());
        conversationData.put("nombreMessages", conversation.getMessages() != null ? conversation.getMessages().size() : 0);
        
        return ResponseEntity.ok(
                ApiResponse.success("Conversation récupérée", conversationData)
        );
    }

    /**
     * Obtient toutes les conversations d'un utilisateur
     */
    @GetMapping("/utilisateur/{utilisateurId}")
    @Operation(
            summary = "Liste des conversations d'un utilisateur",
            description = "Retourne toutes les conversations où l'utilisateur est participant"
    )
    public ResponseEntity<ApiResponse<List<Conversation>>> getConversationsByUtilisateur(
            @PathVariable Long utilisateurId) {
        
        List<Conversation> conversations = conversationService.getConversationsByUtilisateur(utilisateurId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Conversations récupérées", conversations)
        );
    }

    /**
     * Obtient les détails d'une conversation
     */
    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une conversation")
    public ResponseEntity<ApiResponse<Conversation>> getConversation(@PathVariable Long id) {
        Conversation conversation = conversationService.getConversationById(id);
        return ResponseEntity.ok(ApiResponse.success("Conversation trouvée", conversation));
    }
}

