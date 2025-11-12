package com.keneyamuso.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.keneyamuso.model.enums.CategorieConseil;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Représente un contenu éducatif (article, vidéo, audio)
 */
@Entity
@Table(name = "conseils")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conseil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Column(nullable = false)
    private String titre;

    @Column(length = 5000)
    private String contenu;

    @Column(length = 500)
    private String lienMedia; // URL vers vidéo ou audio

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieConseil categorie;

    @Column(nullable = false)
    private String cible; // Femme enceinte, Jeune mère, etc.

    @Column(nullable = false)
    private Boolean actif = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professionnel_id")
    @JsonIgnore
    private ProfessionnelSante createur;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    private LocalDateTime dateModification;

    // Getter pour exposer l'ID et le nom du créateur sans charger toute la relation
    public Long getCreateurId() {
        return createur != null ? createur.getId() : null;
    }

    public String getCreateurNom() {
        return createur != null ? createur.getNom() + " " + createur.getPrenom() : null;
    }
}

