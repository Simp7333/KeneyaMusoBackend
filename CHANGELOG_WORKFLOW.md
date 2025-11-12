# 📝 Changelog - Workflow Automatisé

## Version 2.0.0 - Workflow Automatisé (16 Octobre 2024)

### 🎉 Nouveautés Majeures

#### 1. Inscription avec Profil Complet

**Before** :
- Inscription basique avec nom, prénom, téléphone, mot de passe, rôle

**Now** :
- ✅ **PATIENTE** : Inscription avec date de naissance, adresse, et assignation optionnelle à un médecin
- ✅ **MEDECIN** : Inscription avec spécialité (GYNECOLOGUE, PEDIATRE, GENERALISTE) et identifiant professionnel
- ✅ Création automatique des entités `Patiente` et `ProfessionnelSante` selon le rôle

**Fichiers modifiés** :
- `RegisterRequest.java` : Ajout des champs spécifiques par rôle
- `AuthService.java` : Logique d'inscription différenciée

---

#### 2. Calcul Automatique de la DPA (Date Prévue d'Accouchement)

**Before** :
- La patiente devait saisir manuellement la date de début et la DPA

**Now** :
- ✅ La patiente saisit uniquement la **LMP** (Last Menstrual Period = Date de Dernières Menstruations)
- ✅ **DPA calculée automatiquement** : LMP + 280 jours (règle de Naegele)
- ✅ Simplicité maximale pour l'utilisatrice

**Fichiers modifiés** :
- `GrossesseRequest.java` : Remplacé `dateDebut` et `datePrevueAccouchement` par `dateDernieresMenstruations`
- `GrossesseService.java` : Ajout du calcul automatique de la DPA

---

#### 3. Génération Automatique des 4 CPN (Consultations Prénatales)

**Before** :
- Les CPN devaient être créées manuellement une par une

**Now** :
- ✅ **4 CPN créées automatiquement** lors de la déclaration d'une grossesse :
  - CPN1 : LMP + 12 semaines (1er trimestre)
  - CPN2 : LMP + 24 semaines (2e trimestre)
  - CPN3 : LMP + 32 semaines (3e trimestre)
  - CPN4 : LMP + 36 semaines (fin de grossesse)
- ✅ Statut initial : `A_VENIR`
- ✅ Conformité aux recommandations OMS

**Fichiers modifiés** :
- `GrossesseService.java` : Ajout de `genererConsultationsPrenatales()` et `creerCPN()`
- Injection de `ConsultationPrenataleRepository`

---

#### 4. Génération Automatique des 3 CPoN (Consultations Postnatales)

**Before** :
- Les CPoN devaient être créées manuellement après l'accouchement

**Now** :
- ✅ **3 CPoN créées automatiquement** lors de la clôture d'une grossesse :
  - CPoN J+3 : 3 jours après l'accouchement
  - CPoN J+7 : 7 jours après l'accouchement
  - CPoN 6e semaine : 42 jours après l'accouchement
- ✅ Création déclenchée par `PUT /api/grossesses/{id}/terminer`
- ✅ Conformité aux recommandations OMS

**Fichiers modifiés** :
- `GrossesseService.terminerGrossesse()` : Ajout de `genererConsultationsPostnatales()` et `creerCPoN()`
- Injection de `ConsultationPostnataleRepository`

---

#### 5. Génération Automatique du Calendrier Vaccinal (19 vaccinations)

**Before** :
- Les vaccinations devaient être créées manuellement

**Now** :
- ✅ **19 vaccinations créées automatiquement** lors de l'enregistrement d'un enfant
- ✅ Calendrier complet selon le **Programme Élargi de Vaccination (PEV) du Mali** :

| Âge | Vaccins | Nombre |
|-----|---------|--------|
| Naissance | BCG, Polio 0 | 2 |
| 6 semaines | Pentavalent 1, Polio 1, Pneumocoque 1, Rotavirus 1 | 4 |
| 10 semaines | Pentavalent 2, Polio 2, Pneumocoque 2, Rotavirus 2 | 4 |
| 14 semaines | Pentavalent 3, Polio 3, Pneumocoque 3 | 3 |
| 9 mois | Rougeole-Rubéole, Fièvre jaune, Méningite A | 3 |
| 15 mois | Rougeole-Rubéole 2 (rappel) | 1 |
| **Total** | | **19** |

**Fichiers modifiés** :
- `EnfantService.java` : Ajout de `genererCalendrierVaccinal()` et `creerVaccin()`
- Injection de `VaccinationRepository`

---

### 📋 Résumé des Automatismes

#### Un Cycle Complet de Maternité

1. **Inscription** : Profil complet créé automatiquement
2. **Déclaration Grossesse** : DPA calculée + **4 CPN générées**
3. **Accouchement** : **3 CPoN générées**
4. **Enregistrement Enfant** : **19 vaccinations générées**

**Total : 27 éléments créés automatiquement ! 🎉**

---

### 🔄 Changements dans les DTOs

#### RegisterRequest

```java
// Nouveaux champs
private LocalDate dateDeNaissance;        // PATIENTE (obligatoire)
private String adresse;                   // PATIENTE (optionnel)
private Long professionnelSanteId;        // PATIENTE (optionnel)
private Specialite specialite;            // MEDECIN (obligatoire)
private String identifiantProfessionnel;  // MEDECIN (obligatoire)
```

#### GrossesseRequest

```java
// Avant
private LocalDate dateDebut;
private LocalDate datePrevueAccouchement;

// Maintenant
private LocalDate dateDernieresMenstruations; // LMP uniquement
// DPA calculée automatiquement
```

---

### 🗂️ Nouveaux Fichiers de Documentation

1. **WORKFLOW.md** : Guide complet du workflow côté patiente et médecin
2. **WORKFLOW_MIGRATION.md** : Guide de migration des données existantes
3. **FRONTEND_INTEGRATION.md** : Guide d'intégration frontend avec exemples React/TypeScript
4. **CHANGELOG_WORKFLOW.md** : Ce fichier

---

### 🚀 Impact sur les Performances

#### Avant (approche manuelle)
- Déclaration grossesse : **1 requête HTTP** → 1 entité créée
- Enregistrement enfant : **1 requête HTTP** → 1 entité créée
- **19 requêtes supplémentaires** pour créer les vaccinations
- **Total : 20+ requêtes**

#### Maintenant (approche automatisée)
- Déclaration grossesse : **1 requête HTTP** → 5 entités créées (1 grossesse + 4 CPN)
- Enregistrement enfant : **1 requête HTTP** → 20 entités créées (1 enfant + 19 vaccinations)
- **Total : 2 requêtes** pour le même résultat

**Réduction de 90% des requêtes HTTP ! 🚀**

---

### 🛠️ Modifications Techniques

#### Services modifiés

| Service | Méthode | Changement |
|---------|---------|------------|
| `AuthService` | `register()` | Création différenciée Patiente/ProfessionnelSante/Administrateur |
| `GrossesseService` | `createGrossesse()` | Calcul DPA + génération 4 CPN |
| `GrossesseService` | `terminerGrossesse()` | Génération 3 CPoN |
| `EnfantService` | `createEnfant()` | Génération calendrier vaccinal (19 vaccinations) |

#### Nouvelles dépendances injectées

- `GrossesseService` : `ConsultationPrenataleRepository`, `ConsultationPostnataleRepository`
- `EnfantService` : `VaccinationRepository`

---

### ✅ Tests Recommandés

#### Test 1 : Inscription Patiente
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Test",
    "prenom": "Patiente",
    "telephone": "+22370000001",
    "motDePasse": "test123",
    "role": "PATIENTE",
    "dateDeNaissance": "1995-01-01",
    "adresse": "Bamako"
  }'
```

**Vérifications** :
- ✅ Table `patientes` contient la nouvelle entrée
- ✅ Champs `date_de_naissance` et `adresse` renseignés

#### Test 2 : Déclaration Grossesse
```bash
curl -X POST http://localhost:8080/api/grossesses \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dateDernieresMenstruations": "2024-01-15",
    "patienteId": 1
  }'
```

**Vérifications** :
- ✅ `datePrevueAccouchement` = 2024-10-21 (LMP + 280 jours)
- ✅ 4 CPN créées dans `consultations_prenatales`
- ✅ Dates : 2024-04-08, 2024-07-01, 2024-08-26, 2024-09-23

#### Test 3 : Terminer Grossesse
```bash
curl -X PUT http://localhost:8080/api/grossesses/1/terminer \
  -H "Authorization: Bearer TOKEN"
```

**Vérifications** :
- ✅ Statut grossesse = `TERMINEE`
- ✅ 3 CPoN créées dans `consultations_postnatales`
- ✅ Dates : J+3, J+7, J+42

#### Test 4 : Enregistrement Enfant
```bash
curl -X POST http://localhost:8080/api/enfants \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Test",
    "prenom": "Bebe",
    "dateDeNaissance": "2024-10-21",
    "sexe": "MASCULIN",
    "patienteId": 1
  }'
```

**Vérifications** :
- ✅ 19 vaccinations créées dans `vaccinations`
- ✅ Dates correctes selon le PEV Mali

---

### 🔜 Prochaines Étapes

#### Sprint 2 (à venir)
- [ ] Système de rappels automatiques (Firebase)
- [ ] Notifications 24h avant chaque CPN/CPoN/Vaccination
- [ ] WebSockets pour messagerie temps réel
- [ ] Tableau de bord statistiques

#### Sprint 3 (à venir)
- [ ] Géolocalisation des centres de santé
- [ ] Téléconsultation vidéo
- [ ] Support multilingue (Bambara, Soninké)
- [ ] Module communautaire

---

### 📞 Support

Pour toute question sur cette nouvelle version :
- **Documentation** : Voir WORKFLOW.md
- **Migration** : Voir WORKFLOW_MIGRATION.md
- **Frontend** : Voir FRONTEND_INTEGRATION.md
- **Email** : contact@keneyamuso.ml

---

**KènèyaMuso** - *Pour une maternité saine au Mali* 🇲🇱

