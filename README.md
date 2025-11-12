# KènèyaMuso Backend

## 📋 Description

**KènèyaMuso** est une application mobile communautaire dédiée au suivi de la santé maternelle et infantile au Mali. Ce repository contient le backend développé avec Spring Boot.

### Objectifs du projet

- Réduire la mortalité maternelle et infantile par un suivi numérique continu
- Offrir aux mères un outil simple et accessible pour gérer leur santé et celle de leur enfant
- Créer un lien direct entre les femmes et les professionnels de santé
- Sensibiliser les communautés par des modules d'éducation et de prévention

## 🚀 Technologies utilisées

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** avec JWT
- **Spring Data JPA**
- **MySQL** (production) / **H2** (développement)
- **Swagger/OpenAPI** pour la documentation
- **Maven** pour la gestion des dépendances
- **Lombok** pour réduire le code boilerplate

## 📦 Fonctionnalités principales

### Module CPN (Consultations Prénatales)
- Création et suivi de grossesses
- Calcul automatique de la DPA (date prévue d'accouchement)
- Rappels automatiques pour les CPN
- Conseils nutritionnels adaptés

### Module CPoN (Consultations Postnatales)
- Rappels automatiques (J+3, J+7, 6e semaine)
- Suivi postnatal de la mère
- Suivi postnatal du nouveau-né

### Module Vaccination
- Calendrier vaccinal automatisé
- Notifications personnalisées selon l'âge de l'enfant
- Suivi des vaccins effectués

### Conseils & Éducation
- Articles pratiques (nutrition, allaitement, hygiène)
- Tutoriels vidéo
- Contenu multilingue

### Communication
- Chat sécurisé avec des professionnels de santé
- Système de messagerie en temps réel

## 🏗️ Architecture

```
src/
├── main/
│   ├── java/com/keneyamuso/
│   │   ├── config/              # Configuration (Security, OpenAPI)
│   │   ├── controller/          # Controllers REST
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── request/         # DTOs pour les requêtes
│   │   │   └── response/        # DTOs pour les réponses
│   │   ├── exception/           # Gestion des exceptions
│   │   ├── model/               # Entités JPA
│   │   │   ├── entity/          # Entités métier
│   │   │   └── enums/           # Énumérations
│   │   ├── repository/          # Repositories JPA
│   │   ├── security/            # Sécurité JWT
│   │   └── service/             # Services métier
│   └── resources/
│       ├── application.properties
│       └── application-dev.properties
```

## 🛠️ Installation et démarrage

### Prérequis

- Java 17 ou supérieur
- Maven 3.6+
- MySQL 8.0+ (ou utiliser H2 pour le développement)

### 1. Cloner le repository

```bash
git clone https://github.com/votre-org/keneyamuso-backend.git
cd keneyamuso-backend
```

### 2. Configuration de la base de données

#### Option A : Utiliser MySQL (Production)

Créez une base de données MySQL :

```sql
CREATE DATABASE keneyamuso_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Modifiez `src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/keneyamuso_db
spring.datasource.username=votre_utilisateur
spring.datasource.password=votre_mot_de_passe
```

#### Option B : Utiliser H2 (Développement)

Lancez l'application avec le profil `dev` :

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. Compiler et lancer l'application

```bash
# Compiler
mvn clean install

# Lancer
mvn spring-boot:run
```

L'application sera accessible sur `http://localhost:8080`

## 📖 Documentation

📚 **[Voir l'index complet de la documentation](DOCS_INDEX.md)** - Tous les guides organisés par catégorie

### Documentation API

#### Swagger UI

Une fois l'application lancée, accédez à la documentation interactive :

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **OpenAPI JSON** : http://localhost:8080/api-docs

### Authentification

L'API utilise JWT pour l'authentification. Pour accéder aux endpoints protégés :

1. **Inscription** : `POST /api/auth/register`
2. **Connexion** : `POST /api/auth/login`
3. Utilisez le token JWT reçu dans le header `Authorization: Bearer {token}`

### Endpoints principaux

#### Authentification
- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion

#### Grossesses
- `POST /api/grossesses` - Créer une grossesse
- `GET /api/grossesses/{id}` - Obtenir une grossesse
- `GET /api/grossesses/patiente/{patienteId}` - Grossesses d'une patiente
- `PUT /api/grossesses/{id}` - Mettre à jour une grossesse
- `PUT /api/grossesses/{id}/terminer` - Terminer une grossesse

#### Consultations Prénatales
- `POST /api/consultations-prenatales` - Créer une CPN
- `GET /api/consultations-prenatales/{id}` - Obtenir une CPN
- `GET /api/consultations-prenatales/grossesse/{grossesseId}` - CPN d'une grossesse
- `PUT /api/consultations-prenatales/{id}` - Mettre à jour une CPN

#### Consultations Postnatales
- `POST /api/consultations-postnatales` - Créer une CPoN
- `GET /api/consultations-postnatales/{id}` - Obtenir une CPoN
- `GET /api/consultations-postnatales/patiente/{patienteId}` - CPoN d'une patiente

#### Enfants
- `POST /api/enfants` - Créer un enfant
- `GET /api/enfants/{id}` - Obtenir un enfant
- `GET /api/enfants/patiente/{patienteId}` - Enfants d'une patiente

#### Vaccinations
- `POST /api/vaccinations` - Créer une vaccination
- `GET /api/vaccinations/enfant/{enfantId}` - Calendrier vaccinal d'un enfant
- `PUT /api/vaccinations/{id}` - Mettre à jour une vaccination

#### Conseils
- `GET /api/conseils` - Obtenir tous les conseils
- `GET /api/conseils/{id}` - Obtenir un conseil
- `POST /api/conseils` - Créer un conseil (Admin)

#### Messages
- `POST /api/messages` - Envoyer un message
- `GET /api/messages/conversation/{conversationId}` - Messages d'une conversation

### 🧪 Test des Endpoints

📖 **Consultez [TEST_ENDPOINTS.md](TEST_ENDPOINTS.md)** pour le guide complet !

#### Test Rapide (Windows PowerShell)

```powershell
# Tester tous les endpoints automatiquement
.\test-all-endpoints.ps1
```

Ce script teste :
- ✅ Inscription & Authentification
- ✅ Création grossesse → **4 CPN générées automatiquement**
- ✅ Terminaison grossesse → **3 CPoN générées automatiquement**
- ✅ Enregistrement enfant → **19 vaccinations générées automatiquement**
- ✅ Messagerie et conseils

**Résultat** : **29 entités créées** en quelques secondes ! 🎉

## 🔒 Sécurité

### JWT (JSON Web Token)

Le système d'authentification utilise JWT avec les caractéristiques suivantes :

- **Algorithme** : HMAC avec SHA-256
- **Durée de validité** : 24 heures (configurable)
- **Format** : `Authorization: Bearer {token}`

### Rôles utilisateurs

- **PATIENTE** : Femmes enceintes et jeunes mères
- **MEDECIN** : Gynécologues, pédiatres et médecins généralistes
- **ADMINISTRATEUR** : Administrateurs système

### Endpoints publics

- `/api/auth/**` - Authentification
- `/swagger-ui/**` - Documentation
- `/api-docs/**` - Documentation OpenAPI

## 🧪 Tests

```bash
# Lancer tous les tests
mvn test

# Lancer les tests avec coverage
mvn test jacoco:report
```

## 🌍 Internationalisation

L'application supporte le français (fr_FR) par défaut. Les dates sont formatées selon le format dd/MM/yyyy.

## 📊 Base de données

### Entités principales

- **Utilisateur** (classe de base)
  - Patiente
  - ProfessionnelSante
- **Grossesse**
- **ConsultationPrenatale**
- **ConsultationPostnatale**
- **Enfant**
- **Vaccination**
- **Rappel**
- **Conseil**
- **Conversation**
- **Message**

### Schéma relationnel

Les entités sont liées par des relations JPA :
- Une patiente a plusieurs grossesses
- Une grossesse a plusieurs consultations prénatales
- Une patiente a plusieurs enfants
- Un enfant a plusieurs vaccinations
- etc.

## 🚀 Déploiement

### Profils Spring

- **dev** : Utilise H2, génère les tables automatiquement
- **prod** : Utilise MySQL, met à jour les tables

### Variables d'environnement

```bash
export JWT_SECRET=votre_secret_jwt_securise
export DB_URL=jdbc:mysql://localhost:3306/keneyamuso_db
export DB_USERNAME=votre_utilisateur
export DB_PASSWORD=votre_mot_de_passe
```

### Build pour production

```bash
mvn clean package -DskipTests
java -jar target/keneyamuso-backend-1.0.0.jar --spring.profiles.active=prod
```

## 📝 Roadmap

### Sprint 1 (Actuel)
- ✅ Module CPN + CPoN
- ✅ Module vaccination + rappels
- ✅ Authentification JWT
- ✅ Documentation Swagger

### Sprint 2
- ⏳ Système de notifications push
- ⏳ Tableau de bord communautaire
- ⏳ Export des données en PDF

### Sprint 3
- ⏳ Téléconsultation
- ⏳ Statistiques avancées
- ⏳ Support multilingue (Bambara, Soninké)

## 🤝 Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. Fork le projet
2. Créez une branche (`git checkout -b feature/AmazingFeature`)
3. Commit vos changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## 👥 Équipe

- **Chef de projet** : [Nom]
- **Développeurs Backend** : [Noms]
- **Développeurs Mobile** : [Noms]
- **Conseillers médicaux** : [Noms]

## 📞 Contact

- **Email** : contact@keneyamuso.ml
- **Site web** : https://keneyamuso.ml

## 🙏 Remerciements

- Ministère de la Santé du Mali
- OMS (Organisation Mondiale de la Santé)
- Partenaires ONG et associations de santé

---

**KènèyaMuso** - *Pour une maternité saine au Mali* 🇲🇱

