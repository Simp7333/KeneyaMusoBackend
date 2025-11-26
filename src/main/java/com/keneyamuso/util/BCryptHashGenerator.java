package com.keneyamuso.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitaire pour générer des hash BCrypt
 * 
 * Utilisation:
 * 1. Exécutez cette classe avec votre mot de passe
 * 2. Copiez le hash généré
 * 3. Utilisez-le dans le script SQL V3__create_default_admin.sql
 */
public class BCryptHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Mot de passe par défaut pour l'admin
        String password = "admin123";
        
        // Générer le hash
        String hash = encoder.encode(password);
        
        System.out.println("==========================================");
        System.out.println("Mot de passe: " + password);
        System.out.println("Hash BCrypt: " + hash);
        System.out.println("==========================================");
        System.out.println("\nCopiez le hash ci-dessus dans le script SQL V3__create_default_admin.sql");
        
        // Vérifier que le hash fonctionne
        boolean matches = encoder.matches(password, hash);
        System.out.println("Vérification: " + (matches ? "✓ OK" : "✗ ERREUR"));
    }
}

