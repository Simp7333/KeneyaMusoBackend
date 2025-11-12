package com.keneyamuso.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une femme enceinte ou une jeune mère
 */
@Entity
@Table(name = "patientes")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"dossierMedical", "professionnelSanteAssigne", "grossesses", "enfants", "conversations"})
public class Patiente extends Utilisateur {

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @Column(nullable = false)
    private LocalDate dateDeNaissance;

    @Column(length = 500)
    private String adresse;

    @OneToOne(mappedBy = "patiente", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    @JsonIgnore  // ← AJOUTÉ : EMPÊCHE LA SÉRIALISATION DU DOSSIER DANS LA LISTE
    private DossierMedical dossierMedical;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professionnel_sante_id")
    private ProfessionnelSante professionnelSanteAssigne;

    @OneToMany(mappedBy = "patiente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference("patiente-grossesses")
    private List<Grossesse> grossesses;

    @OneToMany(mappedBy = "patiente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference("patiente-enfants")
    private List<Enfant> enfants;


    @ManyToMany(mappedBy = "participants")
    private List<Conversation> conversations = new ArrayList<>();
}