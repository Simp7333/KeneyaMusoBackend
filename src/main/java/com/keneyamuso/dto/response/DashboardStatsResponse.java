package com.keneyamuso.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse pour les statistiques du tableau de bord d'un professionnel de santé.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalPatientes;
    private long suivisTermines;
    private long suivisEnCours;
    private long rappelsActifs;      // Rappels CPN/CPON/Vaccination non lus
    private long alertesActives;     // Soumissions de dossiers en attente
}


