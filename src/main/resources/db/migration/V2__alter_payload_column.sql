-- Migration pour augmenter la taille de la colonne payload
-- Date: 2025-11-12
-- Raison: Le payload JSON des formulaires CPN/CPON peut être volumineux

-- Modifier la colonne payload pour utiliser LONGTEXT au lieu de TEXT/VARCHAR
ALTER TABLE dossier_medical_submissions 
MODIFY COLUMN payload LONGTEXT NOT NULL;

-- Commentaire pour information
-- LONGTEXT peut stocker jusqu'à 4GB de données texte
-- Ce qui est largement suffisant pour nos formulaires JSON

