package com.keneyamuso.service;

import com.keneyamuso.dto.request.LoginRequest;
import com.keneyamuso.dto.request.RegisterRequest;
import com.keneyamuso.dto.response.JwtAuthResponse;
import com.keneyamuso.exception.BadRequestException;
import com.keneyamuso.model.entity.Patiente;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.repository.UtilisateurRepository;
import com.keneyamuso.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'authentification et d'inscription.
 * 
 * Ce service gère l'authentification des utilisateurs (patientes, médecins, administrateurs)
 * via JWT (JSON Web Token). Il permet l'inscription de nouveaux utilisateurs et la connexion
 * sécurisée avec génération de tokens.
 * 
 * @author KènèyaMuso Team
 * @version 1.0
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * Authentifie un utilisateur et génère un token JWT.
     * 
     * Cette méthode vérifie les credentials (téléphone + mot de passe) de l'utilisateur,
     * génère un token JWT valide pour 24 heures et retourne les informations de l'utilisateur.
     * 
     * @param request Les credentials de connexion (téléphone et mot de passe)
     * @return JwtAuthResponse contenant le token JWT et les informations de l'utilisateur
     * @throws BadRequestException si le téléphone ou le mot de passe est incorrect
     * @throws BadRequestException si l'utilisateur n'existe pas
     */
    @Transactional
    public JwtAuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getTelephone(),
                        request.getMotDePasse()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        // Le téléphone dans l'authentification est celui de l'utilisateur trouvé par CustomUserDetailsService
        // (qui correspond au téléphone stocké dans la base)
        String telephone = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new BadRequestException("Utilisateur non trouvé"));

        JwtAuthResponse.JwtAuthResponseBuilder responseBuilder = JwtAuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .telephone(utilisateur.getTelephone())
                .role(utilisateur.getRole())
                .photoProfil(utilisateur.getPhotoProfil());
        
        // Si c'est une Patiente, ajouter la date de naissance
        if (utilisateur instanceof Patiente) {
            responseBuilder.dateDeNaissance(((Patiente) utilisateur).getDateDeNaissance());
        }
        
        return responseBuilder.build();
    }

    /**
     * Inscrit un nouvel utilisateur dans l'application avec profil complet.
     * 
     * Cette méthode crée un compte selon le rôle spécifié :
     * - PATIENTE : Crée un profil Patiente avec date de naissance, adresse, et médecin assigné
     * - MEDECIN : Crée un profil ProfessionnelSante avec spécialité et identifiant professionnel
     * - ADMINISTRATEUR : Crée un utilisateur de base
     * 
     * Le mot de passe est encodé avec BCrypt et un token JWT est généré pour connexion immédiate.
     * 
     * @param request Les informations d'inscription complètes selon le rôle
     * @return JwtAuthResponse contenant le token JWT et les informations du nouvel utilisateur
     * @throws BadRequestException si le numéro de téléphone est déjà utilisé
     * @throws BadRequestException si les champs obligatoires pour le rôle sont manquants
     */
    @Transactional
    public JwtAuthResponse register(RegisterRequest request) {
        // Vérifier si le téléphone existe déjà
        if (utilisateurRepository.existsByTelephone(request.getTelephone())) {
            throw new BadRequestException("Ce numéro de téléphone est déjà utilisé");
        }

        Utilisateur utilisateur;

        // Créer l'utilisateur selon le rôle
        switch (request.getRole()) {
            case PATIENTE:
                utilisateur = creerPatiente(request);
                break;
            case MEDECIN:
                utilisateur = creerProfessionnelSante(request);
                break;
            case ADMINISTRATEUR:
                utilisateur = creerUtilisateurBase(request);
                break;
            default:
                throw new BadRequestException("Rôle non reconnu");
        }

        utilisateur = utilisateurRepository.save(utilisateur);

        // Générer le token
        String token = tokenProvider.generateTokenFromTelephone(utilisateur.getTelephone());

        JwtAuthResponse.JwtAuthResponseBuilder responseBuilder = JwtAuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .telephone(utilisateur.getTelephone())
                .role(utilisateur.getRole())
                .photoProfil(utilisateur.getPhotoProfil());
        
        // Si c'est une Patiente, ajouter la date de naissance
        if (utilisateur instanceof Patiente) {
            responseBuilder.dateDeNaissance(((Patiente) utilisateur).getDateDeNaissance());
        }
        
        return responseBuilder.build();
    }

    /**
     * Crée une Patiente avec son profil complet.
     */
    private com.keneyamuso.model.entity.Patiente creerPatiente(RegisterRequest request) {
        if (request.getDateDeNaissance() == null) {
            throw new BadRequestException("La date de naissance est obligatoire pour une patiente");
        }

        com.keneyamuso.model.entity.Patiente patiente = new com.keneyamuso.model.entity.Patiente();
        patiente.setNom(request.getNom());
        patiente.setPrenom(request.getPrenom());
        patiente.setTelephone(request.getTelephone());
        patiente.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        patiente.setRole(request.getRole());
        patiente.setLangue(request.getLangue());
        patiente.setActif(true);
        patiente.setPhotoProfil(request.getPhotoProfil());
        
        // Champs spécifiques Patiente
        patiente.setDateDeNaissance(request.getDateDeNaissance());
        patiente.setAdresse(request.getAdresse());

        // Assigner un professionnel de santé si spécifié
        if (request.getProfessionnelSanteId() != null) {
            com.keneyamuso.model.entity.ProfessionnelSante professionnel = utilisateurRepository
                    .findById(request.getProfessionnelSanteId())
                    .filter(u -> u instanceof com.keneyamuso.model.entity.ProfessionnelSante)
                    .map(u -> (com.keneyamuso.model.entity.ProfessionnelSante) u)
                    .orElseThrow(() -> new BadRequestException("Professionnel de santé non trouvé"));
            patiente.setProfessionnelSanteAssigne(professionnel);
        }

        return patiente;
    }

    /**
     * Crée un ProfessionnelSante avec ses informations professionnelles.
     */
    private com.keneyamuso.model.entity.ProfessionnelSante creerProfessionnelSante(RegisterRequest request) {
        if (request.getSpecialite() == null || request.getIdentifiantProfessionnel() == null) {
            throw new BadRequestException("La spécialité et l'identifiant professionnel sont obligatoires pour un médecin");
        }

        com.keneyamuso.model.entity.ProfessionnelSante professionnel = new com.keneyamuso.model.entity.ProfessionnelSante();
        professionnel.setNom(request.getNom());
        professionnel.setPrenom(request.getPrenom());
        professionnel.setTelephone(request.getTelephone());
        professionnel.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        professionnel.setRole(request.getRole());
        professionnel.setLangue(request.getLangue());
        professionnel.setActif(true);
        professionnel.setPhotoProfil(request.getPhotoProfil());
        
        // Champs spécifiques ProfessionnelSante
        professionnel.setSpecialite(request.getSpecialite());
        professionnel.setIdentifiantProfessionnel(request.getIdentifiantProfessionnel());

        return professionnel;
    }

    /**
     * Crée un utilisateur de base (Administrateur).
     */
    private Utilisateur creerUtilisateurBase(RegisterRequest request) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setRole(request.getRole());
        utilisateur.setLangue(request.getLangue());
        utilisateur.setActif(true);
        utilisateur.setPhotoProfil(request.getPhotoProfil());

        return utilisateur;
    }
}

