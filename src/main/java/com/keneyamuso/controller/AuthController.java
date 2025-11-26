package com.keneyamuso.controller;

import com.keneyamuso.dto.request.LoginRequest;
import com.keneyamuso.dto.request.RegisterRequest;
import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.dto.response.JwtAuthResponse;
import com.keneyamuso.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour l'authentification (inscription et connexion)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "APIs pour l'inscription et la connexion")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Permet à un utilisateur de se connecter avec son téléphone et mot de passe")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        JwtAuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription", description = "Permet à un nouvel utilisateur de s'inscrire")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        JwtAuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Inscription réussie", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Permet à un utilisateur de se déconnecter")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Pour un système JWT, la déconnexion est principalement côté client
        // Ici, on peut logger l'événement ou invalider le token dans une blacklist si nécessaire
        
        // Log de la déconnexion
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // TODO: Optionnel - Ajouter le token à une blacklist Redis pour invalidation
            System.out.println("Utilisateur déconnecté - Token invalidé: " + token.substring(0, Math.min(20, token.length())) + "...");
        }
        
        return ResponseEntity.ok(ApiResponse.success("Déconnexion réussie", null));
    }
}

