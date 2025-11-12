package com.keneyamuso.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.keneyamuso.model.enums.StatutConsultation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une consultation postnatale (CPoN)
 */
@Entity
@Table(name = "consultations_postnatales")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationPostnatale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le type de consultation est obligatoire")
    @Column(nullable = false)
    private String type; // J+3, J+7, 6e semaine

    @NotNull(message = "La date prévue est obligatoire")
    @Column(nullable = false)
    private LocalDate datePrevue;

    private LocalDate dateRealisee;

    @Column(length = 1000)
    private String notesMere;

    @Column(length = 1000)
    private String notesNouveauNe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutConsultation statut = StatutConsultation.A_VENIR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patiente_id", nullable = false)
    @JsonIgnore
    private Patiente patiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enfant_id")
    @JsonIgnore
    private Enfant enfant;

    @OneToMany(mappedBy = "consultationPostnatale", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Rappel> rappels = new ArrayList<>();
    
    @OneToOne(mappedBy = "consultationPostnatale", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private SuiviConsultation suiviConsultation;
    
    @OneToOne(mappedBy = "consultationPostnatale", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private Ordonnance ordonnance;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    private LocalDateTime dateModification;
    
    /**
     * Getters pour les IDs (pour la sérialisation JSON)
     */
    public Long getPatienteId() {
        return patiente != null ? patiente.getId() : null;
    }
    
    public Long getEnfantId() {
        return enfant != null ? enfant.getId() : null;
    }
}

