package com.keneyamuso.mapper;

import com.keneyamuso.dto.response.PatienteListDto;
import com.keneyamuso.model.entity.Patiente;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PatienteMapper {

    public PatienteListDto toDto(Patiente patiente) {
        PatienteListDto dto = new PatienteListDto();

        dto.setId(patiente.getId());
        dto.setNom(patiente.getNom());
        dto.setPrenom(patiente.getPrenom());
        dto.setTelephone(patiente.getTelephone());
        dto.setDateDeNaissance(patiente.getDateDeNaissance());
        dto.setAdresse(patiente.getAdresse());

        dto.setGrossesses(
                patiente.getGrossesses().stream().map(g -> {
                    PatienteListDto.GrossesseBrief gb = new PatienteListDto.GrossesseBrief();
                    gb.setId(g.getId());
                    gb.setDateDebut(g.getDateDebut());
                    gb.setDatePrevueAccouchement(g.getDatePrevueAccouchement());
                    gb.setStatut(g.getStatut().name());
                    return gb;
                }).collect(Collectors.toList())
        );

        dto.setEnfants(
                patiente.getEnfants().stream().map(e -> {
                    PatienteListDto.EnfantBrief eb = new PatienteListDto.EnfantBrief();
                    eb.setId(e.getId());
                    eb.setNom(e.getNom());
                    eb.setPrenom(e.getPrenom());
                    eb.setDateDeNaissance(e.getDateDeNaissance());
                    eb.setSexe(e.getSexe().name());
                    return eb;
                }).collect(Collectors.toList())
        );

        return dto;
    }
}
