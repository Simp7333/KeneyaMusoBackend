package com.keneyamuso.dto.response;

import com.keneyamuso.model.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les messages de chat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private MessageType type;
    private String contenu;
    private String fileUrl;
    private Long conversationId;
    private Long expediteurId;
    private String expediteurNom;
    private String expediteurPrenom;
    private String expediteurTelephone;
    private Boolean lu;
    private LocalDateTime timestamp;
}

