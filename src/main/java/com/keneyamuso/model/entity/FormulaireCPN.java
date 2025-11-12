package com.keneyamuso.model.entity;

import com.keneyamuso.model.enums.GroupeSanguin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "formulaires_cpn")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormulaireCPN {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double taille;
    private Double poids;
    private LocalDate dernierControle;
    private LocalDate dateDernieresRegles;
    private Integer nombreMoisGrossesse;

    @Enumerated(EnumType.STRING)
    private GroupeSanguin groupeSanguin;

    private boolean complications;
    @Column(columnDefinition = "TEXT")
    private String complicationsDetails;
    
    private boolean mouvementsBebeReguliers;

    @ElementCollection
    @CollectionTable(name = "formulaire_cpn_symptomes", joinColumns = @JoinColumn(name = "formulaire_cpn_id"))
    @Column(name = "symptome")
    private List<String> symptomes;
    
    @Column(columnDefinition = "TEXT")
    private String symptomesAutre;

    private boolean prendMedicamentsOuVitamines;
    @Column(columnDefinition = "TEXT")
    private String medicamentsOuVitaminesDetails;

    private boolean aEuMaladies;
    @Column(columnDefinition = "TEXT")
    private String maladiesDetails;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateSoumission;
}
