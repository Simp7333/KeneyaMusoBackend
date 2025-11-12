package com.keneyamuso.dto.response;

import com.keneyamuso.model.enums.SubmissionStatus;
import com.keneyamuso.model.enums.SubmissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierSubmissionResponse {

    private Long id;
    private SubmissionType type;
    private SubmissionStatus status;
    private Long patienteId;
    private String patienteNom;
    private String patientePrenom;
    private String payload;
    private String commentaire;
    private LocalDateTime dateCreation;
}


