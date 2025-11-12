package com.keneyamuso.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO pour la création/modification d'une grossesse.
 * 
 * La date de dernière menstruation (LMP - Last Menstrual Period) est utilisée
 * pour calculer automatiquement la DPA (Date Prévue d'Accouchement) :
 * DPA = LMP + 280 jours (40 semaines)
 * 
 * Lors de la création, 4 CPN sont automatiquement générées selon le calendrier :
 * - CPN1 : LMP + 12 semaines (1er trimestre)
 * - CPN2 : LMP + 24 semaines (2e trimestre)
 * - CPN3 : LMP + 32 semaines (3e trimestre)
 * - CPN4 : LMP + 36 semaines (3e trimestre avancé)
 */
@Data
public class GrossesseRequest {
    
    @NotNull(message = "La date de dernière menstruation (LMP) est obligatoire")
    private LocalDate dateDernieresMenstruations; // LMP
    
    @NotNull(message = "L'ID de la patiente est obligatoire")
    private Long patienteId;
}

