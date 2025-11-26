-- =====================================================
-- SCRIPT DE CORRECTION: Colonne 'payload' trop petite
-- =====================================================
-- Date: 2025-11-12
-- Problème: Data truncation: Data too long for column 'payload'
-- Solution: Modifier la colonne payload en LONGTEXT
-- =====================================================

-- Afficher la structure actuelle de la table
DESCRIBE dossier_medical_submissions;

-- Modifier la colonne payload pour utiliser LONGTEXT
ALTER TABLE dossier_medical_submissions 
MODIFY COLUMN payload LONGTEXT NOT NULL;

-- Vérifier la modification
DESCRIBE dossier_medical_submissions;

-- =====================================================
-- TAILLES DES COLONNES TEXTE DANS MySQL/MariaDB:
-- =====================================================
-- TINYTEXT:   255 caractères
-- TEXT:       65,535 caractères (~64 KB)
-- MEDIUMTEXT: 16,777,215 caractères (~16 MB)
-- LONGTEXT:   4,294,967,295 caractères (~4 GB)
-- =====================================================

-- Le formulaire JSON peut facilement dépasser 65KB avec toutes les données
-- LONGTEXT est donc le choix le plus sûr pour éviter les problèmes futurs

