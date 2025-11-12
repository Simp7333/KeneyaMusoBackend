package com.keneyamuso.controller;

import com.keneyamuso.dto.request.FormulaireCPNRequest;
import com.keneyamuso.dto.request.FormulaireCPONRequest;
import com.keneyamuso.dto.request.OrdonnanceRequest;
import com.keneyamuso.dto.request.SuiviConsultationRequest;
import com.keneyamuso.model.entity.*;
import com.keneyamuso.service.DossierMedicalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DossierMedicalController {

    private final DossierMedicalService dossierMedicalService;

    @PostMapping("/patients/{patienteId}/dossier-medical")
    public ResponseEntity<DossierMedical> createDossierMedical(@PathVariable Long patienteId) {
        return new ResponseEntity<>(dossierMedicalService.createDossierMedical(patienteId), HttpStatus.CREATED);
    }

    @GetMapping("/patients/{patienteId}/dossier-medical")
    public ResponseEntity<DossierMedical> getDossierMedicalByPatienteId(@PathVariable Long patienteId) {
        return ResponseEntity.ok(dossierMedicalService.getDossierMedicalByPatienteId(patienteId));
    }

    @PostMapping("/patients/{patienteId}/dossier-medical/cpn")
    public ResponseEntity<FormulaireCPN> addFormulaireCPN(@PathVariable Long patienteId, @Valid @RequestBody FormulaireCPNRequest request) {
        FormulaireCPN formulaireCPN = mapToEntity(request);
        return new ResponseEntity<>(dossierMedicalService.addFormulaireCPN(patienteId, formulaireCPN), HttpStatus.CREATED);
    }

    @PostMapping("/patients/{patienteId}/dossier-medical/cpon")
    public ResponseEntity<FormulaireCPON> addFormulaireCPON(@PathVariable Long patienteId, @Valid @RequestBody FormulaireCPONRequest request) {
        FormulaireCPON formulaireCPON = mapToEntity(request);
        return new ResponseEntity<>(dossierMedicalService.addFormulaireCPON(patienteId, formulaireCPON), HttpStatus.CREATED);
    }

    @GetMapping("/patients/{patienteId}/dossier-medical/formulaires-cpon")
    public ResponseEntity<List<FormulaireCPON>> getFormulairesCPONByPatiente(@PathVariable Long patienteId) {
        return ResponseEntity.ok(dossierMedicalService.getFormulairesCPONByPatiente(patienteId));
    }

    @PostMapping("/consultations/prenatales/{consultationId}/suivi")
    public ResponseEntity<SuiviConsultation> addSuiviToConsultationPrenatale(@PathVariable Long consultationId, @Valid @RequestBody SuiviConsultationRequest request) {
        SuiviConsultation suivi = mapToEntity(request);
        return new ResponseEntity<>(dossierMedicalService.addSuiviToConsultationPrenatale(consultationId, suivi), HttpStatus.CREATED);
    }

    @PostMapping("/consultations/postnatales/{consultationId}/suivi")
    public ResponseEntity<SuiviConsultation> addSuiviToConsultationPostnatale(@PathVariable Long consultationId, @Valid @RequestBody SuiviConsultationRequest request) {
        SuiviConsultation suivi = mapToEntity(request);
        return new ResponseEntity<>(dossierMedicalService.addSuiviToConsultationPostnatale(consultationId, suivi), HttpStatus.CREATED);
    }

    @PostMapping("/consultations/prenatales/{consultationId}/ordonnance")
    public ResponseEntity<Ordonnance> createOrdonnanceForConsultationPrenatale(@PathVariable Long consultationId, @Valid @RequestBody OrdonnanceRequest request) {
        Ordonnance ordonnance = new Ordonnance();
        ordonnance.setMedicaments(request.getMedicaments());
        ordonnance.setObservations(request.getObservations());
        return new ResponseEntity<>(dossierMedicalService.createOrdonnanceForConsultationPrenatale(consultationId, request.getMedecinId(), ordonnance), HttpStatus.CREATED);
    }

    @PostMapping("/consultations/postnatales/{consultationId}/ordonnance")
    public ResponseEntity<Ordonnance> createOrdonnanceForConsultationPostnatale(@PathVariable Long consultationId, @Valid @RequestBody OrdonnanceRequest request) {
        Ordonnance ordonnance = new Ordonnance();
        ordonnance.setMedicaments(request.getMedicaments());
        ordonnance.setObservations(request.getObservations());
        return new ResponseEntity<>(dossierMedicalService.createOrdonnanceForConsultationPostnatale(consultationId, request.getMedecinId(), ordonnance), HttpStatus.CREATED);
    }


    // --- Mappers ---
    private FormulaireCPN mapToEntity(FormulaireCPNRequest request) {
        FormulaireCPN entity = new FormulaireCPN();
        entity.setTaille(request.getTaille());
        entity.setPoids(request.getPoids());
        entity.setDernierControle(request.getDernierControle());
        entity.setDateDernieresRegles(request.getDateDernieresRegles());
        entity.setNombreMoisGrossesse(request.getNombreMoisGrossesse());
        entity.setGroupeSanguin(request.getGroupeSanguin());
        entity.setComplications(request.isComplications());
        entity.setComplicationsDetails(request.getComplicationsDetails());
        entity.setMouvementsBebeReguliers(request.isMouvementsBebeReguliers());
        entity.setSymptomes(request.getSymptomes());
        entity.setSymptomesAutre(request.getSymptomesAutre());
        entity.setPrendMedicamentsOuVitamines(request.isPrendMedicamentsOuVitamines());
        entity.setMedicamentsOuVitaminesDetails(request.getMedicamentsOuVitaminesDetails());
        entity.setAEuMaladies(request.isAEuMaladies());
        entity.setMaladiesDetails(request.getMaladiesDetails());
        return entity;
    }

    private FormulaireCPON mapToEntity(FormulaireCPONRequest request) {
        FormulaireCPON entity = new FormulaireCPON();
        entity.setAccouchementType(request.getAccouchementType());
        entity.setNombreEnfants(request.getNombreEnfants());
        entity.setSentiment(request.getSentiment());
        entity.setSaignements(request.isSaignements());
        entity.setConsultation(request.getConsultation());
        entity.setSexeBebe(request.getSexeBebe());
        entity.setAlimentation(request.getAlimentation());
        return entity;
    }

    private SuiviConsultation mapToEntity(SuiviConsultationRequest request) {
        SuiviConsultation entity = new SuiviConsultation();
        entity.setAgeGrossesse(request.getAgeGrossesse());
        entity.setPoids(request.getPoids());
        entity.setTensionArterielle(request.getTensionArterielle());
        entity.setHauteurUterine(request.getHauteurUterine());
        entity.setMouvementsFoetaux(request.getMouvementsFoetaux());
        entity.setBruitsDuCoeur(request.getBruitsDuCoeur());
        entity.setOedeme(request.getOedeme());
        entity.setAlbumine(request.getAlbumine());
        entity.setEtatCol(request.getEtatCol());
        entity.setToucherVaginal(request.getToucherVaginal());
        entity.setObservations(request.getObservations());
        entity.setDateProchainRendezVous(request.getDateProchainRendezVous());
        entity.setSoinsCuratifs(request.getSoinsCuratifs());
        entity.setEtatConjonctives(request.getEtatConjonctives());
        entity.setGainPoidsDepuisDebutGrossesse(request.getGainPoidsDepuisDebutGrossesse());
        entity.setExamenObstetrical(request.getExamenObstetrical());
        entity.setInspectionPalpation(request.getInspectionPalpation());
        entity.setPresentation(request.getPresentation());
        entity.setEtatBassinAtteintePromontoire(request.getEtatBassinAtteintePromontoire());
        entity.setRecommandationsAccouchement(request.getRecommandationsAccouchement());
        return entity;
    }
}
