package com.keneyamuso.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PatienteListDto {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private LocalDate dateDeNaissance;
    private String adresse;
    private List<GrossesseBrief> grossesses;
    private List<EnfantBrief> enfants;

    @Data
    public static class GrossesseBrief {
        private Long id;
        private LocalDate dateDebut;
        private LocalDate datePrevueAccouchement;
        private String statut;
    }

    @Data
    public static class EnfantBrief {
        private Long id;
        private String nom;
        private String prenom;
        private LocalDate dateDeNaissance;
        private String sexe;
    }
}
