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
    private final ObjectMapper objectMapper;
    private final RappelRepository rappelRepository;

    @Transactional
    public DossierMedicalSubmission createSubmission(Long patienteId, SubmissionType type, JsonNode data) {
        Patiente patiente = patienteRepository.findById(patienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "id", patienteId));

        return createSubmission(patiente, type, data);
    }

    @Transactional
    public DossierMedicalSubmission createSubmissionForTelephone(String telephone, SubmissionType type, JsonNode data) {
        Patiente patiente = patienteRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "telephone", telephone));
        return createSubmission(patiente, type, data);
    }

    private DossierMedicalSubmission createSubmission(Patiente patiente, SubmissionType type, JsonNode data) {
        // Permettre les soumissions même sans médecin assigné
        // Si la patiente a un médecin, l'assigner automatiquement
        // Sinon, la soumission sera sans médecin et visible par tous les médecins
        ProfessionnelSante medecin = patiente.getProfessionnelSanteAssigne();

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

        return submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<DossierMedicalSubmission> getPendingSubmissionsForMedecin(Long medecinId) {
        // Récupérer les soumissions assignées au médecin
        List<DossierMedicalSubmission> submissionsAssigned = submissionRepository
                .findByProfessionnelSanteIdAndStatusInOrderByDateCreationDesc(
                        medecinId,
                        List.of(SubmissionStatus.EN_ATTENTE)
                );
        
        // Récupérer TOUTES les soumissions sans médecin assigné (disponibles pour tous)
        List<DossierMedicalSubmission> submissionsUnassigned = submissionRepository
                .findByProfessionnelSanteIsNullAndStatusOrderByDateCreationDesc(SubmissionStatus.EN_ATTENTE);
        
        // Combiner et retourner
        submissionsUnassigned.addAll(submissionsAssigned);
        return submissionsUnassigned;
    }

    @Transactional(readOnly = true)
    public List<DossierMedicalSubmission> getSubmissionsForPatiente(Long patienteId) {
        return submissionRepository.findByPatienteIdOrderByDateCreationDesc(patienteId);
    }

    @Transactional
    public void approveSubmission(Long submissionId, Long medecinId, String commentaire) {
        DossierMedicalSubmission submission = getSubmissionById(submissionId);

        if (submission.getStatus() != SubmissionStatus.EN_ATTENTE) {
            throw new BadRequestException("Cette demande a déjà été traitée.");
        }

        // Vérifier si le médecin est autorisé à traiter cette soumission
        checkMedecinAuthorization(submission, medecinId);

        // Si la soumission n'a pas de médecin assigné, assigner le médecin
        if (submission.getProfessionnelSante() == null) {
            ProfessionnelSante medecin = professionnelSanteRepository.findById(medecinId)
                    .orElseThrow(() -> new ResourceNotFoundException("Professionnel de santé", "id", medecinId));
            submission.setProfessionnelSante(medecin);
            
            // Assigner le médecin à la patiente
            Patiente patiente = submission.getPatiente();
            patiente.setProfessionnelSanteAssigne(medecin);
            patienteRepository.save(patiente);
            log.info("Médecin {} assigné automatiquement à la patiente {} après acceptation", medecinId, patiente.getId());
        }

        try {
            switch (submission.getType()) {
                case CPN -> traiterSoumissionCpn(submission);
                case CPON -> traiterSoumissionCpon(submission);
            }
        } catch (JsonProcessingException e) {
            log.error("Erreur de parsing du formulaire", e);
            throw new BadRequestException("Données du formulaire invalides");
        }

        submission.setStatus(SubmissionStatus.APPROUVEE);
        submission.setRemarqueMedecin(commentaire);
        submissionRepository.save(submission);

        // Envoyer une alerte à la patiente
        envoyerAlerteApprobation(submission);
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
        FormulaireCPNRequest request = objectMapper.readValue(submission.getPayload(), FormulaireCPNRequest.class);

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

        ensureDossierMedicalExists(submission.getPatiente().getId());
        dossierMedicalService.addFormulaireCPN(submission.getPatiente().getId(), formulaire);
    }

    private void traiterSoumissionCpon(DossierMedicalSubmission submission) throws JsonProcessingException {
        FormulaireCPONRequest request = objectMapper.readValue(submission.getPayload(), FormulaireCPONRequest.class);

        FormulaireCPON formulaire = new FormulaireCPON();
        formulaire.setAccouchementType(request.getAccouchementType());
        formulaire.setNombreEnfants(request.getNombreEnfants());
        formulaire.setSentiment(request.getSentiment());
        formulaire.setSaignements(request.isSaignements());
        formulaire.setConsultation(request.getConsultation());
        formulaire.setSexeBebe(request.getSexeBebe());
        formulaire.setAlimentation(request.getAlimentation());

        ensureDossierMedicalExists(submission.getPatiente().getId());
        dossierMedicalService.addFormulaireCPON(submission.getPatiente().getId(), formulaire);
    }

    private void ensureDossierMedicalExists(Long patienteId) {
        // Vérifier si le dossier existe déjà
        if (dossierMedicalRepository.findByPatienteId(patienteId).isEmpty()) {
            // Créer le dossier s'il n'existe pas
            try {
                dossierMedicalService.createDossierMedical(patienteId);
            } catch (IllegalStateException ignored) {
                // Le dossier existe déjà, ignorer l'erreur (race condition)
            }
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
        return professionnelSanteRepository.findByTelephone(telephone)
                .map(ProfessionnelSante::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Professionnel", "telephone", telephone));
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


