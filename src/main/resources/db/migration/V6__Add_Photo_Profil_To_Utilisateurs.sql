-- Migration pour ajouter le champ photo_profil dans la table utilisateurs
-- Date: 2024-11-24
-- Description: Permet de stocker l'URL de la photo de profil des utilisateurs

ALTER TABLE utilisateurs 
ADD COLUMN photo_profil VARCHAR(500) NULL COMMENT 'URL de la photo de profil de l''utilisateur';

-- Index pour améliorer les performances si besoin de rechercher par photo
-- CREATE INDEX idx_utilisateurs_photo_profil ON utilisateurs(photo_profil);

