package com.keneyamuso.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keneyamuso.dto.request.FormulaireCPNRequest;
import com.keneyamuso.dto.request.FormulaireCPONRequest;
import com.keneyamuso.dto.response.DossierSubmissionResponse;
import com.keneyamuso.exception.BadRequestException;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.*;
import com.keneyamuso.model.enums.StatutRappel;
import com.keneyamuso.model.enums.SubmissionStatus;
import com.keneyamuso.model.enums.SubmissionType;
import com.keneyamuso.model.enums.TypeRappel;
import com.keneyamuso.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DossierMedicalSubmissionService {

    private final DossierMedicalSubmissionRepository submissionRepository;
    private final PatienteRepository patienteRepository;
    private final ProfessionnelSanteRepository professionnelSanteRepository;
    private final DossierMedicalService dossierMedicalService;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final FormulaireCPNRepository formulaireCPNRepository;
    private final FormulaireCPONRepository formulaireCPONRepository;
    private final ObjectMapper objectMapper;
    private final RappelRepository rappelRepository;

    @Transactional
    public DossierMedicalSubmission createSubmission(Long patienteId, SubmissionType type, JsonNode data) {
        Patiente patiente = patienteRepository.findById(patienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "id", patienteId));

        return createSubmission(patiente, type, data, null);
    }

    @Transactional
    public DossierMedicalSubmission createSubmissionForTelephone(String telephone, SubmissionType type, JsonNode data) {
        Patiente patiente = patienteRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "telephone", telephone));
        return createSubmission(patiente, type, data, null);
    }

    @Transactional
    public DossierMedicalSubmission createSubmissionForTelephone(String telephone, SubmissionType type, JsonNode data, String medecinTelephone) {
        Patiente patiente = patienteRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "telephone", telephone));
        return createSubmission(patiente, type, data, medecinTelephone);
    }

    private String normalizeTelephone(String telephone) {
        if (telephone == null || telephone.isBlank()) {
            return null;
        }
        // Retirer les espaces et autres caractères non numériques sauf le + au début
        return telephone.trim().replaceAll("\\s+", "");
    }

    private DossierMedicalSubmission createSubmission(Patiente patiente, SubmissionType type, JsonNode data, String medecinTelephone) {
        ProfessionnelSante medecin = null;
        
        log.info("📝 Création de soumission - Patiente ID: {}, Type: {}, Médecin téléphone: {}", 
                patiente.getId(), type, medecinTelephone != null ? medecinTelephone : "null");
        
        // Priorité 1: Si un médecin est spécifié dans la requête, l'utiliser
        if (medecinTelephone != null && !medecinTelephone.isBlank()) {
            String normalizedTelephone = normalizeTelephone(medecinTelephone);
            log.info("🔍 Recherche du médecin avec téléphone (normalisé): {}", normalizedTelephone);
            
            // Essayer d'abord avec le téléphone normalisé
            medecin = professionnelSanteRepository.findByTelephone(normalizedTelephone)
                    .orElse(null);
            
            // Si pas trouvé, essayer avec le téléphone original
            if (medecin == null && !normalizedTelephone.equals(medecinTelephone)) {
                log.info("🔍 Tentative avec téléphone original: {}", medecinTelephone);
                medecin = professionnelSanteRepository.findByTelephone(medecinTelephone)
                        .orElse(null);
            }
            
            if (medecin != null) {
                log.info("✅ Médecin trouvé: ID={}, Nom={}, Téléphone={}", 
                        medecin.getId(), medecin.getNom() + " " + medecin.getPrenom(), medecin.getTelephone());
            } else {
                log.warn("⚠️ Aucun médecin trouvé avec le téléphone: {} (normalisé: {})", 
                        medecinTelephone, normalizedTelephone);
            }
        }
        
        // Priorité 2: Sinon, utiliser le médecin assigné à la patiente
        if (medecin == null) {
            medecin = patiente.getProfessionnelSanteAssigne();
            if (medecin != null) {
                log.info("📋 Utilisation du médecin assigné à la patiente: ID={}, Nom={}", 
                        medecin.getId(), medecin.getNom() + " " + medecin.getPrenom());
            } else {
                log.info("ℹ️ Aucun médecin assigné à la patiente - la soumission sera visible par tous les médecins");
            }
        }
        
        // Si aucun médecin n'est assigné, la soumission sera visible par tous les médecins (null)

        DossierMedicalSubmission submission = new DossierMedicalSubmission();
        submission.setPatiente(patiente);
        submission.setProfessionnelSante(medecin); // Peut être null
        submission.setType(type);
        submission.setStatus(SubmissionStatus.EN_ATTENTE);
        try {
            submission.setPayload(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Impossible de sérialiser les données du formulaire.");
        }

        DossierMedicalSubmission savedSubmission = submissionRepository.save(submission);
        log.info("✅ Soumission créée - ID: {}, Statut: {}, Médecin assigné: {}", 
                savedSubmission.getId(), 
                savedSubmission.getStatus(),
                savedSubmission.getProfessionnelSante() != null 
                    ? savedSubmission.getProfessionnelSante().getId().toString() 
                    : "null (visible par tous)");
        
        return savedSubmission;
    }

    @Transactional(readOnly = true)
    public List<DossierMedicalSubmission> getPendingSubmissionsForMedecin(Long medecinId) {
        log.info("🔍 Récupération des soumissions en attente pour le médecin ID: {}", medecinId);
        
        // DEBUG: Lister TOUTES les soumissions en base pour debug
        List<DossierMedicalSubmission> allSubmissions = submissionRepository.findAll();
        log.info("🔍 DEBUG - Total de soumissions en base: {}", allSubmissions.size());
        for (DossierMedicalSubmission sub : allSubmissions) {
            Long medecinIdInSub = sub.getProfessionnelSante() != null ? sub.getProfessionnelSante().getId() : null;
            log.info("  - Soumission ID: {}, Type: {}, Statut: {}, Médecin ID: {}, Patiente ID: {}", 
                    sub.getId(), sub.getType(), sub.getStatus(), medecinIdInSub, sub.getPatiente().getId());
        }
        
        // Récupérer les soumissions assignées au médecin
        List<DossierMedicalSubmission> submissionsAssigned = submissionRepository
                .findByProfessionnelSanteIdAndStatusInOrderByDateCreationDesc(
                        medecinId,
                        List.of(SubmissionStatus.EN_ATTENTE)
                );
        log.info("📋 Soumissions assignées au médecin {}: {}", medecinId, submissionsAssigned.size());
        for (DossierMedicalSubmission sub : submissionsAssigned) {
            log.info("  - Soumission ID: {}, Type: {}, Patiente: {}", 
                    sub.getId(), sub.getType(), sub.getPatiente().getId());
        }
        
        // Récupérer TOUTES les soumissions sans médecin assigné (disponibles pour tous)
        List<DossierMedicalSubmission> submissionsUnassigned = submissionRepository
                .findByProfessionnelSanteIsNullAndStatusOrderByDateCreationDesc(SubmissionStatus.EN_ATTENTE);
        log.info("📋 Soumissions non assignées (disponibles pour tous): {}", submissionsUnassigned.size());
        for (DossierMedicalSubmission sub : submissionsUnassigned) {
            log.info("  - Soumission ID: {}, Type: {}, Patiente: {}", 
                    sub.getId(), sub.getType(), sub.getPatiente().getId());
        }
        
        // Combiner et retourner
        submissionsUnassigned.addAll(submissionsAssigned);
        log.info("✅ Total de soumissions retournées: {}", submissionsUnassigned.size());
        return submissionsUnassigned;
    }

    @Transactional(readOnly = true)
    public List<DossierMedicalSubmission> getSubmissionsForPatiente(Long patienteId) {
        return submissionRepository.findByPatienteIdOrderByDateCreationDesc(patienteId);
    }

    @Transactional
    public void approveSubmission(Long submissionId, Long medecinId, String commentaire) {
        log.info("🚀 Début de l'approbation - Submission ID: {}, Médecin ID: {}", submissionId, medecinId);
        
        try {
            DossierMedicalSubmission submission = getSubmissionById(submissionId);
            log.info("✅ Soumission trouvée - Type: {}, Statut: {}, Patiente ID: {}", 
                    submission.getType(), submission.getStatus(), submission.getPatiente().getId());

            if (submission.getStatus() != SubmissionStatus.EN_ATTENTE) {
                log.warn("⚠️ La soumission {} a déjà été traitée - Statut actuel: {}", submissionId, submission.getStatus());
                throw new BadRequestException("Cette demande a déjà été traitée.");
            }

            // Vérifier si le médecin est autorisé à traiter cette soumission
            checkMedecinAuthorization(submission, medecinId);
            log.info("✅ Autorisation du médecin vérifiée");

            // Récupérer le médecin qui approuve
            ProfessionnelSante medecin = professionnelSanteRepository.findById(medecinId)
                    .orElseThrow(() -> new ResourceNotFoundException("Professionnel de santé", "id", medecinId));
            log.info("✅ Médecin trouvé - ID: {}, Nom: {}", medecinId, medecin.getNom() + " " + medecin.getPrenom());
            
            // Si la soumission n'a pas de médecin assigné, l'assigner maintenant
            if (submission.getProfessionnelSante() == null) {
                submission.setProfessionnelSante(medecin);
                log.info("✅ Médecin {} assigné à la soumission {} après acceptation", medecinId, submissionId);
            }
            
            // Assigner le médecin à la patiente (toujours lors de l'acceptation, même si déjà assigné à la soumission)
            Patiente patiente = submission.getPatiente();
            if (patiente.getProfessionnelSanteAssigne() == null || 
                !patiente.getProfessionnelSanteAssigne().getId().equals(medecinId)) {
                patiente.setProfessionnelSanteAssigne(medecin);
                patienteRepository.save(patiente);
                log.info("✅ Médecin {} assigné à la patiente {} après acceptation de la soumission", medecinId, patiente.getId());
            } else {
                log.info("ℹ️ Médecin {} était déjà assigné à la patiente {}", medecinId, patiente.getId());
            }

            // Traiter le formulaire selon le type
            log.info("📝 Début du traitement du formulaire - Type: {}", submission.getType());
            try {
                switch (submission.getType()) {
                    case CPN -> {
                        log.info("📋 Traitement du formulaire CPN...");
                        traiterSoumissionCpn(submission);
                        log.info("✅ Formulaire CPN traité avec succès");
                    }
                    case CPON -> {
                        log.info("📋 Traitement du formulaire CPON...");
                        traiterSoumissionCpon(submission);
                        log.info("✅ Formulaire CPON traité avec succès");
                    }
                }
            } catch (JsonProcessingException e) {
                log.error("❌ Erreur de parsing du formulaire JSON", e);
                log.error("❌ Payload qui a causé l'erreur: {}", submission.getPayload());
                throw new BadRequestException("Données du formulaire invalides: " + e.getMessage());
            } catch (Exception e) {
                log.error("❌ Erreur inattendue lors du traitement du formulaire", e);
                throw new BadRequestException("Erreur lors du traitement du formulaire: " + e.getMessage());
            }

            // Mettre à jour le statut de la soumission
            log.info("💾 Mise à jour du statut de la soumission...");
            submission.setStatus(SubmissionStatus.APPROUVEE);
            submission.setRemarqueMedecin(commentaire);
            submissionRepository.save(submission);
            log.info("✅ Statut de la soumission mis à jour: APPROUVEE");

            // Envoyer une alerte à la patiente
            log.info("📧 Envoi de l'alerte d'approbation à la patiente...");
            try {
                envoyerAlerteApprobation(submission);
                log.info("✅ Alerte d'approbation envoyée");
            } catch (Exception e) {
                log.error("⚠️ Erreur lors de l'envoi de l'alerte (non bloquant)", e);
                // Ne pas bloquer l'approbation si l'alerte échoue
            }
            
            log.info("🎉 Approbation terminée avec succès - Submission ID: {}", submissionId);
            
        } catch (BadRequestException | ResourceNotFoundException e) {
            log.error("❌ Erreur métier lors de l'approbation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur inattendue lors de l'approbation - Submission ID: {}", submissionId, e);
            throw new BadRequestException("Erreur lors de l'approbation: " + e.getMessage());
        }
    }

    @Transactional
    public void rejectSubmission(Long submissionId, Long medecinId, String raison) {
        DossierMedicalSubmission submission = getSubmissionById(submissionId);

        if (submission.getStatus() != SubmissionStatus.EN_ATTENTE) {
            throw new BadRequestException("Cette demande a déjà été traitée.");
        }

        // Vérifier si le médecin est autorisé à traiter cette soumission
        checkMedecinAuthorization(submission, medecinId);

        submission.setStatus(SubmissionStatus.REJETEE);
        submission.setRemarqueMedecin(raison);
        submissionRepository.save(submission);

        // Envoyer une alerte à la patiente
        envoyerAlerteRejet(submission, raison);
    }

    private DossierMedicalSubmission getSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));
    }

    private void checkMedecinAuthorization(DossierMedicalSubmission submission, Long medecinId) {
        // Si la soumission n'a pas de médecin assigné, n'importe quel médecin peut la traiter
        if (submission.getProfessionnelSante() == null) {
            return;
        }
        
        // Si la soumission a un médecin assigné, seul ce médecin peut la traiter
        if (!submission.getProfessionnelSante().getId().equals(medecinId)) {
            throw new BadRequestException("Vous n'êtes pas autorisé à traiter cette demande.");
        }
    }

    private void traiterSoumissionCpn(DossierMedicalSubmission submission) throws JsonProcessingException {
        log.info("📄 Parsing du payload CPN pour la soumission {}", submission.getId());
        log.debug("Payload JSON: {}", submission.getPayload());
        
        FormulaireCPNRequest request;
        try {
            request = objectMapper.readValue(submission.getPayload(), FormulaireCPNRequest.class);
            log.info("✅ Payload CPN parsé avec succès");
        } catch (Exception e) {
            log.error("❌ Erreur lors du parsing du payload CPN", e);
            throw e;
        }

        log.info("📝 Création du formulaire CPN à partir de la requête...");
        FormulaireCPN formulaire = new FormulaireCPN();
        formulaire.setTaille(request.getTaille());
        formulaire.setPoids(request.getPoids());
        formulaire.setDernierControle(request.getDernierControle());
        formulaire.setDateDernieresRegles(request.getDateDernieresRegles());
        formulaire.setNombreMoisGrossesse(request.getNombreMoisGrossesse());
        formulaire.setGroupeSanguin(request.getGroupeSanguin());
        formulaire.setComplications(request.isComplications());
        formulaire.setComplicationsDetails(request.getComplicationsDetails());
        formulaire.setMouvementsBebeReguliers(request.isMouvementsBebeReguliers());
        formulaire.setSymptomes(request.getSymptomes());
        formulaire.setSymptomesAutre(request.getSymptomesAutre());
        formulaire.setPrendMedicamentsOuVitamines(request.isPrendMedicamentsOuVitamines());
        formulaire.setMedicamentsOuVitaminesDetails(request.getMedicamentsOuVitaminesDetails());
        formulaire.setAEuMaladies(request.isAEuMaladies());
        formulaire.setMaladiesDetails(request.getMaladiesDetails());
        log.info("✅ Formulaire CPN créé - Taille: {}, Poids: {}, Mois: {}", 
                request.getTaille(), request.getPoids(), request.getNombreMoisGrossesse());

        Long patienteId = submission.getPatiente().getId();
        log.info("🔍 Vérification du dossier médical pour la patiente {}", patienteId);
        DossierMedical dossierMedical = ensureDossierMedicalExists(patienteId);
        
        log.info("💾 Ajout du formulaire CPN au dossier médical...");
        try {
            // Utiliser directement le dossier médical récupéré au lieu de le rechercher à nouveau
            formulaire.setDossierMedical(dossierMedical);
            FormulaireCPN savedFormulaire = formulaireCPNRepository.save(formulaire);
            log.info("✅ Formulaire CPN ajouté avec succès - ID: {}", savedFormulaire.getId());
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'ajout du formulaire CPN au dossier médical", e);
            throw new BadRequestException("Erreur lors de l'ajout du formulaire CPN: " + e.getMessage());
        }
    }

    private void traiterSoumissionCpon(DossierMedicalSubmission submission) throws JsonProcessingException {
        log.info("📄 Parsing du payload CPON pour la soumission {}", submission.getId());
        log.debug("Payload JSON: {}", submission.getPayload());
        
        FormulaireCPONRequest request;
        try {
            request = objectMapper.readValue(submission.getPayload(), FormulaireCPONRequest.class);
            log.info("✅ Payload CPON parsé avec succès");
        } catch (Exception e) {
            log.error("❌ Erreur lors du parsing du payload CPON", e);
            throw e;
        }

        log.info("📝 Création du formulaire CPON à partir de la requête...");
        FormulaireCPON formulaire = new FormulaireCPON();
        formulaire.setAccouchementType(request.getAccouchementType());
        formulaire.setNombreEnfants(request.getNombreEnfants());
        formulaire.setSentiment(request.getSentiment());
        formulaire.setSaignements(request.isSaignements());
        formulaire.setConsultation(request.getConsultation());
        formulaire.setSexeBebe(request.getSexeBebe());
        formulaire.setAlimentation(request.getAlimentation());
        log.info("✅ Formulaire CPON créé - Type accouchement: {}, Nombre enfants: {}", 
                request.getAccouchementType(), request.getNombreEnfants());

        Long patienteId = submission.getPatiente().getId();
        log.info("🔍 Vérification du dossier médical pour la patiente {}", patienteId);
        DossierMedical dossierMedical = ensureDossierMedicalExists(patienteId);
        
        log.info("💾 Ajout du formulaire CPON au dossier médical...");
        try {
            // Utiliser directement le dossier médical récupéré au lieu de le rechercher à nouveau
            formulaire.setDossierMedical(dossierMedical);
            FormulaireCPON savedFormulaire = formulaireCPONRepository.save(formulaire);
            log.info("✅ Formulaire CPON ajouté avec succès - ID: {}", savedFormulaire.getId());
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'ajout du formulaire CPON au dossier médical", e);
            throw new BadRequestException("Erreur lors de l'ajout du formulaire CPON: " + e.getMessage());
        }
    }

    private DossierMedical ensureDossierMedicalExists(Long patienteId) {
        log.info("🔍 Vérification de l'existence du dossier médical pour la patiente {}", patienteId);
        
        // Vérifier si le dossier existe déjà
        var dossierOptional = dossierMedicalRepository.findByPatienteId(patienteId);
        
        if (dossierOptional.isPresent()) {
            DossierMedical dossier = dossierOptional.get();
            log.info("✅ Dossier médical existant trouvé - ID: {} pour la patiente {}", 
                     dossier.getId(), patienteId);
            return dossier;
        }
        
        // Créer le dossier s'il n'existe pas
        log.info("📋 Aucun dossier médical trouvé. Création pour la patiente {}", patienteId);
        try {
            DossierMedical nouveauDossier = dossierMedicalService.createDossierMedical(patienteId);
            // Flush explicitement pour s'assurer que le dossier est persistant
            dossierMedicalRepository.flush();
            log.info("✅ Dossier médical créé avec succès - ID: {} pour la patiente {}", 
                     nouveauDossier.getId(), patienteId);
            return nouveauDossier;
        } catch (IllegalStateException e) {
            // Le dossier existe déjà (race condition possible)
            log.warn("⚠️ Le dossier médical existe déjà pour la patiente {} (race condition détectée): {}", 
                     patienteId, e.getMessage());
            // Vérifier à nouveau pour confirmer
            var dossierVerif = dossierMedicalRepository.findByPatienteId(patienteId);
            if (dossierVerif.isEmpty()) {
                log.error("❌ ERREUR CRITIQUE: Impossible de créer ou trouver le dossier médical pour la patiente {}", patienteId);
                throw new IllegalStateException("Impossible de créer le dossier médical pour la patiente " + patienteId);
            }
            return dossierVerif.get();
        } catch (Exception e) {
            log.error("❌ Erreur inattendue lors de la création du dossier médical pour la patiente {}: {}", 
                     patienteId, e.getMessage(), e);
            throw e;
        }
    }

    public List<DossierSubmissionResponse> mapToResponses(List<DossierMedicalSubmission> submissions) {
        return submissions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public DossierSubmissionResponse mapToResponse(DossierMedicalSubmission submission) {
        return DossierSubmissionResponse.builder()
                .id(submission.getId())
                .type(submission.getType())
                .status(submission.getStatus())
                .patienteId(submission.getPatiente().getId())
                .patienteNom(submission.getPatiente().getNom())
                .patientePrenom(submission.getPatiente().getPrenom())
                .payload(submission.getPayload())
                .commentaire(submission.getRemarqueMedecin())
                .dateCreation(submission.getDateCreation())
                .build();
    }

    public Long getPatienteIdFromTelephone(String telephone) {
        return patienteRepository.findByTelephone(telephone)
                .map(Patiente::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "telephone", telephone));
    }

    public Long getMedecinIdFromTelephone(String telephone) {
        log.info("🔍 Recherche du médecin ID par téléphone: {}", telephone);
        Optional<ProfessionnelSante> medecin = professionnelSanteRepository.findByTelephone(telephone);
        
        if (medecin.isPresent()) {
            Long medecinId = medecin.get().getId();
            log.info("✅ Médecin trouvé - ID: {}, Nom: {}", medecinId, medecin.get().getNom() + " " + medecin.get().getPrenom());
            return medecinId;
        } else {
            log.error("❌ Aucun médecin trouvé avec le téléphone: {}", telephone);
            throw new ResourceNotFoundException("Professionnel", "telephone", telephone);
        }
    }

    @Transactional(readOnly = true)
    public long countPendingForMedecin(Long medecinId, SubmissionStatus statut) {
        // Compter les soumissions assignées au médecin
        long countAssigned = submissionRepository.countByProfessionnelSanteIdAndStatus(medecinId, statut);
        
        // Si on demande le statut EN_ATTENTE, ajouter aussi les soumissions non assignées
        if (statut == SubmissionStatus.EN_ATTENTE) {
            long countUnassigned = submissionRepository
                    .findByProfessionnelSanteIsNullAndStatusOrderByDateCreationDesc(SubmissionStatus.EN_ATTENTE)
                    .size();
            return countAssigned + countUnassigned;
        }
        
        return countAssigned;
    }

    /**
     * Envoie une alerte à la patiente après l'approbation de sa soumission.
     */
    private void envoyerAlerteApprobation(DossierMedicalSubmission submission) {
        TypeRappel typeRappel = submission.getType() == SubmissionType.CPN ? TypeRappel.CPN : TypeRappel.CPON;
        String message = String.format("Votre formulaire %s a été approuvé par votre médecin.", 
                submission.getType() == SubmissionType.CPN ? "prénatal (CPN)" : "postnatal (CPON)");
        
        Rappel rappel = new Rappel();
        rappel.setUtilisateur(submission.getPatiente());
        rappel.setType(typeRappel);
        rappel.setMessage(message);
        rappel.setDateEnvoi(java.time.LocalDateTime.now());
        rappel.setStatut(StatutRappel.ENVOYE);
        
        rappelRepository.save(rappel);
        log.info("Alerte d'approbation envoyée à la patiente {}", submission.getPatiente().getId());
    }

    /**
     * Envoie une alerte à la patiente après le rejet de sa soumission.
     */
    private void envoyerAlerteRejet(DossierMedicalSubmission submission, String raison) {
        TypeRappel typeRappel = submission.getType() == SubmissionType.CPN ? TypeRappel.CPN : TypeRappel.CPON;
        String message = String.format("Votre formulaire %s a été rejeté. Raison: %s", 
                submission.getType() == SubmissionType.CPN ? "prénatal (CPN)" : "postnatal (CPON)", 
                raison != null && !raison.isEmpty() ? raison : "Non spécifiée");
        
        Rappel rappel = new Rappel();
        rappel.setUtilisateur(submission.getPatiente());
        rappel.setType(typeRappel);
        rappel.setMessage(message);
        rappel.setDateEnvoi(java.time.LocalDateTime.now());
        rappel.setStatut(StatutRappel.ENVOYE);
        
        rappelRepository.save(rappel);
        log.info("Alerte de rejet envoyée à la patiente {}", submission.getPatiente().getId());
    }
}


