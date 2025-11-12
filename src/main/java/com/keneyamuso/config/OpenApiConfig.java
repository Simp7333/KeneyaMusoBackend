package com.keneyamuso.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Configuration de la documentation OpenAPI/Swagger
 */
@OpenAPIDefinition(
        info = @Info(
                title = "KènèyaMuso API",
                version = "1.0.0",
                description = "API REST pour l'application de suivi de santé maternelle et infantile au Mali.\n\n" +
                        "Cette API permet de :\n" +
                        "- Gérer les utilisateurs (patientes, professionnels de santé)\n" +
                        "- Suivre les grossesses (CPN, DPA)\n" +
                        "- Gérer les consultations prénatales et postnatales\n" +
                        "- Gérer le calendrier vaccinal des enfants\n" +
                        "- Fournir des conseils éducatifs\n" +
                        "- Permettre la communication entre patientes et soignants\n\n" +
                        "**Authentification :** L'API utilise JWT (JSON Web Token) pour sécuriser les endpoints.",
                contact = @Contact(
                        name = "Équipe KènèyaMuso",
                        email = "contact@keneyamuso.ml"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        description = "Environnement de développement",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Environnement de production",
                        url = "https://api.keneyamuso.ml"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Authentification JWT. Utilisez le token obtenu lors de la connexion.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Configuration déclarative via annotations
}

