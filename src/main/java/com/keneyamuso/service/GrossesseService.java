package com.keneyamuso.service;

import com.keneyamuso.dto.request.GrossesseRequest;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.ConsultationPrenatale;
import com.keneyamuso.model.entity.ConsultationPostnatale;
import com.keneyamuso.model.entity.Grossesse;
import com.keneyamuso.model.entity.Patiente;
import com.keneyamuso.model.enums.StatutConsultation;
import com.keneyamuso.model.enums.StatutGrossesse;
import com.keneyamuso.repository.ConsultationPostnataleRepository;
import com.keneyamuso.repository.ConsultationPrenataleRepository;
import com.keneyamuso.repository.GrossesseRepository;
import com.keneyamuso.repository.PatienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service de gestion des grossesses.
 * 
 * Ce service gère le cycle de vie complet des grossesses dans l'application :
 * - Création d'une nouvelle grossesse avec calcul de la DPA
 * - Suivi de l'évolution de la grossesse
 * - Mise à jour des informations
 * - Clôture de la grossesse après l'accouchement
 * 
 * Chaque grossesse est liée à une patiente et peut avoir plusieurs consultations prénatales.
 * 
 * @author KènèyaMuso Team
 * @version 1.0
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
public class GrossesseService {

    private final GrossesseRepository grossesseRepository;
    private final PatienteRepository patienteRepository;
    private final ConsultationPrenataleRepository consultationPrenataleRepository;
    private final ConsultationPostnataleService consultationPostnataleService;

    /**
     * Crée une nouvelle grossesse pour une patiente avec génération automatique du calendrier CPN.
     * 
     * Cette méthode :
     * 1. Calcule la DPA (Date Prévue d'Accouchement) = LMP + 280 jours (40 semaines)
     * 2. Crée la grossesse avec statut EN_COURS
     * 3. Génère automatiquement 4 consultations prénatales (CPN) selon le calendrier OMS :
     *    - CPN1 : 12 semaines après LMP (1er trimestre)
     *    - CPN2 : 24 semaines après LMP (2e trimestre)
     *    - CPN3 : 32 semaines après LMP (3e trimestre)
     *    - CPN4 : 36 semaines après LMP (fin de grossesse)
     * 
     * Les rappels automatiques seront envoyés 24h avant chaque CPN.
     * 
     * @param request Les informations de la grossesse (LMP, ID patiente)
     * @return La grossesse créée avec son identifiant et ses CPN
     * @throws ResourceNotFoundException si la patiente n'existe pas
     */
    @Transactional
    public Grossesse createGrossesse(GrossesseRequest request) {
        Patiente patiente = patienteRepository.findById(request.getPatienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "id", request.getPatienteId()));

        // Calcul automatique de la DPA (Date Prévue d'Accouchement)
        // DPA = LMP + 280 jours (40 semaines selon la règle de Naegele)
        LocalDate lmp = request.getDateDernieresMenstruations();
        LocalDate dpa = lmp.plusDays(280);

        // Créer la grossesse
        Grossesse grossesse = new Grossesse();
        grossesse.setDateDebut(lmp);
        grossesse.setDatePrevueAccouchement(dpa);
        grossesse.setStatut(StatutGrossesse.EN_COURS);
        grossesse.setPatiente(patiente);

        grossesse = grossesseRepository.save(grossesse);

        // Générer automatiquement les 4 CPN selon le calendrier recommandé
        genererConsultationsPrenatales(grossesse, lmp);

        return grossesse;
    }

    /**
     * Génère automatiquement les 4 consultations prénatales pour une grossesse.
     * 
     * @param grossesse La grossesse pour laquelle créer les CPN
     * @param lmp La date de dernière menstruation
     */
    private void genererConsultationsPrenatales(Grossesse grossesse, LocalDate lmp) {
        // CPN1 : 1er trimestre (12 semaines = 84 jours)
        creerCPN(grossesse, lmp.plusWeeks(12), "CPN1 - Premier trimestre");

        // CPN2 : 2e trimestre (24 semaines = 168 jours)
        creerCPN(grossesse, lmp.plusWeeks(24), "CPN2 - Deuxième trimestre");

        // CPN3 : 3e trimestre (32 semaines = 224 jours)
        creerCPN(grossesse, lmp.plusWeeks(32), "CPN3 - Troisième trimestre");

        // CPN4 : Fin de grossesse (36 semaines = 252 jours)
        creerCPN(grossesse, lmp.plusWeeks(36), "CPN4 - Préparation à l'accouchement");
    }

    /**
     * Crée une consultation prénatale automatique.
     */
    private void creerCPN(Grossesse grossesse, LocalDate datePrevue, String notes) {
        ConsultationPrenatale cpn = new ConsultationPrenatale();
        cpn.setGrossesse(grossesse);
        cpn.setDatePrevue(datePrevue);
        cpn.setStatut(StatutConsultation.A_VENIR);
        cpn.setNotes(notes);
        consultationPrenataleRepository.save(cpn);
    }

    /**
     * Récupère une grossesse par son identifiant.
     * 
     * Vérifie également que si la grossesse est EN_COURS, elle a ses CPN générées automatiquement
     * si elles n'existent pas encore.
     * 
     * @param id L'identifiant unique de la grossesse
     * @return La grossesse trouvée
     * @throws ResourceNotFoundException si la grossesse n'existe pas
     */
    @Transactional
    public Grossesse getGrossesseById(Long id) {
        Grossesse grossesse = grossesseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grossesse", "id", id));
        
        // Si la grossesse est EN_COURS, vérifier et générer les CPN si elles n'existent pas
        if (grossesse.getStatut() == StatutGrossesse.EN_COURS) {
            List<ConsultationPrenatale> existingCPN = consultationPrenataleRepository
                    .findByGrossesseId(grossesse.getId());
            
            // Si aucune CPN n'existe, les générer automatiquement
            if (existingCPN == null || existingCPN.isEmpty()) {
                if (grossesse.getDateDebut() != null) {
                    genererConsultationsPrenatales(grossesse, grossesse.getDateDebut());
                }
            }
        }
        
        return grossesse;
    }

    /**
     * Récupère toutes les grossesses d'une patiente.
     * 
     * Cette méthode retourne l'historique complet des grossesses (en cours et terminées)
     * pour une patiente donnée, triées par date de création.
     * 
     * Vérifie également que les grossesses EN_COURS ont leurs CPN générées automatiquement
     * si elles n'existent pas encore.
     * 
     * @param patienteId L'identifiant de la patiente
     * @return La liste des grossesses de la patiente (peut être vide)
     */
    @Transactional
    public List<Grossesse> getGrossessesByPatiente(Long patienteId) {
        List<Grossesse> grossesses = grossesseRepository.findByPatienteId(patienteId);
        
        // Pour chaque grossesse EN_COURS, vérifier et générer les CPN si elles n'existent pas
        for (Grossesse grossesse : grossesses) {
            if (grossesse.getStatut() == StatutGrossesse.EN_COURS) {
                List<ConsultationPrenatale> existingCPN = consultationPrenataleRepository
                        .findByGrossesseId(grossesse.getId());
                
                // Si aucune CPN n'existe, les générer automatiquement
                if (existingCPN == null || existingCPN.isEmpty()) {
                    if (grossesse.getDateDebut() != null) {
                        genererConsultationsPrenatales(grossesse, grossesse.getDateDebut());
                    }
                }
            }
        }
        
        return grossesses;
    }

    /**
     * Récupère toutes les grossesses de l'application.
     * 
     * Cette méthode est réservée aux médecins et administrateurs pour
     * avoir une vue d'ensemble de toutes les grossesses en cours de suivi.
     * 
     * @return La liste de toutes les grossesses
     */
    @Transactional(readOnly = true)
    public List<Grossesse> getAllGrossesses() {
        return grossesseRepository.findAll();
    }

    /**
     * Met à jour les informations d'une grossesse existante.
     * 
     * Permet de modifier la LMP, ce qui recalcule automatiquement la DPA.
     * Le statut de la grossesse n'est pas modifié par cette méthode.
     * 
     * @param id L'identifiant de la grossesse à modifier
     * @param request Les nouvelles informations de la grossesse
     * @return La grossesse mise à jour
     * @throws ResourceNotFoundException si la grossesse n'existe pas
     */
    @Transactional
    public Grossesse updateGrossesse(Long id, GrossesseRequest request) {
        Grossesse grossesse = getGrossesseById(id);
        
        // Recalculer la DPA si la LMP change
        LocalDate lmp = request.getDateDernieresMenstruations();
        LocalDate dpa = lmp.plusDays(280);
        
        grossesse.setDateDebut(lmp);
        grossesse.setDatePrevueAccouchement(dpa);

        return grossesseRepository.save(grossesse);
    }

    /**
     * Marque une grossesse comme terminée après l'accouchement et génère les CPoN.
     * 
     * Cette méthode :
     * 1. Change le statut de la grossesse à TERMINEE
     * 2. Génère automatiquement 3 consultations postnatales (CPoN) :
     *    - CPoN J+3 : 3 jours après l'accouchement
     *    - CPoN J+7 : 7 jours après l'accouchement
     *    - CPoN 6e semaine : 42 jours après l'accouchement
     * 
     * Les rappels automatiques seront envoyés 24h avant chaque CPoN.
     * 
     * @param id L'identifiant de la grossesse à terminer
     * @throws ResourceNotFoundException si la grossesse n'existe pas
     */
    @Transactional
    public void terminerGrossesse(Long id) {
        Grossesse grossesse = getGrossesseById(id);
        grossesse.setStatut(StatutGrossesse.TERMINEE);
        grossesseRepository.save(grossesse);

        // Générer automatiquement les 3 consultations postnatales
        LocalDate dateAccouchement = LocalDate.now(); // Date du jour = date d'accouchement
        consultationPostnataleService.genererConsultationsPostnatales(grossesse.getPatiente(), dateAccouchement);
    }

    /**
     * Supprime une grossesse de la base de données.
     * 
     * ⚠️ ATTENTION : Cette opération supprime également toutes les consultations
     * prénatales associées (cascade). À utiliser avec précaution.
     * Réservé aux médecins et administrateurs.
     * 
     * @param id L'identifiant de la grossesse à supprimer
     * @throws ResourceNotFoundException si la grossesse n'existe pas
     */
    @Transactional
    public void deleteGrossesse(Long id) {
        if (!grossesseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Grossesse", "id", id);
        }
        grossesseRepository.deleteById(id);
    }
}

