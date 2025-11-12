package com.keneyamuso.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.keneyamuso.model.enums.Specialite;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un professionnel de santé (sage-femme, médecin, etc.)
 */
@Entity
@Table(name = "professionnels_sante")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"patientes", "conversations"})
public class ProfessionnelSante extends Utilisateur {

    @NotNull(message = "La spécialité est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Specialite specialite;

    @NotBlank(message = "L'identifiant professionnel est obligatoire")
    @Column(nullable = false, unique = true)
    private String identifiantProfessionnel;

    @OneToMany(mappedBy = "professionnelSanteAssigne", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"professionnelSanteAssigne", "dossierMedical", "grossesses", "enfants", "conversations", "motDePasse", "role"})
    private List<Patiente> patientes = new ArrayList<>();

    @ManyToMany(mappedBy = "participants")
    @JsonIgnore
    private List<Conversation> conversations = new ArrayList<>();
}