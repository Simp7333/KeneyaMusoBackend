# 💬 Guide Complet - Système de Messagerie KènèyaMuso

## 📋 Vue d'ensemble

Le système de messagerie permet la communication sécurisée entre **patientes** et **professionnels de santé** (médecins) pour poser des questions, recevoir des conseils et assurer un suivi continu.

---

## 🏗️ Architecture du Système

### Structure de Base : 2 Entités Principales

```
┌─────────────────────────────────────────────────────────┐
│                     CONVERSATION                         │
│                                                          │
│  - ID                                                   │
│  - Titre (ex: "Suivi grossesse - Fatoumata Traoré")   │
│  - Participants (Many-to-Many avec Utilisateur)        │
│  - Messages (One-to-Many)                              │
│  - Active (true/false)                                 │
│  - Date création                                       │
└─────────────────────────────────────────────────────────┘
                           │
                           │ contient
                           ▼
┌─────────────────────────────────────────────────────────┐
│                       MESSAGE                            │
│                                                          │
│  - ID                                                   │
│  - Contenu (texte du message, max 2000 caractères)    │
│  - Expéditeur (référence vers Utilisateur)            │
│  - Conversation (référence)                            │
│  - Lu (true/false)                                     │
│  - Timestamp (date/heure d'envoi)                      │
└─────────────────────────────────────────────────────────┘
```

---

## 🔄 Fonctionnement Détaillé

### 1. Structure de la Conversation

#### Table : `conversations`

```sql
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    date_creation DATETIME NOT NULL,
    date_modification DATETIME
);
```

#### Table de liaison : `conversation_participants`

```sql
CREATE TABLE conversation_participants (
    conversation_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    PRIMARY KEY (conversation_id, utilisateur_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id)
);
```

**Exemple de données** :

| conversation_id | utilisateur_id | Nom | Rôle |
|----------------|----------------|-----|------|
| 1 | 12 | Fatoumata Traoré | PATIENTE |
| 1 | 5 | Dr. Moussa Diarra | MEDECIN |

➡️ La conversation 1 a **2 participants** : une patiente et son médecin

---

### 2. Structure des Messages

#### Table : `messages`

```sql
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contenu TEXT NOT NULL,
    conversation_id BIGINT NOT NULL,
    expediteur_id BIGINT NOT NULL,
    lu BOOLEAN NOT NULL DEFAULT false,
    timestamp DATETIME NOT NULL,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (expediteur_id) REFERENCES utilisateurs(id)
);
```

**Exemple de données** :

| id | contenu | conversation_id | expediteur_id | lu | timestamp |
|----|---------|-----------------|---------------|----|-----------|
| 1 | "Bonjour Docteur, j'ai des nausées" | 1 | 12 | true | 2024-10-16 09:30 |
| 2 | "C'est normal au 1er trimestre. Buvez beaucoup d'eau" | 1 | 5 | true | 2024-10-16 09:45 |
| 3 | "Merci Docteur !" | 1 | 12 | false | 2024-10-16 09:50 |

---

## 📊 Flux de Données Complet

### Scénario : Une patiente envoie un message à son médecin

```
┌──────────────┐                                    ┌──────────────┐
│   PATIENTE   │                                    │   MEDECIN    │
│  Fatoumata   │                                    │  Dr. Diarra  │
└──────┬───────┘                                    └──────┬───────┘
       │                                                    │
       │ 1. Envoie message                                 │
       │    "J'ai des nausées"                             │
       ▼                                                    │
┌─────────────────────────────────────────────────┐       │
│          POST /api/messages                      │       │
│  {                                               │       │
│    "conversationId": 1,                          │       │
│    "contenu": "J'ai des nausées"                │       │
│  }                                               │       │
│  + Authorization: Bearer TOKEN_PATIENTE          │       │
└─────────────────────────────────────────────────┘       │
       │                                                    │
       │ 2. MessageService crée le message                 │
       ▼                                                    │
┌─────────────────────────────────────────────────┐       │
│  MessageService.envoyerMessage()                 │       │
│  - Vérifie que la conversation existe           │       │
│  - Récupère l'expéditeur via le token JWT       │       │
│  - Crée le message avec :                       │       │
│    * contenu = "J'ai des nausées"               │       │
│    * expediteur = Fatoumata (id: 12)            │       │
│    * conversation_id = 1                         │       │
│    * lu = false                                  │       │
│    * timestamp = NOW()                           │       │
│  - Sauvegarde en base de données                │       │
└─────────────────────────────────────────────────┘       │
       │                                                    │
       │ 3. Message enregistré ✅                          │
       │                                                    │
       │                    4. Médecin consulte ses messages
       │                                                    │
       │                                                    ▼
       │                        ┌─────────────────────────────────┐
       │                        │ GET /api/messages/conversation/1│
       │                        │ + Authorization: Bearer TOKEN_MEDECIN
       │                        └─────────────────────────────────┘
       │                                                    │
       │                    5. Récupère tous les messages  │
       │                       triés par timestamp ASC     │
       │                                                    ▼
       │                        ┌─────────────────────────────────┐
       │                        │ [                               │
       │                        │   {                             │
       │                        │     "id": 1,                    │
       │                        │     "contenu": "J'ai des nausées"│
       │                        │     "expediteur": {...},        │
       │                        │     "lu": false,                │
       │                        │     "timestamp": "..."          │
       │                        │   }                             │
       │                        │ ]                               │
       │                        └─────────────────────────────────┘
       │                                                    │
       │                    6. Marque comme lu             │
       │                                                    ▼
       │                        ┌─────────────────────────────────┐
       │                        │ PUT /api/messages/1/lire        │
       │                        └─────────────────────────────────┘
       │                                                    │
       │                    7. Répond au message           │
       │                                                    ▼
       │                        ┌─────────────────────────────────┐
       │                        │ POST /api/messages              │
       │                        │ {                               │
       │                        │   "conversationId": 1,          │
       │                        │   "contenu": "C'est normal..."  │
       │                        │ }                               │
       │                        └─────────────────────────────────┘
       │                                                    │
       ▼ 8. Patiente reçoit la réponse                    │
```

---

## 💻 Code d'Implémentation

### 1. Entité Conversation

```java
@Entity
@Table(name = "conversations")
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titre;
    
    // Many-to-Many : Une conversation peut avoir plusieurs participants
    // Un utilisateur peut participer à plusieurs conversations
    @ManyToMany
    @JoinTable(
        name = "conversation_participants",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    private List<Utilisateur> participants = new ArrayList<>();
    
    // One-to-Many : Une conversation a plusieurs messages
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();
    
    private Boolean active = true;
    
    @CreatedDate
    private LocalDateTime dateCreation;
}
```

### 2. Entité Message

```java
@Entity
@Table(name = "messages")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 2000)
    private String contenu;
    
    // Many-to-One : Plusieurs messages appartiennent à une conversation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;
    
    // Many-to-One : Un message a un seul expéditeur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediteur_id", nullable = false)
    private Utilisateur expediteur;
    
    @Column(nullable = false)
    private Boolean lu = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
```

### 3. Service de Messagerie

```java
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Envoie un nouveau message dans une conversation.
     * L'expéditeur est identifié automatiquement via le token JWT.
     */
    @Transactional
    public Message envoyerMessage(MessageRequest request, String telephoneExpediteur) {
        // 1. Vérifier que la conversation existe
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", request.getConversationId()));

        // 2. Récupérer l'expéditeur via le téléphone (extrait du JWT)
        Utilisateur expediteur = utilisateurRepository.findByTelephone(telephoneExpediteur)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephoneExpediteur));

        // 3. Créer le message
        Message message = new Message();
        message.setContenu(request.getContenu());
        message.setConversation(conversation);
        message.setExpediteur(expediteur);
        message.setLu(false);  // Non lu par défaut

        // 4. Sauvegarder et retourner
        return messageRepository.save(message);
    }

    /**
     * Récupère tous les messages d'une conversation, triés chronologiquement.
     */
    @Transactional(readOnly = true)
    public List<Message> getMessagesByConversation(Long conversationId) {
        return messageRepository.findByConversationIdOrderByTimestamp(conversationId);
    }

    /**
     * Marque un message comme lu.
     */
    @Transactional
    public void marquerCommeLu(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));
        message.setLu(true);
        messageRepository.save(message);
    }
}
```

### 4. Controller REST

```java
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * POST /api/messages
     * Envoie un message
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Message>> envoyerMessage(
            @Valid @RequestBody MessageRequest request,
            Authentication authentication) {
        
        // Le téléphone est extrait du JWT automatiquement
        String telephone = authentication.getName();
        
        Message message = messageService.envoyerMessage(request, telephone);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message envoyé", message));
    }

    /**
     * GET /api/messages/conversation/{conversationId}
     * Récupère tous les messages d'une conversation
     */
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<List<Message>>> getMessagesByConversation(
            @PathVariable Long conversationId) {
        
        List<Message> messages = messageService.getMessagesByConversation(conversationId);
        return ResponseEntity.ok(ApiResponse.success("Messages trouvés", messages));
    }

    /**
     * PUT /api/messages/{id}/lire
     * Marque un message comme lu
     */
    @PutMapping("/{id}/lire")
    public ResponseEntity<ApiResponse<String>> marquerCommeLu(@PathVariable Long id) {
        messageService.marquerCommeLu(id);
        return ResponseEntity.ok(ApiResponse.success("Message marqué comme lu", null));
    }
}
```

---

## 🔑 Points Clés du Système

### 1. **Relation Many-to-Many (Conversation ↔ Utilisateurs)**

```
Conversation 1 : "Suivi grossesse - Fatoumata"
├── Participant 1 : Fatoumata Traoré (PATIENTE)
└── Participant 2 : Dr. Moussa Diarra (MEDECIN)

Conversation 2 : "Suivi vaccination - Ibrahim"
├── Participant 1 : Fatoumata Traoré (PATIENTE)
└── Participant 2 : Dr. Aissata Koné (PEDIATRE)
```

➡️ **Une patiente peut avoir plusieurs conversations** (une par médecin ou par sujet)  
➡️ **Un médecin peut avoir plusieurs conversations** (une par patiente)

### 2. **Identification Automatique de l'Expéditeur**

```java
// Dans MessageController
@PostMapping
public ResponseEntity<ApiResponse<Message>> envoyerMessage(
        @RequestBody MessageRequest request,
        Authentication authentication) {  // ← JWT injecté automatiquement
    
    String telephone = authentication.getName();  // ← Extrait du token
    Message message = messageService.envoyerMessage(request, telephone);
    return ResponseEntity.ok(...);
}
```

➡️ **Sécurité** : L'expéditeur est identifié via le token JWT, impossible de se faire passer pour quelqu'un d'autre

### 3. **Statut de Lecture**

```java
// Chaque message a un boolean "lu"
private Boolean lu = false;  // Non lu par défaut

// Lorsque le destinataire consulte le message
PUT /api/messages/{id}/lire
→ lu = true
```

➡️ Permet d'afficher des indicateurs visuels (messages non lus en gras, badge avec nombre)

### 4. **Ordre Chronologique**

```java
// Les messages sont triés par timestamp
@Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.timestamp ASC")
List<Message> findByConversationIdOrderByTimestamp(@Param("conversationId") Long conversationId);
```

➡️ Les messages apparaissent toujours dans l'ordre d'envoi (du plus ancien au plus récent)

---

## 📱 Exemples d'Utilisation

### 1. Créer une Conversation (manuelle pour l'instant)

```sql
-- 1. Créer la conversation
INSERT INTO conversations (titre, active, date_creation)
VALUES ('Suivi grossesse - Fatoumata Traoré', true, NOW());

-- 2. Ajouter les participants
INSERT INTO conversation_participants (conversation_id, utilisateur_id)
VALUES (1, 12),  -- Fatoumata (patiente)
       (1, 5);   -- Dr. Diarra (médecin)
```

**Note** : Dans une future version, on pourrait créer un endpoint pour créer automatiquement une conversation.

### 2. Envoyer un Message (Patiente)

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Authorization: Bearer TOKEN_PATIENTE" \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": 1,
    "contenu": "Bonjour Docteur, j'ai des nausées matinales depuis 3 jours. Est-ce normal ?"
  }'
```

**Réponse** :
```json
{
  "success": true,
  "message": "Message envoyé",
  "data": {
    "id": 1,
    "contenu": "Bonjour Docteur, j'ai des nausées matinales depuis 3 jours. Est-ce normal ?",
    "expediteur": {
      "id": 12,
      "nom": "Traoré",
      "prenom": "Fatoumata",
      "role": "PATIENTE"
    },
    "lu": false,
    "timestamp": "2024-10-16T09:30:00"
  }
}
```

### 3. Consulter les Messages (Médecin)

```bash
curl -X GET http://localhost:8080/api/messages/conversation/1 \
  -H "Authorization: Bearer TOKEN_MEDECIN"
```

**Réponse** :
```json
{
  "success": true,
  "message": "Messages trouvés",
  "data": [
    {
      "id": 1,
      "contenu": "Bonjour Docteur, j'ai des nausées matinales depuis 3 jours. Est-ce normal ?",
      "expediteur": {
        "id": 12,
        "nom": "Traoré",
        "prenom": "Fatoumata",
        "role": "PATIENTE"
      },
      "lu": false,
      "timestamp": "2024-10-16T09:30:00"
    }
  ]
}
```

### 4. Marquer comme Lu

```bash
curl -X PUT http://localhost:8080/api/messages/1/lire \
  -H "Authorization: Bearer TOKEN_MEDECIN"
```

### 5. Répondre au Message (Médecin)

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Authorization: Bearer TOKEN_MEDECIN" \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": 1,
    "contenu": "Bonjour Fatoumata, c'est tout à fait normal au premier trimestre. Buvez beaucoup d'eau et mangez des aliments légers. Si les nausées persistent, on en parlera à la prochaine CPN."
  }'
```

---

## 🚀 Améliorations Futures

### 1. Création Automatique de Conversation

```java
// À implémenter
@PostMapping("/conversations")
public ResponseEntity<Conversation> creerConversation(
        @RequestBody CreateConversationRequest request,
        Authentication authentication) {
    
    // Créer une conversation entre la patiente et son médecin assigné
    Conversation conversation = conversationService.creerConversation(
        request.getPatienteId(),
        request.getMedecinId(),
        request.getTitre()
    );
    
    return ResponseEntity.ok(conversation);
}
```

### 2. WebSocket pour Messagerie en Temps Réel

```java
// Utiliser Spring WebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
}
```

### 3. Notifications Push

```java
// Envoyer une notification quand un message est reçu
public Message envoyerMessage(MessageRequest request, String telephoneExpediteur) {
    Message message = // ... créer le message
    
    // Envoyer notification aux autres participants
    List<Utilisateur> destinataires = conversation.getParticipants()
        .stream()
        .filter(u -> !u.getTelephone().equals(telephoneExpediteur))
        .collect(Collectors.toList());
    
    notificationService.envoyerNotification(
        destinataires,
        "Nouveau message de " + expediteur.getPrenom()
    );
    
    return message;
}
```

### 4. Indicateur "En train d'écrire..."

```java
// WebSocket pour signaler qu'un utilisateur est en train d'écrire
@MessageMapping("/conversation/{conversationId}/typing")
public void userTyping(@DestinationVariable Long conversationId, Principal principal) {
    messagingTemplate.convertAndSend(
        "/topic/conversation/" + conversationId + "/typing",
        principal.getName() + " est en train d'écrire..."
    );
}
```

### 5. Support des Pièces Jointes

```java
@Entity
public class Message {
    // ... champs existants
    
    @Column(length = 500)
    private String pieceJointeUrl;  // URL vers image/PDF
    
    @Enumerated(EnumType.STRING)
    private TypePieceJointe typePieceJointe;  // IMAGE, PDF, AUDIO
}
```

### 6. Messages Vocaux

```java
// Stocker des fichiers audio
@PostMapping("/messages/vocal")
public ResponseEntity<Message> envoyerMessageVocal(
        @RequestParam("conversationId") Long conversationId,
        @RequestParam("audio") MultipartFile audio) {
    
    String audioUrl = fileStorageService.storeFile(audio);
    Message message = messageService.envoyerMessageVocal(conversationId, audioUrl);
    return ResponseEntity.ok(message);
}
```

---

## 📊 Requêtes Utiles pour la Base de Données

### Voir toutes les conversations d'un utilisateur

```sql
SELECT c.id, c.titre, c.date_creation
FROM conversations c
JOIN conversation_participants cp ON c.id = cp.conversation_id
WHERE cp.utilisateur_id = 12;  -- ID de la patiente
```

### Compter les messages non lus pour un utilisateur

```sql
SELECT c.id, c.titre, COUNT(m.id) as messages_non_lus
FROM conversations c
JOIN conversation_participants cp ON c.id = cp.conversation_id
JOIN messages m ON m.conversation_id = c.id
WHERE cp.utilisateur_id = 5  -- ID du médecin
  AND m.lu = false
  AND m.expediteur_id != 5  -- Exclure ses propres messages
GROUP BY c.id, c.titre;
```

### Dernier message de chaque conversation

```sql
SELECT c.id, c.titre, 
       m.contenu as dernier_message, 
       m.timestamp as date_dernier_message,
       u.prenom as expediteur
FROM conversations c
JOIN messages m ON m.conversation_id = c.id
JOIN utilisateurs u ON u.id = m.expediteur_id
WHERE m.timestamp = (
    SELECT MAX(m2.timestamp)
    FROM messages m2
    WHERE m2.conversation_id = c.id
)
ORDER BY m.timestamp DESC;
```

---

## ✅ Résumé

| Aspect | Implémentation |
|--------|----------------|
| **Architecture** | 2 entités : Conversation + Message |
| **Relation Participants** | Many-to-Many via table `conversation_participants` |
| **Sécurité** | Expéditeur identifié via JWT automatiquement |
| **Ordre des messages** | Triés chronologiquement (timestamp ASC) |
| **Statut de lecture** | Boolean `lu` sur chaque message |
| **Temps réel** | À implémenter (WebSocket recommandé) |
| **Notifications** | À implémenter (Firebase Cloud Messaging) |

---

**KènèyaMuso** - *Pour une maternité saine au Mali* 🇲🇱

