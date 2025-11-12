package com.keneyamuso.service;

import com.keneyamuso.dto.request.EnfantRequest;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.Enfant;
import com.keneyamuso.model.entity.Patiente;
import com.keneyamuso.model.entity.Vaccination;
import com.keneyamuso.model.enums.StatutVaccination;
import com.keneyamuso.repository.EnfantRepository;
import com.keneyamuso.repository.PatienteRepository;
import com.keneyamuso.repository.VaccinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des enfants.
 *
 * Ce service gère le cycle de vie des informations des enfants nés des patientes.
 * Chaque enfant est lié à sa mère et dispose de son propre carnet de santé numérique
 * incluant :
 * - Informations d'état civil (nom, prénom, date de naissance, sexe)
 * - Calendrier vaccinal personnalisé
 * - Historique des consultations postnatales
 * - Suivi de croissance et développement
 *
 * @author KènèyaMuso Team
 * @version 1.0
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
public class EnfantService {

    private final EnfantRepository enfantRepository;
    private final PatienteRepository patienteRepository;
    private final VaccinationRepository vaccinationRepository;

    /**
     * Enregistre un nouvel enfant dans le système avec calendrier vaccinal complet.
     *
     * Cette méthode est généralement appelée après un accouchement pour créer
     * le dossier de l'enfant et initialiser son suivi de santé. 
     *
     * Elle génère automatiquement le calendrier vaccinal selon le Programme Élargi
     * de Vaccination (PEV) du Mali :
     * - À la naissance : BCG, Polio 0
     * - À 6 semaines : Pentavalent 1, Polio 1, Pneumocoque 1, Rotavirus 1
     * - À 10 semaines : Pentavalent 2, Polio 2, Pneumocoque 2, Rotavirus 2
     * - À 14 semaines : Pentavalent 3, Polio 3, Pneumocoque 3
     * - À 9 mois : Rougeole, Fièvre jaune
     *
     * Des rappels automatiques seront envoyés 24h avant chaque vaccination.
     *
     * @param request Les informations de l'enfant (nom, prénom, date de naissance, sexe)
     * @return L'enfant créé avec son identifiant et son calendrier vaccinal
     * @throws ResourceNotFoundException si la patiente (mère) n'existe pas
     */
    @Transactional
    public Enfant createEnfant(EnfantRequest request) {
        Patiente patiente = patienteRepository.findById(request.getPatienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "id", request.getPatienteId()));

        Enfant enfant = new Enfant();
        enfant.setNom(request.getNom());
        enfant.setPrenom(request.getPrenom());
        enfant.setDateDeNaissance(request.getDateDeNaissance());
        enfant.setSexe(request.getSexe());
        enfant.setPatiente(patiente);

        enfant = enfantRepository.save(enfant);

        // Générer automatiquement le calendrier vaccinal selon le PEV Mali
        genererCalendrierVaccinal(enfant);

        // Recharger l'enfant avec ses vaccinations pour la sérialisation JSON
        // Cela garantit que les vaccinations sont chargées et disponibles dans la réponse
        final Long enfantId = enfant.getId(); // Variable finale pour la lambda
        Enfant enfantAvecVaccinations = enfantRepository.findById(enfantId)
                .orElseThrow(() -> new ResourceNotFoundException("Enfant", "id", enfantId));
        
        // Forcer le chargement des vaccinations si elles sont en lazy loading
        if (enfantAvecVaccinations.getVaccinations() != null) {
            enfantAvecVaccinations.getVaccinations().size(); // Force le chargement de la collection
        }

        return enfantAvecVaccinations;
    }

    /**
     * Génère automatiquement le calendrier vaccinal complet pour un enfant.
     *
     * @param enfant L'enfant pour lequel créer le calendrier vaccinal
     */
    private void genererCalendrierVaccinal(Enfant enfant) {
        LocalDate dateNaissance = enfant.getDateDeNaissance();

        // À la naissance (J+0)
        creerVaccin(enfant, dateNaissance, "BCG");
        creerVaccin(enfant, dateNaissance, "Polio 0 (VPO)");

        // À 6 semaines
        LocalDate sixSemaines = dateNaissance.plusWeeks(6);
        creerVaccin(enfant, sixSemaines, "Pentavalent 1 (DTC-HepB-Hib)");
        creerVaccin(enfant, sixSemaines, "Polio 1 (VPO)");
        creerVaccin(enfant, sixSemaines, "Pneumocoque 1 (PCV13)");
        creerVaccin(enfant, sixSemaines, "Rotavirus 1");

        // À 10 semaines
        LocalDate dixSemaines = dateNaissance.plusWeeks(10);
        creerVaccin(enfant, dixSemaines, "Pentavalent 2 (DTC-HepB-Hib)");
        creerVaccin(enfant, dixSemaines, "Polio 2 (VPO)");
        creerVaccin(enfant, dixSemaines, "Pneumocoque 2 (PCV13)");
        creerVaccin(enfant, dixSemaines, "Rotavirus 2");

        // À 14 semaines
        LocalDate quatorzeSemaines = dateNaissance.plusWeeks(14);
        creerVaccin(enfant, quatorzeSemaines, "Pentavalent 3 (DTC-HepB-Hib)");
        creerVaccin(enfant, quatorzeSemaines, "Polio 3 (VPO)");
        creerVaccin(enfant, quatorzeSemaines, "Pneumocoque 3 (PCV13)");

        // À 9 mois
        LocalDate neufMois = dateNaissance.plusMonths(9);
        creerVaccin(enfant, neufMois, "Rougeole-Rubéole (RR)");
        creerVaccin(enfant, neufMois, "Fièvre jaune");
        creerVaccin(enfant, neufMois, "Méningite A");

        // À 15 mois (rappel)
        LocalDate quinzeMois = dateNaissance.plusMonths(15);
        creerVaccin(enfant, quinzeMois, "Rougeole-Rubéole 2 (rappel)");
    }

    /**
     * Crée une vaccination automatique dans le calendrier.
     */
    private void creerVaccin(Enfant enfant, LocalDate datePrevue, String nomVaccin) {
        Vaccination vaccination = new Vaccination();
        vaccination.setEnfant(enfant);
        vaccination.setNomVaccin(nomVaccin);
        vaccination.setDatePrevue(datePrevue);
        vaccination.setStatut(StatutVaccination.A_FAIRE);
        vaccination = vaccinationRepository.save(vaccination);
        
        // Maintenir la relation bidirectionnelle
        if (enfant.getVaccinations() == null) {
            enfant.setVaccinations(new ArrayList<>());
        }
        enfant.getVaccinations().add(vaccination);
    }

    /**
     * Récupère un enfant par son identifiant.
     *
     * @param id L'identifiant unique de l'enfant
     * @return L'enfant trouvé
     * @throws ResourceNotFoundException si l'enfant n'existe pas
     */
    @Transactional(readOnly = true)
    public Enfant getEnfantById(Long id) {
        return enfantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enfant", "id", id));
    }

    /**
     * Récupère tous les enfants d'une patiente.
     *
     * Cette méthode retourne la liste complète des enfants d'une mère,
     * permettant de gérer les familles avec plusieurs enfants.
     *
     * @param patienteId L'identifiant de la patiente (mère)
     * @return La liste des enfants de la patiente (peut être vide)
     */
    @Transactional(readOnly = true)
    public List<Enfant> getEnfantsByPatiente(Long patienteId) {
        return enfantRepository.findByPatiente_Id(patienteId);
    }

    /**
     * Récupère tous les enfants de l'application.
     *
     * Cette méthode est réservée aux médecins et administrateurs pour
     * avoir une vue d'ensemble de tous les enfants suivis.
     *
     * @return La liste de tous les enfants
     */
    @Transactional(readOnly = true)
    public List<Enfant> getAllEnfants() {
        return enfantRepository.findAll();
    }

    /**
     * Met à jour les informations d'un enfant.
     *
     * Permet de corriger ou compléter les informations d'état civil d'un enfant.
     *
     * @param id L'identifiant de l'enfant à modifier
     * @param request Les nouvelles informations de l'enfant
     * @return L'enfant mis à jour
     * @throws ResourceNotFoundException si l'enfant n'existe pas
     */
    @Transactional
    public Enfant updateEnfant(Long id, EnfantRequest request) {
        Enfant enfant = getEnfantById(id);

        enfant.setNom(request.getNom());
        enfant.setPrenom(request.getPrenom());
        enfant.setDateDeNaissance(request.getDateDeNaissance());
        enfant.setSexe(request.getSexe());

        return enfantRepository.save(enfant);
    }

    /**
     * Supprime un enfant de la base de données.
     *
     * ATTENTION : Cette opération supprime également tout l'historique associé
     * (vaccinations, consultations postnatales). À utiliser avec extrême précaution.
     * Réservé aux médecins et administrateurs.
     *
     * @param id L'identifiant de l'enfant à supprimer
     * @throws ResourceNotFoundException si l'enfant n'existe pas
     */
    @Transactional
    public void deleteEnfant(Long id) {
        if (!enfantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Enfant", "id", id);
        }
        enfantRepository.deleteById(id);
    }
}