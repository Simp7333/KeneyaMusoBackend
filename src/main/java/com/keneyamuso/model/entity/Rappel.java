package com.keneyamuso.model.entity;

import com.keneyamuso.model.enums.StatutRappel;
import com.keneyamuso.model.enums.TypeRappel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Représente une notification/rappel envoyé à l'utilisateur
 */
@Entity
@Table(name = "rappels")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rappel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le message est obligatoire")
    @Column(nullable = false, length = 500)
    private String message;

    @NotNull(message = "La date d'envoi est obligatoire")
    @Column(nullable = false)
    private LocalDateTime dateEnvoi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeRappel type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutRappel statut = StatutRappel.ENVOYE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_prenatale_id")
    private ConsultationPrenatale consultationPrenatale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_postnatale_id")
    private ConsultationPostnatale consultationPostnatale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccination_id")
    private Vaccination vaccination;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}

