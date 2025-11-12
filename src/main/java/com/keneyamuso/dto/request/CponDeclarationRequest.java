package com.keneyamuso.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CponDeclarationRequest {

    @NotNull(message = "L'ID de la patiente est obligatoire")
    private Long patienteId;

    @NotNull(message = "La date d'accouchement est obligatoire")
    private LocalDate dateAccouchement;
}
