# Exemples d'utilisation de l'API KènèyaMuso

Ce document contient des exemples de requêtes pour tester l'API.

## 🔐 Authentification

### 1. Inscription d'une patiente

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Traoré",
    "prenom": "Aminata",
    "telephone": "+22370123456",
    "motDePasse": "password123",
    "role": "PATIENTE",
    "langue": "fr"
  }'
```

### 2. Inscription d'un médecin

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Diallo",
    "prenom": "Fatoumata",
    "telephone": "+22370654321",
    "motDePasse": "password123",
    "role": "MEDECIN",
    "langue": "fr"
  }'
```

### 3. Connexion

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "telephone": "+22370123456",
    "motDePasse": "password123"
  }'
```

**Réponse :**
```json
{
  "success": true,
  "message": "Connexion réussie",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 1,
    "nom": "Traoré",
    "prenom": "Aminata",
    "telephone": "+22370123456",
    "role": "PATIENTE"
  },
  "timestamp": "2025-10-16T10:30:00"
}
```

## 🤰 Gestion des grossesses

### 1. Créer une grossesse

```bash
curl -X POST http://localhost:8080/api/grossesses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "dateDebut": "2025-02-15",
    "datePrevueAccouchement": "2025-11-22",
    "patienteId": 1
  }'
```

### 2. Obtenir toutes les grossesses d'une patiente

```bash
curl -X GET http://localhost:8080/api/grossesses/patiente/1 \
  -H "Authorization: Bearer {votre_token}"
```

### 3. Terminer une grossesse

```bash
curl -X PUT http://localhost:8080/api/grossesses/1/terminer \
  -H "Authorization: Bearer {votre_token}"
```

## 👶 Consultations Prénatales (CPN)

### 1. Créer une CPN

```bash
curl -X POST http://localhost:8080/api/consultations-prenatales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "datePrevue": "2025-05-15",
    "notes": "Première consultation, tout va bien",
    "poids": 65.5,
    "tensionArterielle": "120/80",
    "hauteurUterine": 20.0,
    "grossesseId": 1
  }'
```

### 2. Mettre à jour une CPN (consultation réalisée)

```bash
curl -X PUT http://localhost:8080/api/consultations-prenatales/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "datePrevue": "2025-05-15",
    "dateRealisee": "2025-05-15",
    "notes": "Consultation effectuée, bébé en bonne santé",
    "poids": 67.0,
    "tensionArterielle": "118/78",
    "hauteurUterine": 22.0,
    "grossesseId": 1
  }'
```

### 3. Obtenir toutes les CPN d'une grossesse

```bash
curl -X GET http://localhost:8080/api/consultations-prenatales/grossesse/1 \
  -H "Authorization: Bearer {votre_token}"
```

## 👶 Gestion des enfants

### 1. Créer un enfant

```bash
curl -X POST http://localhost:8080/api/enfants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "nom": "Traoré",
    "prenom": "Sekou",
    "dateDeNaissance": "2025-11-20",
    "sexe": "MASCULIN",
    "patienteId": 1
  }'
```

### 2. Obtenir les enfants d'une patiente

```bash
curl -X GET http://localhost:8080/api/enfants/patiente/1 \
  -H "Authorization: Bearer {votre_token}"
```

## 💉 Gestion des vaccinations

### 1. Créer une vaccination

```bash
curl -X POST http://localhost:8080/api/vaccinations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "nomVaccin": "BCG",
    "datePrevue": "2025-11-27",
    "notes": "À faire à la naissance",
    "enfantId": 1
  }'
```

### 2. Marquer une vaccination comme effectuée

```bash
curl -X PUT http://localhost:8080/api/vaccinations/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "nomVaccin": "BCG",
    "datePrevue": "2025-11-27",
    "dateRealisee": "2025-11-27",
    "notes": "Vaccination effectuée avec succès",
    "enfantId": 1
  }'
```

### 3. Obtenir le calendrier vaccinal d'un enfant

```bash
curl -X GET http://localhost:8080/api/vaccinations/enfant/1 \
  -H "Authorization: Bearer {votre_token}"
```

### 4. Créer plusieurs vaccinations (calendrier complet)

```bash
# Polio 0 (naissance)
curl -X POST http://localhost:8080/api/vaccinations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "nomVaccin": "Polio 0",
    "datePrevue": "2025-11-27",
    "enfantId": 1
  }'

# Penta 1 (6 semaines)
curl -X POST http://localhost:8080/api/vaccinations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "nomVaccin": "Pentavalent 1",
    "datePrevue": "2026-01-08",
    "enfantId": 1
  }'

# Penta 2 (10 semaines)
curl -X POST http://localhost:8080/api/vaccinations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "nomVaccin": "Pentavalent 2",
    "datePrevue": "2026-02-05",
    "enfantId": 1
  }'

# Penta 3 (14 semaines)
curl -X POST http://localhost:8080/api/vaccinations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "nomVaccin": "Pentavalent 3",
    "datePrevue": "2026-03-05",
    "enfantId": 1
  }'

# Rougeole (9 mois)
curl -X POST http://localhost:8080/api/vaccinations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "nomVaccin": "Rougeole",
    "datePrevue": "2026-08-27",
    "enfantId": 1
  }'
```

## 🩺 Consultations Postnatales (CPoN)

### 1. Créer une CPoN (J+3)

```bash
curl -X POST http://localhost:8080/api/consultations-postnatales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "type": "J+3",
    "datePrevue": "2025-11-23",
    "notesMere": "Récupération normale, pas de complications",
    "notesNouveauNe": "Bébé en bonne santé, allaitement maternel",
    "patienteId": 1,
    "enfantId": 1
  }'
```

### 2. Créer une CPoN (J+7)

```bash
curl -X POST http://localhost:8080/api/consultations-postnatales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "type": "J+7",
    "datePrevue": "2025-11-27",
    "patienteId": 1,
    "enfantId": 1
  }'
```

### 3. Créer une CPoN (6e semaine)

```bash
curl -X POST http://localhost:8080/api/consultations-postnatales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "type": "6e semaine",
    "datePrevue": "2026-01-01",
    "patienteId": 1,
    "enfantId": 1
  }'
```

## 📚 Gestion des conseils

### 1. Créer un conseil (Admin/Professionnel)

```bash
curl -X POST http://localhost:8080/api/conseils \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "titre": "L'\''importance de l'\''allaitement maternel",
    "contenu": "L'\''allaitement maternel exclusif pendant les 6 premiers mois...",
    "categorie": "ALLAITEMENT",
    "cible": "Jeune mère"
  }'
```

### 2. Obtenir tous les conseils actifs

```bash
curl -X GET http://localhost:8080/api/conseils \
  -H "Authorization: Bearer {votre_token}"
```

## 💬 Messagerie

### 1. Envoyer un message

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {votre_token}" \
  -d '{
    "contenu": "Bonjour docteur, j'\''ai une question concernant...",
    "conversationId": 1
  }'
```

### 2. Obtenir les messages d'une conversation

```bash
curl -X GET http://localhost:8080/api/messages/conversation/1 \
  -H "Authorization: Bearer {votre_token}"
```

### 3. Marquer un message comme lu

```bash
curl -X PUT http://localhost:8080/api/messages/1/lire \
  -H "Authorization: Bearer {votre_token}"
```

## 📊 Scénario complet : Suivi d'une grossesse

Voici un scénario complet du début à la fin :

```bash
# 1. Inscription de la patiente
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Coulibaly",
    "prenom": "Mariam",
    "telephone": "+22376543210",
    "motDePasse": "secure123",
    "role": "PATIENTE",
    "langue": "fr"
  }')

# Extraire le token
TOKEN=$(echo $RESPONSE | jq -r '.data.token')
PATIENTE_ID=$(echo $RESPONSE | jq -r '.data.id')

echo "Token: $TOKEN"
echo "Patiente ID: $PATIENTE_ID"

# 2. Créer une grossesse
curl -X POST http://localhost:8080/api/grossesses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"dateDebut\": \"2025-03-01\",
    \"datePrevueAccouchement\": \"2025-12-06\",
    \"patienteId\": $PATIENTE_ID
  }"

# 3. Créer les 4 CPN
curl -X POST http://localhost:8080/api/consultations-prenatales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "datePrevue": "2025-04-01",
    "notes": "CPN 1",
    "grossesseId": 1
  }'

# 4. À la naissance : créer l'\''enfant
curl -X POST http://localhost:8080/api/enfants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"nom\": \"Coulibaly\",
    \"prenom\": \"Ibrahim\",
    \"dateDeNaissance\": \"2025-12-05\",
    \"sexe\": \"MASCULIN\",
    \"patienteId\": $PATIENTE_ID
  }"

# 5. Terminer la grossesse
curl -X PUT http://localhost:8080/api/grossesses/1/terminer \
  -H "Authorization: Bearer $TOKEN"

# 6. Créer les consultations postnatales
curl -X POST http://localhost:8080/api/consultations-postnatales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"type\": \"J+3\",
    \"datePrevue\": \"2025-12-08\",
    \"patienteId\": $PATIENTE_ID,
    \"enfantId\": 1
  }"

# 7. Créer le calendrier vaccinal
curl -X POST http://localhost:8080/api/vaccinations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "nomVaccin": "BCG",
    "datePrevue": "2025-12-05",
    "enfantId": 1
  }'
```

## 🔍 Tester avec Postman

1. Importez la collection Postman (à venir)
2. Configurez la variable `{{baseUrl}}` = `http://localhost:8080`
3. Après connexion, configurez `{{token}}` avec le token JWT reçu
4. Tous les endpoints protégés utiliseront automatiquement ce token

## 📝 Notes

- Remplacez `{votre_token}` par le token JWT obtenu lors de la connexion
- Les dates doivent être au format `yyyy-MM-dd` (ISO 8601)
- Les numéros de téléphone doivent être au format international (+223...)
- Pour les professionnels de santé, utilisez le rôle `MEDECIN`
- Pour les administrateurs, utilisez le rôle `ADMINISTRATEUR`

## 🐛 Debugging

Pour voir les logs détaillés :

```bash
# Activer les logs SQL
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Activer les logs de sécurité
logging.level.org.springframework.security=DEBUG
```

## 📧 Support

Pour toute question, contactez : contact@keneyamuso.ml

