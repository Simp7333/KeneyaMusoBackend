package com.keneyamuso.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse pour les statistiques du tableau de bord administrateur.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStats {
    private long totalPatientes;
    private long totalProfessionnels;
    private long totalGrossessesEnCours;
    private long totalGrossessesTerminees;
    private long totalEnfants;
    private Double cpnRespectRate;      // Pourcentage CPN respectées
    private Double vaccinationRate;     // Pourcentage vaccinations à jour
    private long totalRappelsEnvoyes;
}

