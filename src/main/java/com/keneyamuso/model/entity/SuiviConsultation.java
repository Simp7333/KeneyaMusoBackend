package com.keneyamuso.model.entity;

import com.keneyamuso.model.enums.EtatConjonctives;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "suivis_consultation")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuiviConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ageGrossesse;
    private Double poids;
    private String tensionArterielle;
    private Double hauteurUterine;
    private String mouvementsFoetaux;
    private String bruitsDuCoeur;
    private String oedeme;
    private String albumine;
    private String etatCol;
    private String toucherVaginal;
    @Column(columnDefinition = "TEXT")
    private String observations;
    private LocalDate dateProchainRendezVous;

    @Column(columnDefinition = "TEXT")
    private String soinsCuratifs;

    @Enumerated(EnumType.STRING)
    private EtatConjonctives etatConjonctives;

    private Double gainPoidsDepuisDebutGrossesse;

    @Column(columnDefinition = "TEXT")
    private String examenObstetrical;

    @Column(columnDefinition = "TEXT")
    private String inspectionPalpation;

    private String presentation;
    private String etatBassinAtteintePromontoire;

    @Column(columnDefinition = "TEXT")
    private String recommandationsAccouchement;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_prenatale_id", unique = true)
    private ConsultationPrenatale consultationPrenatale;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_postnatale_id", unique = true)
    private ConsultationPostnatale consultationPostnatale;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    private LocalDateTime dateModification;
}
