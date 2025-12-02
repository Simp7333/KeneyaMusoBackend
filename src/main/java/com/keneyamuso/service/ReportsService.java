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
    public ReportsStatsDto getReportsStats(String period, Integer year) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = now;
        int targetYear = year != null ? year : now.getYear();

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

        // Nouvelles statistiques CPN/CPON
        ReportsStatsDto.RepartitionCpnCponDto repartitionCpnCpon = calculateRepartitionCpnCpon();
        List<ReportsStatsDto.TrimestreData> cpnParTrimestre = calculateCpnParTrimestre();
        List<ReportsStatsDto.MonthlyDetailData> monthlyDetailData = calculateMonthlyDetailData(targetYear);

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
                .repartitionCpnCpon(repartitionCpnCpon)
                .cpnParTrimestre(cpnParTrimestre)
                .monthlyDetailData(monthlyDetailData)
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
     * Calcule la répartition CPN/CPON (Réalisées vs Manquées)
     */
    private ReportsStatsDto.RepartitionCpnCponDto calculateRepartitionCpnCpon() {
        // Compter toutes les consultations CPN
        List<ConsultationPrenatale> allCpn = consultationPrenataleRepository.findAll();
        long cpnRealisees = allCpn.stream()
                .filter(c -> c.getStatut() == StatutConsultation.REALISEE)
                .count();
        long cpnManquees = allCpn.stream()
                .filter(c -> c.getStatut() == StatutConsultation.MANQUEE)
                .count();

        // Compter toutes les consultations CPON
        List<ConsultationPostnatale> allCponList = consultationPostnataleRepository.findAll();
        long cponRealisees = allCponList.stream()
                .filter(c -> c.getStatut() == StatutConsultation.REALISEE)
                .count();
        long cponManquees = allCponList.stream()
                .filter(c -> c.getStatut() == StatutConsultation.MANQUEE)
                .count();

        return ReportsStatsDto.RepartitionCpnCponDto.builder()
                .cpnRealisees(cpnRealisees)
                .cpnManquees(cpnManquees)
                .cponRealisees(cponRealisees)
                .cponManquees(cponManquees)
                .build();
    }

    /**
     * Calcule les CPN par trimestre de grossesse
     */
    private List<ReportsStatsDto.TrimestreData> calculateCpnParTrimestre() {
        List<ReportsStatsDto.TrimestreData> trimestreData = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // Calculer pour chaque trimestre
        long[] cpnRealisees = new long[3];
        long[] cpnManquees = new long[3];

        List<ConsultationPrenatale> allCpn = consultationPrenataleRepository.findAll();

        for (ConsultationPrenatale cpn : allCpn) {
            Grossesse grossesse = cpn.getGrossesse();
            if (grossesse == null || grossesse.getDateDebut() == null) continue;

            // Calculer le trimestre basé sur la date de début de grossesse et la date de consultation
            LocalDate dateDebutGrossesse = grossesse.getDateDebut();
            LocalDate dateConsultation = cpn.getDatePrevue() != null ? cpn.getDatePrevue() : 
                                         (cpn.getDateRealisee() != null ? cpn.getDateRealisee() : now);

            // Calculer les semaines de grossesse
            long semainesDeGrossesse = java.time.temporal.ChronoUnit.WEEKS.between(dateDebutGrossesse, dateConsultation);

            int trimestre;
            if (semainesDeGrossesse <= 13) {
                trimestre = 0; // 1er trimestre (0-13 semaines)
            } else if (semainesDeGrossesse <= 27) {
                trimestre = 1; // 2ème trimestre (14-27 semaines)
            } else {
                trimestre = 2; // 3ème trimestre (28+ semaines)
            }

            if (cpn.getStatut() == StatutConsultation.REALISEE) {
                cpnRealisees[trimestre]++;
            } else if (cpn.getStatut() == StatutConsultation.MANQUEE) {
                cpnManquees[trimestre]++;
            }
        }

        trimestreData.add(ReportsStatsDto.TrimestreData.builder()
                .trimestre("1er Trimestre")
                .cpnRealisees(cpnRealisees[0])
                .cpnManquees(cpnManquees[0])
                .build());

        trimestreData.add(ReportsStatsDto.TrimestreData.builder()
                .trimestre("2ème Trimestre")
                .cpnRealisees(cpnRealisees[1])
                .cpnManquees(cpnManquees[1])
                .build());

        trimestreData.add(ReportsStatsDto.TrimestreData.builder()
                .trimestre("3ème Trimestre")
                .cpnRealisees(cpnRealisees[2])
                .cpnManquees(cpnManquees[2])
                .build());

        return trimestreData;
    }

    /**
     * Calcule les données mensuelles détaillées pour une année donnée
     */
    private List<ReportsStatsDto.MonthlyDetailData> calculateMonthlyDetailData(int year) {
        List<ReportsStatsDto.MonthlyDetailData> monthlyData = new ArrayList<>();
        String[] moisNoms = {"Janvier", "Fevrier", "Mars", "Avril", "Mai", "Juin",
                            "Juillet", "Aout", "Septembre", "Octobre", "Novembre", "Decembre"};

        // Récupérer toutes les consultations CPN et CPON de l'année
        LocalDate anneeDebut = LocalDate.of(year, 1, 1);
        LocalDate anneeFin = LocalDate.of(year, 12, 31);
        
        // Récupérer toutes les CPN de l'année (basées sur datePrevue OU dateRealisee)
        List<ConsultationPrenatale> allCpn = consultationPrenataleRepository.findAll().stream()
                .filter(c -> {
                    LocalDate dateRef = (c.getStatut() == StatutConsultation.REALISEE && c.getDateRealisee() != null)
                            ? c.getDateRealisee()
                            : c.getDatePrevue();
                    return dateRef != null && !dateRef.isBefore(anneeDebut) && !dateRef.isAfter(anneeFin);
                })
                .collect(Collectors.toList());
        
        System.out.println("📅 Calcul des données mensuelles pour l'année " + year);
        System.out.println("  - Total CPN trouvées pour l'année: " + allCpn.size());
        
        // Récupérer toutes les CPON de l'année (basées sur datePrevue OU dateRealisee)
        List<ConsultationPostnatale> allCpon = consultationPostnataleRepository.findAll().stream()
                .filter(c -> {
                    LocalDate dateRef = (c.getStatut() == StatutConsultation.REALISEE && c.getDateRealisee() != null)
                            ? c.getDateRealisee()
                            : c.getDatePrevue();
                    return dateRef != null && !dateRef.isBefore(anneeDebut) && !dateRef.isAfter(anneeFin);
                })
                .collect(Collectors.toList());
        
        System.out.println("  - Total CPON trouvées pour l'année: " + allCpon.size());

        for (int mois = 1; mois <= 12; mois++) {
            LocalDate moisDebut = LocalDate.of(year, mois, 1);
            LocalDate moisFin = moisDebut.withDayOfMonth(moisDebut.lengthOfMonth());

            // CPN réalisées et manquées pour ce mois
            // Pour les réalisées : utiliser dateRealisee si disponible, sinon datePrevue
            // Pour les manquées : utiliser datePrevue
            long cpnRealisees = allCpn.stream()
                    .filter(c -> {
                        if (c.getStatut() == StatutConsultation.REALISEE) {
                            LocalDate dateRef = (c.getDateRealisee() != null) ? c.getDateRealisee() : c.getDatePrevue();
                            return dateRef != null && !dateRef.isBefore(moisDebut) && !dateRef.isAfter(moisFin);
                        }
                        return false;
                    })
                    .count();
            
            long cpnManquees = allCpn.stream()
                    .filter(c -> {
                        if (c.getStatut() == StatutConsultation.MANQUEE) {
                            return c.getDatePrevue() != null && 
                                   !c.getDatePrevue().isBefore(moisDebut) && 
                                   !c.getDatePrevue().isAfter(moisFin);
                        }
                        return false;
                    })
                    .count();

            // CPON réalisées et manquées pour ce mois
            long cponRealisees = allCpon.stream()
                    .filter(c -> {
                        if (c.getStatut() == StatutConsultation.REALISEE) {
                            LocalDate dateRef = (c.getDateRealisee() != null) ? c.getDateRealisee() : c.getDatePrevue();
                            return dateRef != null && !dateRef.isBefore(moisDebut) && !dateRef.isAfter(moisFin);
                        }
                        return false;
                    })
                    .count();
            
            long cponManquees = allCpon.stream()
                    .filter(c -> {
                        if (c.getStatut() == StatutConsultation.MANQUEE) {
                            return c.getDatePrevue() != null && 
                                   !c.getDatePrevue().isBefore(moisDebut) && 
                                   !c.getDatePrevue().isAfter(moisFin);
                        }
                        return false;
                    })
                    .count();

            // Calculer le taux de réussite
            long totalCpn = cpnRealisees + cpnManquees;
            long totalCpon = cponRealisees + cponManquees;
            long totalRealisees = cpnRealisees + cponRealisees;
            long total = totalCpn + totalCpon;

            String tauxReussite = "0,0";
            if (total > 0) {
                double taux = (totalRealisees * 100.0 / total);
                tauxReussite = String.format("%.1f", taux).replace(".", ",");
            }

            monthlyData.add(ReportsStatsDto.MonthlyDetailData.builder()
                    .nom(moisNoms[mois - 1])
                    .cpnRealisees(cpnRealisees)
                    .cpnManquees(cpnManquees)
                    .cponRealisees(cponRealisees)
                    .cponManquees(cponManquees)
                    .tauxReussite(tauxReussite)
                    .build());
            
            // Log pour déboguer
            if (cpnRealisees > 0 || cpnManquees > 0 || cponRealisees > 0 || cponManquees > 0) {
                System.out.println("  - " + moisNoms[mois - 1] + ": CPN Réalisées=" + cpnRealisees + 
                                   ", CPN Manquées=" + cpnManquees + 
                                   ", CPON Réalisées=" + cponRealisees + 
                                   ", CPON Manquées=" + cponManquees);
            }
        }

        System.out.println("✅ Données mensuelles calculées: " + monthlyData.size() + " mois");
        return monthlyData;
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

