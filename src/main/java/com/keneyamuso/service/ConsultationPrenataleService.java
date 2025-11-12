package com.keneyamuso.service;

import com.keneyamuso.dto.request.ConsultationPrenataleRequest;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.ConsultationPrenatale;
import com.keneyamuso.model.entity.Grossesse;
import com.keneyamuso.model.enums.StatutConsultation;
import com.keneyamuso.repository.ConsultationPrenataleRepository;
import com.keneyamuso.repository.GrossesseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service de gestion des consultations prénatales (CPN).
 * 
 * Ce service gère les consultations prénatales qui font partie du suivi de la grossesse.
 * Selon les recommandations de l'OMS, une femme enceinte doit effectuer au minimum 4 CPN :
 * - CPN1 : Avant 12 semaines de grossesse
 * - CPN2 : Entre 24-26 semaines
 * - CPN3 : À 32 semaines
 * - CPN4 : À 36 semaines
 * 
 * Chaque consultation enregistre les paramètres vitaux (poids, tension, hauteur utérine)
 * et permet le suivi de l'évolution de la grossesse.
 * 
 * @author KènèyaMuso Team
 * @version 1.0
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
public class ConsultationPrenataleService {

    private final ConsultationPrenataleRepository consultationRepository;
    private final GrossesseRepository grossesseRepository;

    /**
     * Crée une nouvelle consultation prénatale.
     * 
     * Cette méthode enregistre une CPN avec sa date prévue et éventuellement
     * les informations de la consultation si elle a déjà eu lieu (date réalisée,
     * poids, tension, hauteur utérine). Le statut est automatiquement déterminé :
     * REALISEE si dateRealisee est renseignée, A_VENIR sinon.
     * 
     * @param request Les informations de la consultation (date, mesures, notes)
     * @return La consultation créée avec son identifiant
     * @throws ResourceNotFoundException si la grossesse n'existe pas
     */
    @Transactional
    public ConsultationPrenatale createConsultation(ConsultationPrenataleRequest request) {
        Grossesse grossesse = grossesseRepository.findById(request.getGrossesseId())
                .orElseThrow(() -> new ResourceNotFoundException("Grossesse", "id", request.getGrossesseId()));

        ConsultationPrenatale consultation = new ConsultationPrenatale();
        consultation.setDatePrevue(request.getDatePrevue());
        consultation.setDateRealisee(request.getDateRealisee());
        consultation.setNotes(request.getNotes());
        consultation.setPoids(request.getPoids());
        consultation.setTensionArterielle(request.getTensionArterielle());
        consultation.setHauteurUterine(request.getHauteurUterine());
        consultation.setStatut(request.getDateRealisee() != null ? 
                StatutConsultation.REALISEE : StatutConsultation.A_VENIR);
        consultation.setGrossesse(grossesse);

        return consultationRepository.save(consultation);
    }

    /**
     * Récupère une consultation prénatale par son identifiant.
     * 
     * @param id L'identifiant unique de la consultation
     * @return La consultation trouvée
     * @throws ResourceNotFoundException si la consultation n'existe pas
     */
    @Transactional(readOnly = true)
    public ConsultationPrenatale getConsultationById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConsultationPrenatale", "id", id));
    }

    /**
     * Récupère toutes les CPN d'une grossesse spécifique.
     * 
     * Cette méthode retourne l'historique complet des consultations prénatales
     * pour une grossesse donnée, permettant de suivre l'évolution du suivi.
     * 
     * @param grossesseId L'identifiant de la grossesse
     * @return La liste des consultations de la grossesse (peut être vide)
     */
    @Transactional(readOnly = true)
    public List<ConsultationPrenatale> getConsultationsByGrossesse(Long grossesseId) {
        return consultationRepository.findByGrossesseId(grossesseId);
    }

    /**
     * Récupère toutes les CPN d'une patiente (toutes grossesses confondues).
     * 
     * Cette méthode retourne l'historique complet du suivi prénatal d'une patiente,
     * utile pour avoir une vue d'ensemble de son parcours de santé maternelle.
     * 
     * @param patienteId L'identifiant de la patiente
     * @return La liste des consultations de la patiente (peut être vide)
     */
    @Transactional(readOnly = true)
    public List<ConsultationPrenatale> getConsultationsByPatiente(Long patienteId) {
        return consultationRepository.findByPatienteId(patienteId);
    }

    /**
     * Récupère toutes les consultations prénatales de l'application.
     * 
     * Cette méthode est réservée aux médecins et administrateurs pour
     * avoir une vue d'ensemble de toutes les CPN.
     * 
     * @return La liste de toutes les consultations
     */
    @Transactional(readOnly = true)
    public List<ConsultationPrenatale> getAllConsultations() {
        return consultationRepository.findAll();
    }

    /**
     * Met à jour les informations d'une consultation prénatale.
     * 
     * Permet de modifier ou compléter les informations d'une CPN, notamment
     * d'ajouter la date réalisée et les mesures si la consultation vient d'avoir lieu.
     * Le statut passe automatiquement à REALISEE si dateRealisee est renseignée.
     * 
     * @param id L'identifiant de la consultation à modifier
     * @param request Les nouvelles informations de la consultation
     * @return La consultation mise à jour
     * @throws ResourceNotFoundException si la consultation n'existe pas
     */
    @Transactional
    public ConsultationPrenatale updateConsultation(Long id, ConsultationPrenataleRequest request) {
        ConsultationPrenatale consultation = getConsultationById(id);

        consultation.setDatePrevue(request.getDatePrevue());
        consultation.setDateRealisee(request.getDateRealisee());
        consultation.setNotes(request.getNotes());
        consultation.setPoids(request.getPoids());
        consultation.setTensionArterielle(request.getTensionArterielle());
        consultation.setHauteurUterine(request.getHauteurUterine());
        
        if (request.getDateRealisee() != null) {
            consultation.setStatut(StatutConsultation.REALISEE);
        }

        return consultationRepository.save(consultation);
    }

    /**
     * Marque une consultation comme manquée.
     * 
     * Cette méthode est utilisée lorsqu'une patiente ne se présente pas à sa CPN
     * prévue. Le statut passe à MANQUEE, ce qui permet de déclencher des rappels
     * et d'assurer un suivi approprié de la patiente.
     * 
     * @param id L'identifiant de la consultation manquée
     * @throws ResourceNotFoundException si la consultation n'existe pas
     */
    @Transactional
    public void marquerCommeManquee(Long id) {
        ConsultationPrenatale consultation = getConsultationById(id);
        consultation.setStatut(StatutConsultation.MANQUEE);
        consultationRepository.save(consultation);
    }

    /**
     * Supprime une consultation prénatale de la base de données.
     * 
     * ⚠️ ATTENTION : À utiliser avec précaution car cela supprime l'historique.
     * Réservé aux médecins et administrateurs.
     * 
     * @param id L'identifiant de la consultation à supprimer
     * @throws ResourceNotFoundException si la consultation n'existe pas
     */
    @Transactional
    public void deleteConsultation(Long id) {
        if (!consultationRepository.existsById(id)) {
            throw new ResourceNotFoundException("ConsultationPrenatale", "id", id);
        }
        consultationRepository.deleteById(id);
    }
}

