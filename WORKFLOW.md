# 🔄 Workflow KènèyaMuso - Guide Complet

## 📋 Vue d'ensemble

Ce document décrit le workflow complet de l'application KènèyaMuso, de l'inscription jusqu'au suivi vaccinal, en détaillant tous les automatismes mis en place.

---

## 👤 Workflow Patiente

### 1️⃣ Inscription & Profil

#### Étape 1 : Création du compte

**Endpoint** : `POST /api/auth/register`

**Exemple de requête** :
```json
{
  "nom": "Traoré",
  "prenom": "Fatoumata",
  "telephone": "+22370123456",
  "motDePasse": "SecurePass123!",
  "role": "PATIENTE",
  "langue": "fr",
  "dateDeNaissance": "1995-03-15",
  "adresse": "Quartier Hippodrome, Bamako",
  "professionnelSanteId": 5
}
```

**Ce qui se passe** :
- ✅ Création automatique d'un profil `Patiente` (avec date de naissance, adresse)
- ✅ Assignation optionnelle à un professionnel de santé
- ✅ Génération d'un token JWT pour connexion immédiate
- ✅ Mot de passe encodé avec BCrypt

**Réponse** :
```json
{
  "success": true,
  "message": "Inscription réussie",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 12,
    "nom": "Traoré",
    "prenom": "Fatoumata",
    "telephone": "+22370123456",
    "role": "PATIENTE"
  }
}
```

---

### 2️⃣ Suivi de Grossesse (CPN)

#### Étape 1 : Déclaration de la grossesse

**Endpoint** : `POST /api/grossesses`

**Exemple de requête** :
```json
{
  "dateDernieresMenstruations": "2024-01-15",
  "patienteId": 12
}
```

**Calculs automatiques** :

| Élément | Calcul | Résultat |
|---------|--------|----------|
| **DPA** | LMP + 280 jours | 2024-10-21 |
| **CPN1** | LMP + 12 semaines | 2024-04-08 |
| **CPN2** | LMP + 24 semaines | 2024-07-01 |
| **CPN3** | LMP + 32 semaines | 2024-08-26 |
| **CPN4** | LMP + 36 semaines | 2024-09-23 |

**Ce qui est créé automatiquement** :
- ✅ 1 Grossesse (statut : `EN_COURS`)
- ✅ 4 Consultations Prénatales (statut : `A_VENIR`)

**Réponse** :
```json
{
  "success": true,
  "message": "Grossesse créée avec succès",
  "data": {
    "id": 1,
    "dateDebut": "2024-01-15",
    "datePrevueAccouchement": "2024-10-21",
    "statut": "EN_COURS",
    "patiente": { "id": 12, "nom": "Traoré", "prenom": "Fatoumata" }
  }
}
```

#### Étape 2 : Rappels automatiques

**24h avant chaque CPN** :
- 📲 Notification push (à implémenter)
- 💬 Message dans l'application
- 📧 SMS de rappel (optionnel)

#### Étape 3 : Réalisation d'une CPN

**Endpoint** : `PUT /api/consultations-prenatales/{id}`

**Exemple** :
```json
{
  "datePrevue": "2024-04-08",
  "dateRealisee": "2024-04-08",
  "poids": 65.5,
  "tensionArterielle": "120/80",
  "hauteurUterine": 12,
  "notes": "Tout va bien. Grossesse évoluant normalement."
}
```

**Changement automatique** :
- ✅ Statut passe de `A_VENIR` à `REALISEE`

#### Étape 4 : Consultation de l'historique

**Endpoint** : `GET /api/consultations-prenatales/patiente/{patienteId}`

**Réponse** : Liste de toutes les CPN (passées et à venir)

---

### 3️⃣ Accouchement & Suivi Postnatal (CPoN)

#### Étape 1 : Terminer la grossesse

**Endpoint** : `PUT /api/grossesses/{id}/terminer`

**Ce qui se passe automatiquement** :
- ✅ Grossesse passe en statut `TERMINEE`
- ✅ **3 Consultations Postnatales créées automatiquement** :

| CPoN | Date | Description |
|------|------|-------------|
| **J+3** | Accouchement + 3 jours | Contrôle précoce mère et bébé |
| **J+7** | Accouchement + 7 jours | Suivi de récupération |
| **6e semaine** | Accouchement + 42 jours | Bilan complet |

#### Étape 2 : Enregistrement de l'enfant

**Endpoint** : `POST /api/enfants`

**Exemple** :
```json
{
  "nom": "Traoré",
  "prenom": "Ibrahim",
  "dateDeNaissance": "2024-10-21",
  "sexe": "MASCULIN",
  "patienteId": 12
}
```

**Génération automatique du calendrier vaccinal** :

| Âge | Vaccins |
|-----|---------|
| **À la naissance** | BCG, Polio 0 |
| **6 semaines** | Pentavalent 1, Polio 1, Pneumocoque 1, Rotavirus 1 |
| **10 semaines** | Pentavalent 2, Polio 2, Pneumocoque 2, Rotavirus 2 |
| **14 semaines** | Pentavalent 3, Polio 3, Pneumocoque 3 |
| **9 mois** | Rougeole-Rubéole, Fièvre jaune, Méningite A |
| **15 mois** | Rougeole-Rubéole 2 (rappel) |

**Total** : **19 vaccinations créées automatiquement** !

#### Étape 3 : Réalisation d'une CPoN

**Endpoint** : `PUT /api/consultations-postnatales/{id}`

**Exemple** :
```json
{
  "datePrevue": "2024-10-24",
  "dateRealisee": "2024-10-24",
  "notesMere": "Cicatrisation normale, pas de fièvre, allaitement exclusif",
  "notesNouveauNe": "Poids : 3.5kg, allaitement efficace, pas d'ictère"
}
```

---

### 4️⃣ Suivi Vaccinal

#### Étape 1 : Consultation du calendrier

**Endpoint** : `GET /api/vaccinations/enfant/{enfantId}`

**Réponse** : Liste complète des vaccinations (à faire et faites)

#### Étape 2 : Confirmation d'une vaccination

**Endpoint** : `PUT /api/vaccinations/{id}`

**Exemple** :
```json
{
  "nomVaccin": "BCG",
  "datePrevue": "2024-10-21",
  "dateRealisee": "2024-10-21",
  "notes": "Vaccin bien toléré, pas de réaction"
}
```

**Changement automatique** :
- ✅ Statut passe de `A_FAIRE` à `FAIT`

#### Étape 3 : Rappels automatiques

**24h avant chaque vaccination** :
- 📲 Notification : "Vaccination de Ibrahim prévue demain : BCG"
- 📅 Ajout au calendrier

---

### 5️⃣ Messagerie & Conseils

#### Envoyer un message au médecin

**Endpoint** : `POST /api/messages`

**Exemple** :
```json
{
  "conversationId": 3,
  "contenu": "Bonjour Docteur, j'ai des nausées matinales. Est-ce normal ?"
}
```

#### Recevoir des conseils personnalisés

**Endpoint** : `GET /api/conseils`

**Filtrage automatique** selon :
- ✅ Statut (enceinte / jeune mère)
- ✅ Langue préférée
- ✅ Catégorie (NUTRITION, HYGIENE, ALLAITEMENT, etc.)

---

## 👨‍⚕️ Workflow Médecin

### 1️⃣ Inscription

**Endpoint** : `POST /api/auth/register`

**Exemple** :
```json
{
  "nom": "Diarra",
  "prenom": "Moussa",
  "telephone": "+22376543210",
  "motDePasse": "MedecinSecure123!",
  "role": "MEDECIN",
  "langue": "fr",
  "specialite": "GYNECOLOGUE",
  "identifiantProfessionnel": "ML-GYN-12345"
}
```

**Ce qui se passe** :
- ✅ Création d'un profil `ProfessionnelSante`
- ✅ Enregistrement de la spécialité et de l'identifiant professionnel

---

### 2️⃣ Gestion des Patientes

#### Voir mes patientes assignées

**Endpoint** : `GET /api/patientes` (à créer)

#### Accéder au dossier complet d'une patiente

**Endpoints disponibles** :
- `GET /api/grossesses/patiente/{patienteId}` - Grossesses
- `GET /api/consultations-prenatales/patiente/{patienteId}` - CPN
- `GET /api/consultations-postnatales/patiente/{patienteId}` - CPoN
- `GET /api/enfants/patiente/{patienteId}` - Enfants
- `GET /api/vaccinations/enfant/{enfantId}` - Vaccinations

---

### 3️⃣ Validation des Consultations

#### Valider une CPN

**Endpoint** : `PUT /api/consultations-prenatales/{id}`

**Actions possibles** :
- ✅ Ajouter des notes médicales
- ✅ Enregistrer les mesures (poids, tension, hauteur utérine)
- ✅ Marquer comme `REALISEE` ou `MANQUEE`

#### Générer un rappel supplémentaire

**Endpoint** : `POST /api/rappels` (à créer)

---

### 4️⃣ Communication

#### Répondre aux messages

**Endpoint** : `POST /api/messages`

#### Envoyer des conseils personnalisés

**Endpoint** : `POST /api/conseils`

---

## 📊 Résumé des Automatismes

| Action | Automatisme | Nombre |
|--------|-------------|--------|
| **Inscription Patiente** | Profil complet créé | 1 profil |
| **Création Grossesse** | CPN générées | 4 consultations |
| **Terminer Grossesse** | CPoN générées | 3 consultations |
| **Création Enfant** | Calendrier vaccinal complet | 19 vaccinations |

**Total : 27 éléments créés automatiquement pour un cycle complet !** 🎉

---

## 🔔 Système de Rappels (à implémenter)

### Types de rappels
- 📅 CPN (24h avant)
- 📅 CPoN (24h avant)
- 💉 Vaccinations (24h avant)
- 💊 Prise de médicaments

### Canaux
- 📲 Notifications push
- 💬 Messages in-app
- 📧 SMS (optionnel)

---

## 🔒 Sécurité & Autorisations

### Endpoints publics
- `/api/auth/register`
- `/api/auth/login`
- `/swagger-ui/**`

### Endpoints protégés

| Endpoint | Rôles autorisés |
|----------|-----------------|
| `POST /api/grossesses` | PATIENTE |
| `GET /api/grossesses/{id}` | PATIENTE, MEDECIN, ADMINISTRATEUR |
| `POST /api/consultations-prenatales` | MEDECIN |
| `PUT /api/consultations-prenatales/{id}` | MEDECIN |
| `POST /api/conseils` | ADMINISTRATEUR |

---

## 📱 Prochaines étapes

### Sprint 2
- [ ] Système de rappels automatiques (Firebase Cloud Messaging)
- [ ] WebSockets pour messagerie temps réel
- [ ] Statistiques et tableaux de bord
- [ ] Export PDF des carnets de santé

### Sprint 3
- [ ] Géolocalisation des centres de santé
- [ ] Support multilingue (Bambara, Soninké)
- [ ] Téléconsultation vidéo
- [ ] Module communautaire (forum, témoignages)

---

## 🎯 Points clés à retenir

1. **Automatisation maximale** : Moins de saisie = Plus de temps pour le suivi
2. **Calendriers intelligents** : DPA, CPN, CPoN, Vaccinations calculés automatiquement
3. **Conformité OMS/PEV Mali** : Respect des recommandations officielles
4. **Expérience simplifiée** : Un seul clic pour créer tout un workflow

---

**KènèyaMuso** - *Pour une maternité saine au Mali* 🇲🇱

