package com.keneyamuso.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.keneyamuso.model.enums.SubmissionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DossierSubmissionRequest {

    @NotNull(message = "Le type de formulaire est obligatoire")
    private SubmissionType type;

    @NotNull(message = "Le contenu du formulaire est obligatoire")
    private JsonNode data;
    
    // Téléphone du médecin auquel la patiente soumet le dossier (optionnel)
    private String medecinTelephone;
}


