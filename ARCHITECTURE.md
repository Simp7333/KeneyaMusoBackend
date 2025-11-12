# Architecture du Backend KènèyaMuso

## Vue d'ensemble

Le backend KènèyaMuso suit une architecture en couches (Layered Architecture) basée sur les principes de Spring Boot.

```
┌─────────────────────────────────────┐
│         Client (Mobile App)         │
└──────────────┬──────────────────────┘
               │ HTTP/REST + JWT
               │
┌──────────────▼──────────────────────┐
│      Controllers (REST API)         │  ← Exposition des endpoints
├─────────────────────────────────────┤
│           Services                  │  ← Logique métier
├─────────────────────────────────────┤
│         Repositories                │  ← Accès aux données
├─────────────────────────────────────┤
│         Base de données             │  ← Persistance (MySQL)
└─────────────────────────────────────┘
```

## Couches de l'application

### 1. Controller Layer (Présentation)

**Responsabilité** : Gérer les requêtes HTTP et les réponses

**Fichiers** : `src/main/java/com/keneyamuso/controller/`

**Caractéristiques** :
- Annotations `@RestController` et `@RequestMapping`
- Validation des entrées avec `@Valid`
- Documentation Swagger avec annotations OpenAPI
- Gestion des autorisations avec `@PreAuthorize`

**Exemple** :
```java
@RestController
@RequestMapping("/api/grossesses")
@SecurityRequirement(name = "bearerAuth")
public class GrossesseController {
    
    @PostMapping
    public ResponseEntity<ApiResponse<Grossesse>> createGrossesse(
            @Valid @RequestBody GrossesseRequest request) {
        // Déléguer au service
    }
}
```

### 2. Service Layer (Métier)

**Responsabilité** : Implémenter la logique métier

**Fichiers** : `src/main/java/com/keneyamuso/service/`

**Caractéristiques** :
- Annotations `@Service` et `@Transactional`
- Logique de validation métier
- Orchestration entre différents repositories
- Gestion des règles métier spécifiques

**Exemple** :
```java
@Service
@RequiredArgsConstructor
public class GrossesseService {
    
    private final GrossesseRepository grossesseRepository;
    private final PatienteRepository patienteRepository;
    
    @Transactional
    public Grossesse createGrossesse(GrossesseRequest request) {
        // Logique métier
        // Validation
        // Création
    }
}
```

### 3. Repository Layer (Données)

**Responsabilité** : Accès à la base de données

**Fichiers** : `src/main/java/com/keneyamuso/repository/`

**Caractéristiques** :
- Extension de `JpaRepository`
- Requêtes JPQL personnalisées avec `@Query`
- Méthodes de recherche conventionnelles

**Exemple** :
```java
@Repository
public interface GrossesseRepository extends JpaRepository<Grossesse, Long> {
    
    List<Grossesse> findByPatienteId(Long patienteId);
    
    @Query("SELECT g FROM Grossesse g WHERE g.statut = :statut")
    List<Grossesse> findByStatut(@Param("statut") StatutGrossesse statut);
}
```

### 4. Model Layer (Domaine)

**Responsabilité** : Représenter les données métier

**Fichiers** : 
- `src/main/java/com/keneyamuso/model/entity/` - Entités JPA
- `src/main/java/com/keneyamuso/model/enums/` - Énumérations

**Caractéristiques** :
- Annotations JPA (`@Entity`, `@Table`, etc.)
- Relations entre entités (`@ManyToOne`, `@OneToMany`, etc.)
- Validation Jakarta (`@NotNull`, `@NotBlank`, etc.)
- Auditing avec `@CreatedDate`, `@LastModifiedDate`

## Patterns utilisés

### 1. DTO (Data Transfer Object)

Séparer les objets de transfert des entités métier :

```
src/main/java/com/keneyamuso/dto/
├── request/         # DTOs pour les requêtes entrantes
│   ├── LoginRequest.java
│   ├── GrossesseRequest.java
│   └── ...
└── response/        # DTOs pour les réponses
    ├── JwtAuthResponse.java
    ├── ApiResponse.java
    └── ...
```

**Avantages** :
- Découplage entre API et modèle de données
- Contrôle sur les données exposées
- Validation spécifique aux opérations

### 2. Repository Pattern

Abstraction de l'accès aux données via Spring Data JPA.

### 3. Dependency Injection

Utilisation de l'injection de dépendances Spring avec `@RequiredArgsConstructor` (Lombok).

### 4. Exception Handling

Gestion centralisée des exceptions avec `@RestControllerAdvice`.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        // Gestion uniforme
    }
}
```

## Sécurité

### Architecture de sécurité

```
Requête HTTP
    │
    ▼
JwtAuthenticationFilter (vérification du token)
    │
    ▼
SecurityContext (stockage de l'authentification)
    │
    ▼
Controller (vérification des autorisations)
    │
    ▼
Service (logique métier)
```

### Composants de sécurité

1. **JwtTokenProvider** : Génération et validation des tokens JWT
2. **JwtAuthenticationFilter** : Interception et validation des requêtes
3. **CustomUserDetailsService** : Chargement des utilisateurs
4. **SecurityConfig** : Configuration Spring Security

### Flux d'authentification

```
1. POST /api/auth/login
   ↓
2. AuthService vérifie les credentials
   ↓
3. JwtTokenProvider génère un token
   ↓
4. Client reçoit le token JWT
   ↓
5. Client envoie le token dans Authorization: Bearer {token}
   ↓
6. JwtAuthenticationFilter valide le token
   ↓
7. Accès autorisé aux endpoints protégés
```

## Base de données

### Stratégie d'héritage

Pour les entités `Utilisateur`, `Patiente`, et `ProfessionnelSante`, nous utilisons **JOINED** :

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Utilisateur {
    // Attributs communs
}

@Entity
public class Patiente extends Utilisateur {
    // Attributs spécifiques
}
```

**Avantage** : Normalisation des données, pas de duplication.

### Relations

#### One-to-Many
- Une `Patiente` a plusieurs `Grossesse`
- Une `Grossesse` a plusieurs `ConsultationPrenatale`
- Un `Enfant` a plusieurs `Vaccination`

#### Many-to-One
- Une `ConsultationPrenatale` appartient à une `Grossesse`
- Un `Message` appartient à une `Conversation`

#### Many-to-Many
- `Conversation` ↔ `Utilisateur` (participants)

### Indexation

Les colonnes suivantes sont indexées :
- `telephone` (unique) dans `Utilisateur`
- `identifiantProfessionnel` (unique) dans `ProfessionnelSante`
- Foreign keys automatiquement indexées

## Flow de données

### Exemple : Création d'une grossesse

```
1. Client → POST /api/grossesses
   Body: { "dateDebut": "...", "datePrevueAccouchement": "...", "patienteId": 1 }
   
2. GrossesseController.createGrossesse()
   ↓ Validation @Valid
   ↓
   
3. GrossesseService.createGrossesse()
   ↓ Vérification patiente existe
   ↓ Création entité Grossesse
   ↓ Calcul logique métier
   ↓
   
4. GrossesseRepository.save()
   ↓ Persistence JPA
   ↓
   
5. Base de données
   INSERT INTO grossesses (...)
   
6. Retour au client
   Response: ApiResponse<Grossesse>
```

## Gestion des erreurs

### Hiérarchie des exceptions

```
RuntimeException
    ├── ResourceNotFoundException (404)
    ├── BadRequestException (400)
    └── ...
```

### Flow de gestion d'erreur

```
Exception lancée dans Service
    ↓
GlobalExceptionHandler intercepte
    ↓
Conversion en ApiResponse avec message approprié
    ↓
Retour au client avec code HTTP correct
```

## Auditing

Toutes les entités importantes utilisent l'auditing JPA :

```java
@EntityListeners(AuditingEntityListener.class)
public class Grossesse {
    
    @CreatedDate
    private LocalDateTime dateCreation;
    
    @LastModifiedDate
    private LocalDateTime dateModification;
}
```

Activé avec `@EnableJpaAuditing` dans la classe principale.

## API Documentation

### Swagger/OpenAPI

Configuration dans `OpenApiConfig` :
- Informations générales de l'API
- Schéma de sécurité JWT
- Serveurs (dev, prod)

Documentation automatique générée à partir des annotations :
- `@Operation` : Description de l'endpoint
- `@Tag` : Groupement des endpoints
- `@SecurityRequirement` : Indication de sécurité

## Performance

### Optimisations

1. **Lazy Loading** : Relations chargées à la demande
2. **@Transactional(readOnly = true)** : Optimisation des lectures
3. **Indexation** : Sur les colonnes de recherche
4. **DTO Projection** : Ne charger que les données nécessaires

### Considérations futures

- Cache avec Redis
- Pagination systématique
- Optimisation des requêtes N+1
- Connection pooling (HikariCP déjà configuré)

## Scalabilité

### Stateless Architecture

L'application est stateless grâce à JWT :
- Pas de session serveur
- Scalabilité horizontale possible
- Load balancing facile

### Microservices (Future)

L'architecture actuelle est un monolithe modulaire qui peut évoluer vers des microservices :

```
API Gateway
    ├── Service Authentification
    ├── Service Grossesses
    ├── Service Vaccinations
    ├── Service Messagerie
    └── Service Notifications
```

## Testing

### Stratégie de tests

1. **Unit Tests** : Services et logique métier
2. **Integration Tests** : Controllers et repositories
3. **E2E Tests** : Scénarios complets

### Structure

```
src/test/java/com/keneyamuso/
    ├── controller/      # Tests d'intégration
    ├── service/         # Tests unitaires
    └── repository/      # Tests de repository
```

## Monitoring (Future)

Ajout prévu de :
- Spring Boot Actuator (métriques)
- Prometheus + Grafana (monitoring)
- ELK Stack (logs centralisés)
- Sentry (tracking d'erreurs)

## Déploiement

### Environnements

1. **Dev** : H2 in-memory, auto-création des tables
2. **Test** : MySQL avec données de test
3. **Prod** : MySQL avec backup automatique

### CI/CD (Future)

```
GitHub → Actions → Build → Test → Docker → Deploy
```

## Évolutions futures

1. **Notifications push** : Firebase Cloud Messaging
2. **WebSockets** : Chat en temps réel
3. **File upload** : Pour images et documents
4. **Scheduled tasks** : Envoi automatique de rappels
5. **Analytics** : Tableaux de bord pour professionnels
6. **Multilangue** : Support du Bambara, Soninké

---

Cette architecture est conçue pour être :
- ✅ **Maintenable** : Code organisé et documenté
- ✅ **Testable** : Séparation des responsabilités
- ✅ **Scalable** : Architecture stateless
- ✅ **Sécurisée** : JWT, validation, gestion des erreurs
- ✅ **Performante** : Optimisations JPA, indexation

