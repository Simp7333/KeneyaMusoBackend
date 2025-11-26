package com.keneyamuso.dto.request;

import com.keneyamuso.model.enums.GroupeSanguin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FormulaireCPNRequest {
    
    private Double taille;
    
    private Double poids;
    
    private LocalDate dernierControle;
    
    @NotNull(message = "La date des dernières règles est obligatoire")
    private LocalDate dateDernieresRegles;
    
    private Integer nombreMoisGrossesse;
    
    private GroupeSanguin groupeSanguin;
    
    private boolean complications;
    
    private String complicationsDetails;
    
    private boolean mouvementsBebeReguliers;
    
    private List<String> symptomes;
    
    private String symptomesAutre;
    
    private boolean prendMedicamentsOuVitamines;
    
    private String medicamentsOuVitaminesDetails;
    
    private boolean aEuMaladies;
    
    private String maladiesDetails;
}

