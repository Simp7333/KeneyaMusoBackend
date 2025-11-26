# 🐛 BUGFIX: Colonne 'payload' trop petite

## Problème

Lors de la soumission d'un formulaire CPN (Consultation Prénatale), l'erreur suivante se produit :

```
Data truncation: Data too long for column 'payload' at row 1
```

### Cause

La colonne `payload` dans la table `dossier_medical_submissions` était définie comme `TEXT` (limite de ~65 KB), ce qui n'est pas suffisant pour stocker les gros formulaires JSON avec toutes les données.

### Données du formulaire qui causent le problème

```json
{
  "type": "CPN",
  "data": {
    "taille": null,
    "poids": 45.0,
    "dernierControle": null,
    "dateDernieresRegles": "2025-03-05T15:00:46.792862",
    "nombreMoisGrossesse": 9,
    "groupeSanguin": "O_POSITIF",
    "complications": true,
    "complicationsDetails": null,
    "mouvementsBebeReguliers": true,
    "symptomes": ["nausées fortes", "saignements", "maux de tête"],
    "symptomesAutre": null,
    "prendMedicamentsOuVitamines": true,
    "medicamentsOuVitaminesDetails": null,
    "aEuMaladies": false,
    "maladiesDetails": null
  }
}
```

---

## Solution

### 1. Modification de l'entité Java ✅

**Fichier**: `src/main/java/com/keneyamuso/model/entity/DossierMedicalSubmission.java`

**Avant**:
```java
@Lob
@Column(nullable = false)
private String payload;
```

**Après**:
```java
@Lob
@Column(nullable = false, columnDefinition = "LONGTEXT")
private String payload;
```

### 2. Migration de la base de données 🔧

#### Option A: Exécution manuelle du script SQL (RECOMMANDÉ)

1. **Se connecter à la base de données** :
   ```bash
   mysql -u root -p
   ```
   
   Ou utiliser un outil comme MySQL Workbench, DBeaver, ou phpMyAdmin

2. **Sélectionner la base de données** :
   ```sql
   USE keneya_muso;  -- ou votre nom de base de données
   ```

3. **Exécuter le script de correction** :
   ```sql
   ALTER TABLE dossier_medical_submissions 
   MODIFY COLUMN payload LONGTEXT NOT NULL;
   ```

4. **Vérifier la modification** :
   ```sql
   DESCRIBE dossier_medical_submissions;
   ```
   
   Vous devriez voir :
   ```
   +-----------------------+--------------+------+-----+---------+----------------+
   | Field                 | Type         | Null | Key | Default | Extra          |
   +-----------------------+--------------+------+-----+---------+----------------+
   | ...                   | ...          | ...  | ... | ...     | ...            |
   | payload               | longtext     | NO   |     | NULL    |                |
   | ...                   | ...          | ...  | ... | ...     | ...            |
   +-----------------------+--------------+------+-----+---------+----------------+
   ```

#### Option B: Utilisation de Flyway (si configuré)

Si Flyway est configuré dans votre projet, le fichier de migration a déjà été créé :
- `src/main/resources/db/migration/V2__alter_payload_column.sql`

Il sera exécuté automatiquement au prochain démarrage de l'application.

#### Option C: Script SQL autonome

Un script SQL complet a été créé dans :
- `KeneyaMusoBackend/fix_payload_column.sql`

Exécutez-le via :
```bash
mysql -u root -p keneya_muso < fix_payload_column.sql
```

---

## Comparaison des types de colonnes texte MySQL

| Type       | Taille maximale | Bytes    | Utilisation                    |
|------------|-----------------|----------|--------------------------------|
| TINYTEXT   | 255 caractères  | 255 B    | Très petits textes             |
| TEXT       | 65,535 car.     | ~64 KB   | ⚠️ **ANCIEN** (trop petit)    |
| MEDIUMTEXT | 16,777,215 car. | ~16 MB   | Textes moyens à grands         |
| LONGTEXT   | 4,294,967,295   | ~4 GB    | ✅ **NOUVEAU** (recommandé)   |

---

## Test de validation

Après avoir appliqué la correction :

1. **Redémarrer le backend** :
   ```bash
   cd KeneyaMusoBackend
   mvn spring-boot:run
   ```

2. **Tester la soumission du formulaire** depuis l'application Flutter

3. **Vérifier dans les logs** qu'il n'y a plus d'erreur "Data truncation"

4. **Vérifier en base de données** que les données sont bien enregistrées :
   ```sql
   SELECT id, type, status, LENGTH(payload) as payload_size 
   FROM dossier_medical_submissions 
   ORDER BY id DESC 
   LIMIT 5;
   ```

---

## Prévention future

### Recommandations

1. **Utiliser LONGTEXT par défaut** pour tous les champs JSON/payload
2. **Tester avec des données réalistes** avant le déploiement
3. **Monitorer la taille des payloads** en production
4. **Documenter les limites de taille** dans le code

### Monitoring

Ajouter des logs pour surveiller la taille des payloads :

```java
@Service
public class DossierSubmissionService {
    public void submitFormulaire(DossierSubmissionRequest request) {
        String payload = objectMapper.writeValueAsString(request);
        int payloadSize = payload.length();
        
        logger.info("Soumission formulaire - Taille payload: {} caractères (~{} KB)", 
                    payloadSize, payloadSize / 1024);
        
        if (payloadSize > 50000) {  // Plus de 50KB
            logger.warn("⚠️ Payload volumineux détecté: {} KB", payloadSize / 1024);
        }
        
        // ... suite du code
    }
}
```

---

## Checklist

- [x] Modifier l'entité `DossierMedicalSubmission.java`
- [x] Créer le script SQL de migration
- [x] Créer le fichier Flyway (si applicable)
- [ ] **EXÉCUTER LE SCRIPT SQL SUR LA BASE DE DONNÉES** ⚠️ **ÉTAPE CRITIQUE**
- [ ] Redémarrer le backend
- [ ] Tester la soumission du formulaire
- [ ] Vérifier les données en base

---

## Impact

- **Avant** : Formulaires ne peuvent pas être soumis si le JSON dépasse ~65KB
- **Après** : Formulaires peuvent contenir jusqu'à ~4GB de données (largement suffisant)

---

## Date de correction

**12 Novembre 2025**

## Priorité

🔴 **CRITIQUE** - Bloque la soumission des formulaires CPN/CPON

## Status

✅ **Code corrigé** - ⚠️ **En attente d'exécution du script SQL**

