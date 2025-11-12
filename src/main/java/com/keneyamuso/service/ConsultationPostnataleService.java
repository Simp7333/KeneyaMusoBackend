package com.keneyamuso.service;

import com.keneyamuso.dto.request.ConsultationPostnataleRequest;
import com.keneyamuso.dto.request.CponDeclarationRequest;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.ConsultationPostnatale;
import com.keneyamuso.model.entity.Enfant;
import com.keneyamuso.model.entity.Patiente;
import com.keneyamuso.model.enums.StatutConsultation;
import com.keneyamuso.repository.ConsultationPostnataleRepository;
import com.keneyamuso.repository.EnfantRepository;
import com.keneyamuso.repository.PatienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des consultations postnatales (CPoN).
 * 
 * Ce service gère les consultations après l'accouchement pour la mère et le nouveau-né.
 * Selon les recommandations de l'OMS, le suivi postnatal comprend :
 * - CPoN à J+3 : Première visite post-accouchement
 * - CPoN à J+7 : Deuxième visite de suivi
 * - CPoN à 6 semaines : Consultation complète mère-enfant
 * 
 * Ces consultations permettent de détecter et prévenir les complications postnatales
 * (hémorragie, infection, problèmes d'allaitement, etc.) et d'assurer le bon suivi du nouveau-né.
 * 
 * @author KènèyaMuso Team
 * @version 1.0
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
public class ConsultationPostnataleService {

    private final ConsultationPostnataleRepository consultationRepository;
    private final PatienteRepository patienteRepository;
    private final EnfantRepository enfantRepository;

    @Transactional
    public List<ConsultationPostnatale> declarerCpon(CponDeclarationRequest request) {
        Patiente patiente = patienteRepository.findById(request.getPatienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "id", request.getPatienteId()));

        return genererConsultationsPostnatales(patiente, request.getDateAccouchement());
    }

    /**
     * Crée une nouvelle consultation postnatale.
     * 
     * Cette méthode enregistre une CPoN avec son type (J+3, J+7, 6e semaine),
     * sa date prévue et optionnellement les notes sur l'état de la mère et
     * du nouveau-né. Elle peut être liée à un enfant spécifique.
     * 
     * @param request Les informations de la consultation (type, date, notes mère/bébé)
     * @return La consultation créée avec son identifiant
     * @throws ResourceNotFoundException si la patiente n'existe pas
     * @throws ResourceNotFoundException si l'enfant spécifié n'existe pas
     */
    @Transactional
    public ConsultationPostnatale createConsultation(ConsultationPostnataleRequest request) {
        Patiente patiente = patienteRepository.findById(request.getPatienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "id", request.getPatienteId()));

        ConsultationPostnatale consultation = new ConsultationPostnatale();
        consultation.setType(request.getType());
        consultation.setDatePrevue(request.getDatePrevue());
        consultation.setDateRealisee(request.getDateRealisee());
        consultation.setNotesMere(request.getNotesMere());
        consultation.setNotesNouveauNe(request.getNotesNouveauNe());
        consultation.setStatut(request.getDateRealisee() != null ? 
                StatutConsultation.REALISEE : StatutConsultation.A_VENIR);
        consultation.setPatiente(patiente);

        if (request.getEnfantId() != null) {
            Enfant enfant = enfantRepository.findById(request.getEnfantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enfant", "id", request.getEnfantId()));
            consultation.setEnfant(enfant);
        }

        return consultationRepository.save(consultation);
    }

    /**
     * Génère automatiquement les 3 consultations postnatales après un accouchement.
     *
     * @param patiente La patiente qui vient d'accoucher
     * @param dateAccouchement La date de l'accouchement
     * @return La liste des consultations créées
     */
    @Transactional
    public List<ConsultationPostnatale> genererConsultationsPostnatales(Patiente patiente, LocalDate dateAccouchement) {
        List<ConsultationPostnatale> cpons = new ArrayList<>();
        
        // CPoN J+3
        cpons.add(creerCPoN(patiente, dateAccouchement.plusDays(3), "JOUR_3",
                "Consultation postnatale J+3 - Contrôle précoce mère et bébé"));

        // CPoN J+7
        cpons.add(creerCPoN(patiente, dateAccouchement.plusDays(7), "JOUR_7",
                "Consultation postnatale J+7 - Suivi de récupération"));

        // CPoN 6e semaine
        cpons.add(creerCPoN(patiente, dateAccouchement.plusWeeks(6), "SEMAINE_6",
                "Consultation postnatale 6e semaine - Bilan complet"));
        
        return cpons;
    }

    /**
     * Crée une consultation postnatale automatique.
     */
    private ConsultationPostnatale creerCPoN(Patiente patiente, LocalDate datePrevue, String type, String notes) {
        ConsultationPostnatale cpon = new ConsultationPostnatale();
        cpon.setPatiente(patiente);
        cpon.setDatePrevue(datePrevue);
        cpon.setStatut(StatutConsultation.A_VENIR);
        cpon.setNotesMere(notes);
        cpon.setType(type);
        return consultationRepository.save(cpon);
    }

    /**
     * Récupère une consultation postnatale par son identifiant.
     * 
     * @param id L'identifiant unique de la consultation
     * @return La consultation trouvée
     * @throws ResourceNotFoundException si la consultation n'existe pas
     */
    @Transactional(readOnly = true)
    public ConsultationPostnatale getConsultationById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConsultationPostnatale", "id", id));
    }

    /**
     * Récupère toutes les CPoN d'une patiente.
     * 
     * Cette méthode retourne l'historique complet du suivi postnatal d'une mère,
     * permettant de suivre sa récupération après l'accouchement et le développement
     * de ses enfants.
     * 
     * @param patienteId L'identifiant de la patiente
     * @return La liste des consultations de la patiente (peut être vide)
     */
    @Transactional(readOnly = true)
    public List<ConsultationPostnatale> getConsultationsByPatiente(Long patienteId) {
        return consultationRepository.findByPatienteId(patienteId);
    }
    
    /**
     * Récupère toutes les CPoN d'un enfant.
     * 
     * Cette méthode retourne les consultations postnatales spécifiques à un enfant.
     * 
     * @param enfantId L'identifiant de l'enfant
     * @return La liste des consultations de l'enfant (peut être vide)
     */
    @Transactional(readOnly = true)
    public List<ConsultationPostnatale> getConsultationsByEnfant(Long enfantId) {
        return consultationRepository.findByEnfantId(enfantId);
    }

    /**
     * Récupère toutes les consultations postnatales de l'application.
     * 
     * Cette méthode est réservée aux médecins et administrateurs pour
     * avoir une vue d'ensemble de toutes les CPoN.
     * 
     * @return La liste de toutes les consultations
     */
    @Transactional(readOnly = true)
    public List<ConsultationPostnatale> getAllConsultations() {
        return consultationRepository.findAll();
    }

    /**
     * Met à jour les informations d'une consultation postnatale.
     * 
     * Permet de modifier ou compléter les informations d'une CPoN, notamment
     * d'ajouter la date réalisée et les observations cliniques après la consultation.
     * Le statut passe automatiquement à REALISEE si dateRealisee est renseignée.
     * 
     * @param id L'identifiant de la consultation à modifier
     * @param request Les nouvelles informations de la consultation
     * @return La consultation mise à jour
     * @throws ResourceNotFoundException si la consultation n'existe pas
     */
    @Transactional
    public ConsultationPostnatale updateConsultation(Long id, ConsultationPostnataleRequest request) {
        ConsultationPostnatale consultation = getConsultationById(id);

        consultation.setType(request.getType());
        consultation.setDatePrevue(request.getDatePrevue());
        consultation.setDateRealisee(request.getDateRealisee());
        consultation.setNotesMere(request.getNotesMere());
        consultation.setNotesNouveauNe(request.getNotesNouveauNe());
        
        if (request.getDateRealisee() != null) {
            consultation.setStatut(StatutConsultation.REALISEE);
        }

        return consultationRepository.save(consultation);
    }

    /**
     * Supprime une consultation postnatale de la base de données.
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
            throw new ResourceNotFoundException("ConsultationPostnatale", "id", id);
        }
        consultationRepository.deleteById(id);
    }
}
