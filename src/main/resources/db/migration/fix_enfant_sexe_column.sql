-- Migration pour corriger la taille de la colonne 'sexe' dans la table 'enfants'
-- La colonne doit pouvoir stocker 'GARCON' (6 caractères) et 'FILLE' (5 caractères)

-- Pour MySQL/MariaDB
ALTER TABLE enfants MODIFY COLUMN sexe VARCHAR(10) NOT NULL;

-- Alternative si c'est un ENUM (décommenter si nécessaire)
-- ALTER TABLE enfants MODIFY COLUMN sexe ENUM('GARCON', 'FILLE') NOT NULL;
