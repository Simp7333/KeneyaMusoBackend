package com.keneyamuso.service;

import com.keneyamuso.dto.request.VaccinationRequest;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.Enfant;
import com.keneyamuso.model.entity.Vaccination;
import com.keneyamuso.model.enums.StatutVaccination;
import com.keneyamuso.repository.EnfantRepository;
import com.keneyamuso.repository.VaccinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des vaccinations.
 * 
 * Ce service gère le calendrier vaccinal des enfants selon le Programme Élargi
 * de Vaccination (PEV) du Mali. Les vaccins principaux incluent :
 * 
 * - À la naissance : BCG, Polio 0
 * - À 6 semaines : Pentavalent 1, Polio 1, Pneumocoque 1, Rotavirus 1
 * - À 10 semaines : Pentavalent 2, Polio 2, Pneumocoque 2, Rotavirus 2
 * - À 14 semaines : Pentavalent 3, Polio 3, Pneumocoque 3
 * - À 9 mois : Rougeole, Fièvre jaune
 * 
 * Ce service permet de planifier, suivre et enregistrer l'administration des vaccins,
 * avec génération automatique de rappels pour les parents.
 * 
 * @author KènèyaMuso Team
 * @version 1.0
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
public class VaccinationService {

    private final VaccinationRepository vaccinationRepository;
    private final EnfantRepository enfantRepository;

    /**
     * Crée une nouvelle vaccination dans le calendrier d'un enfant.
     * 
     * Cette méthode planifie un vaccin avec sa date prévue et enregistre
     * optionnellement sa réalisation si elle a déjà eu lieu. Le statut est
     * automatiquement déterminé : FAIT si dateRealisee est renseignée, A_FAIRE sinon.
     * 
     * @param request Les informations de la vaccination (nom vaccin, dates, notes)
     * @return La vaccination créée avec son identifiant
     * @throws ResourceNotFoundException si l'enfant n'existe pas
     */
    @Transactional
    public Vaccination createVaccination(VaccinationRequest request) {
        Enfant enfant = enfantRepository.findById(request.getEnfantId())
                .orElseThrow(() -> new ResourceNotFoundException("Enfant", "id", request.getEnfantId()));

        Vaccination vaccination = new Vaccination();
        vaccination.setNomVaccin(request.getNomVaccin());
        vaccination.setDatePrevue(request.getDatePrevue());
        vaccination.setDateRealisee(request.getDateRealisee());
        vaccination.setNotes(request.getNotes());
        vaccination.setStatut(request.getDateRealisee() != null ? 
                StatutVaccination.FAIT : StatutVaccination.A_FAIRE);
        vaccination.setEnfant(enfant);

        return vaccinationRepository.save(vaccination);
    }

    /**
     * Récupère une vaccination par son identifiant.
     * 
     * @param id L'identifiant unique de la vaccination
     * @return La vaccination trouvée
     * @throws ResourceNotFoundException si la vaccination n'existe pas
     */
    @Transactional(readOnly = true)
    public Vaccination getVaccinationById(Long id) {
        return vaccinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination", "id", id));
    }

    /**
     * Récupère toutes les vaccinations d'un enfant (calendrier vaccinal complet).
     * 
     * Cette méthode retourne l'historique vaccinal complet de l'enfant,
     * incluant les vaccins réalisés et ceux à venir, trié par date.
     * 
     * @param enfantId L'identifiant de l'enfant
     * @return La liste des vaccinations de l'enfant (peut être vide)
     */
    @Transactional(readOnly = true)
    public List<Vaccination> getVaccinationsByEnfant(Long enfantId) {
        return vaccinationRepository.findByEnfantId(enfantId);
    }

    /**
     * Récupère toutes les vaccinations de l'application.
     * 
     * Cette méthode est réservée aux médecins et administrateurs pour
     * avoir une vue d'ensemble de toutes les vaccinations.
     * 
     * @return La liste de toutes les vaccinations
     */
    @Transactional(readOnly = true)
    public List<Vaccination> getAllVaccinations() {
        return vaccinationRepository.findAll();
    }

    /**
     * Met à jour les informations d'une vaccination.
     * 
     * Permet de modifier les dates ou d'enregistrer l'administration d'un vaccin.
     * Le statut passe automatiquement à FAIT si dateRealisee est renseignée.
     * Cette méthode est utilisée notamment pour confirmer qu'un vaccin a été administré.
     * 
     * @param id L'identifiant de la vaccination à modifier
     * @param request Les nouvelles informations de la vaccination
     * @return La vaccination mise à jour
     * @throws ResourceNotFoundException si la vaccination n'existe pas
     */
    @Transactional
    public Vaccination updateVaccination(Long id, VaccinationRequest request) {
        Vaccination vaccination = getVaccinationById(id);

        vaccination.setNomVaccin(request.getNomVaccin());
        vaccination.setDatePrevue(request.getDatePrevue());
        vaccination.setDateRealisee(request.getDateRealisee());
        vaccination.setNotes(request.getNotes());
        
        if (request.getDateRealisee() != null) {
            vaccination.setStatut(StatutVaccination.FAIT);
        }

        return vaccinationRepository.save(vaccination);
    }

    /**
     * Supprime une vaccination de la base de données.
     * 
     * ⚠️ ATTENTION : À utiliser avec précaution car cela supprime l'historique vaccinal.
     * Réservé aux médecins et administrateurs.
     * 
     * @param id L'identifiant de la vaccination à supprimer
     * @throws ResourceNotFoundException si la vaccination n'existe pas
     */
    @Transactional
    public void deleteVaccination(Long id) {
        if (!vaccinationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vaccination", "id", id);
        }
        vaccinationRepository.deleteById(id);
    }
}
