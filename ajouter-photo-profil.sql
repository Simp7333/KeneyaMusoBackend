-- Script SQL pour ajouter le champ photo_profil à la table utilisateurs
-- À exécuter si vous n'utilisez pas Flyway

USE keneya_muso;

-- Ajouter la colonne photo_profil
ALTER TABLE utilisateurs 
ADD COLUMN photo_profil VARCHAR(500) NULL COMMENT 'URL de la photo de profil de l''utilisateur';

-- Vérifier que la colonne a bien été ajoutée
DESCRIBE utilisateurs;

-- Afficher tous les utilisateurs avec leur photo de profil
SELECT id, nom, prenom, telephone, role, photo_profil 
FROM utilisateurs
ORDER BY id;

