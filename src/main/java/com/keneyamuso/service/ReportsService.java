package com.keneyamuso.service;

import com.keneyamuso.dto.response.ReportsStatsDto;
import com.keneyamuso.model.entity.*;
import com.keneyamuso.model.enums.StatutConsultation;
import com.keneyamuso.model.enums.StatutGrossesse;
import com.keneyamuso.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour générer les statistiques de rapports
 */
@Service
@RequiredArgsConstructor
public class ReportsService {

    private final PatienteRepository patienteRepository;
    private final ConsultationPrenataleRepository consultationPrenataleRepository;
    private final ConsultationPostnataleRepository consultationPostnataleRepository;
    private final GrossesseRepository grossesseRepository;
    private final EnfantRepository enfantRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Récupère toutes les statistiques pour les rapports
     */
    @Transactional(readOnly = true)
    public ReportsStatsDto getReportsStats(String period) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = now;

        // Déterminer la période
        switch (period != null ? period.toLowerCase() : "month") {
            case "week":
                startDate = now.minusWeeks(1);
                break;
            case "quarter":
                startDate = now.minusMonths(3);
                break;
            case "year":
                startDate = now.minusYears(1);
                break;
            default: // month
                startDate = now.minusMonths(1);
        }

        // Statistiques principales
        long totalPatientes = patienteRepository.count();
        long totalConsultations = consultationPrenataleRepository.count() + consultationPostnataleRepository.count();
        long totalAccouchements = grossesseRepository.findByStatut(StatutGrossesse.TERMINEE).size();
        
        // Calculer le taux de suivi (patientes avec au moins une consultation)
        long patientesAvecConsultation = patienteRepository.countDistinctPatientesWithConsultations();
        double tauxSuivi = totalPatientes > 0 
            ? (patientesAvecConsultation * 100.0 / totalPatientes) 
            : 0.0;

        // Tendances
        long nouvellesPatientesCeMois = patienteRepository.countByDateCreationBetween(
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59)
        );
        
        long nouvellesConsultationsCeMois = consultationPrenataleRepository.findConsultationsByDateRange(startDate, endDate).size() +
                                           consultationPostnataleRepository.findConsultationsByDateRange(startDate, endDate).size();
        
        LocalDate semaineDebut = now.minusDays(7);
        long nouveauxAccouchementsCetteSemaine = grossesseRepository.findGrossessesWithDPAInRange(semaineDebut, now)
            .stream()
            .filter(g -> g.getStatut() == StatutGrossesse.TERMINEE)
            .count();

        // Évolution mensuelle des inscriptions (12 derniers mois)
        List<ReportsStatsDto.MonthlyData> evolutionInscriptions = calculateMonthlyInscriptions();

        // Consultations hebdomadaires (7 derniers jours)
        List<ReportsStatsDto.WeeklyConsultationData> consultationsHebdomadaires = calculateWeeklyConsultations();

        // Répartition par statut
        Map<String, Long> repartitionParStatut = calculateRepartitionParStatut();

        // Patientes récentes (15 dernières)
        List<ReportsStatsDto.PatienteReportDto> patientesRecentes = getPatientesRecentes(15);

        // Consultations récentes (10 dernières)
        List<ReportsStatsDto.ConsultationReportDto> consultationsRecentes = getConsultationsRecentes(10);

        return ReportsStatsDto.builder()
                .totalPatientes(totalPatientes)
                .totalConsultations(totalConsultations)
                .totalAccouchements(totalAccouchements)
                .tauxSuivi(Math.round(tauxSuivi * 10.0) / 10.0) // Arrondir à 1 décimale
                .nouvellesPatientesCeMois(nouvellesPatientesCeMois)
                .nouvellesConsultationsCeMois(nouvellesConsultationsCeMois)
                .nouveauxAccouchementsCetteSemaine(nouveauxAccouchementsCetteSemaine)
                .variationTauxSuivi(0.0) // TODO: Calculer la variation
                .evolutionInscriptions(evolutionInscriptions)
                .consultationsHebdomadaires(consultationsHebdomadaires)
                .repartitionParStatut(repartitionParStatut)
                .patientesRecentes(patientesRecentes)
                .consultationsRecentes(consultationsRecentes)
                .build();
    }

    private List<ReportsStatsDto.MonthlyData> calculateMonthlyInscriptions() {
        List<ReportsStatsDto.MonthlyData> monthlyData = new ArrayList<>();
        LocalDate now = LocalDate.now();
        String[] moisNoms = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};

        for (int i = 11; i >= 0; i--) {
            LocalDate moisDebut = now.minusMonths(i).withDayOfMonth(1);
            LocalDate moisFin = moisDebut.plusMonths(1).minusDays(1);
            
            long count = patienteRepository.countByDateCreationBetween(
                moisDebut.atStartOfDay(),
                moisFin.atTime(23, 59, 59)
            );

            monthlyData.add(ReportsStatsDto.MonthlyData.builder()
                    .mois(moisNoms[moisDebut.getMonthValue() - 1])
                    .nombre(count)
                    .build());
        }

        return monthlyData;
    }

    private List<ReportsStatsDto.WeeklyConsultationData> calculateWeeklyConsultations() {
        List<ReportsStatsDto.WeeklyConsultationData> weeklyData = new ArrayList<>();
        LocalDate now = LocalDate.now();
        String[] joursNoms = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

        for (int i = 6; i >= 0; i--) {
            LocalDate jour = now.minusDays(i);
            
            // CPN
            long cpn = consultationPrenataleRepository.findConsultationsByDateRange(jour, jour).size();
            
            // CPON
            long cpon = consultationPostnataleRepository.findConsultationsByDateRange(jour, jour).size();
            
            // Urgences (consultations manquées ou à venir le jour même - logique simplifiée)
            // Note: L'énumération StatutConsultation n'a pas de valeur URGENT
            // On considère les consultations MANQUEE comme urgentes à gérer
            long urgences = consultationPrenataleRepository.findConsultationsByDateRange(jour, jour)
                .stream()
                .filter(c -> c.getStatut() == StatutConsultation.MANQUEE)
                .count() +
                consultationPostnataleRepository.findConsultationsByDateRange(jour, jour)
                .stream()
                .filter(c -> c.getStatut() == StatutConsultation.MANQUEE)
                .count();

            weeklyData.add(ReportsStatsDto.WeeklyConsultationData.builder()
                    .jour(joursNoms[jour.getDayOfWeek().getValue() - 1])
                    .cpn(cpn)
                    .cpon(cpon)
                    .urgences(urgences)
                    .build());
        }

        return weeklyData;
    }

    private Map<String, Long> calculateRepartitionParStatut() {
        Map<String, Long> repartition = new HashMap<>();
        
        // Prénatale (grossesses en cours)
        long prenatale = grossesseRepository.findByStatut(StatutGrossesse.EN_COURS).size();
        
        // Postnatale (grossesses terminées avec suivi postnatal)
        long postnatale = grossesseRepository.findByStatut(StatutGrossesse.TERMINEE)
            .stream()
            .filter(g -> {
                // Vérifier si la patiente a des enfants ou des consultations postnatales
                return !enfantRepository.findByPatiente_Id(g.getPatiente().getId()).isEmpty() ||
                       !consultationPostnataleRepository.findByPatienteId(g.getPatiente().getId()).isEmpty();
            })
            .count();
        
        // Terminé (patientes sans grossesse en cours et sans suivi actif)
        long totalPatientes = patienteRepository.count();
        long terminé = totalPatientes - prenatale - postnatale;

        repartition.put("Prénatale", prenatale);
        repartition.put("Postnatale", postnatale);
        repartition.put("Terminé", terminé > 0 ? terminé : 0L);

        return repartition;
    }

    private List<ReportsStatsDto.PatienteReportDto> getPatientesRecentes(int limit) {
        List<Patiente> patientes = patienteRepository.findAllByOrderByDateCreationDesc()
            .stream()
            .limit(limit)
            .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return patientes.stream().map(p -> {
            // Déterminer le statut
            String statut = "postnatale";
            if (p.getGrossesses() != null && !p.getGrossesses().isEmpty()) {
                boolean hasEnCours = p.getGrossesses().stream()
                    .anyMatch(g -> g.getStatut() == StatutGrossesse.EN_COURS);
                if (hasEnCours) {
                    statut = "prenatale";
                }
            }

            // Compter les consultations
            long nbConsultations = consultationPrenataleRepository.findByPatienteId(p.getId()).size() +
                                  consultationPostnataleRepository.findByPatienteId(p.getId()).size();

            // Prochain RDV (première consultation prévue non complétée)
            String prochainRDV = null;
            List<ConsultationPrenatale> cpnFutures = consultationPrenataleRepository.findConsultationsByDateRange(
                LocalDate.now(), LocalDate.now().plusMonths(1)
            ).stream()
            .filter(c -> c.getGrossesse().getPatiente().getId().equals(p.getId()))
            .filter(c -> c.getStatut() != StatutConsultation.REALISEE)
            .sorted(Comparator.comparing(ConsultationPrenatale::getDatePrevue))
            .collect(Collectors.toList());
            
            if (!cpnFutures.isEmpty()) {
                prochainRDV = cpnFutures.get(0).getDatePrevue().format(formatter);
            }

            // Calculer l'âge
            Integer age = null;
            if (p.getDateDeNaissance() != null) {
                age = LocalDate.now().getYear() - p.getDateDeNaissance().getYear();
            }

            return ReportsStatsDto.PatienteReportDto.builder()
                    .id(p.getId())
                    .nom(p.getNom())
                    .prenom(p.getPrenom())
                    .age(age)
                    .dateInscription(p.getDateCreation() != null 
                        ? p.getDateCreation().format(formatter) 
                        : "")
                    .statut(statut)
                    .nombreConsultations(nbConsultations)
                    .prochainRDV(prochainRDV)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<ReportsStatsDto.ConsultationReportDto> getConsultationsRecentes(int limit) {
        List<ReportsStatsDto.ConsultationReportDto> consultations = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Récupérer les consultations prénatales récentes
        List<ConsultationPrenatale> cpnRecentes = consultationPrenataleRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(ConsultationPrenatale::getDatePrevue).reversed())
            .limit(limit)
            .collect(Collectors.toList());

        for (ConsultationPrenatale cpn : cpnRecentes) {
            String medecinNom = "N/A";
            if (cpn.getGrossesse().getPatiente().getProfessionnelSanteAssigne() != null) {
                ProfessionnelSante medecin = cpn.getGrossesse().getPatiente().getProfessionnelSanteAssigne();
                medecinNom = "Dr. " + medecin.getPrenom() + " " + medecin.getNom();
            }

            consultations.add(ReportsStatsDto.ConsultationReportDto.builder()
                    .date(cpn.getDatePrevue().format(formatter))
                    .patiente(cpn.getGrossesse().getPatiente().getPrenom() + " " + cpn.getGrossesse().getPatiente().getNom())
                    .medecin(medecinNom)
                    .type("CPN")
                    .statut(mapStatutToFrontend(cpn.getStatut()))
                    .build());
        }

        // Récupérer les consultations postnatales récentes
        List<ConsultationPostnatale> cponRecentes = consultationPostnataleRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(ConsultationPostnatale::getDatePrevue).reversed())
            .limit(limit)
            .collect(Collectors.toList());

        for (ConsultationPostnatale cpon : cponRecentes) {
            String medecinNom = "N/A";
            if (cpon.getPatiente().getProfessionnelSanteAssigne() != null) {
                ProfessionnelSante medecin = cpon.getPatiente().getProfessionnelSanteAssigne();
                medecinNom = "Dr. " + medecin.getPrenom() + " " + medecin.getNom();
            }

            consultations.add(ReportsStatsDto.ConsultationReportDto.builder()
                    .date(cpon.getDatePrevue().format(formatter))
                    .patiente(cpon.getPatiente().getPrenom() + " " + cpon.getPatiente().getNom())
                    .medecin(medecinNom)
                    .type("CPON")
                    .statut(mapStatutToFrontend(cpon.getStatut()))
                    .build());
        }

        // Trier par date et limiter
        return consultations.stream()
            .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Mappe le statut de consultation backend vers le format attendu par le frontend
     */
    private String mapStatutToFrontend(StatutConsultation statut) {
        if (statut == null) {
            return "en_attente";
        }
        switch (statut) {
            case REALISEE:
                return "completee";
            case MANQUEE:
                return "annulee";
            case A_VENIR:
                return "en_attente";
            default:
                return "en_attente";
        }
    }
}

