# 🔧 Solution : Timeout de connexion depuis l'émulateur Android

## 🔍 Problème identifié

L'erreur montre :
- **Connection Timeout** après 30 secondes
- Backend en écoute sur `0.0.0.0:8080` mais ne **répond pas** aux requêtes
- L'émulateur Android utilise correctement `http://10.0.2.2:8080/api`

## ✅ Solutions à essayer (dans l'ordre)

### Solution 1 : Redémarrer le backend proprement

Le backend semble démarré (port en écoute) mais ne répond pas. Il peut être bloqué ou en cours de démarrage.

**Étapes :**

1. **Arrêtez le backend** :
   - Dans le terminal où le backend tourne, appuyez sur `Ctrl+C`
   - Ou fermez la fenêtre du terminal
   - Attendez 5 secondes

2. **Vérifiez qu'il n'y a plus de processus Java Spring Boot** :
   ```powershell
   Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force
   ```

3. **Vérifiez que MySQL est démarré** :
   ```powershell
   # Vérifier si MySQL tourne
   Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue
   ```
   
   Si MySQL n'est pas démarré, démarrez-le ou utilisez H2 en mode développement.

4. **Redemarrez le backend** :
   ```powershell
   cd KeneyaMusoBackend
   .\start-backend.bat
   ```

5. **Attendez que vous voyiez** :
   ```
   Started KeneyaMusoApplication in X.XXX seconds
   ```

### Solution 2 : Utiliser le mode développement (H2) - Plus rapide

Si MySQL pose problème, utilisez H2 (base de données en mémoire) :

```powershell
cd KeneyaMusoBackend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Cela évite les problèmes de connexion MySQL.

### Solution 3 : Vérifier la configuration MySQL

Si vous utilisez MySQL, vérifiez dans `application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/keneyamuso_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
```

**Testez la connexion MySQL :**
```powershell
mysql -u root -e "SHOW DATABASES;"
```

Si ça ne fonctionne pas, utilisez H2 (Solution 2).

### Solution 4 : Augmenter les timeouts Flutter (temporaire)

Si le backend démarre lentement, les timeouts de 60s peuvent ne pas suffire. Mais d'abord, résolvez le problème du backend qui ne répond pas.

### Solution 5 : Vérifier les logs du backend

Quand vous démarrez le backend, vérifiez les logs pour voir s'il y a des erreurs :
- Erreurs de connexion MySQL ?
- Erreurs de démarrage Spring Boot ?
- Warnings importants ?

## 🎯 Checklist de vérification

Avant de tester Flutter, vérifiez :

- [ ] Backend redémarré proprement
- [ ] Message "Started KeneyaMusoApplication" visible dans les logs
- [ ] MySQL démarré (ou utilisation de H2)
- [ ] Test `http://localhost:8080/swagger-ui.html` fonctionne dans le navigateur
- [ ] Émulateur Android connecté (`adb devices` montre `device`)
- [ ] `ApiConfig.baseUrl` dans Flutter pointe vers `http://10.0.2.2:8080/api` pour Android

## 🚀 Commande rapide : Redémarrage complet

```powershell
# 1. Arrêter tous les processus Java
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force

# 2. Attendre 3 secondes
Start-Sleep -Seconds 3

# 3. Démarrer le backend avec H2 (plus rapide)
cd KeneyaMusoBackend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 4. Dans un autre terminal, vérifier que ça fonctionne
Start-Sleep -Seconds 15
Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui.html" -UseBasicParsing | Select-Object StatusCode
```

## 📝 Note importante

Le fait que le port 8080 soit en écoute **ne garantit pas** que Spring Boot est prêt à répondre. Spring Boot peut être :
- En train de démarrer (peut prendre 30-60 secondes)
- Bloqué par une erreur (vérifiez les logs)
- En attente d'une connexion MySQL

**Attendez toujours de voir "Started KeneyaMusoApplication" avant de tester !**

