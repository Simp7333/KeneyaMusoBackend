# 🧪 Guide de Test Complet - Tous les Endpoints KènèyaMuso

## 📋 Vue d'ensemble

Ce guide vous permet de tester **tous les endpoints** de l'API dans l'ordre logique d'utilisation.

**Prérequis** : L'application doit être lancée sur `http://localhost:8080`

---

## 🔐 1. Authentification

### 1.1 Inscription d'une Patiente

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Traoré",
    "prenom": "Fatoumata",
    "telephone": "+22370123456",
    "motDePasse": "Test123!",
    "role": "PATIENTE",
    "langue": "fr",
    "dateDeNaissance": "1995-03-15",
    "adresse": "Quartier Hippodrome, Bamako"
  }'
```

**Résultat attendu** :
```json
{
  "success": true,
  "message": "Inscription réussie",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 1,
    "nom": "Traoré",
    "prenom": "Fatoumata",
    "telephone": "+22370123456",
    "role": "PATIENTE"
  }
}
```

**📝 Copier le `token` pour les prochaines requêtes !**

---

### 1.2 Inscription d'un Médecin

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Diarra",
    "prenom": "Moussa",
    "telephone": "+22376543210",
    "motDePasse": "Medecin123!",
    "role": "MEDECIN",
    "langue": "fr",
    "specialite": "GYNECOLOGUE",
    "identifiantProfessionnel": "ML-GYN-12345"
  }'
```

---

### 1.3 Connexion

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "telephone": "+22370123456",
    "motDePasse": "Test123!"
  }'
```

---

## 🤰 2. Gestion des Grossesses

### 2.1 Créer une Grossesse (→ Génère 4 CPN automatiquement !)

```bash
curl -X POST http://localhost:8080/api/grossesses \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dateDernieresMenstruations": "2024-01-15",
    "patienteId": 1
  }'
```

**Résultat** :
- ✅ 1 Grossesse créée
- ✅ **4 CPN générées automatiquement** (12, 24, 32, 36 semaines)
- ✅ DPA calculée automatiquement (LMP + 280 jours)

---

### 2.2 Voir une Grossesse

```bash
curl -X GET http://localhost:8080/api/grossesses/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 2.3 Voir toutes les Grossesses d'une Patiente

```bash
curl -X GET http://localhost:8080/api/grossesses/patiente/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 2.4 Voir toutes les Grossesses (Médecin/Admin)

```bash
curl -X GET http://localhost:8080/api/grossesses \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 2.5 Mettre à jour une Grossesse

```bash
curl -X PUT http://localhost:8080/api/grossesses/1 \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dateDernieresMenstruations": "2024-01-20",
    "patienteId": 1
  }'
```

---

### 2.6 Terminer une Grossesse (→ Génère 3 CPoN automatiquement !)

```bash
curl -X PUT http://localhost:8080/api/grossesses/1/terminer \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

**Résultat** :
- ✅ Grossesse statut → `TERMINEE`
- ✅ **3 CPoN générées automatiquement** (J+3, J+7, 6e semaine)

---

## 📅 3. Consultations Prénatales (CPN)

### 3.1 Voir toutes les CPN d'une Grossesse

```bash
curl -X GET http://localhost:8080/api/consultations-prenatales/grossesse/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

**Résultat attendu** : 4 CPN automatiquement créées

---

### 3.2 Voir toutes les CPN d'une Patiente

```bash
curl -X GET http://localhost:8080/api/consultations-prenatales/patiente/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 3.3 Voir une CPN spécifique

```bash
curl -X GET http://localhost:8080/api/consultations-prenatales/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 3.4 Mettre à jour une CPN (Enregistrer la consultation)

```bash
curl -X PUT http://localhost:8080/api/consultations-prenatales/1 \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "datePrevue": "2024-04-08",
    "dateRealisee": "2024-04-08",
    "poids": 65.5,
    "tensionArterielle": "120/80",
    "hauteurUterine": 12,
    "notes": "Grossesse évoluant normalement. Tout va bien.",
    "grossesseId": 1
  }'
```

**Résultat** : Statut passe à `REALISEE` ✅

---

### 3.5 Marquer une CPN comme Manquée

```bash
curl -X PUT http://localhost:8080/api/consultations-prenatales/2/manquee \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 3.6 Créer une CPN Manuelle (optionnel)

```bash
curl -X POST http://localhost:8080/api/consultations-prenatales \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "datePrevue": "2024-05-15",
    "notes": "CPN supplémentaire",
    "grossesseId": 1
  }'
```

---

## 🍼 4. Consultations Postnatales (CPoN)

### 4.1 Voir toutes les CPoN d'une Patiente

```bash
curl -X GET http://localhost:8080/api/consultations-postnatales/patiente/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

**Résultat attendu** : 3 CPoN si vous avez terminé une grossesse

---

### 4.2 Voir une CPoN spécifique

```bash
curl -X GET http://localhost:8080/api/consultations-postnatales/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 4.3 Mettre à jour une CPoN

```bash
curl -X PUT http://localhost:8080/api/consultations-postnatales/1 \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "datePrevue": "2024-10-24",
    "dateRealisee": "2024-10-24",
    "notesMere": "Cicatrisation normale, pas de fièvre, allaitement exclusif",
    "notesNouveauNe": "Poids: 3.5kg, allaitement efficace, pas d'ictère",
    "patienteId": 1
  }'
```

---

## 👶 5. Gestion des Enfants

### 5.1 Enregistrer un Enfant (→ Génère 19 vaccinations automatiquement !)

```bash
curl -X POST http://localhost:8080/api/enfants \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Traoré",
    "prenom": "Ibrahim",
    "dateDeNaissance": "2024-10-21",
    "sexe": "MASCULIN",
    "patienteId": 1
  }'
```

**Résultat** :
- ✅ 1 Enfant créé
- ✅ **19 vaccinations générées automatiquement** selon le PEV Mali !

---

### 5.2 Voir tous les Enfants d'une Patiente

```bash
curl -X GET http://localhost:8080/api/enfants/patiente/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 5.3 Voir un Enfant spécifique

```bash
curl -X GET http://localhost:8080/api/enfants/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 5.4 Mettre à jour un Enfant

```bash
curl -X PUT http://localhost:8080/api/enfants/1 \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Traoré",
    "prenom": "Ibrahim Bakary",
    "dateDeNaissance": "2024-10-21",
    "sexe": "MASCULIN",
    "patienteId": 1
  }'
```

---

## 💉 6. Vaccinations

### 6.1 Voir le Calendrier Vaccinal Complet d'un Enfant

```bash
curl -X GET http://localhost:8080/api/vaccinations/enfant/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

**Résultat attendu** : 19 vaccinations (BCG, Polio, Pentavalent, etc.)

---

### 6.2 Voir une Vaccination spécifique

```bash
curl -X GET http://localhost:8080/api/vaccinations/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 6.3 Confirmer une Vaccination (Marquer comme FAIT)

```bash
curl -X PUT http://localhost:8080/api/vaccinations/1 \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nomVaccin": "BCG",
    "datePrevue": "2024-10-21",
    "dateRealisee": "2024-10-21",
    "notes": "Vaccin bien toléré, aucune réaction",
    "enfantId": 1
  }'
```

**Résultat** : Statut passe à `FAIT` ✅

---

### 6.4 Créer une Vaccination Manuelle (optionnel)

```bash
curl -X POST http://localhost:8080/api/vaccinations \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nomVaccin": "COVID-19",
    "datePrevue": "2025-01-15",
    "notes": "Vaccination supplémentaire recommandée",
    "enfantId": 1
  }'
```

---

## 📚 7. Conseils Éducatifs

### 7.1 Voir tous les Conseils Actifs

```bash
curl -X GET http://localhost:8080/api/conseils \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 7.2 Voir un Conseil spécifique

```bash
curl -X GET http://localhost:8080/api/conseils/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 7.3 Créer un Conseil (Admin uniquement)

```bash
curl -X POST http://localhost:8080/api/conseils \
  -H "Authorization: Bearer VOTRE_TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Importance de l'allaitement maternel",
    "contenu": "L'allaitement maternel exclusif pendant les 6 premiers mois est recommandé par l'OMS...",
    "categorie": "ALLAITEMENT",
    "cible": "Jeunes mères",
    "lienMedia": "https://youtube.com/watch?v=example"
  }'
```

---

### 7.4 Mettre à jour un Conseil

```bash
curl -X PUT http://localhost:8080/api/conseils/1 \
  -H "Authorization: Bearer VOTRE_TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Importance de l'allaitement maternel - Mis à jour",
    "contenu": "Contenu mis à jour...",
    "categorie": "ALLAITEMENT",
    "cible": "Toutes les mères"
  }'
```

---

## 💬 8. Messagerie

### 8.1 Envoyer un Message

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": 1,
    "contenu": "Bonjour Docteur, j'ai des nausées matinales. Est-ce normal ?"
  }'
```

---

### 8.2 Voir les Messages d'une Conversation

```bash
curl -X GET http://localhost:8080/api/messages/conversation/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

### 8.3 Marquer un Message comme Lu

```bash
curl -X PUT http://localhost:8080/api/messages/1/lire \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

## 🧪 9. Script de Test Complet (Bash)

Créer un fichier `test-all-endpoints.sh` :

```bash
#!/bin/bash

API_URL="http://localhost:8080"
TOKEN=""

echo "======================================"
echo "  Test Complet API KènèyaMuso"
echo "======================================"
echo ""

# 1. Inscription Patiente
echo "1️⃣  Inscription d'une patiente..."
RESPONSE=$(curl -s -X POST $API_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Traoré",
    "prenom": "Fatoumata",
    "telephone": "+22370123456",
    "motDePasse": "Test123!",
    "role": "PATIENTE",
    "langue": "fr",
    "dateDeNaissance": "1995-03-15",
    "adresse": "Bamako"
  }')

TOKEN=$(echo $RESPONSE | jq -r '.data.token')
echo "✅ Token récupéré: ${TOKEN:0:20}..."
echo ""

# 2. Créer une grossesse
echo "2️⃣  Création d'une grossesse..."
GROSSESSE=$(curl -s -X POST $API_URL/api/grossesses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dateDernieresMenstruations": "2024-01-15",
    "patienteId": 1
  }')

GROSSESSE_ID=$(echo $GROSSESSE | jq -r '.data.id')
echo "✅ Grossesse créée (ID: $GROSSESSE_ID)"
echo "✅ 4 CPN générées automatiquement !"
echo ""

# 3. Voir les CPN
echo "3️⃣  Récupération des CPN..."
CPN=$(curl -s -X GET $API_URL/api/consultations-prenatales/grossesse/$GROSSESSE_ID \
  -H "Authorization: Bearer $TOKEN")

CPN_COUNT=$(echo $CPN | jq -r '.data | length')
echo "✅ $CPN_COUNT CPN trouvées"
echo ""

# 4. Terminer la grossesse
echo "4️⃣  Terminaison de la grossesse..."
curl -s -X PUT $API_URL/api/grossesses/$GROSSESSE_ID/terminer \
  -H "Authorization: Bearer $TOKEN"
echo "✅ Grossesse terminée"
echo "✅ 3 CPoN générées automatiquement !"
echo ""

# 5. Créer un enfant
echo "5️⃣  Enregistrement d'un enfant..."
ENFANT=$(curl -s -X POST $API_URL/api/enfants \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Traoré",
    "prenom": "Ibrahim",
    "dateDeNaissance": "2024-10-21",
    "sexe": "MASCULIN",
    "patienteId": 1
  }')

ENFANT_ID=$(echo $ENFANT | jq -r '.data.id')
echo "✅ Enfant créé (ID: $ENFANT_ID)"
echo "✅ 19 vaccinations générées automatiquement !"
echo ""

# 6. Voir les vaccinations
echo "6️⃣  Récupération du calendrier vaccinal..."
VACCINS=$(curl -s -X GET $API_URL/api/vaccinations/enfant/$ENFANT_ID \
  -H "Authorization: Bearer $TOKEN")

VACCINS_COUNT=$(echo $VACCINS | jq -r '.data | length')
echo "✅ $VACCINS_COUNT vaccinations trouvées"
echo ""

# 7. Envoyer un message
echo "7️⃣  Envoi d'un message..."
curl -s -X POST $API_URL/api/messages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": 1,
    "contenu": "Test message automatique"
  }' > /dev/null
echo "✅ Message envoyé"
echo ""

echo "======================================"
echo "  ✅ Tous les tests réussis !"
echo "======================================"
echo ""
echo "📊 Résumé:"
echo "  - 1 Patiente inscrite"
echo "  - 1 Grossesse créée"
echo "  - 4 CPN générées automatiquement"
echo "  - 3 CPoN générées automatiquement"
echo "  - 1 Enfant enregistré"
echo "  - 19 Vaccinations générées automatiquement"
echo "  - 1 Message envoyé"
echo ""
echo "Total: 30 entités créées automatiquement ! 🎉"
```

**Exécuter** :
```bash
chmod +x test-all-endpoints.sh
./test-all-endpoints.sh
```

---

## 📊 10. Collection Postman

### Importer dans Postman

1. Ouvrir Postman
2. **Import** → **Link**
3. Coller : `http://localhost:8080/api-docs`
4. Tous les endpoints seront importés automatiquement ! 🚀

### Ou créer manuellement

**Collection : KènèyaMuso**

Variables :
- `baseUrl` : `http://localhost:8080`
- `token` : (à remplir après login)

Requêtes :
- Auth → Register Patiente
- Auth → Register Médecin
- Auth → Login
- Grossesses → Create
- Grossesses → Get All
- CPN → Get by Grossesse
- Enfants → Create
- Vaccinations → Get by Enfant
- etc.

---

## ✅ Checklist de Test

### Authentification
- [ ] Inscription Patiente
- [ ] Inscription Médecin
- [ ] Inscription Admin
- [ ] Connexion

### Grossesses
- [ ] Créer grossesse → Vérifier 4 CPN créées
- [ ] Voir grossesses
- [ ] Mettre à jour grossesse → Vérifier recalcul DPA
- [ ] Terminer grossesse → Vérifier 3 CPoN créées

### CPN
- [ ] Voir CPN d'une grossesse
- [ ] Mettre à jour CPN → Vérifier statut REALISEE
- [ ] Marquer CPN comme manquée

### CPoN
- [ ] Voir CPoN d'une patiente
- [ ] Mettre à jour CPoN

### Enfants
- [ ] Créer enfant → Vérifier 19 vaccinations créées
- [ ] Voir enfants d'une patiente
- [ ] Mettre à jour enfant

### Vaccinations
- [ ] Voir calendrier vaccinal
- [ ] Confirmer vaccination → Vérifier statut FAIT

### Conseils
- [ ] Voir conseils
- [ ] Créer conseil (admin)
- [ ] Mettre à jour conseil

### Messages
- [ ] Envoyer message
- [ ] Voir messages conversation
- [ ] Marquer comme lu

---

## 🎯 Résultat Attendu

Après avoir testé tous les endpoints, vous devriez avoir :

```
📊 Base de données contient :
├── 1 Patiente
├── 1 Médecin
├── 1 Grossesse
├── 4 CPN (générées auto)
├── 3 CPoN (générées auto)
├── 1 Enfant
├── 19 Vaccinations (générées auto)
├── N Conseils
└── N Messages

Total : 30+ entités créées automatiquement ! 🎉
```

---

**Tous les endpoints sont testés ! 🚀**

Documentation complète dans `TEST_ENDPOINTS.md`

