# Création d'un Administrateur par Défaut

## 📋 Description

Ce document explique comment créer un administrateur par défaut pour l'application KènèyaMuso.

## 🚀 Méthode Recommandée : Via l'API (Plus Simple)

### Étape 1 : Démarrer le backend

Assurez-vous que le backend est démarré et accessible.

### Étape 2 : Créer l'admin par défaut

Utilisez l'endpoint d'initialisation :

```bash
curl -X POST http://localhost:8080/api/init/create-default-admin
```

**Réponse attendue :**
```json
{
  "success": true,
  "message": "Administrateur par défaut créé avec succès. Téléphone: +22370123456, Mot de passe: admin123. ⚠️ Changez le mot de passe après la première connexion!",
  "data": null
}
```

### Étape 3 : Vérifier que l'admin existe

```bash
curl -X GET http://localhost:8080/api/init/check-admin
```

### Informations de connexion par défaut :
- **Téléphone** : `+22370123456`
- **Mot de passe** : `admin123`
- **Nom** : `Admin`
- **Prénom** : `Système`
- **Rôle** : `ADMINISTRATEUR`

### ⚠️ IMPORTANT
**Changez le mot de passe après la première connexion !**

## 🔧 Méthode Alternative : Via l'API d'inscription

Vous pouvez créer un administrateur via l'endpoint d'inscription :

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Admin",
    "prenom": "Système",
    "telephone": "+22370123456",
    "motDePasse": "admin123",
    "role": "ADMINISTRATEUR",
    "langue": "fr"
  }'
```

## 🔧 Méthode SQL (Avancée)

Si vous préférez utiliser SQL directement :

### Étape 1 : Générer le hash BCrypt

Exécutez la classe `BCryptHashGenerator.java` :

```bash
cd KeneyaMusoBackend
mvn compile exec:java -Dexec.mainClass="com.keneyamuso.util.BCryptHashGenerator"
```

### Étape 2 : Utiliser le hash dans SQL

Copiez le hash généré et utilisez-le dans une requête SQL :

```sql
INSERT INTO utilisateurs (nom, prenom, telephone, mot_de_passe, role, langue, actif, date_creation, date_modification)
VALUES (
    'Admin',
    'Système',
    '+22370123456',
    'VOTRE_HASH_BCRYPT_GENERE', -- Collez le hash ici
    'ADMINISTRATEUR',
    'fr',
    true,
    NOW(),
    NOW()
)
ON CONFLICT (telephone) DO NOTHING;
```

## 📝 Notes

- L'endpoint `/api/init/create-default-admin` vérifie automatiquement si un admin existe déjà
- Le hash BCrypt est généré automatiquement par l'API (plus sûr)
- Après la première connexion, utilisez l'interface d'administration (`/admins`) pour créer d'autres admins
- L'endpoint ne créera qu'un seul admin par défaut (protection contre les doublons)

## 🔐 Sécurité

1. **Changez le mot de passe par défaut immédiatement**
2. **Ne partagez pas les identifiants par défaut**
3. **Créez des comptes individuels pour chaque administrateur**
4. **Utilisez des mots de passe forts**

## 🐛 Dépannage

### L'admin n'apparaît pas dans la base de données

1. Vérifiez que le backend est démarré
2. Vérifiez les logs pour voir si l'endpoint a été appelé
3. Vérifiez que la table `utilisateurs` existe
4. Vérifiez les contraintes de la base de données (unique sur telephone)

### Erreur "Un administrateur existe déjà"

C'est normal si un admin existe déjà. L'endpoint ne créera pas de doublon.

### Erreur "Le téléphone est déjà utilisé"

Le téléphone `+22370123456` est déjà utilisé par un autre utilisateur. Utilisez un autre téléphone ou supprimez l'utilisateur existant.
