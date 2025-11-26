package com.keneyamuso.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "dossiers_medicaux")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"patiente", "formulairesCPN", "formulairesCPON"})
public class DossierMedical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patiente_id", nullable = false, unique = true)
    @JsonBackReference
    private Patiente patiente;

    // Antecedents médicaux, allergies, etc.
    @Column(columnDefinition = "TEXT")
    private String antecedentsMedicaux;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    // Changé de List à Set pour éviter MultipleBagFetchException avec @EntityGraph
    @OneToMany(mappedBy = "dossierMedical", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FormulaireCPN> formulairesCPN = new HashSet<>();

    @OneToMany(mappedBy = "dossierMedical", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FormulaireCPON> formulairesCPON = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    private LocalDateTime dateModification;
}