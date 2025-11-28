package com.keneyamuso.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO pour les statistiques de rapports
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportsStatsDto {
    
    // Statistiques principales
    private Long totalPatientes;
    private Long totalConsultations;
    private Long totalAccouchements;
    private Double tauxSuivi;
    
    // Tendances (variations)
    private Long nouvellesPatientesCeMois;
    private Long nouvellesConsultationsCeMois;
    private Long nouveauxAccouchementsCetteSemaine;
    private Double variationTauxSuivi;
    
    // Données pour les graphiques
    private List<MonthlyData> evolutionInscriptions; // Évolution mensuelle des inscriptions
    private List<WeeklyConsultationData> consultationsHebdomadaires; // Consultations par jour de la semaine
    private Map<String, Long> repartitionParStatut; // Prénatale, Postnatale, Terminé
    
    // Liste des patientes récentes
    private List<PatienteReportDto> patientesRecentes;
    
    // Consultations récentes
    private List<ConsultationReportDto> consultationsRecentes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyData {
        private String mois; // "Jan", "Fév", etc.
        private Long nombre;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyConsultationData {
        private String jour; // "Lun", "Mar", etc.
        private Long cpn;
        private Long cpon;
        private Long urgences;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatienteReportDto {
        private Long id;
        private String nom;
        private String prenom;
        private Integer age;
        private String dateInscription;
        private String statut; // "prenatale" ou "postnatale"
        private Long nombreConsultations;
        private String prochainRDV; // Optionnel
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultationReportDto {
        private String date;
        private String patiente; // Nom complet
        private String medecin; // Nom complet
        private String type; // "CPN", "CPON", etc.
        private String statut; // "completee", "annulee", "en_attente"
    }
}

