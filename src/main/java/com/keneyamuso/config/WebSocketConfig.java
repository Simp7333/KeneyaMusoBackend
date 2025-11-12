package com.keneyamuso.config;

import com.keneyamuso.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket pour la messagerie en temps réel.
 * 
 * Cette configuration permet aux patientes et professionnels de santé de communiquer
 * en temps réel via WebSocket/STOMP. Les messages sont diffusés instantanément aux
 * participants d'une conversation.
 * 
 * Architecture :
 * - Endpoint : /ws (connexion WebSocket)
 * - Broker : /topic (messages publics), /queue (messages privés)
 * - Destination : /app (préfixe pour les messages entrants)
 * 
 * Sécurité :
 * - Authentification JWT via header "Authorization"
 * - Vérification de l'identité de l'expéditeur
 * - Validation des permissions sur les conversations
 * 
 * @author KènèyaMuso Team
 * @version 1.0
 * @since 2025-10-16
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Configure le message broker.
     * 
     * - /topic : Pour les messages diffusés à plusieurs abonnés (conversations de groupe)
     * - /queue : Pour les messages privés (1 à 1)
     * - /app : Préfixe pour les destinations des messages envoyés par les clients
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages from clients
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Enregistre les endpoints WebSocket.
     * 
     * - /ws : Point de connexion WebSocket
     * - SockJS : Fallback pour navigateurs ne supportant pas WebSocket
     * - CORS : Autorise les connexions depuis le frontend
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // À restreindre en production
                .withSockJS(); // Fallback pour navigateurs anciens
    }

    /**
     * Configure le canal de messages entrants avec authentification JWT.
     * 
     * Intercepte tous les messages CONNECT pour :
     * 1. Extraire le token JWT du header "Authorization"
     * 2. Valider le token
     * 3. Charger l'utilisateur
     * 4. Définir l'authentification dans le contexte de sécurité
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extraire le token JWT du header
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        
                        try {
                            // Valider et extraire le téléphone du token
                            if (jwtTokenProvider.validateToken(token)) {
                                String telephone = jwtTokenProvider.getTelephoneFromToken(token);
                                
                                // Charger l'utilisateur
                                UserDetails userDetails = userDetailsService.loadUserByUsername(telephone);
                                
                                // Créer l'authentification
                                UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(
                                        userDetails, 
                                        null, 
                                        userDetails.getAuthorities()
                                    );
                                
                                // Définir l'utilisateur dans la session WebSocket
                                accessor.setUser(authentication);
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }
                        } catch (Exception e) {
                            // Token invalide - la connexion sera refusée
                            throw new IllegalArgumentException("Token JWT invalide", e);
                        }
                    } else {
                        throw new IllegalArgumentException("Header Authorization manquant");
                    }
                }
                
                return message;
            }
        });
    }
}

