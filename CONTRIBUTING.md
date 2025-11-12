# Guide de contribution

Merci de votre intérêt pour contribuer à **KènèyaMuso** ! 🎉

## Comment contribuer

### 1. Fork et clone

```bash
# Fork le repository sur GitHub, puis :
git clone https://github.com/votre-username/keneyamuso-backend.git
cd keneyamuso-backend
```

### 2. Créer une branche

```bash
git checkout -b feature/ma-nouvelle-fonctionnalite
# ou
git checkout -b fix/correction-bug
```

### 3. Conventions de nommage

#### Branches
- `feature/` : Nouvelles fonctionnalités
- `fix/` : Corrections de bugs
- `refactor/` : Refactoring du code
- `docs/` : Modifications de documentation
- `test/` : Ajout ou modification de tests

#### Commits
Utilisez des messages de commit clairs et descriptifs :

```bash
# Format : <type>: <description>

git commit -m "feat: ajout du module de rappels automatiques"
git commit -m "fix: correction du calcul de la DPA"
git commit -m "docs: mise à jour du README avec les exemples d'API"
git commit -m "refactor: optimisation des requêtes JPA"
git commit -m "test: ajout de tests pour le service de vaccination"
```

Types de commits :
- `feat` : Nouvelle fonctionnalité
- `fix` : Correction de bug
- `docs` : Documentation
- `style` : Formatage, point-virgule manquant, etc.
- `refactor` : Refactoring du code
- `test` : Ajout ou modification de tests
- `chore` : Maintenance, dépendances, etc.

### 4. Standards de code

#### Java
- Suivre les conventions de nommage Java
- Utiliser Lombok pour réduire le boilerplate
- Documenter les classes et méthodes publiques avec Javadoc
- Utiliser les annotations Spring appropriées

Exemple :

```java
/**
 * Service de gestion des consultations prénatales.
 * Fournit les opérations CRUD et la logique métier associée.
 */
@Service
@RequiredArgsConstructor
public class ConsultationPrenataleService {
    
    private final ConsultationPrenataleRepository repository;
    
    /**
     * Crée une nouvelle consultation prénatale.
     * 
     * @param request les données de la consultation
     * @return la consultation créée
     * @throws ResourceNotFoundException si la grossesse n'existe pas
     */
    @Transactional
    public ConsultationPrenatale createConsultation(ConsultationPrenataleRequest request) {
        // Implementation
    }
}
```

#### REST API
- Utiliser les verbes HTTP appropriés (GET, POST, PUT, DELETE)
- Retourner les codes HTTP corrects (200, 201, 400, 404, etc.)
- Documenter avec Swagger/OpenAPI
- Utiliser ApiResponse pour les réponses uniformes

### 5. Tests

Ajoutez des tests pour vos modifications :

```java
@SpringBootTest
class GrossesseServiceTest {
    
    @Autowired
    private GrossesseService grossesseService;
    
    @Test
    void testCreateGrossesse() {
        // Given
        GrossesseRequest request = new GrossesseRequest();
        // ...
        
        // When
        Grossesse result = grossesseService.createGrossesse(request);
        
        // Then
        assertNotNull(result);
        assertEquals(StatutGrossesse.EN_COURS, result.getStatut());
    }
}
```

Lancez les tests avant de soumettre :

```bash
mvn test
```

### 6. Documentation

- Mettez à jour le README.md si nécessaire
- Ajoutez des exemples dans API_EXAMPLES.md pour les nouveaux endpoints
- Documentez les nouvelles fonctionnalités

### 7. Pull Request

```bash
# Push votre branche
git push origin feature/ma-nouvelle-fonctionnalite

# Créez une Pull Request sur GitHub avec :
# - Un titre clair
# - Une description détaillée
# - Des captures d'écran si pertinent
# - La référence aux issues associées
```

#### Template de Pull Request

```markdown
## Description
Brève description de ce qui a été fait et pourquoi.

## Type de changement
- [ ] Bug fix
- [ ] Nouvelle fonctionnalité
- [ ] Breaking change
- [ ] Documentation

## Comment tester
1. Étape 1
2. Étape 2
3. Résultat attendu

## Checklist
- [ ] Mon code suit les standards du projet
- [ ] J'ai ajouté des tests
- [ ] Tous les tests passent
- [ ] J'ai mis à jour la documentation
- [ ] Mon code ne génère pas de nouveaux warnings
```

## Structure du projet

```
src/
├── main/
│   ├── java/com/keneyamuso/
│   │   ├── config/          # Configuration Spring
│   │   ├── controller/      # REST Controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Gestion des exceptions
│   │   ├── model/           # Entités et enums
│   │   ├── repository/      # Repositories JPA
│   │   ├── security/        # Sécurité et JWT
│   │   └── service/         # Logique métier
│   └── resources/
│       └── application.properties
└── test/
```

## Bonnes pratiques

### Sécurité
- Ne jamais commit de secrets ou mots de passe
- Utiliser les variables d'environnement
- Valider toutes les entrées utilisateur
- Utiliser les annotations de validation Jakarta

### Performance
- Utiliser `@Transactional(readOnly = true)` pour les lectures
- Optimiser les requêtes JPA (éviter N+1)
- Utiliser le lazy loading approprié

### Base de données
- Utiliser les migrations avec Flyway (à venir)
- Toujours indexer les colonnes de recherche
- Utiliser les contraintes de base de données

### Documentation API
- Documenter tous les endpoints avec Swagger
- Fournir des exemples de requêtes/réponses
- Décrire les codes d'erreur possibles

## Questions fréquentes

### Comment ajouter une nouvelle entité ?

1. Créer l'entité dans `model/entity/`
2. Créer le repository dans `repository/`
3. Créer les DTOs dans `dto/request/` et `dto/response/`
4. Créer le service dans `service/`
5. Créer le controller dans `controller/`
6. Ajouter les tests

### Comment gérer une nouvelle relation entre entités ?

Utilisez les annotations JPA appropriées :
- `@OneToMany` / `@ManyToOne`
- `@ManyToMany`
- `@JoinColumn` pour spécifier la colonne de jointure

### Comment ajouter un nouveau rôle utilisateur ?

1. Ajouter dans `RoleUtilisateur` enum
2. Mettre à jour la configuration de sécurité
3. Ajouter les annotations `@PreAuthorize` nécessaires
4. Mettre à jour la documentation

**Rôles actuels** : PATIENTE, MEDECIN, ADMINISTRATEUR

## Code de conduite

- Soyez respectueux et professionnel
- Acceptez les critiques constructives
- Concentrez-vous sur ce qui est meilleur pour le projet
- Faites preuve d'empathie envers les autres contributeurs

## Besoin d'aide ?

- Ouvrez une issue sur GitHub
- Contactez-nous à contact@keneyamuso.ml
- Consultez la documentation existante

## Licence

En contribuant, vous acceptez que vos contributions soient sous licence MIT.

---

Merci de contribuer à améliorer la santé maternelle et infantile au Mali ! 🇲🇱

