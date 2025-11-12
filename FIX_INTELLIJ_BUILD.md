# 🔧 Résolution du Problème de Build IntelliJ

## ❌ Erreur Actuelle
```
Abnormal build process termination
Unable to make field private static java.util.IdentityHashMap 
java.lang.ApplicationShutdownHooks.hooks accessible
```

## ✅ Solutions (Testez dans l'ordre)

### Solution 1 : Recharger Maven (Le Plus Simple)

1. **Ouvrez la fenêtre Maven** :
   - Cliquez sur l'onglet `Maven` sur le côté droit d'IntelliJ
   - OU : `View` → `Tool Windows` → `Maven`

2. **Rechargez le projet** :
   - Cliquez sur l'icône 🔄 "Reload All Maven Projects" (en haut de la fenêtre Maven)
   - OU : Clic droit sur `KeneyaMusoBackend` → `Maven` → `Reload project`

3. **Attendez** que toutes les dépendances se téléchargent (barre de progression en bas)

4. **Relancez l'application** :
   - Ouvrez `src/main/java/com/keneyamuso/KeneyaMusoApplication.java`
   - Clic droit → `Run 'KeneyaMusoApplication'`

---

### Solution 2 : Invalider les Caches

Si la solution 1 ne fonctionne pas :

1. **Menu** : `File` → `Invalidate Caches...`

2. **Cochez** :
   - ✅ Clear file system cache and Local History
   - ✅ Clear VCS Log caches and indexes
   - ✅ Clear downloaded shared indexes

3. **Cliquez** : `Invalidate and Restart`

4. **Après le redémarrage** : Répétez Solution 1 (Reload Maven)

---

### Solution 3 : Reconfigurer le JDK

1. **Ouvrez les paramètres** : `File` → `Project Structure` (Ctrl+Alt+Shift+S)

2. **Vérifiez le SDK** :
   - `Project` → `SDK` : Doit être **Java 17** (ou 21)
   - `Project language level` : **17 - Sealed types, always-strict floating-point semantics**

3. **Modules** :
   - `Modules` → `KeneyaMusoBackend` → `Dependencies`
   - Vérifiez que `Module SDK` = **<Project SDK>**

4. **Appliquez** : `Apply` → `OK`

---

### Solution 4 : Build Manuel avec Maven

Si IntelliJ a toujours des problèmes, utilisez le terminal intégré :

1. **Ouvrez le terminal** dans IntelliJ : `View` → `Tool Windows` → `Terminal`

2. **Vérifiez Maven** :
   ```bash
   mvn -version
   ```

3. **Si Maven est trouvé**, compilez :
   ```bash
   mvn clean compile
   ```

4. **Si Maven n'est PAS trouvé** :
   - IntelliJ inclut Maven intégré
   - Utilisez : `View` → `Tool Windows` → `Maven`
   - Double-cliquez sur : `KeneyaMusoBackend` → `Lifecycle` → `compile`

---

### Solution 5 : Vérifier pom.xml

Ouvrez `pom.xml` et vérifiez qu'il n'y a pas d'erreurs rouges soulignées.

Si des dépendances sont manquantes :
1. Clic droit sur `pom.xml`
2. `Maven` → `Reimport`

---

## 🚀 Démarrage Après Correction

Une fois le build réussi :

1. **Trouvez** : `src/main/java/com/keneyamuso/KeneyaMusoApplication.java`

2. **Lancez** : Clic droit → `Run 'KeneyaMusoApplication.main()'`

3. **Ou** : Cliquez sur le bouton ▶️ vert à gauche de :
   ```java
   public static void main(String[] args) {
   ```

4. **Vérifiez les logs** :
   ```
   Started KeneyaMusoApplication in X.XXX seconds (JVM running for X.XXX)
   ```

5. **Testez** dans votre navigateur :
   - http://localhost:8080/api/auth/health

---

## 📝 Vérification que Tout Fonctionne

### Backend démarré ✅
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.x.x)

Started KeneyaMusoApplication in 8.123 seconds
```

### Test Flutter
Dans votre app, cliquez sur l'onglet **Postnatale** :
- ❌ Avant : Erreur 500 + StackOverflowError
- ✅ Après : Liste des patientes s'affiche correctement

---

## 💡 Si Rien ne Fonctionne

**Alternative** : Utilisez VS Code au lieu d'IntelliJ

1. Ouvrez VS Code
2. Installez : "Extension Pack for Java"
3. Ouvrez le dossier `KeneyaMusoBackend`
4. Attendez que Maven se synchronise
5. Clic droit sur `KeneyaMusoApplication.java` → `Run`

---

## 🆘 Besoin d'Aide ?

Dites-moi :
1. Quelle solution avez-vous essayée ?
2. Quel message d'erreur voyez-vous maintenant ?
3. Y a-t-il des erreurs rouges dans `pom.xml` ?

