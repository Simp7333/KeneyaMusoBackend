# 📚 Index de la Documentation - KènèyaMuso

Bienvenue ! Voici **tous les documents** disponibles organisés par catégorie.

---

## 🚀 Démarrage Rapide

| Document | Description | Quand l'utiliser ? |
|----------|-------------|-------------------|
| **[README.md](README.md)** | 📖 Documentation principale | Toujours commencer ici ! |
| **[QUICKSTART.md](QUICKSTART.md)** | ⚡ Guide de démarrage rapide | Installation en 5 minutes |
| **[QUICKTEST.md](QUICKTEST.md)** | 🧪 Test rapide des endpoints | Tester l'API en 3 étapes |

---

## 🧪 Tests & Validation

| Document | Description | Quand l'utiliser ? |
|----------|-------------|-------------------|
| **[TEST_ENDPOINTS.md](TEST_ENDPOINTS.md)** | 🔍 Guide complet de test de tous les endpoints | Tester manuellement chaque endpoint |
| **[CHECKLIST.md](CHECKLIST.md)** | ✅ Checklist de développement | Vérifier que tout fonctionne |
| `test-all-endpoints.ps1` | 🤖 Script PowerShell automatique | Tester automatiquement tous les endpoints |
| `test-endpoints.bat` | 🖱️ Lanceur Windows simple | Double-clic pour lancer les tests |
| `test-websocket.html` | 🔌 Test WebSocket interactif | Tester le chat en temps réel |

---

## 📖 Guides d'Utilisation

| Document | Description | Quand l'utiliser ? |
|----------|-------------|-------------------|
| **[API_EXAMPLES.md](API_EXAMPLES.md)** | 💡 Exemples d'utilisation de l'API | Apprendre à utiliser chaque endpoint |
| **[WORKFLOW.md](WORKFLOW.md)** | 🔄 Workflow complet de l'application | Comprendre le fonctionnement global |
| **[MESSAGERIE_GUIDE.md](MESSAGERIE_GUIDE.md)** | 💬 Guide du système de chat | Implémenter la messagerie |
| **[WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md)** | 🔌 Guide complet WebSocket | Intégrer le chat en temps réel |
| **[WEBSOCKET_RESUME.md](WEBSOCKET_RESUME.md)** | 📝 Résumé technique WebSocket | Référence rapide WebSocket |
| **[README_WEBSOCKET.md](README_WEBSOCKET.md)** | ⚡ Guide rapide WebSocket | Démarrage rapide WebSocket |

---

## 🏗️ Architecture & Design

| Document | Description | Quand l'utiliser ? |
|----------|-------------|-------------------|
| **[ARCHITECTURE.md](ARCHITECTURE.md)** | 🏛️ Architecture détaillée du projet | Comprendre la structure du code |
| **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** | 📋 Résumé du projet complet | Vue d'ensemble technique |

---

## 🔄 Migration & Changements

| Document | Description | Quand l'utiliser ? |
|----------|-------------|-------------------|
| **[MIGRATION_ROLES.md](MIGRATION_ROLES.md)** | 🔄 Migration des rôles utilisateurs | Migrer les anciennes données (SageFemme → Médecin) |
| **[WORKFLOW_MIGRATION.md](WORKFLOW_MIGRATION.md)** | 🔄 Migration vers le nouveau workflow | Adapter les données au workflow automatisé |
| **[CHANGELOG_WORKFLOW.md](CHANGELOG_WORKFLOW.md)** | 📝 Changelog du workflow | Voir l'historique des changements |

---

## 💻 Intégration Frontend

| Document | Description | Quand l'utiliser ? |
|----------|-------------|-------------------|
| **[FRONTEND_INTEGRATION.md](FRONTEND_INTEGRATION.md)** | 🎨 Guide d'intégration frontend | Développer l'interface React/TypeScript |
| **[WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md)** | 🔌 Intégration WebSocket frontend | Implémenter le chat côté client |

---

## 🤝 Contribution & Licence

| Document | Description | Quand l'utiliser ? |
|----------|-------------|-------------------|
| **[CONTRIBUTING.md](CONTRIBUTING.md)** | 🤝 Guide de contribution | Contribuer au projet |
| **[LICENSE](LICENSE)** | ⚖️ Licence du projet | Vérifier les droits d'utilisation |

---

## 🛠️ Déploiement & Configuration

| Fichier | Description | Quand l'utiliser ? |
|---------|-------------|-------------------|
| `Dockerfile` | 🐳 Configuration Docker | Déployer en conteneur |
| `docker-compose.yml` | 🐳 Orchestration Docker | Déployer l'app complète (backend + MySQL) |
| `setup.sh` | 🐧 Script de démarrage Linux/Mac | Installation automatique (Linux/Mac) |
| `setup.bat` | 🪟 Script de démarrage Windows | Installation automatique (Windows) |
| `.env.example` | 🔐 Variables d'environnement | Configuration production |

---

## 📊 Récapitulatif par Cas d'Usage

### 🎯 "Je débute avec le projet"

1. Lire **[README.md](README.md)**
2. Suivre **[QUICKSTART.md](QUICKSTART.md)**
3. Tester avec **[QUICKTEST.md](QUICKTEST.md)** ou `test-endpoints.bat`

---

### 🔧 "Je veux développer l'API"

1. Comprendre **[ARCHITECTURE.md](ARCHITECTURE.md)**
2. Consulter **[WORKFLOW.md](WORKFLOW.md)**
3. Utiliser **[API_EXAMPLES.md](API_EXAMPLES.md)** comme référence
4. Vérifier avec **[CHECKLIST.md](CHECKLIST.md)**

---

### 🎨 "Je développe le frontend"

1. Lire **[FRONTEND_INTEGRATION.md](FRONTEND_INTEGRATION.md)**
2. Intégrer le chat avec **[WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md)**
3. Tester avec **[TEST_ENDPOINTS.md](TEST_ENDPOINTS.md)**
4. Utiliser `test-websocket.html` pour le chat

---

### 🧪 "Je veux tester l'API"

1. **Test automatique** : Lancer `test-endpoints.bat`
2. **Test manuel** : Suivre **[TEST_ENDPOINTS.md](TEST_ENDPOINTS.md)**
3. **Test WebSocket** : Ouvrir `test-websocket.html`
4. **Swagger UI** : http://localhost:8080/swagger-ui/index.html

---

### 🔄 "Je dois migrer des données existantes"

1. Migration des rôles : **[MIGRATION_ROLES.md](MIGRATION_ROLES.md)**
2. Migration du workflow : **[WORKFLOW_MIGRATION.md](WORKFLOW_MIGRATION.md)**
3. Voir les changements : **[CHANGELOG_WORKFLOW.md](CHANGELOG_WORKFLOW.md)**

---

### 🚀 "Je déploie en production"

1. Configurer **`.env`** (copier `.env.example`)
2. Utiliser **`docker-compose.yml`**
3. Lire **[README.md](README.md)** section "Déploiement"

---

### 💬 "Je veux implémenter le chat"

1. Comprendre **[MESSAGERIE_GUIDE.md](MESSAGERIE_GUIDE.md)**
2. Implémenter **[WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md)**
3. Référence rapide : **[README_WEBSOCKET.md](README_WEBSOCKET.md)**
4. Tester avec `test-websocket.html`

---

## 📝 Conventions de Documentation

- **README.md** : Vue d'ensemble et démarrage
- **GUIDE.md** : Guides détaillés pour une fonctionnalité
- **MIGRATION.md** : Instructions de migration
- **CHANGELOG.md** : Historique des modifications
- **EXAMPLES.md** : Exemples concrets d'utilisation
- **.sh / .bat / .ps1** : Scripts d'automatisation

---

## 🆘 Besoin d'Aide ?

| Problème | Solution |
|----------|----------|
| ❓ Problème d'installation | → [QUICKSTART.md](QUICKSTART.md) |
| ❓ Erreur de test | → [TEST_ENDPOINTS.md](TEST_ENDPOINTS.md) |
| ❓ Problème WebSocket | → [WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md) |
| ❓ Question architecture | → [ARCHITECTURE.md](ARCHITECTURE.md) |
| ❓ Problème de migration | → [MIGRATION_ROLES.md](MIGRATION_ROLES.md) ou [WORKFLOW_MIGRATION.md](WORKFLOW_MIGRATION.md) |

---

## 📬 Contact & Contribution

- **Contribuer** : Voir [CONTRIBUTING.md](CONTRIBUTING.md)
- **Licence** : [LICENSE](LICENSE)

---

**🎉 Bonne lecture et bon développement !**

