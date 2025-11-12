package com.keneyamuso.dto.request;

import com.keneyamuso.model.enums.EtatConjonctives;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SuiviConsultationRequest {
    private String ageGrossesse;
    private Double poids;
    private String tensionArterielle;
    private Double hauteurUterine;
    private String mouvementsFoetaux;
    private String bruitsDuCoeur;
    private String oedeme;
    private String albumine;
    private String etatCol;
    private String toucherVaginal;
    private String observations;
    private LocalDate dateProchainRendezVous;
    private String soinsCuratifs;
    private EtatConjonctives etatConjonctives;
    private Double gainPoidsDepuisDebutGrossesse;
    private String examenObstetrical;
    private String inspectionPalpation;
    private String presentation;
    private String etatBassinAtteintePromontoire;
    private String recommandationsAccouchement;
}
