package com.keneyamuso.dto.request;

import com.keneyamuso.model.enums.AlimentationBebe;
import com.keneyamuso.model.enums.Sexe;
import com.keneyamuso.model.enums.TypeAccouchement;
import lombok.Data;

import java.util.List;

@Data
public class FormulaireCPONRequest {
    private TypeAccouchement accouchementType;
    private String nombreEnfants;
    private List<String> sentiment;
    private boolean saignements;
    private String consultation;
    private Sexe sexeBebe;
    private AlimentationBebe alimentation;
}
