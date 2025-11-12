# Migration des rôles utilisateurs

## Changements effectués

### Ancienne structure
- **PATIENTE** : Femmes enceintes et jeunes mères
- **SAGE_FEMME** : Sages-femmes et matrones ❌ SUPPRIMÉ
- **MEDECIN** : Gynécologues et pédiatres
- **ADMIN** : Administrateurs système ❌ RENOMMÉ

### Nouvelle structure
- **PATIENTE** : Femmes enceintes et jeunes mères ✅
- **MEDECIN** : Gynécologues, pédiatres et médecins généralistes ✅
- **ADMINISTRATEUR** : Administrateurs système ✅ (anciennement ADMIN)

## Changements dans les spécialités

### Anciennes spécialités
- SAGE_FEMME ❌ SUPPRIMÉ
- MATRONE ❌ SUPPRIMÉ
- GYNECOLOGUE ✅
- PEDIATRE ✅

### Nouvelles spécialités
- GYNECOLOGUE ✅
- PEDIATRE ✅
- GENERALISTE ✅ AJOUTÉ

## Impact sur le code

### 1. Enum RoleUtilisateur
```java
// Avant
public enum RoleUtilisateur {
    PATIENTE,
    SAGE_FEMME,
    MEDECIN,
    ADMIN
}

// Après
public enum RoleUtilisateur {
    PATIENTE,
    MEDECIN,
    ADMINISTRATEUR
}
```

### 2. Enum Specialite
```java
// Avant
public enum Specialite {
    SAGE_FEMME,
    MATRONE,
    GYNECOLOGUE,
    PEDIATRE
}

// Après
public enum Specialite {
    GYNECOLOGUE,
    PEDIATRE,
    GENERALISTE
}
```

### 3. Controllers - Annotations @PreAuthorize
Tous les controllers ont été mis à jour :

```java
// Avant
@PreAuthorize("hasAnyRole('SAGE_FEMME', 'MEDECIN', 'ADMIN')")

// Après
@PreAuthorize("hasAnyRole('MEDECIN', 'ADMINISTRATEUR')")
```

Controllers concernés :
- ✅ GrossesseController
- ✅ ConsultationPrenataleController
- ✅ ConsultationPostnataleController
- ✅ EnfantController
- ✅ VaccinationController
- ✅ ConseilController

## Migration de la base de données

### Si vous avez déjà des données en base

⚠️ **IMPORTANT** : Si vous avez déjà créé des utilisateurs avec les anciens rôles, vous devez migrer les données.

#### Option 1 : Supprimer et recréer la base (DÉVELOPPEMENT SEULEMENT)
```sql
DROP DATABASE keneyamuso_db;
CREATE DATABASE keneyamuso_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Puis relancer l'application qui recréera les tables automatiquement.

#### Option 2 : Migrer les données existantes (PRODUCTION)
```sql
-- Migrer les rôles SAGE_FEMME vers MEDECIN
UPDATE utilisateurs 
SET role = 'MEDECIN' 
WHERE role = 'SAGE_FEMME';

-- Migrer les rôles ADMIN vers ADMINISTRATEUR
UPDATE utilisateurs 
SET role = 'ADMINISTRATEUR' 
WHERE role = 'ADMIN';

-- Migrer les spécialités (table professionnels_sante)
UPDATE professionnels_sante 
SET specialite = 'GENERALISTE' 
WHERE specialite IN ('SAGE_FEMME', 'MATRONE');
```

#### Option 3 : Script de migration Java
Un script de migration automatique peut être créé si nécessaire.

## Exemples d'utilisation mis à jour

### Inscription d'un médecin
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

### Inscription d'un administrateur
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Admin",
    "prenom": "System",
    "telephone": "+22370999999",
    "motDePasse": "admin123",
    "role": "ADMINISTRATEUR",
    "langue": "fr"
  }'
```

## Vérifications à effectuer

### ✅ Checklist
- [x] Enum RoleUtilisateur mis à jour
- [x] Enum Specialite mis à jour
- [x] Tous les controllers mis à jour
- [x] Documentation mise à jour (README, API_EXAMPLES, etc.)
- [ ] Base de données migrée (si nécessaire)
- [ ] Tests mis à jour
- [ ] Frontend mis à jour (si applicable)

## Questions fréquentes

### Q: Pourquoi avoir supprimé SAGE_FEMME ?
**R:** Simplification du modèle : tous les professionnels de santé sont maintenant des MEDECIN avec différentes spécialités.

### Q: Que faire des anciens comptes SAGE_FEMME ?
**R:** Ils doivent être migrés vers MEDECIN avec la spécialité GENERALISTE ou appropriée.

### Q: Puis-je encore créer des sages-femmes ?
**R:** Non, créez plutôt un médecin avec la spécialité appropriée (GYNECOLOGUE, PEDIATRE ou GENERALISTE).

### Q: ADMIN et ADMINISTRATEUR sont-ils différents ?
**R:** Non, c'est juste un renommage. ADMIN devient ADMINISTRATEUR pour plus de clarté.

## Support

Si vous rencontrez des problèmes lors de la migration :
1. Vérifiez que tous les fichiers ont été mis à jour
2. Supprimez le dossier `target/` et recompilez : `mvn clean install`
3. Si en développement, supprimez la base H2 et relancez
4. Consultez les logs pour identifier les erreurs

---

**Date de migration** : 16/10/2025
**Version** : 1.0.0 → 1.1.0

