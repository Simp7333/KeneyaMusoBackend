# 🔌 Guide WebSocket - Messagerie Temps Réel

## 📋 Vue d'ensemble

Le système de messagerie utilise **WebSocket avec STOMP** pour permettre la communication en temps réel entre patientes et professionnels de santé. Les messages sont reçus instantanément sans avoir besoin de recharger la page ou de faire du polling.

---

## 🏗️ Architecture WebSocket

### Stack Technologique

```
┌─────────────────────────────────────────────────────────┐
│                    FRONTEND                              │
│  - SockJS Client (fallback pour anciens navigateurs)   │
│  - STOMP.js (protocole messaging)                       │
│  - React/Vue/Angular                                    │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ WebSocket Connection
                     │ ws://localhost:8080/ws
                     │
┌────────────────────▼────────────────────────────────────┐
│                    BACKEND                               │
│  - Spring WebSocket                                     │
│  - STOMP Message Broker                                 │
│  - JWT Authentication                                   │
└─────────────────────────────────────────────────────────┘
```

### Flux de Communication

```
CLIENT 1 (Patiente)                 SERVEUR                    CLIENT 2 (Médecin)
       │                                │                              │
       │ 1. CONNECT /ws                 │                              │
       │    + JWT Token                 │                              │
       ├───────────────────────────────>│                              │
       │                                │                              │
       │ 2. SUBSCRIBE                   │                              │
       │    /topic/conversation/1       │                              │
       ├───────────────────────────────>│                              │
       │                                │      3. SUBSCRIBE            │
       │                                │      /topic/conversation/1   │
       │                                │<─────────────────────────────┤
       │                                │                              │
       │ 4. SEND                        │                              │
       │    /app/chat.sendMessage       │                              │
       │    {conversationId: 1, ...}    │                              │
       ├───────────────────────────────>│                              │
       │                                │                              │
       │                                │ 5. Sauvegarde en DB          │
       │                                │                              │
       │ 6. MESSAGE                     │ 7. MESSAGE                   │
       │    /topic/conversation/1       │    /topic/conversation/1     │
       │<───────────────────────────────┤                              │
       │                                ├─────────────────────────────>│
       │                                │                              │
```

---

## 💻 Implémentation Frontend

### 1. Installation des Dépendances

#### React/JavaScript

```bash
npm install sockjs-client @stomp/stompjs
```

#### TypeScript

```bash
npm install sockjs-client @stomp/stompjs
npm install --save-dev @types/sockjs-client
```

---

### 2. Configuration de la Connexion WebSocket

#### React Hook Personnalisé

```typescript
// hooks/useWebSocket.ts
import { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client, IMessage } from '@stomp/stompjs';

interface UseWebSocketProps {
  token: string;
  conversationId: number;
  onMessageReceived: (message: any) => void;
  onTyping?: (user: string) => void;
}

export function useWebSocket({
  token,
  conversationId,
  onMessageReceived,
  onTyping
}: UseWebSocketProps) {
  
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    // Créer la connexion WebSocket
    const socket = new SockJS('http://localhost:8080/ws');
    
    const stompClient = new Client({
      webSocketFactory: () => socket,
      
      // Headers de connexion avec JWT
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      
      // Callback de connexion réussie
      onConnect: () => {
        console.log('✅ WebSocket connecté');
        setConnected(true);
        setError(null);
        
        // S'abonner aux messages de la conversation
        stompClient.subscribe(
          `/topic/conversation/${conversationId}`,
          (message: IMessage) => {
            const notification = JSON.parse(message.body);
            console.log('📩 Message reçu:', notification);
            onMessageReceived(notification);
          }
        );
        
        // S'abonner à l'indicateur "en train d'écrire"
        if (onTyping) {
          stompClient.subscribe(
            `/topic/conversation/${conversationId}/typing`,
            (message: IMessage) => {
              const typingUser = message.body;
              onTyping(typingUser);
            }
          );
        }
        
        // S'abonner aux confirmations de lecture
        stompClient.subscribe(
          `/topic/conversation/${conversationId}/read`,
          (message: IMessage) => {
            const messageId = JSON.parse(message.body);
            console.log(`✓ Message ${messageId} lu`);
          }
        );
      },
      
      // Callback d'erreur
      onStompError: (frame) => {
        console.error('❌ Erreur WebSocket:', frame.headers['message']);
        setError(frame.headers['message'] || 'Erreur de connexion');
        setConnected(false);
      },
      
      // Callback de déconnexion
      onDisconnect: () => {
        console.log('🔌 WebSocket déconnecté');
        setConnected(false);
      },
      
      // Reconnecter automatiquement
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    // Activer la connexion
    stompClient.activate();
    clientRef.current = stompClient;

    // Cleanup à la déconnexion du composant
    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [token, conversationId, onMessageReceived, onTyping]);

  /**
   * Envoie un message via WebSocket
   */
  const sendMessage = (contenu: string) => {
    if (clientRef.current && connected) {
      clientRef.current.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify({
          conversationId,
          contenu
        })
      });
    } else {
      console.error('❌ WebSocket non connecté');
    }
  };

  /**
   * Indique qu'on est en train d'écrire
   */
  const sendTypingIndicator = () => {
    if (clientRef.current && connected) {
      clientRef.current.publish({
        destination: `/app/chat.typing/${conversationId}`,
        body: ''
      });
    }
  };

  /**
   * Marque un message comme lu
   */
  const markAsRead = (messageId: number) => {
    if (clientRef.current && connected) {
      clientRef.current.publish({
        destination: `/app/chat.markAsRead/${conversationId}/${messageId}`,
        body: ''
      });
    }
  };

  return {
    connected,
    error,
    sendMessage,
    sendTypingIndicator,
    markAsRead
  };
}
```

---

### 3. Composant Chat avec WebSocket

```typescript
// components/Chat.tsx
import React, { useState, useEffect, useRef } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';

interface Message {
  messageId: number;
  conversationId: number;
  contenu: string;
  expediteurId: number;
  expediteurNom: string;
  timestamp: string;
}

interface ChatProps {
  conversationId: number;
  token: string;
  userId: number;
}

export function Chat({ conversationId, token, userId }: ChatProps) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [nouveauMessage, setNouveauMessage] = useState('');
  const [isTyping, setIsTyping] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const typingTimeoutRef = useRef<NodeJS.Timeout>();

  // Charger les messages historiques au chargement
  useEffect(() => {
    fetch(`http://localhost:8080/api/messages/conversation/${conversationId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    .then(res => res.json())
    .then(data => {
      if (data.success) {
        setMessages(data.data);
      }
    });
  }, [conversationId, token]);

  // Scroll automatique vers le bas
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Gestion WebSocket
  const {
    connected,
    error,
    sendMessage,
    sendTypingIndicator,
    markAsRead
  } = useWebSocket({
    token,
    conversationId,
    
    // Callback quand un nouveau message est reçu
    onMessageReceived: (notification: Message) => {
      // Ajouter le message à la liste
      setMessages(prev => [...prev, notification]);
      
      // Si ce n'est pas notre message, le marquer comme lu
      if (notification.expediteurId !== userId) {
        markAsRead(notification.messageId);
      }
    },
    
    // Callback quand quelqu'un est en train d'écrire
    onTyping: (user: string) => {
      setIsTyping(user);
      
      // Cacher l'indicateur après 3 secondes
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }
      typingTimeoutRef.current = setTimeout(() => {
        setIsTyping(null);
      }, 3000);
    }
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (nouveauMessage.trim() && connected) {
      // Envoyer via WebSocket
      sendMessage(nouveauMessage);
      setNouveauMessage('');
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setNouveauMessage(e.target.value);
    
    // Envoyer l'indicateur "en train d'écrire"
    if (e.target.value.length > 0) {
      sendTypingIndicator();
    }
  };

  return (
    <div className="chat-container">
      {/* Header avec statut de connexion */}
      <div className="chat-header">
        <h2>Conversation</h2>
        <div className="connection-status">
          {connected ? (
            <span className="status-connected">🟢 Connecté</span>
          ) : (
            <span className="status-disconnected">🔴 Déconnecté</span>
          )}
        </div>
        {error && (
          <div className="connection-error">
            ⚠️ {error}
          </div>
        )}
      </div>

      {/* Liste des messages */}
      <div className="messages-list">
        {messages.map((msg) => (
          <div
            key={msg.messageId}
            className={`message ${msg.expediteurId === userId ? 'sent' : 'received'}`}
          >
            <div className="message-header">
              <span className="sender-name">{msg.expediteurNom}</span>
              <span className="message-time">
                {new Date(msg.timestamp).toLocaleTimeString('fr-FR', {
                  hour: '2-digit',
                  minute: '2-digit'
                })}
              </span>
            </div>
            <div className="message-content">{msg.contenu}</div>
          </div>
        ))}
        
        {/* Indicateur "en train d'écrire" */}
        {isTyping && (
          <div className="typing-indicator">
            {isTyping}
          </div>
        )}
        
        <div ref={messagesEndRef} />
      </div>

      {/* Formulaire d'envoi */}
      <form onSubmit={handleSubmit} className="message-form">
        <input
          type="text"
          value={nouveauMessage}
          onChange={handleInputChange}
          placeholder="Votre message..."
          disabled={!connected}
          className="message-input"
        />
        <button 
          type="submit" 
          disabled={!connected || !nouveauMessage.trim()}
          className="send-button"
        >
          Envoyer 📤
        </button>
      </form>
    </div>
  );
}
```

---

### 4. CSS pour le Chat

```css
/* styles/Chat.css */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  background: #f5f5f5;
}

.chat-header {
  background: #007bff;
  color: white;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-connected {
  color: #4CAF50;
  font-weight: bold;
}

.status-disconnected {
  color: #f44336;
  font-weight: bold;
}

.connection-error {
  background: #ff9800;
  color: white;
  padding: 0.5rem;
  border-radius: 4px;
  margin-top: 0.5rem;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  background: white;
}

.message {
  margin-bottom: 1rem;
  max-width: 70%;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.sent {
  margin-left: auto;
  background: #007bff;
  color: white;
  border-radius: 18px 18px 0 18px;
}

.message.received {
  margin-right: auto;
  background: #e9ecef;
  color: #333;
  border-radius: 18px 18px 18px 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.25rem;
  font-size: 0.75rem;
  opacity: 0.8;
  padding: 0.5rem 1rem 0;
}

.message-content {
  padding: 0.5rem 1rem 1rem;
  word-wrap: break-word;
}

.typing-indicator {
  color: #666;
  font-style: italic;
  padding: 0.5rem;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.message-form {
  display: flex;
  gap: 0.5rem;
  padding: 1rem;
  background: white;
  border-top: 1px solid #ddd;
}

.message-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 1rem;
  outline: none;
}

.message-input:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.1);
}

.send-button {
  padding: 0.75rem 1.5rem;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.send-button:hover:not(:disabled) {
  background: #0056b3;
}

.send-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
```

---

## 🧪 Tests et Debugging

### 1. Tester la Connexion WebSocket

```javascript
// test-websocket.js
const SockJS = require('sockjs-client');
const Stomp = require('@stomp/stompjs');

const token = 'VOTRE_TOKEN_JWT';
const conversationId = 1;

const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: `Bearer ${token}` },
  () => {
    console.log('✅ Connecté au WebSocket');
    
    // S'abonner
    stompClient.subscribe(`/topic/conversation/${conversationId}`, (message) => {
      console.log('📩 Message reçu:', JSON.parse(message.body));
    });
    
    // Envoyer un message de test
    stompClient.send(
      '/app/chat.sendMessage',
      {},
      JSON.stringify({
        conversationId: conversationId,
        contenu: 'Test WebSocket'
      })
    );
  },
  (error) => {
    console.error('❌ Erreur:', error);
  }
);
```

### 2. Logs de Debugging

Activez les logs dans `application.properties` :

```properties
# Logs WebSocket
logging.level.org.springframework.messaging=DEBUG
logging.level.org.springframework.web.socket=DEBUG
```

### 3. Outils de Test

#### Chrome DevTools
1. Ouvrir les DevTools (F12)
2. Aller dans l'onglet "Network"
3. Filtrer par "WS" (WebSocket)
4. Observer les frames CONNECT, SUBSCRIBE, SEND, MESSAGE

#### Postman (WebSocket Support)
1. Créer une nouvelle requête WebSocket
2. URL : `ws://localhost:8080/ws`
3. Ajouter le header `Authorization: Bearer TOKEN`
4. Envoyer des messages STOMP

---

## 🔒 Sécurité

### 1. Authentification JWT

```java
// Chaque connexion WebSocket nécessite un JWT valide
@Override
public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = ...;
            
            // Extraire et valider le JWT
            String token = accessor.getFirstNativeHeader("Authorization");
            if (jwtTokenProvider.validateToken(token)) {
                // Authentifier l'utilisateur
                accessor.setUser(authentication);
            } else {
                throw new IllegalArgumentException("Token invalide");
            }
            
            return message;
        }
    });
}
```

### 2. Validation des Permissions

```java
// Vérifier que l'utilisateur a accès à la conversation
@MessageMapping("/chat.sendMessage")
public void sendMessage(@Payload MessageRequest request, Principal principal) {
    // Vérifier que l'utilisateur est participant de la conversation
    if (!conversationService.isParticipant(request.getConversationId(), principal.getName())) {
        throw new AccessDeniedException("Accès refusé à cette conversation");
    }
    
    // ...
}
```

### 3. CORS Configuration

En production, restreindre les origines :

```java
@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .setAllowedOrigins("https://keneyamuso.ml")  // ← Domaine spécifique
            .withSockJS();
}
```

---

## 📊 Monitoring et Performance

### 1. Métriques WebSocket

```java
@Component
public class WebSocketMetrics {
    
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicLong messagesSent = new AtomicLong(0);
    
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        activeConnections.incrementAndGet();
        log.info("Nouvelle connexion WebSocket. Total: {}", activeConnections.get());
    }
    
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        activeConnections.decrementAndGet();
        log.info("Déconnexion WebSocket. Total: {}", activeConnections.get());
    }
}
```

### 2. Limiter les Messages

```java
// Limiter à 10 messages par seconde par utilisateur
@Component
public class RateLimitInterceptor implements ChannelInterceptor {
    
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = ...;
        String user = accessor.getUser().getName();
        
        RateLimiter limiter = limiters.computeIfAbsent(
            user,
            k -> RateLimiter.create(10.0) // 10 messages/sec
        );
        
        if (!limiter.tryAcquire()) {
            throw new IllegalStateException("Rate limit dépassé");
        }
        
        return message;
    }
}
```

---

## ✅ Checklist d'Implémentation

### Backend
- [x] Dépendance `spring-boot-starter-websocket` ajoutée
- [x] Configuration WebSocket avec JWT
- [x] Contrôleur WebSocket créé
- [x] DTO MessageNotification créé
- [x] Sécurité JWT intégrée

### Frontend
- [ ] Installation de sockjs-client et @stomp/stompjs
- [ ] Hook useWebSocket créé
- [ ] Composant Chat implémenté
- [ ] Gestion des erreurs de connexion
- [ ] Indicateur "en train d'écrire"
- [ ] Confirmation de lecture
- [ ] Reconnexion automatique
- [ ] Scrolling automatique
- [ ] Design responsive

### Tests
- [ ] Test de connexion WebSocket
- [ ] Test d'envoi de message
- [ ] Test de réception de message
- [ ] Test de déconnexion/reconnexion
- [ ] Test avec plusieurs clients simultanés

---

**KènèyaMuso** - *Pour une maternité saine au Mali* 🇲🇱

