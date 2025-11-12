package com.keneyamuso.model.entity;

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
 * Représente une consultation prénatale (CPN)
 */
@Entity
@Table(name = "consultations_prenatales")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationPrenatale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date prévue est obligatoire")
    @Column(nullable = false)
    private LocalDate datePrevue;

    private LocalDate dateRealisee;

    @OneToOne(mappedBy = "consultationPrenatale", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private SuiviConsultation suiviConsultation;

    @OneToOne(mappedBy = "consultationPrenatale", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Ordonnance ordonnance;

    @Column(length = 1000)
    private String notes;

    @Column(name = "poids_kg")
    private Double poids;

    @Column(name = "tension_arterielle")
    private String tensionArterielle;

    @Column(name = "hauteur_uterine_cm")
    private Double hauteurUterine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutConsultation statut = StatutConsultation.A_VENIR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grossesse_id", nullable = false)
    private Grossesse grossesse;

    @OneToMany(mappedBy = "consultationPrenatale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rappel> rappels = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    private LocalDateTime dateModification;
}

