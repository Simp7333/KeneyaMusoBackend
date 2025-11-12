package com.keneyamuso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO pour l'envoi d'un message
 */
@Data
public class MessageRequest {
    
    @NotBlank(message = "Le contenu du message est obligatoire")
    private String contenu;
    
    @NotNull(message = "L'ID de la conversation est obligatoire")
    private Long conversationId;
}

