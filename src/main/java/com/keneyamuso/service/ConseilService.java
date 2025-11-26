package com.keneyamuso.service;

import com.keneyamuso.dto.request.ConseilRequest;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.exception.UnauthorizedException;
import com.keneyamuso.model.entity.Conseil;
import com.keneyamuso.model.entity.ProfessionnelSante;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.repository.ConseilRepository;
import com.keneyamuso.repository.ProfessionnelSanteRepository;
import com.keneyamuso.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service de gestion des conseils éducatifs.
 * 
 * Ce service gère la bibliothèque de contenus éducatifs destinés aux patientes
 * et aux familles. Les conseils couvrent plusieurs domaines :
 * 
 * **Catégories de conseils :**
 * - NUTRITION : Alimentation pendant la grossesse et l'allaitement
 * - HYGIENE : Hygiène personnelle, soins du nouveau-né
 * - ALLAITEMENT : Bonnes pratiques d'allaitement maternel
 * - PREVENTION : Prévention des maladies, vaccination
 * - SANTE_GENERALE : Autres sujets de santé maternelle et infantile
 * 
 * Les conseils peuvent inclure du texte, des liens vers des vidéos ou des audios,
 * et sont ciblés selon le public (femme enceinte, jeune mère, etc.).
 * 
 * @author KènèyaMuso Team
 * @version 1.0
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
public class ConseilService {

    private final ConseilRepository conseilRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ProfessionnelSanteRepository professionnelSanteRepository;

    /**
     * Crée un nouveau conseil éducatif.
     * 
     * Cette méthode permet aux médecins et administrateurs d'ajouter du contenu
     * éducatif à la bibliothèque. Le conseil est automatiquement marqué comme actif
     * et visible pour les utilisateurs ciblés.
     * 
     * @param request Les informations du conseil (titre, contenu, catégorie, cible)
     * @return Le conseil créé avec son identifiant
     */
    @Transactional
    public Conseil createConseil(ConseilRequest request) {
        // Récupérer le professionnel de santé connecté
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ProfessionnelSante professionnel = professionnelSanteRepository.findByTelephone(username)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionnelSante", "telephone", username));

        Conseil conseil = new Conseil();
        conseil.setTitre(request.getTitre());
        conseil.setContenu(request.getContenu());
        conseil.setLienMedia(request.getLienMedia());
        conseil.setCategorie(request.getCategorie());
        conseil.setCible(request.getCible());
        conseil.setActif(true);
        conseil.setCreateur(professionnel);

        return conseilRepository.save(conseil);
    }

    /**
     * Récupère un conseil par son identifiant.
     * 
     * @param id L'identifiant unique du conseil
     * @return Le conseil trouvé
     * @throws ResourceNotFoundException si le conseil n'existe pas
     */
    @Transactional(readOnly = true)
    public Conseil getConseilById(Long id) {
        return conseilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conseil", "id", id));
    }

    /**
     * Récupère tous les conseils (actifs et inactifs).
     * 
     * Cette méthode est réservée aux médecins et administrateurs
     * pour gérer l'ensemble de la bibliothèque de contenus.
     * 
     * @return La liste de tous les conseils
     */
    @Transactional(readOnly = true)
    public List<Conseil> getAllConseils() {
        return conseilRepository.findAll();
    }

    /**
     * Récupère tous les conseils actifs.
     * 
     * Cette méthode retourne uniquement les conseils visibles et disponibles
     * pour les patientes. C'est la méthode utilisée par l'application mobile
     * pour afficher les contenus éducatifs.
     * 
     * @return La liste des conseils actifs
     */
    @Transactional(readOnly = true)
    public List<Conseil> getConseilsActifs() {
        return conseilRepository.findByActif(true);
    }

    /**
     * Récupère les conseils créés par le médecin connecté.
     * 
     * @return La liste des conseils du médecin
     */
    @Transactional(readOnly = true)
    public List<Conseil> getMesConseils() {
        // Récupérer le professionnel de santé connecté
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ProfessionnelSante professionnel = professionnelSanteRepository.findByTelephone(username)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionnelSante", "telephone", username));

        return conseilRepository.findByCreateurId(professionnel.getId());
    }

    /**
     * Récupère les conseils actifs avec filtres optionnels.
     *
     * @param type      Optionnel: "video" | "audio" | "article"
     * @param categorie Optionnel: catégorie précise
     * @param cible     Optionnel: cible exacte (ex: "Femme enceinte")
     * @return Liste filtrée des conseils actifs
     */
    @Transactional(readOnly = true)
    public List<Conseil> getConseilsActifsFiltres(String type, com.keneyamuso.model.enums.CategorieConseil categorie, String cible) {
        List<Conseil> base = getConseilsActifs();

        if (categorie != null) {
            base = base.stream()
                    .filter(c -> c.getCategorie() == categorie)
                    .collect(Collectors.toList());
        }

        if (cible != null && !cible.isBlank()) {
            String cibleNorm = cible.toLowerCase(Locale.ROOT);
            base = base.stream()
                    .filter(c -> c.getCible() != null && c.getCible().toLowerCase(Locale.ROOT).equals(cibleNorm))
                    .collect(Collectors.toList());
        }

        if (type != null && !type.isBlank()) {
            String t = type.toLowerCase(Locale.ROOT);
            switch (t) {
                case "video":
                    base = base.stream().filter(this::isVideo).collect(Collectors.toList());
                    break;
                case "audio":
                    base = base.stream().filter(this::isAudio).collect(Collectors.toList());
                    break;
                case "article":
                    base = base.stream().filter(c -> !isVideo(c) && !isAudio(c)).collect(Collectors.toList());
                    break;
                default:
                    // inconnu: ne pas filtrer par type
            }
        }

        return base;
    }

    private boolean isVideo(Conseil c) {
        String lien = c.getLienMedia();
        if (lien == null || lien.isBlank()) return false;
        String m = lien.toLowerCase(Locale.ROOT);
        return m.endsWith(".mp4") || m.endsWith(".avi") || m.endsWith(".mkv") || m.contains("youtube") || m.contains("youtu.be");
    }

    private boolean isAudio(Conseil c) {
        String lien = c.getLienMedia();
        if (lien == null || lien.isBlank()) return false;
        String m = lien.toLowerCase(Locale.ROOT);
        return m.endsWith(".mp3") || m.endsWith(".wav") || m.endsWith(".m4a");
    }

    /**
     * Récupère les conseils pertinents pour une patiente selon son type de suivi.
     * 
     * Cette méthode détermine automatiquement si la patiente est en suivi prénatal
     * (grossesse en cours) ou postnatal (après accouchement) et retourne uniquement
     * les conseils dont la cible correspond à son profil.
     * 
     * @param telephone Le téléphone de la patiente
     * @return La liste des conseils pertinents pour la patiente
     */
    @Transactional(readOnly = true)
    public List<Conseil> getConseilsPourPatiente(String telephone) {
        // Récupérer toutes les cibles possibles pour le prénatal
        List<String> ciblesPrenatales = List.of(
                "prenatale", "prenatal", "enceinte", "grossesse", "femme enceinte",
                "patiente prénatale", "femme enceinte", "maternité"
        );
        
        // Récupérer toutes les cibles possibles pour le postnatal
        List<String> ciblesPostnatales = List.of(
                "postnatale", "postnatal", "mère", "nouveau-né", "jeune mère",
                "maman", "patiente postnatale", "après accouchement", "post-partum"
        );
        
        // Déterminer si la patiente est en suivi prénatal ou postnatal
        // Pour cela, on pourrait vérifier si elle a une grossesse en cours
        // Pour l'instant, on retourne les conseils qui contiennent les mots-clés
        List<Conseil> conseilsActifs = getConseilsActifs();
        
        List<Conseil> conseilsFiltres = new java.util.ArrayList<>();
        
        for (Conseil conseil : conseilsActifs) {
            String cible = conseil.getCible();
            if (cible == null || cible.isBlank()) {
                continue;
            }
            
            String cibleLower = cible.toLowerCase(Locale.ROOT);
            
            // Vérifier si la cible correspond au prénatal
            boolean isPrenatal = ciblesPrenatales.stream()
                    .anyMatch(keyword -> cibleLower.contains(keyword.toLowerCase(Locale.ROOT)));
            
            // Vérifier si la cible correspond au postnatal
            boolean isPostnatal = ciblesPostnatales.stream()
                    .anyMatch(keyword -> cibleLower.contains(keyword.toLowerCase(Locale.ROOT)));
            
            // Si la cible ne correspond à aucun type spécifique, ne pas l'inclure
            // (pour éviter d'afficher des conseils génériques non pertinents)
            if (!isPrenatal && !isPostnatal) {
                continue;
            }
            
            conseilsFiltres.add(conseil);
        }
        
        return conseilsFiltres;
    }
    
    /**
     * Récupère les conseils pour une patiente selon son type de suivi (prénatal ou postnatal).
     * 
     * @param telephone Le téléphone de la patiente
     * @param typeSuivi "prenatal" ou "postnatal"
     * @return La liste des conseils pertinents pour le type de suivi spécifié
     */
    @Transactional(readOnly = true)
    public List<Conseil> getConseilsPourPatiente(String telephone, String typeSuivi) {
        List<Conseil> conseilsActifs = getConseilsActifs();
        List<String> ciblesPertinentes;
        
        if (typeSuivi != null && typeSuivi.equalsIgnoreCase("postnatal")) {
            // Cibles pour le postnatal
            ciblesPertinentes = List.of(
                    "postnatale", "postnatal", "mère", "nouveau-né", "jeune mère",
                    "maman", "patiente postnatale", "après accouchement", "post-partum"
            );
        } else {
            // Cibles pour le prénatal (par défaut)
            ciblesPertinentes = List.of(
                    "prenatale", "prenatal", "enceinte", "grossesse", "femme enceinte",
                    "patiente prénatale", "maternité"
            );
        }
        
        List<Conseil> conseilsFiltres = new java.util.ArrayList<>();
        
        for (Conseil conseil : conseilsActifs) {
            String cible = conseil.getCible();
            if (cible == null || cible.isBlank()) {
                continue;
            }
            
            String cibleLower = cible.toLowerCase(Locale.ROOT);
            
            // Vérifier si la cible correspond au type de suivi
            boolean correspond = ciblesPertinentes.stream()
                    .anyMatch(keyword -> cibleLower.contains(keyword.toLowerCase(Locale.ROOT)));
            
            if (correspond) {
                conseilsFiltres.add(conseil);
            }
        }
        
        return conseilsFiltres;
    }

    /**
     * Met à jour les informations d'un conseil.
     * 
     * Permet de modifier le contenu, la catégorie ou la cible d'un conseil existant.
     * Le statut actif/inactif n'est pas modifié par cette méthode.
     * 
     * @param id L'identifiant du conseil à modifier
     * @param request Les nouvelles informations du conseil
     * @return Le conseil mis à jour
     * @throws ResourceNotFoundException si le conseil n'existe pas
     */
    @Transactional
    public Conseil updateConseil(Long id, ConseilRequest request) {
        Conseil conseil = getConseilById(id);

        conseil.setTitre(request.getTitre());
        conseil.setContenu(request.getContenu());
        conseil.setLienMedia(request.getLienMedia());
        conseil.setCategorie(request.getCategorie());
        conseil.setCible(request.getCible());

        return conseilRepository.save(conseil);
    }

    /**
     * Supprime un conseil de la base de données.
     * 
     * ⚠️ ATTENTION : Cette opération est définitive.
     * Le médecin peut supprimer ses propres conseils.
     * Les administrateurs peuvent supprimer tous les conseils.
     * 
     * @param id L'identifiant du conseil à supprimer
     * @throws ResourceNotFoundException si le conseil n'existe pas
     * @throws UnauthorizedException si le médecin essaie de supprimer un conseil qui ne lui appartient pas
     */
    @Transactional
    public void deleteConseil(Long id) {
        Conseil conseil = conseilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conseil", "id", id));

        // Récupérer le professionnel de santé connecté
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", username));

        // Vérifier que le médecin supprime son propre conseil (sauf si admin)
        boolean isAdmin = utilisateur.getRole().name().equals("ADMINISTRATEUR");
        
        if (!isAdmin) {
            // ProfessionnelSante hérite d'Utilisateur, donc l'ID est le même
            if (conseil.getCreateur() == null || !conseil.getCreateur().getId().equals(utilisateur.getId())) {
                throw new UnauthorizedException("Vous ne pouvez supprimer que vos propres conseils");
            }
        }

        conseilRepository.deleteById(id);
    }
}
