package com.keneyamuso.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.keneyamuso.model.enums.MessageType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Représente un message unique dans une conversation
 */
@Entity
@Table(name = "messages")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXTE;

    @Column(length = 2000) // N'est plus NotBlank, car peut être null pour les fichiers
    private String contenu;
    
    @Column(name = "file_url")
    private String fileUrl; // Pour stocker l'URL du fichier audio, image ou doc

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnore
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediteur_id", nullable = false)
    @JsonIgnore
    private Utilisateur expediteur;

    @Column(nullable = false)
    private Boolean lu = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    // Getters pour les IDs et infos expéditeur (sérialisation JSON)
    public Long getConversationId() {
        return conversation != null ? conversation.getId() : null;
    }
    
    public Long getExpediteurId() {
        return expediteur != null ? expediteur.getId() : null;
    }
    
    public String getExpediteurNom() {
        return expediteur != null ? expediteur.getNom() : null;
    }
    
    public String getExpediteurPrenom() {
        return expediteur != null ? expediteur.getPrenom() : null;
    }
    
    public String getExpediteurTelephone() {
        return expediteur != null ? expediteur.getTelephone() : null;
    }
}

