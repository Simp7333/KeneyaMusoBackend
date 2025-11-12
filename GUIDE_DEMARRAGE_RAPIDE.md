# 🚀 Guide de démarrage rapide - Résolution du problème de connexion

## ⚡ Solution rapide (3 étapes)

### Étape 1 : Démarrer le backend

**Double-cliquez sur :**
```
KeneyaMusoBackend\start-backend.bat
```

Ou executez dans PowerShell :
```powershell
cd KeneyaMusoBackend
.\start-backend.bat
```

**Attendez que vous voyiez :**
```
Started KeneyaMusoApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http) with context path ''
```

### Étape 2 : Vérifier que le backend fonctionne

**Dans un nouveau terminal, double-cliquez sur :**
```
KeneyaMusoBackend\check-backend.bat
```

**Ou exécutez :**
```powershell
cd KeneyaMusoBackend
.\check-backend.bat
```

Ce script va :
- ✅ Vérifier que le port 8080 est en écoute
- ✅ Vérifier que le serveur écoute sur `0.0.0.0` (accessible depuis l'extérieur)
- ✅ Tester l'accès local et via IP
- ✅ Vous donner l'URL correcte pour Flutter

### Étape 3 : Vérifier l'IP dans Flutter

1. **Notez l'IP affichée par `check-backend.bat`**
2. **Ouvrez** `Keneya_muso/lib/services/api_config.dart`
3. **Verifiez que l'IP correspond :**
   ```dart
   static const String baseUrl = 'http://VOTRE_IP:8080/api';
   ```
4. **Si l'IP est différente, mettez-la à jour**

## 🔍 Tests manuels

### Test 1 : Vérifier que le backend répond

**Ouvrez votre navigateur et allez à :**
```
http://localhost:8080/swagger-ui.html
```

Si cette page s'affiche, le backend fonctionne ! ✅

### Test 2 : Tester l'API directement (Flutter Web)

**Ouvrez la console du navigateur (F12) dans votre application Flutter Web et exécutez :**

```javascript
fetch('http://192.168.43.183:8080/api/auth/register', {
  method: 'POST',
  headers: { 
    'Content-Type': 'application/json',
    'Origin': window.location.origin
  },
  body: JSON.stringify({
    nom: 'Test',
    prenom: 'User',
    telephone: '999999999',
    motDePasse: 'test123',
    role: 'PATIENTE',
    dateDeNaissance: '1990-01-01T00:00:00.000'
  })
})
.then(r => r.json().then(d => ({status: r.status, data: d})))
.then(result => console.log('Success:', result))
.catch(e => console.error('Error:', e));
```

**Interprétation :**
- ✅ Si vous voyez `Success` → Le backend est accessible, problème côté Flutter
- ❌ Si vous voyez une erreur CORS → Vérifiez `SecurityConfig.java`
- ❌ Si vous voyez `Failed to fetch` → Problème réseau/IP

## 🐛 Dépannage

### Problème : "Le port 8080 n'est pas en écoute"

**Solution :**
1. Vérifiez que le backend est bien démarré (`start-backend.bat`)
2. Attendez quelques secondes (le démarrage peut prendre 30-60 secondes)
3. Vérifiez qu'il n'y a pas d'erreur dans la console du backend

### Problème : "Le serveur écoute seulement sur 127.0.0.1"

**Solution :**
1. Vérifiez `KeneyaMusoBackend\src\main\resources\application.properties`
2. Assurez-vous que cette ligne existe :
   ```properties
   server.address=0.0.0.0
   ```
3. Redémarrez le backend

### Problème : "Impossible d'accéder via l'IP"

**Causes possibles :**
1. **Mauvaise IP** → Vérifiez avec `ipconfig` et mettez à jour `api_config.dart`
2. **Pare-feu bloque** → Créez une règle pour le port 8080 (voir ci-dessous)
3. **Pas sur le même réseau** → Assurez-vous d'être sur le même WiFi

### Problème : Erreurs CORS dans la console du navigateur

**Solution :**
1. Vérifiez `KeneyaMusoBackend\src\main\java\com\keneyamuso\config\SecurityConfig.java`
2. Assurez-vous que cette ligne existe :
   ```java
   configuration.setAllowedOrigins(List.of("*"));
   ```
3. Redémarrez le backend

## 🔥 Autoriser le port 8080 dans le pare-feu Windows

1. **Ouvrir le Pare-feu Windows Defender**
2. **Paramètres avancés**
3. **Règles de trafic entrant** → **Nouvelle règle...**
4. **Sélectionner "Port"**
5. **TCP** → **Ports spécifiques locaux** → `8080`
6. **Autoriser la connexion**
7. **Sélectionner tous les profils**
8. **Nommer** : "Spring Boot 8080"

## 📋 Checklist de vérification

Avant de tester Flutter, vérifiez :

- [ ] Backend démarré avec `start-backend.bat`
- [ ] `check-backend.bat` montre tous les tests en ✅
- [ ] Swagger UI accessible : http://localhost:8080/swagger-ui.html
- [ ] IP dans `api_config.dart` correspond à votre IP actuelle
- [ ] Si Flutter Web : Pas d'erreurs CORS dans la console du navigateur (F12)
- [ ] Pare-feu Windows autorise le port 8080

## 🎯 Commandes utiles

### Vérifier l'IP actuelle
```powershell
ipconfig | Select-String "IPv4"
```

### Vérifier que le port est en écoute
```powershell
netstat -an | findstr ":8080"
```

### Tester l'API avec PowerShell
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api-docs" -UseBasicParsing
```

## 💡 Astuce pour Flutter Web

Si vous développez avec Flutter Web et rencontrez toujours des problèmes :

1. **Ouvrez la console du navigateur (F12)**
2. **Onglet "Network"** → Observez les requêtes
3. **Onglet "Console"** → Lisez les erreurs détaillées
4. **Testez l'URL directement** dans la barre d'adresse du navigateur

## ✅ Après avoir suivi ces étapes

Si le backend répond mais Flutter ne peut toujours pas se connecter :

1. **Vérifiez les logs Flutter** dans la console
2. **Vérifiez la console du navigateur** (F12) si Flutter Web
3. **Relancez l'application Flutter** après avoir démarré le backend

Le problème devrait être résolu ! 🎉

