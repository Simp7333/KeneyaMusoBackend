package com.keneyamuso.model.entity;

import com.keneyamuso.model.enums.AlimentationBebe;
import com.keneyamuso.model.enums.Sexe;
import com.keneyamuso.model.enums.TypeAccouchement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "formulaires_cpon")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormulaireCPON {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeAccouchement accouchementType;

    private String nombreEnfants; // 1er, 2e, 3e, Plus

    @ElementCollection
    @CollectionTable(name = "formulaire_cpon_sentiments", joinColumns = @JoinColumn(name = "formulaire_cpon_id"))
    @Column(name = "sentiment")
    private List<String> sentiment; // Bien, Fatiguée, Douleurs, Fièvre

    private boolean saignements;

    private String consultation; // Non, Oui,CPON1, Oui,CPON2, etc.

    @Enumerated(EnumType.STRING)
    private Sexe sexeBebe;

    @Enumerated(EnumType.STRING)
    private AlimentationBebe alimentation;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateSoumission;

}
