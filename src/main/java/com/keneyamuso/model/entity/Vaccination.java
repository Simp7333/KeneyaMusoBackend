package com.keneyamuso.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.keneyamuso.model.enums.StatutVaccination;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
 * Représente une vaccination dans le calendrier vaccinal de l'enfant
 */
@Entity
@Table(name = "vaccinations")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vaccination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du vaccin est obligatoire")
    @Column(nullable = false)
    private String nomVaccin;

    @NotNull(message = "La date prévue est obligatoire")
    @Column(nullable = false)
    private LocalDate datePrevue;

    private LocalDate dateRealisee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutVaccination statut = StatutVaccination.A_FAIRE;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enfant_id", nullable = false)
    @JsonIgnore
    private Enfant enfant;

    @OneToMany(mappedBy = "vaccination", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Rappel> rappels = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    private LocalDateTime dateModification;
    
    /**
     * Getter pour l'ID de l'enfant (pour la sérialisation JSON)
     */
    public Long getEnfantId() {
        return enfant != null ? enfant.getId() : null;
    }
}