package com.keneyamuso.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour les détails complets d'une patiente
 */
@Data
public class PatienteDetailDto {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private LocalDate dateDeNaissance;
    private String adresse;
    private Integer age;
    
    // Médecin assigné
    private MedecinBrief medecinAssigne;
    
    // Grossesses
    private List<GrossesseDetail> grossesses;
    
    // Enfants
    private List<EnfantDetail> enfants;
    
    // Consultations prénatales
    private List<ConsultationPrenataleDetail> consultationsPrenatales;
    
    // Consultations postnatales
    private List<ConsultationPostnataleDetail> consultationsPostnatales;
    
    @Data
    public static class MedecinBrief {
        private Long id;
        private String nom;
        private String prenom;
        private String telephone;
        private String specialite;
    }
    
    @Data
    public static class GrossesseDetail {
        private Long id;
        private LocalDate dateDebut;
        private LocalDate datePrevueAccouchement;
        private String statut;
        private Integer nombreConsultations;
    }
    
    @Data
    public static class EnfantDetail {
        private Long id;
        private String nom;
        private String prenom;
        private LocalDate dateDeNaissance;
        private String sexe;
        private Integer age;
        private Integer nombreVaccinations;
        private Integer nombreConsultations;
    }
    
    @Data
    public static class ConsultationPrenataleDetail {
        private Long id;
        private LocalDate datePrevue;
        private LocalDate dateRealisee;
        private String statut;
        private Double poids;
        private String tensionArterielle;
        private Double hauteurUterine;
        private String notes;
    }
    
    @Data
    public static class ConsultationPostnataleDetail {
        private Long id;
        private String type;
        private LocalDate datePrevue;
        private LocalDate dateRealisee;
        private String statut;
        private String notesMere;
        private String notesNouveauNe;
    }
}

