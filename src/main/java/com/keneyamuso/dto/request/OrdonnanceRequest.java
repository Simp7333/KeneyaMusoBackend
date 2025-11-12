package com.keneyamuso.dto.request;

import com.keneyamuso.model.entity.Medicament;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrdonnanceRequest {
    @NotNull
    private Long medecinId;

    private List<Medicament> medicaments;
    private String observations;
}
