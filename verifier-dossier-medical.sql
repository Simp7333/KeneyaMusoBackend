-- Script pour vérifier les dossiers médicaux des patientes
-- Utiliser ce script pour déboguer les problèmes d'approbation de formulaires

-- 1. Vérifier toutes les patientes et leurs dossiers médicaux
SELECT 
    p.id as patiente_id,
    u.prenom,
    u.nom,
    u.telephone,
    dm.id as dossier_medical_id,
    CASE 
        WHEN dm.id IS NULL THEN '❌ AUCUN DOSSIER'
        ELSE '✅ Dossier existant'
    END as statut_dossier
FROM patientes p
JOIN utilisateurs u ON p.id = u.id
LEFT JOIN dossiers_medicaux dm ON dm.patiente_id = p.id
ORDER BY p.id;

-- 2. Vérifier les soumissions en attente sans dossier médical
SELECT 
    dms.id as submission_id,
    dms.patiente_id,
    u.prenom,
    u.nom,
    dms.type,
    dms.status,
    dm.id as dossier_medical_id,
    CASE 
        WHEN dm.id IS NULL THEN '⚠️ PATIENTE SANS DOSSIER'
        ELSE '✅ Dossier OK'
    END as diagnostic
FROM dossier_medical_submissions dms
JOIN patientes p ON dms.patiente_id = p.id
JOIN utilisateurs u ON p.id = u.id
LEFT JOIN dossiers_medicaux dm ON dm.patiente_id = p.id
WHERE dms.status = 'EN_ATTENTE'
ORDER BY dms.id;

-- 3. Créer les dossiers médicaux manquants pour les patientes qui ont des soumissions
INSERT INTO dossiers_medicaux (patiente_id, date_creation, date_modification)
SELECT DISTINCT 
    dms.patiente_id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM dossier_medical_submissions dms
LEFT JOIN dossiers_medicaux dm ON dm.patiente_id = dms.patiente_id
WHERE dm.id IS NULL
  AND dms.status = 'EN_ATTENTE';

-- 4. Vérifier le résultat après création
SELECT 
    p.id as patiente_id,
    u.prenom,
    u.nom,
    dm.id as dossier_medical_id,
    COUNT(fcpn.id) as nb_formulaires_cpn,
    COUNT(fcpon.id) as nb_formulaires_cpon
FROM patientes p
JOIN utilisateurs u ON p.id = u.id
LEFT JOIN dossiers_medicaux dm ON dm.patiente_id = p.id
LEFT JOIN formulaires_cpn fcpn ON fcpn.dossier_medical_id = dm.id
LEFT JOIN formulaires_cpon fcpon ON fcpon.dossier_medical_id = dm.id
GROUP BY p.id, u.prenom, u.nom, dm.id
ORDER BY p.id;

