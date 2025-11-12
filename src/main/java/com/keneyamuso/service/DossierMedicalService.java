package com.keneyamuso.service;

import com.keneyamuso.model.entity.*;
import com.keneyamuso.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List; // ✅ Import ajouté

@Service
@RequiredArgsConstructor
@Transactional
public class DossierMedicalService {

    private final DossierMedicalRepository dossierMedicalRepository;
    private final PatienteRepository patienteRepository;
    private final FormulaireCPNRepository formulaireCPNRepository;
    private final FormulaireCPONRepository formulaireCPONRepository;
    private final SuiviConsultationRepository suiviConsultationRepository;
    private final OrdonnanceRepository ordonnanceRepository;
    private final ConsultationPrenataleRepository consultationPrenataleRepository;
    private final ConsultationPostnataleRepository consultationPostnataleRepository;
    private final ProfessionnelSanteRepository professionnelSanteRepository;

    public DossierMedical createDossierMedical(Long patienteId) {
        Patiente patiente = patienteRepository.findById(patienteId)
                .orElseThrow(() -> new RuntimeException("Patiente not found"));
        if (dossierMedicalRepository.findByPatienteId(patienteId).isPresent()) {
            throw new IllegalStateException("Dossier medical already exists for this patiente");
        }
        DossierMedical dossierMedical = new DossierMedical();
        dossierMedical.setPatiente(patiente);
        return dossierMedicalRepository.save(dossierMedical);
    }

    public DossierMedical getDossierMedicalByPatienteId(Long patienteId) {
        return dossierMedicalRepository.findByPatienteId(patienteId)
                .orElseThrow(() -> new RuntimeException("DossierMedical not found for this patiente"));
    }

    public FormulaireCPN addFormulaireCPN(Long patienteId, FormulaireCPN formulaireCPN) {
        DossierMedical dossierMedical = getDossierMedicalByPatienteId(patienteId);
        formulaireCPN.setDossierMedical(dossierMedical);
        return formulaireCPNRepository.save(formulaireCPN);
    }

    public FormulaireCPON addFormulaireCPON(Long patienteId, FormulaireCPON formulaireCPON) {
        DossierMedical dossierMedical = getDossierMedicalByPatienteId(patienteId);
        formulaireCPON.setDossierMedical(dossierMedical);
        return formulaireCPONRepository.save(formulaireCPON);
    }

    public List<FormulaireCPON> getFormulairesCPONByPatiente(Long patienteId) {
        DossierMedical dossierMedical = getDossierMedicalByPatienteId(patienteId);
        return formulaireCPONRepository.findByDossierMedicalId(dossierMedical.getId());
    }

    public SuiviConsultation addSuiviToConsultationPrenatale(Long consultationId, SuiviConsultation suivi) {
        ConsultationPrenatale consultation = consultationPrenataleRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("ConsultationPrenatale not found"));
        suivi.setConsultationPrenatale(consultation);
        return suiviConsultationRepository.save(suivi);
    }

    public SuiviConsultation addSuiviToConsultationPostnatale(Long consultationId, SuiviConsultation suivi) {
        ConsultationPostnatale consultation = consultationPostnataleRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("ConsultationPostnatale not found"));
        suivi.setConsultationPostnatale(consultation);
        return suiviConsultationRepository.save(suivi);
    }

    public Ordonnance createOrdonnanceForConsultationPrenatale(Long consultationId, Long medecinId, Ordonnance ordonnance) {
        ConsultationPrenatale consultation = consultationPrenataleRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("ConsultationPrenatale not found"));
        ProfessionnelSante medecin = professionnelSanteRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Medecin not found"));

        ordonnance.setConsultationPrenatale(consultation);
        ordonnance.setPatiente(consultation.getGrossesse().getPatiente());
        ordonnance.setMedecin(medecin);

        return ordonnanceRepository.save(ordonnance);
    }

    public Ordonnance createOrdonnanceForConsultationPostnatale(Long consultationId, Long medecinId, Ordonnance ordonnance) {
        ConsultationPostnatale consultation = consultationPostnataleRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("ConsultationPostnatale not found"));
        ProfessionnelSante medecin = professionnelSanteRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Medecin not found"));

        ordonnance.setConsultationPostnatale(consultation);
        ordonnance.setPatiente(consultation.getPatiente());
        ordonnance.setMedecin(medecin);

        return ordonnanceRepository.save(ordonnance);
    }
}
