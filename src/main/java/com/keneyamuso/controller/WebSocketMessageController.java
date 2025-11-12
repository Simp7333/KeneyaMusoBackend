package com.keneyamuso.controller;

import com.keneyamuso.dto.request.MessageRequest;
import com.keneyamuso.dto.response.MessageNotification;
import com.keneyamuso.model.entity.Message;
import com.keneyamuso.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Contrôleur WebSocket pour la messagerie en temps réel.
 * 
 * Ce contrôleur gère les messages envoyés via WebSocket/STOMP et les diffuse
 * aux participants des conversations en temps réel.
 * 
 * Endpoints WebSocket :
 * - /app/chat.sendMessage : Envoyer un message dans une conversation
 * - /app/chat.typing/{conversationId} : Indiquer qu'on est en train d'écrire
 * - /topic/conversation/{conversationId} : S'abonner aux messages d'une conversation
 * 
 * @author KènèyaMuso Team
 * @version 1.0
 * @since 2025-10-16
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "WebSocket Messages", description = "WebSocket endpoints pour messagerie temps réel")
public class WebSocketMessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envoie un message dans une conversation et le diffuse à tous les participants.
     * 
     * Flux :
     * 1. Le client envoie un message à /app/chat.sendMessage
     * 2. Le message est sauvegardé en base de données
     * 3. Le message est diffusé à /topic/conversation/{conversationId}
     * 4. Tous les clients abonnés à ce topic reçoivent le message instantanément
     * 
     * @param messageRequest Les données du message (conversationId, contenu)
     * @param headerAccessor Accessor pour récupérer l'utilisateur connecté
     * @return Le message créé (sera diffusé automatiquement via @SendTo)
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/conversation/{conversationId}")
    @Operation(summary = "Envoyer un message via WebSocket", 
               description = "Envoie un message et le diffuse en temps réel aux participants")
    public MessageNotification sendMessage(
            @Payload MessageRequest messageRequest,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            // Récupérer l'utilisateur authentifié
            Principal principal = headerAccessor.getUser();
            if (principal == null) {
                throw new IllegalStateException("Utilisateur non authentifié");
            }
            
            String telephone = principal.getName();
            log.info("Message WebSocket reçu de {} pour conversation {}", 
                     telephone, messageRequest.getConversationId());
            
            // Sauvegarder le message en base de données
            Message message = messageService.envoyerMessage(messageRequest, telephone);
            
            // Créer une notification pour les autres participants
            MessageNotification notification = MessageNotification.builder()
                    .messageId(message.getId())
                    .conversationId(message.getConversation().getId())
                    .contenu(message.getContenu())
                    .expediteurId(message.getExpediteur().getId())
                    .expediteurNom(message.getExpediteur().getPrenom() + " " + message.getExpediteur().getNom())
                    .timestamp(message.getTimestamp())
                    .build();
            
            // Diffuser le message à tous les participants de la conversation
            messagingTemplate.convertAndSend(
                "/topic/conversation/" + message.getConversation().getId(),
                notification
            );
            
            log.info("Message {} diffusé avec succès", message.getId());
            return notification;
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du message WebSocket", e);
            throw new RuntimeException("Erreur lors de l'envoi du message", e);
        }
    }

    /**
     * Indique qu'un utilisateur est en train d'écrire dans une conversation.
     * 
     * Permet d'afficher l'indicateur "En train d'écrire..." aux autres participants.
     * 
     * @param conversationId L'ID de la conversation
     * @param headerAccessor Accessor pour récupérer l'utilisateur
     */
    @MessageMapping("/chat.typing/{conversationId}")
    public void userTyping(
            @DestinationVariable Long conversationId,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Principal principal = headerAccessor.getUser();
        if (principal != null) {
            String nom = principal.getName();
            
            // Diffuser l'indicateur "en train d'écrire" aux autres participants
            messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId + "/typing",
                nom + " est en train d'écrire..."
            );
            
            log.debug("Utilisateur {} est en train d'écrire dans conversation {}", nom, conversationId);
        }
    }

    /**
     * Marque un message comme lu et notifie l'expéditeur.
     * 
     * @param messageId L'ID du message à marquer comme lu
     * @param conversationId L'ID de la conversation
     * @param headerAccessor Accessor pour récupérer l'utilisateur
     */
    @MessageMapping("/chat.markAsRead/{conversationId}/{messageId}")
    public void markAsRead(
            @DestinationVariable Long conversationId,
            @DestinationVariable Long messageId,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            Principal principal = headerAccessor.getUser();
            if (principal != null) {
                // Marquer le message comme lu
                messageService.marquerCommeLu(messageId);
                
                // Notifier l'expéditeur que le message a été lu
                messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversationId + "/read",
                    messageId
                );
                
                log.debug("Message {} marqué comme lu dans conversation {}", messageId, conversationId);
            }
        } catch (Exception e) {
            log.error("Erreur lors du marquage du message comme lu", e);
        }
    }
}

