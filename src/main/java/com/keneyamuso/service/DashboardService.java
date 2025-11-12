package com.keneyamuso.service;

import com.keneyamuso.dto.response.DashboardStatsResponse;
import com.keneyamuso.dto.response.PatienteListDto;
import com.keneyamuso.exception.BadRequestException;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.Patiente;
import com.keneyamuso.model.entity.ProfessionnelSante;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.model.enums.StatutRappel;
import com.keneyamuso.model.enums.SubmissionStatus;
import com.keneyamuso.repository.DossierMedicalSubmissionRepository;
import com.keneyamuso.repository.PatienteRepository;
import com.keneyamuso.repository.RappelRepository;
import com.keneyamuso.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final PatienteRepository patienteRepository;
    private final RappelRepository rappelRepository;
    private final DossierMedicalSubmissionRepository submissionRepository;

    // ====================== STATS DU MÉDECIN ======================
    @Transactional(readOnly = true)
    public DashboardStatsResponse getMedecinDashboardStats(String telephone) {
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));

        if (!(utilisateur instanceof ProfessionnelSante professionnelSante)) {
            throw new ResourceNotFoundException("Professionnel de santé", "telephone", telephone);
        }

        List<Long> patienteIds = patienteRepository.findIdsByProfessionnelSanteId(professionnelSante.getId());
        long totalPatientes = patienteIds.size();

        long suivisTermines = patienteRepository.countGrossessesTermineesByMedecinId(professionnelSante.getId());
        long suivisEnCours = patienteRepository.countGrossessesEnCoursByMedecinId(professionnelSante.getId());

        // Rappels = Notifications CPN/CPON/Vaccination non lues
        long rappelsActifs = rappelRepository.countByProfessionnelIdAndStatut(
                professionnelSante.getId(),
                StatutRappel.ENVOYE
        );
        
        // Alertes = Soumissions de dossiers en attente
        long alertesActives = submissionRepository.countByProfessionnelSanteIdAndStatus(
                professionnelSante.getId(),
                SubmissionStatus.EN_ATTENTE
        );

        return DashboardStatsResponse.builder()
                .totalPatientes(totalPatientes)
                .suivisTermines(suivisTermines)
                .suivisEnCours(suivisEnCours)
                .rappelsActifs(rappelsActifs)
                .alertesActives(alertesActives)
                .build();
    }

    // ====================== LISTE DES PATIENTES ======================
    @Transactional(readOnly = true)
    public List<PatienteListDto> getMedecinPatientes(String telephone, String typeSuivi) {
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));

        if (!(utilisateur instanceof ProfessionnelSante professionnelSante)) {
            throw new ResourceNotFoundException("Professionnel de santé", "telephone", telephone);
        }

        Long medecinId = professionnelSante.getId();
        final String filter = typeSuivi == null ? "" : typeSuivi.toUpperCase();

        List<Patiente> patientes;
        final Map<Long, List<PatienteListDto.EnfantBrief>> enfantsMap = new HashMap<>();
        
        switch (filter) {
            case "PRENATAL" -> patientes = patienteRepository.findPatientesWithGrossesseEnCours(medecinId);
            case "POSTNATAL" -> {
                List<Long> idsTerminees = patienteRepository.findIdsWithGrossesseTerminee(medecinId);
                List<Long> idsAvecEnfants = patienteRepository.findIdsWithEnfants(medecinId);
                Set<Long> allIds = new HashSet<>(idsTerminees);
                allIds.addAll(idsAvecEnfants);
                
                if (allIds.isEmpty()) {
                    patientes = List.of();
                } else {
                    List<Long> idsList = new ArrayList<>(allIds);
                    
                    // Charger les patientes d'abord
                    patientes = patienteRepository.findByIdIn(idsList);
                    
                    // Ensuite charger les enfants séparément (évite MultipleBagFetchException)
                    List<Patiente> patientesAvecEnfants = patienteRepository.findByIdInWithEnfants(idsList);
                    
                    // Mapper les enfants
                    for (Patiente p : patientesAvecEnfants) {
                        if (p.getEnfants() != null && !p.getEnfants().isEmpty()) {
                            enfantsMap.put(p.getId(), p.getEnfants().stream()
                                    .map(e -> {
                                        var b = new PatienteListDto.EnfantBrief();
                                        b.setId(e.getId());
                                        b.setNom(e.getNom());
                                        b.setPrenom(e.getPrenom());
                                        b.setDateDeNaissance(e.getDateDeNaissance());
                                        b.setSexe(e.getSexe().name());
                                        return b;
                                    })
                                    .toList());
                        }
                    }
                }
            }
            case "ENFANTS" -> {
                patientes = patienteRepository.findPatientesWithEnfants(medecinId);
                // Charger les enfants pour le filtre ENFANTS
                for (Patiente p : patientes) {
                    if (p.getEnfants() != null && !p.getEnfants().isEmpty()) {
                        enfantsMap.put(p.getId(), p.getEnfants().stream()
                                .map(e -> {
                                    var b = new PatienteListDto.EnfantBrief();
                                    b.setId(e.getId());
                                    b.setNom(e.getNom());
                                    b.setPrenom(e.getPrenom());
                                    b.setDateDeNaissance(e.getDateDeNaissance());
                                    b.setSexe(e.getSexe().name());
                                    return b;
                                })
                                .toList());
                    }
                }
            }
            default -> patientes = patienteRepository.findByProfessionnelSanteId(medecinId);
        }

        // Mapping final vers DTO
        return patientes.stream().map(p -> {
            PatienteListDto dto = new PatienteListDto();
            dto.setId(p.getId());
            dto.setNom(p.getNom());
            dto.setPrenom(p.getPrenom());
            dto.setTelephone(p.getTelephone());
            dto.setDateDeNaissance(p.getDateDeNaissance());
            dto.setAdresse(p.getAdresse());

            // Grossesses : uniquement pour PRENATAL
            if ("PRENATAL".equals(filter)) {
                dto.setGrossesses(p.getGrossesses().stream()
                        .map(g -> {
                            var b = new PatienteListDto.GrossesseBrief();
                            b.setId(g.getId());
                            b.setDateDebut(g.getDateDebut());
                            b.setDatePrevueAccouchement(g.getDatePrevueAccouchement());
                            b.setStatut(g.getStatut().name());
                            return b;
                        })
                        .toList());
            } else {
                dto.setGrossesses(List.of());
            }

            // Enfants : pour POSTNATAL et ENFANTS
            if ("POSTNATAL".equals(filter) || "ENFANTS".equals(filter)) {
                dto.setEnfants(enfantsMap.getOrDefault(p.getId(), List.of()));
            } else {
                dto.setEnfants(List.of());
            }

            return dto;
        }).toList();
    }

    // ====================== ASSIGNER UNE PATIENTE ======================
    @Transactional
    public void assignerPatiente(String telephoneMedecin, Long patienteId) {
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephoneMedecin)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephoneMedecin));

        if (!(utilisateur instanceof ProfessionnelSante professionnelSante)) {
            throw new BadRequestException("L'utilisateur n'est pas un professionnel de santé.");
        }

        Patiente patiente = patienteRepository.findById(patienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "id", patienteId));

        if (patiente.getProfessionnelSanteAssigne() != null
                && !patiente.getProfessionnelSanteAssigne().getId().equals(professionnelSante.getId())) {
            throw new BadRequestException("Cette patiente est déjà assignée à un autre médecin.");
        }

        patiente.setProfessionnelSanteAssigne(professionnelSante);
        patienteRepository.save(patiente);
    }
}
