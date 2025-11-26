package com.keneyamuso.service;

import com.keneyamuso.dto.response.PatienteDetailDto;
import com.keneyamuso.dto.response.PatienteListDto;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.*;
import com.keneyamuso.repository.ConsultationPostnataleRepository;
import com.keneyamuso.repository.ConsultationPrenataleRepository;
import com.keneyamuso.repository.PatienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PatienteService {

    private final PatienteRepository patienteRepository;
    private final ConsultationPrenataleRepository consultationPrenataleRepository;
    private final ConsultationPostnataleRepository consultationPostnataleRepository;

    /**
     * Liste des patientes avec grossesse en cours
     */
    public List<PatienteListDto> getPatientesAvecGrossesseEnCours(Long medecinId) {
        List<Patiente> patientes = patienteRepository.findPatientesWithGrossesseEnCours(medecinId);
        return mapToDto(patientes, true, false);
    }

    /**
     * Liste des patientes avec grossesse terminée
     */
    public List<PatienteListDto> getPatientesAvecGrossesseTerminee(Long medecinId) {
        List<Patiente> patientes = patienteRepository.findPatientesWithGrossesseTerminee(medecinId);
        return mapToDto(patientes, true, false);
    }

    /**
     * Liste des patientes avec enfants
     */
    public List<PatienteListDto> getPatientesAvecEnfants(Long medecinId) {
        List<Patiente> patientes = patienteRepository.findPatientesWithEnfants(medecinId);
        return mapToDto(patientes, false, true);
    }

    /**
     * Mapping générique
     */
    private List<PatienteListDto> mapToDto(List<Patiente> patientes, boolean includeGrossesses, boolean includeEnfants) {
        return patientes.stream().map(p -> {
            PatienteListDto dto = new PatienteListDto();
            dto.setId(p.getId());
            dto.setNom(p.getNom());
            dto.setPrenom(p.getPrenom());
            dto.setTelephone(p.getTelephone());
            dto.setDateDeNaissance(p.getDateDeNaissance());
            dto.setAdresse(p.getAdresse());

            if (includeGrossesses && p.getGrossesses() != null) {
                dto.setGrossesses(p.getGrossesses().stream().map(g -> {
                    PatienteListDto.GrossesseBrief gb = new PatienteListDto.GrossesseBrief();
                    gb.setId(g.getId());
                    gb.setDateDebut(g.getDateDebut());
                    gb.setDatePrevueAccouchement(g.getDatePrevueAccouchement());
                    gb.setStatut(g.getStatut().name());
                    return gb;
                }).toList());
            }

            if (includeEnfants && p.getEnfants() != null) {
                dto.setEnfants(p.getEnfants().stream().map(e -> {
                    PatienteListDto.EnfantBrief eb = new PatienteListDto.EnfantBrief();
                    eb.setId(e.getId());
                    eb.setNom(e.getNom());
                    eb.setPrenom(e.getPrenom());
                    eb.setDateDeNaissance(e.getDateDeNaissance());
                    eb.setSexe(e.getSexe().name());
                    return eb;
                }).toList());
            }

            return dto;
        }).toList();
    }
    
    /**
     * Récupère les détails complets d'une patiente par ID
     */
    @Transactional(readOnly = true)
    public PatienteDetailDto getPatienteDetailsById(Long id) {
        Patiente patiente = patienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "id", id));
        
        PatienteDetailDto dto = new PatienteDetailDto();
        dto.setId(patiente.getId());
        dto.setNom(patiente.getNom());
        dto.setPrenom(patiente.getPrenom());
        dto.setTelephone(patiente.getTelephone());
        dto.setDateDeNaissance(patiente.getDateDeNaissance());
        dto.setAdresse(patiente.getAdresse());
        dto.setAge(Period.between(patiente.getDateDeNaissance(), LocalDate.now()).getYears());
        
        // Médecin assigné
        if (patiente.getProfessionnelSanteAssigne() != null) {
            ProfessionnelSante medecin = patiente.getProfessionnelSanteAssigne();
            PatienteDetailDto.MedecinBrief medecinDto = new PatienteDetailDto.MedecinBrief();
            medecinDto.setId(medecin.getId());
            medecinDto.setNom(medecin.getNom());
            medecinDto.setPrenom(medecin.getPrenom());
            medecinDto.setTelephone(medecin.getTelephone());
            medecinDto.setSpecialite(medecin.getSpecialite() != null ? medecin.getSpecialite().name() : null);
            dto.setMedecinAssigne(medecinDto);
        }
        
        // Grossesses
        if (patiente.getGrossesses() != null) {
            dto.setGrossesses(patiente.getGrossesses().stream().map(g -> {
                PatienteDetailDto.GrossesseDetail gd = new PatienteDetailDto.GrossesseDetail();
                gd.setId(g.getId());
                gd.setDateDebut(g.getDateDebut());
                gd.setDatePrevueAccouchement(g.getDatePrevueAccouchement());
                gd.setStatut(g.getStatut().name());
                gd.setNombreConsultations(g.getConsultationsPrenatales() != null ? g.getConsultationsPrenatales().size() : 0);
                return gd;
            }).collect(Collectors.toList()));
        }
        
        // Enfants
        if (patiente.getEnfants() != null) {
            dto.setEnfants(patiente.getEnfants().stream().map(e -> {
                PatienteDetailDto.EnfantDetail ed = new PatienteDetailDto.EnfantDetail();
                ed.setId(e.getId());
                ed.setNom(e.getNom());
                ed.setPrenom(e.getPrenom());
                ed.setDateDeNaissance(e.getDateDeNaissance());
                ed.setSexe(e.getSexe().name());
                ed.setAge(Period.between(e.getDateDeNaissance(), LocalDate.now()).getYears());
                ed.setNombreVaccinations(e.getVaccinations() != null ? e.getVaccinations().size() : 0);
                ed.setNombreConsultations(e.getConsultationsPostnatales() != null ? e.getConsultationsPostnatales().size() : 0);
                return ed;
            }).collect(Collectors.toList()));
        }
        
        // Consultations prénatales
        // Récupérer via les grossesses de la patiente
        List<ConsultationPrenatale> consultationsPrenatales = new ArrayList<>();
        if (patiente.getGrossesses() != null) {
            for (Grossesse grossesse : patiente.getGrossesses()) {
                if (grossesse.getConsultationsPrenatales() != null) {
                    consultationsPrenatales.addAll(grossesse.getConsultationsPrenatales());
                }
            }
        }
        if (!consultationsPrenatales.isEmpty()) {
            dto.setConsultationsPrenatales(consultationsPrenatales.stream().map(cpn -> {
                PatienteDetailDto.ConsultationPrenataleDetail cpnDto = new PatienteDetailDto.ConsultationPrenataleDetail();
                cpnDto.setId(cpn.getId());
                cpnDto.setDatePrevue(cpn.getDatePrevue());
                cpnDto.setDateRealisee(cpn.getDateRealisee());
                cpnDto.setStatut(cpn.getStatut() != null ? cpn.getStatut().name() : null);
                if (cpn.getSuiviConsultation() != null) {
                    cpnDto.setPoids(cpn.getSuiviConsultation().getPoids());
                    cpnDto.setTensionArterielle(cpn.getSuiviConsultation().getTensionArterielle());
                    cpnDto.setHauteurUterine(cpn.getSuiviConsultation().getHauteurUterine());
                }
                cpnDto.setNotes(cpn.getNotes());
                return cpnDto;
            }).collect(Collectors.toList()));
        }
        
        // Consultations postnatales
        List<ConsultationPostnatale> consultationsPostnatales = consultationPostnataleRepository
                .findByPatienteId(patiente.getId());
        if (consultationsPostnatales != null && !consultationsPostnatales.isEmpty()) {
            dto.setConsultationsPostnatales(consultationsPostnatales.stream().map(cpon -> {
                PatienteDetailDto.ConsultationPostnataleDetail cponDto = new PatienteDetailDto.ConsultationPostnataleDetail();
                cponDto.setId(cpon.getId());
                cponDto.setType(cpon.getType());
                cponDto.setDatePrevue(cpon.getDatePrevue());
                cponDto.setDateRealisee(cpon.getDateRealisee());
                cponDto.setStatut(cpon.getStatut() != null ? cpon.getStatut().name() : null);
                cponDto.setNotesMere(cpon.getNotesMere());
                cponDto.setNotesNouveauNe(cpon.getNotesNouveauNe());
                return cponDto;
            }).collect(Collectors.toList()));
        }
        
        return dto;
    }
}
