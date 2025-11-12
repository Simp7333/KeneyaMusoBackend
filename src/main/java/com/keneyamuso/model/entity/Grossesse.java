package com.keneyamuso.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.keneyamuso.model.enums.StatutGrossesse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente le suivi d'une grossesse
 */
@Entity
@Table(name = "grossesses")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"patiente", "consultationsPrenatales"})
public class Grossesse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date de début est obligatoire")
    @Column(nullable = false)
    private LocalDate dateDebut;

    @NotNull(message = "La date prévue d'accouchement (DPA) est obligatoire")
    @Column(nullable = false)
    private LocalDate datePrevueAccouchement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutGrossesse statut = StatutGrossesse.EN_COURS;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patiente_id", nullable = false)
    @JsonBackReference("patiente-grossesses")
    private Patiente patiente;

    @OneToMany(mappedBy = "grossesse", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ConsultationPrenatale> consultationsPrenatales = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    private LocalDateTime dateModification;
}