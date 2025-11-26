package com.keneyamuso.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour les conversations avec les informations des participants
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    
    private Long id;
    private String titre;
    private Boolean active;
    private Integer nombreMessages;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    
    // Informations du médecin (pour les patientes)
    private Long medecinId;
    private String medecinNom;
    private String medecinPrenom;
    private String medecinImageUrl;
    private String medecinTelephone;
    
    // Informations de la patiente (pour les médecins)
    private Long patienteId;
    private String patienteNom;
    private String patientePrenom;
    private String patienteImageUrl;
    private String patienteTelephone;
    
    // Liste des participants (optionnel)
    private List<ParticipantDto> participants;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantDto {
        private Long id;
        private String nom;
        private String prenom;
        private String telephone;
        private String role;
        private String imageUrl;
    }
}

