package com.keneyamuso.service;

import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.*;
import com.keneyamuso.model.enums.StatutConsultation;
import com.keneyamuso.model.enums.StatutRappel;
import com.keneyamuso.model.enums.StatutVaccination;
import com.keneyamuso.model.enums.TypeRappel;
import com.keneyamuso.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour la gestion des rappels/notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RappelService {

    private final RappelRepository rappelRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ConsultationPrenataleRepository consultationPrenataleRepository;
    private final ConsultationPostnataleRepository consultationPostnataleRepository;
    private final VaccinationRepository vaccinationRepository;

    /**
     * Récupère tous les rappels d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Rappel> getRappelsByUtilisateurId(Long utilisateurId) {
        return rappelRepository.findByUtilisateurId(utilisateurId);
    }

    /**
     * Récupère les rappels d'un utilisateur avec un statut spécifique
     */
    @Transactional(readOnly = true)
    public List<Rappel> getRappelsByUtilisateurIdAndStatut(Long utilisateurId, StatutRappel statut) {
        return rappelRepository.findByUtilisateurIdAndStatut(utilisateurId, statut);
    }

    /**
     * Marque un rappel comme lu
     */
    @Transactional
    public Rappel marquerCommeLu(Long rappelId) {
        Rappel rappel = rappelRepository.findById(rappelId)
                .orElseThrow(() -> new ResourceNotFoundException("Rappel", "id", rappelId));
        rappel.setStatut(StatutRappel.LU);
        return rappelRepository.save(rappel);
    }

    /**
     * Convertit un Rappel en format JSON pour le frontend (NotificationItem)
     * 
     * Le frontend Flutter attend un format spécifique que nous devons mapper.
     */
    public Map<String, Object> rappelToNotificationMap(Rappel rappel) {
        Map<String, Object> notification = new HashMap<>();
        
        // Champs de base
        notification.put("id", rappel.getId());
        notification.put("message", rappel.getMessage());
        notification.put("dateCreation", rappel.getDateEnvoi());
        
        // Mapper le type de rappel vers le type de notification frontend
        String typeNotification = mapTypeRappelToNotificationType(rappel.getType());
        notification.put("type", typeNotification);
        
        // Mapper le statut
        String statutNotification = mapStatutRappelToNotificationStatus(rappel.getStatut());
        notification.put("statut", statutNotification);
        
        // Priorité basée sur le type
        String priorite = determinePriorite(rappel.getType());
        notification.put("priorite", priorite);
        
        // Titre basé sur le type
        String titre = determineTitre(rappel.getType());
        notification.put("titre", titre);
        
        // Informations sur l'utilisateur destinataire
        if (rappel.getUtilisateur() != null) {
            Utilisateur utilisateur = rappel.getUtilisateur();
            // Si c'est une patiente, on stocke l'ID
            if ("PATIENTE".equals(utilisateur.getRole())) {
                notification.put("patienteId", utilisateur.getId());
            }
            // Si c'est un médecin
            if ("MEDECIN".equals(utilisateur.getRole())) {
                notification.put("medecinId", utilisateur.getId());
            }
        }
        
        return notification;
    }

    /**
     * Détermine le titre selon le type de rappel
     */
    private String determineTitre(TypeRappel type) {
        switch (type) {
            case CPN:
                return "Rappel Consultation Prénatale";
            case CPON:
                return "Rappel Consultation Postnatale";
            case VACCINATION:
                return "Rappel Vaccination";
            case CONSEIL:
                return "Conseil Santé";
            default:
                return "Notification";
        }
    }

    /**
     * Détermine la priorité selon le type de rappel
     */
    private String determinePriorite(TypeRappel type) {
        switch (type) {
            case CPN:
            case CPON:
                return "ELEVEE";
            case VACCINATION:
                return "NORMALE";
            case CONSEIL:
                return "FAIBLE";
            default:
                return "NORMALE";
        }
    }

    /**
     * Mappe TypeRappel (backend) vers NotificationType (frontend)
     */
    private String mapTypeRappelToNotificationType(TypeRappel typeRappel) {
        switch (typeRappel) {
            case CPN:
            case CPON:
                return "RAPPEL_CONSULTATION";
            case VACCINATION:
                return "RAPPEL_VACCINATION";
            case CONSEIL:
                return "CONSEIL";
            default:
                return "AUTRE";
        }
    }

    /**
     * Mappe StatutRappel (backend) vers NotificationStatus (frontend)
     */
    private String mapStatutRappelToNotificationStatus(StatutRappel statutRappel) {
        switch (statutRappel) {
            case ENVOYE:
                return "NON_LUE";
            case LU:
                return "LUE";
            case CONFIRME:
                return "TRAITEE";
            default:
                return "NON_LUE";
        }
    }

    // =====================================================================
    // GÉNÉRATION AUTOMATIQUE DES RAPPELS
    // =====================================================================

    /**
     * Crée un rappel automatique pour une consultation prénatale
     * 
     * Le rappel est envoyé 1 jour avant la date prévue de la consultation
     */
    @Transactional
    public Rappel creerRappelCPN(ConsultationPrenatale consultation) {
        LocalDate datePrevue = consultation.getDatePrevue();
        LocalDateTime dateEnvoi = datePrevue.minusDays(1).atTime(9, 0); // Rappel à 9h le jour précédent
        
        Rappel rappel = new Rappel();
        rappel.setMessage(String.format(
            "Rappel : Vous avez une consultation prénatale prévue demain, le %s. N'oubliez pas votre carnet de suivi.",
            datePrevue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ));
        rappel.setDateEnvoi(dateEnvoi);
        rappel.setType(TypeRappel.CPN);
        rappel.setStatut(StatutRappel.ENVOYE);
        rappel.setUtilisateur(consultation.getGrossesse().getPatiente());
        rappel.setConsultationPrenatale(consultation);
        
        log.info("Création rappel CPN pour patiente {} - Date: {}", 
                consultation.getGrossesse().getPatiente().getId(), datePrevue);
        
        return rappelRepository.save(rappel);
    }

    /**
     * Crée un rappel automatique pour une consultation postnatale
     * 
     * Le rappel est envoyé 1 jour avant la date prévue de la consultation
     */
    @Transactional
    public Rappel creerRappelCPON(ConsultationPostnatale consultation) {
        LocalDate datePrevue = consultation.getDatePrevue();
        LocalDateTime dateEnvoi = datePrevue.minusDays(1).atTime(9, 0); // Rappel à 9h le jour précédent
        
        String typeConsult = determinerTypeConsultationPostnatale(consultation.getType());
        
        Rappel rappel = new Rappel();
        rappel.setMessage(String.format(
            "Rappel : Consultation postnatale %s prévue demain, le %s. Prenez soin de vous et de votre bébé.",
            typeConsult,
            datePrevue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ));
        rappel.setDateEnvoi(dateEnvoi);
        rappel.setType(TypeRappel.CPON);
        rappel.setStatut(StatutRappel.ENVOYE);
        rappel.setUtilisateur(consultation.getPatiente());
        rappel.setConsultationPostnatale(consultation);
        
        log.info("Création rappel CPON pour patiente {} - Date: {}", 
                consultation.getPatiente().getId(), datePrevue);
        
        return rappelRepository.save(rappel);
    }

    /**
     * Crée un rappel automatique pour une vaccination
     * 
     * Le rappel est envoyé 2 jours avant la date prévue de la vaccination
     */
    @Transactional
    public Rappel creerRappelVaccination(Vaccination vaccination) {
        LocalDate datePrevue = vaccination.getDatePrevue();
        LocalDateTime dateEnvoi = datePrevue.minusDays(2).atTime(9, 0); // Rappel 2 jours avant à 9h
        
        Enfant enfant = vaccination.getEnfant();
        
        Rappel rappel = new Rappel();
        rappel.setMessage(String.format(
            "Rappel : Vaccination de %s (%s) prévue le %s. Pensez à apporter le carnet de santé de votre enfant.",
            enfant.getPrenom(),
            vaccination.getNomVaccin(),
            datePrevue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ));
        rappel.setDateEnvoi(dateEnvoi);
        rappel.setType(TypeRappel.VACCINATION);
        rappel.setStatut(StatutRappel.ENVOYE);
        rappel.setUtilisateur(enfant.getPatiente());
        rappel.setVaccination(vaccination);
        
        log.info("Création rappel Vaccination pour patiente {} - Enfant: {} - Date: {}", 
                enfant.getPatiente().getId(), enfant.getPrenom(), datePrevue);
        
        return rappelRepository.save(rappel);
    }

    /**
     * Détermine le type de consultation postnatale en français
     */
    private String determinerTypeConsultationPostnatale(String type) {
        if (type == null) return "";
        
        return switch (type.toUpperCase()) {
            case "JOUR_3", "J+3" -> "J+3";
            case "JOUR_7", "J+7" -> "J+7";
            case "SEMAINE_6", "6E_SEMAINE" -> "6e semaine";
            default -> type;
        };
    }

    // =====================================================================
    // ENVOI QUOTIDIEN AUTOMATIQUE DES RAPPELS
    // =====================================================================

    /**
     * Tâche planifiée qui s'exécute tous les jours à 8h00 du matin
     * pour envoyer les rappels des consultations et vaccinations à venir
     * 
     * Cette méthode vérifie :
     * - Les CPN prévues dans les 1 jours
     * - Les CPON prévues dans les 1 jours
     * - Les vaccinations prévues dans les 2 jours
     */
    @Scheduled(cron = "0 0 8 * * *") // Tous les jours à 8h00
    @Transactional
    public void envoyerRappelsQuotidiens() {
        log.info("=== DÉBUT ENVOI RAPPELS QUOTIDIENS ===");
        
        LocalDate aujourdhui = LocalDate.now();
        LocalDate demain = aujourdhui.plusDays(1);
        LocalDate dans2Jours = aujourdhui.plusDays(2);
        
        int compteurCPN = 0;
        int compteurCPON = 0;
        int compteurVaccinations = 0;
        
        try {
            // 1. Rappels CPN (demain)
            List<ConsultationPrenatale> cpnDemain = consultationPrenataleRepository
                    .findByDatePrevueAndStatut(demain, StatutConsultation.A_VENIR);
            
            for (ConsultationPrenatale cpn : cpnDemain) {
                // Vérifier qu'un rappel n'a pas déjà été créé
                boolean rappelExiste = rappelRepository
                        .existsByConsultationPrenataleIdAndType(cpn.getId(), TypeRappel.CPN);
                
                if (!rappelExiste) {
                    creerRappelCPN(cpn);
                    compteurCPN++;
                }
            }
            
            // 2. Rappels CPON (demain)
            List<ConsultationPostnatale> cponDemain = consultationPostnataleRepository
                    .findByDatePrevueAndStatut(demain, StatutConsultation.A_VENIR);
            
            for (ConsultationPostnatale cpon : cponDemain) {
                boolean rappelExiste = rappelRepository
                        .existsByConsultationPostnataleIdAndType(cpon.getId(), TypeRappel.CPON);
                
                if (!rappelExiste) {
                    creerRappelCPON(cpon);
                    compteurCPON++;
                }
            }
            
            // 3. Rappels Vaccinations (dans 2 jours)
            List<Vaccination> vaccinationsDans2Jours = vaccinationRepository
                    .findByDatePrevueAndStatut(dans2Jours, StatutVaccination.A_FAIRE);
            
            for (Vaccination vaccination : vaccinationsDans2Jours) {
                boolean rappelExiste = rappelRepository
                        .existsByVaccinationIdAndType(vaccination.getId(), TypeRappel.VACCINATION);
                
                if (!rappelExiste) {
                    creerRappelVaccination(vaccination);
                    compteurVaccinations++;
                }
            }
            
            log.info("Rappels envoyés - CPN: {}, CPON: {}, Vaccinations: {}", 
                    compteurCPN, compteurCPON, compteurVaccinations);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi des rappels quotidiens", e);
        }
        
        log.info("=== FIN ENVOI RAPPELS QUOTIDIENS ===");
    }

    /**
     * Méthode manuelle pour forcer l'envoi des rappels (utile pour les tests)
     */
    @Transactional
    public Map<String, Integer> envoyerRappelsManuellement() {
        envoyerRappelsQuotidiens();
        
        Map<String, Integer> stats = new HashMap<>();
        LocalDate demain = LocalDate.now().plusDays(1);
        LocalDate dans2Jours = LocalDate.now().plusDays(2);
        
        stats.put("cpnDemain", consultationPrenataleRepository
                .findByDatePrevueAndStatut(demain, StatutConsultation.A_VENIR).size());
        stats.put("cponDemain", consultationPostnataleRepository
                .findByDatePrevueAndStatut(demain, StatutConsultation.A_VENIR).size());
        stats.put("vaccinationsDans2Jours", vaccinationRepository
                .findByDatePrevueAndStatut(dans2Jours, StatutVaccination.A_FAIRE).size());
        
        return stats;
    }

    /**
     * Confirmer une consultation/vaccination (la patiente vient bien)
     * Marque le rappel comme traité et la consultation comme réalisée
     */
    @Transactional
    public void confirmerRappel(Long rappelId) {
        Rappel rappel = rappelRepository.findById(rappelId)
                .orElseThrow(() -> new ResourceNotFoundException("Rappel", "id", rappelId));
        
        // Marquer le rappel comme traité
        rappel.setStatut(StatutRappel.LU);
        rappelRepository.save(rappel);
        
        // Marquer la consultation/vaccination comme réalisée
        LocalDate dateRealisee = LocalDate.now();
        
        if (rappel.getConsultationPrenatale() != null) {
            ConsultationPrenatale cpn = rappel.getConsultationPrenatale();
            cpn.setStatut(StatutConsultation.REALISEE);
            cpn.setDateRealisee(dateRealisee);
            consultationPrenataleRepository.save(cpn);
            log.info("CPN {} marquée comme réalisée", cpn.getId());
        } 
        else if (rappel.getConsultationPostnatale() != null) {
            ConsultationPostnatale cpon = rappel.getConsultationPostnatale();
            cpon.setStatut(StatutConsultation.REALISEE);
            cpon.setDateRealisee(dateRealisee);
            consultationPostnataleRepository.save(cpon);
            log.info("CPON {} marquée comme réalisée", cpon.getId());
        }
        else if (rappel.getVaccination() != null) {
            Vaccination vaccination = rappel.getVaccination();
            vaccination.setStatut(StatutVaccination.FAIT);
            vaccination.setDateRealisee(dateRealisee);
            vaccinationRepository.save(vaccination);
            log.info("Vaccination {} marquée comme faite", vaccination.getId());
        }
    }

    /**
     * Reprogrammer une consultation/vaccination à une nouvelle date
     * Marque le rappel comme traité et met à jour la date prévue
     */
    @Transactional
    public void reprogrammerRappel(Long rappelId, LocalDate nouvelleDate) {
        Rappel rappel = rappelRepository.findById(rappelId)
                .orElseThrow(() -> new ResourceNotFoundException("Rappel", "id", rappelId));
        
        // Marquer le rappel actuel comme traité
        rappel.setStatut(StatutRappel.LU);
        rappelRepository.save(rappel);
        
        // Mettre à jour la date prévue de la consultation/vaccination
        if (rappel.getConsultationPrenatale() != null) {
            ConsultationPrenatale cpn = rappel.getConsultationPrenatale();
            cpn.setDatePrevue(nouvelleDate);
            consultationPrenataleRepository.save(cpn);
            log.info("CPN {} reprogrammée au {}", cpn.getId(), nouvelleDate);
            
            // Créer un nouveau rappel pour la nouvelle date
            if (nouvelleDate.isAfter(LocalDate.now())) {
                creerRappelCPN(cpn);
            }
        } 
        else if (rappel.getConsultationPostnatale() != null) {
            ConsultationPostnatale cpon = rappel.getConsultationPostnatale();
            cpon.setDatePrevue(nouvelleDate);
            consultationPostnataleRepository.save(cpon);
            log.info("CPON {} reprogrammée au {}", cpon.getId(), nouvelleDate);
            
            // Créer un nouveau rappel pour la nouvelle date
            if (nouvelleDate.isAfter(LocalDate.now())) {
                creerRappelCPON(cpon);
            }
        }
        else if (rappel.getVaccination() != null) {
            Vaccination vaccination = rappel.getVaccination();
            vaccination.setDatePrevue(nouvelleDate);
            vaccinationRepository.save(vaccination);
            log.info("Vaccination {} reprogrammée au {}", vaccination.getId(), nouvelleDate);
            
            // Créer un nouveau rappel pour la nouvelle date
            if (nouvelleDate.isAfter(LocalDate.now())) {
                creerRappelVaccination(vaccination);
            }
        }
    }
}

