# Guide de démarrage rapide - KènèyaMuso Backend

Ce guide vous permet de lancer rapidement l'application en 5 minutes.

## 🚀 Option 1 : Démarrage avec H2 (le plus rapide)

### Étape 1 : Prérequis
- Java 17+ installé
- Maven installé

### Étape 2 : Lancer l'application

```bash
# Cloner le repository
git clone https://github.com/votre-org/keneyamuso-backend.git
cd keneyamuso-backend

# Lancer avec le profil dev (H2)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

✅ **C'est tout !** L'application démarre sur http://localhost:8080

### Étape 3 : Tester

Ouvrez votre navigateur : http://localhost:8080/swagger-ui.html

## 🐳 Option 2 : Démarrage avec Docker

### Étape 1 : Prérequis
- Docker et Docker Compose installés

### Étape 2 : Lancer

```bash
# Lancer MySQL et l'application
docker-compose up -d

# Voir les logs
docker-compose logs -f app
```

✅ L'application démarre sur http://localhost:8080

## 🗄️ Option 3 : Démarrage avec MySQL local

### Étape 1 : Installer MySQL

```bash
# Sur Ubuntu/Debian
sudo apt-get install mysql-server

# Sur macOS
brew install mysql

# Sur Windows
# Télécharger depuis https://dev.mysql.com/downloads/installer/
```

### Étape 2 : Créer la base de données

```bash
mysql -u root -p
```

```sql
CREATE DATABASE keneyamuso_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'keneyamuso'@'localhost' IDENTIFIED BY 'keneyamuso123';
GRANT ALL PRIVILEGES ON keneyamuso_db.* TO 'keneyamuso'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### Étape 3 : Configurer l'application

Modifiez `src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/keneyamuso_db
spring.datasource.username=keneyamuso
spring.datasource.password=keneyamuso123
```

### Étape 4 : Lancer

```bash
mvn spring-boot:run
```

## 🧪 Premiers tests

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

**Réponse attendue :**
```json
{
  "success": true,
  "message": "Inscription réussie",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 1,
    "nom": "Traoré",
    "prenom": "Aminata",
    "telephone": "+22370123456",
    "role": "PATIENTE"
  }
}
```

### 2. Connexion

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "telephone": "+22370123456",
    "motDePasse": "password123"
  }'
```

### 3. Créer une grossesse

⚠️ **Important** : Remplacez `{TOKEN}` par le token reçu lors de l'inscription/connexion

```bash
curl -X POST http://localhost:8080/api/grossesses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "dateDebut": "2025-03-01",
    "datePrevueAccouchement": "2025-12-06",
    "patienteId": 1
  }'
```

### 4. Consulter les grossesses

```bash
curl -X GET http://localhost:8080/api/grossesses/patiente/1 \
  -H "Authorization: Bearer {TOKEN}"
```

## 📚 Ressources utiles

Une fois l'application lancée :

- **Swagger UI** : http://localhost:8080/swagger-ui.html
  - Documentation interactive de l'API
  - Possibilité de tester directement les endpoints

- **Console H2** (si mode dev) : http://localhost:8080/h2-console
  - JDBC URL : `jdbc:h2:mem:keneyamuso_dev`
  - Username : `sa`
  - Password : (laisser vide)

## 🐛 Problèmes courants

### Port 8080 déjà utilisé

```bash
# Linux/Mac - Trouver le processus
lsof -i :8080

# Windows
netstat -ano | findstr :8080

# Ou changer le port dans application.properties
server.port=8081
```

### Erreur de connexion MySQL

Vérifiez que MySQL est lancé :

```bash
# Linux
sudo service mysql status

# macOS
brew services list

# Windows
# Gestionnaire de services → MySQL
```

### Java version incorrecte

```bash
# Vérifier la version
java -version

# Doit afficher Java 17 ou supérieur
```

### Maven introuvable

```bash
# Linux
sudo apt-get install maven

# macOS
brew install maven

# Windows
# Télécharger depuis https://maven.apache.org/download.cgi
```

## 📖 Suite

Une fois l'application lancée avec succès :

1. ✅ Consultez le [README.md](README.md) pour la documentation complète
2. ✅ Testez avec [API_EXAMPLES.md](API_EXAMPLES.md) pour plus d'exemples
3. ✅ Lisez [ARCHITECTURE.md](ARCHITECTURE.md) pour comprendre le code
4. ✅ Consultez [CONTRIBUTING.md](CONTRIBUTING.md) si vous voulez contribuer

## 🎯 Scénario de test complet

Voici un script bash pour tester rapidement toutes les fonctionnalités :

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

echo "🔐 1. Inscription..."
RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Test",
    "prenom": "User",
    "telephone": "+22376543210",
    "motDePasse": "test123",
    "role": "PATIENTE",
    "langue": "fr"
  }')

TOKEN=$(echo $RESPONSE | jq -r '.data.token')
PATIENTE_ID=$(echo $RESPONSE | jq -r '.data.id')

echo "✅ Token: ${TOKEN:0:20}..."
echo "✅ Patiente ID: $PATIENTE_ID"

echo ""
echo "🤰 2. Création d'une grossesse..."
curl -s -X POST $BASE_URL/api/grossesses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"dateDebut\": \"2025-03-01\",
    \"datePrevueAccouchement\": \"2025-12-06\",
    \"patienteId\": $PATIENTE_ID
  }" | jq '.message'

echo ""
echo "📋 3. Création d'une CPN..."
curl -s -X POST $BASE_URL/api/consultations-prenatales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "datePrevue": "2025-04-01",
    "notes": "Première consultation",
    "grossesseId": 1
  }' | jq '.message'

echo ""
echo "👶 4. Création d'un enfant..."
curl -s -X POST $BASE_URL/api/enfants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"nom\": \"Test\",
    \"prenom\": \"Baby\",
    \"dateDeNaissance\": \"2025-12-05\",
    \"sexe\": \"MASCULIN\",
    \"patienteId\": $PATIENTE_ID
  }" | jq '.message'

echo ""
echo "💉 5. Création d'une vaccination..."
curl -s -X POST $BASE_URL/api/vaccinations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "nomVaccin": "BCG",
    "datePrevue": "2025-12-05",
    "enfantId": 1
  }' | jq '.message'

echo ""
echo "✅ Test complet terminé !"
echo "📖 Consultez Swagger UI : $BASE_URL/swagger-ui.html"
```

Sauvegardez ce script dans `test-api.sh`, rendez-le exécutable et lancez-le :

```bash
chmod +x test-api.sh
./test-api.sh
```

## ✨ Prêt à développer !

Vous avez maintenant :
- ✅ Une application fonctionnelle
- ✅ Une base de données configurée
- ✅ Des données de test
- ✅ La documentation Swagger accessible

**Bon développement !** 🚀

Pour toute question : contact@keneyamuso.ml

