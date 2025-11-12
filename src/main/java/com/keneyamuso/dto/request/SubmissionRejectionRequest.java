package com.keneyamuso.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmissionRejectionRequest {

    @NotBlank(message = "La raison du rejet est obligatoire")
    private String raison;
}
