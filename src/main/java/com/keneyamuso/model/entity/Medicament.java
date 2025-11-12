package com.keneyamuso.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medicament {
    private String nom;
    private String posologie;
    private String duree;
    private String observation;
}
