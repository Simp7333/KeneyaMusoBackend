# Démarrage du Backend Sans Maven

## ❌ Problème
Maven n'est pas installé ou n'est pas dans le PATH Windows.

## ✅ Solutions

### Option 1 : IntelliJ IDEA (RECOMMANDÉ)

1. **Téléchargez IntelliJ IDEA Community** (gratuit) :
   - https://www.jetbrains.com/idea/download/

2. **Ouvrez le projet** :
   - File → Open → Sélectionnez le dossier `KeneyaMusoBackend`
   - IntelliJ détectera automatiquement Maven et téléchargera les dépendances

3. **Lancez l'application** :
   - Trouvez `src/main/java/com/keneyamuso/KeneyaMusoApplication.java`
   - Clic droit → Run 'KeneyaMusoApplication'
   - OU cliquez sur le bouton ▶️ vert à gauche de `public static void main`

### Option 2 : VS Code avec Extension Java

1. **Installez VS Code** : https://code.visualstudio.com/

2. **Installez l'extension** : "Extension Pack for Java" de Microsoft

3. **Ouvrez le dossier** `KeneyaMusoBackend`

4. **Lancez** : Clic droit sur `KeneyaMusoApplication.java` → Run

### Option 3 : Installer Maven

1. **Téléchargez Maven** :
   - https://maven.apache.org/download.cgi
   - Prenez `apache-maven-3.9.x-bin.zip`

2. **Installez** :
   - Extrayez dans `C:\Program Files\Maven`
   - Ajoutez `C:\Program Files\Maven\bin` au PATH
   - Redémarrez PowerShell

3. **Vérifiez** :
   ```powershell
   mvn -version
   ```

4. **Démarrez** :
   ```powershell
   cd KeneyaMusoBackend
   .\start-backend.bat
   ```

## 📝 Vérifier que le Backend Fonctionne

Une fois démarré, vous devriez voir dans les logs :
```
Started KeneyaMusoApplication in X.XXX seconds
```

Testez avec :
```powershell
curl http://localhost:8080/api/auth/health
```

## 🐛 Les Corrections Appliquées

Le **StackOverflowError** a été corrigé dans les fichiers suivants :
- ✅ `model/entity/Patiente.java` - Annotations Jackson
- ✅ `model/entity/Enfant.java` - @JsonBackReference
- ✅ `model/entity/Grossesse.java` - @JsonBackReference
- ✅ `repository/PatienteRepository.java` - JOIN FETCH
- ✅ `service/DashboardService.java` - Logique optimisée

Une fois le backend redémarré, l'onglet **Postnatale** dans votre app Flutter devrait fonctionner sans erreur 500.

