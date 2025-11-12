package com.keneyamuso.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les notifications de messages via WebSocket.
 * 
 * Cette classe représente la structure d'un message diffusé en temps réel
 * via WebSocket aux participants d'une conversation.
 * 
 * @author KènèyaMuso Team
 * @version 1.0
 * @since 2025-10-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageNotification {
    
    /**
     * ID du message en base de données
     */
    private Long messageId;
    
    /**
     * ID de la conversation
     */
    private Long conversationId;
    
    /**
     * Contenu du message
     */
    private String contenu;
    
    /**
     * ID de l'expéditeur
     */
    private Long expediteurId;
    
    /**
     * Nom complet de l'expéditeur (Prénom + Nom)
     */
    private String expediteurNom;
    
    /**
     * Horodatage du message
     */
    private LocalDateTime timestamp;
}

