package com.keneyamuso.controller;

import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.dto.response.ConversationDto;
import com.keneyamuso.model.entity.Conversation;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.model.enums.RoleUtilisateur;
import com.keneyamuso.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<ApiResponse<ConversationDto>> getOrCreateConversationWithMedecin(
            @PathVariable Long patienteId) {
        
        Conversation conversation = conversationService.getOrCreateConversationWithMedecin(patienteId);
        ConversationDto dto = mapToDto(conversation);
        
        return ResponseEntity.ok(
                ApiResponse.success("Conversation récupérée", dto)
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
    public ResponseEntity<ApiResponse<List<ConversationDto>>> getConversationsByUtilisateur(
            @PathVariable Long utilisateurId) {
        
        List<Conversation> conversations = conversationService.getConversationsByUtilisateur(utilisateurId);
        List<ConversationDto> dtos = conversations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(
                ApiResponse.success("Conversations récupérées", dtos)
        );
    }

    /**
     * Obtient les détails d'une conversation
     */
    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une conversation")
    public ResponseEntity<ApiResponse<ConversationDto>> getConversation(@PathVariable Long id) {
        Conversation conversation = conversationService.getConversationById(id);
        ConversationDto dto = mapToDto(conversation);
        return ResponseEntity.ok(ApiResponse.success("Conversation trouvée", dto));
    }
    
    /**
     * Mappe une Conversation vers un ConversationDto avec les informations des participants
     */
    private ConversationDto mapToDto(Conversation conversation) {
        ConversationDto.ConversationDtoBuilder builder = ConversationDto.builder()
                .id(conversation.getId())
                .titre(conversation.getTitre())
                .active(conversation.getActive())
                .nombreMessages(conversation.getMessages() != null ? conversation.getMessages().size() : 0)
                .dateCreation(conversation.getDateCreation())
                .dateModification(conversation.getDateModification());
        
        // Extraire les informations des participants
        if (conversation.getParticipants() != null && !conversation.getParticipants().isEmpty()) {
            List<ConversationDto.ParticipantDto> participantDtos = new ArrayList<>();
            
            for (Utilisateur participant : conversation.getParticipants()) {
                // Ajouter aux participants
                ConversationDto.ParticipantDto participantDto = ConversationDto.ParticipantDto.builder()
                        .id(participant.getId())
                        .nom(participant.getNom())
                        .prenom(participant.getPrenom())
                        .telephone(participant.getTelephone())
                        .role(participant.getRole() != null ? participant.getRole().name() : null)
                        .imageUrl(participant.getPhotoProfil())
                        .build();
                participantDtos.add(participantDto);
                
                // Si c'est un médecin, remplir les champs spécifiques
                if (participant.getRole() == RoleUtilisateur.MEDECIN) {
                    builder.medecinId(participant.getId())
                           .medecinNom(participant.getNom())
                           .medecinPrenom(participant.getPrenom())
                           .medecinTelephone(participant.getTelephone())
                           .medecinImageUrl(participant.getPhotoProfil());
                }
                
                // Si c'est une patiente, remplir les champs spécifiques
                if (participant.getRole() == RoleUtilisateur.PATIENTE) {
                    builder.patienteId(participant.getId())
                           .patienteNom(participant.getNom())
                           .patientePrenom(participant.getPrenom())
                           .patienteTelephone(participant.getTelephone())
                           .patienteImageUrl(participant.getPhotoProfil());
                }
            }
            
            builder.participants(participantDtos);
        }
        
        return builder.build();
    }
}

