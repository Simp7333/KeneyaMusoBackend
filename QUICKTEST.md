# ⚡ Test Rapide - KènèyaMuso

## 🎯 En 3 étapes

### 1️⃣ Démarrer l'application

```bash
# Depuis PowerShell
.\setup.bat dev
```

**OU** depuis votre IDE (IntelliJ, Eclipse, VS Code)

---

### 2️⃣ Lancer les tests

**Option 1 : Double-clic sur le fichier**
```
test-endpoints.bat
```

**Option 2 : Ligne de commande**
```powershell
.\test-all-endpoints.ps1
```

---

### 3️⃣ Voir les résultats

Le script va automatiquement :

1. ✅ **Inscrire une patiente** (Fatoumata Traoré)
2. ✅ **Créer une grossesse** → Génère **4 CPN automatiquement**
3. ✅ **Mettre à jour une CPN** → Statut passe à `REALISEE`
4. ✅ **Terminer la grossesse** → Génère **3 CPoN automatiquement**
5. ✅ **Enregistrer un enfant** (Ibrahim) → Génère **19 vaccinations automatiquement**
6. ✅ **Confirmer une vaccination** (BCG) → Statut passe à `FAIT`
7. ✅ **Envoyer un message**

---

## 📊 Résultat Attendu

```
======================================
  ✅ Tous les tests terminés !
======================================

📊 Résumé des entités créées:
  - 1 Patiente inscrite
  - 1 Grossesse créée
  - 4 CPN générées automatiquement
  - 3 CPoN générées automatiquement
  - 1 Enfant enregistré
  - 19 Vaccinations générées automatiquement

Total : 29 entités créées automatiquement ! 🎉

💡 Pour voir tous les détails :
   Swagger UI : http://localhost:8080/swagger-ui/index.html
```

---

## 🔍 Vérifier Manuellement

### 1. Swagger UI

Ouvrir : http://localhost:8080/swagger-ui/index.html

**Tester un endpoint** :
1. Cliquer sur "Authorize" 🔒
2. Entrer : `Bearer VOTRE_TOKEN` (copié depuis les résultats du script)
3. Cliquer sur n'importe quel endpoint
4. Cliquer "Try it out"
5. Remplir les paramètres
6. Cliquer "Execute"

---

### 2. Exemples manuels avec curl

#### Voir toutes les CPN de la grossesse

```bash
curl -X GET http://localhost:8080/api/consultations-prenatales/grossesse/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

#### Voir le calendrier vaccinal de l'enfant

```bash
curl -X GET http://localhost:8080/api/vaccinations/enfant/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

#### Voir les CPoN de la patiente

```bash
curl -X GET http://localhost:8080/api/consultations-postnatales/patiente/1 \
  -H "Authorization: Bearer VOTRE_TOKEN"
```

---

## 📝 Tests Personnalisés

Pour créer vos propres données de test, consultez :
- **[TEST_ENDPOINTS.md](TEST_ENDPOINTS.md)** : Guide complet avec tous les endpoints
- **[API_EXAMPLES.md](API_EXAMPLES.md)** : Exemples détaillés d'utilisation

---

## ❌ Dépannage

### Erreur : "Application n'est pas démarrée"

**Solution** :
```bash
# Démarrer l'application
.\setup.bat dev
```

Attendre que vous voyiez :
```
Started KeneyaMusoApplication in X.XXX seconds
```

---

### Erreur : "Le terme mvn n'est pas reconnu"

**Solution** : Utiliser votre IDE au lieu de Maven en ligne de commande
- IntelliJ : Clic droit sur `KeneyaMusoApplication.java` → Run
- Eclipse/STS : Clic droit → Run As → Spring Boot App

---

### Erreur : "Java version mismatch"

**Solution** : Configurer Java 17
- IntelliJ : File → Project Structure → SDK → Java 17
- Eclipse : Properties → Java Build Path → JRE → Java 17

---

## 🎉 C'est tout !

Vous avez maintenant testé **tous les endpoints principaux** de l'API KènèyaMuso !

**Prochaines étapes** :
1. Explorer les endpoints dans Swagger UI
2. Tester le WebSocket pour le chat en temps réel → [WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md)
3. Intégrer avec votre frontend → [FRONTEND_INTEGRATION.md](FRONTEND_INTEGRATION.md)

---

**Besoin d'aide ?** Consultez la [documentation complète](README.md)

