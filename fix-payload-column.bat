@echo off
REM =====================================================
REM Script de correction: Colonne 'payload' trop petite
REM =====================================================
echo.
echo ========================================
echo CORRECTION: Colonne 'payload' trop petite
echo ========================================
echo.
echo Ce script va modifier la colonne 'payload'
echo de la table 'dossier_medical_submissions'
echo pour utiliser LONGTEXT au lieu de TEXT.
echo.
echo IMPORTANT: Assurez-vous que MySQL/MariaDB est en cours d'execution!
echo.
pause

REM Demander les informations de connexion
set /p DB_NAME="Nom de la base de donnees (par defaut: keneya_muso): "
if "%DB_NAME%"=="" set DB_NAME=keneya_muso

set /p DB_USER="Utilisateur MySQL (par defaut: root): "
if "%DB_USER%"=="" set DB_USER=root

echo.
echo Connexion a la base de donnees...
echo.

REM Exécuter le script SQL
mysql -u %DB_USER% -p %DB_NAME% < fix_payload_column.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCES: La colonne a ete modifiee!
    echo ========================================
    echo.
    echo Vous pouvez maintenant redemarrer le backend
    echo et tester la soumission des formulaires.
    echo.
) else (
    echo.
    echo ========================================
    echo ERREUR: La modification a echoue!
    echo ========================================
    echo.
    echo Verifiez que:
    echo - MySQL/MariaDB est en cours d'execution
    echo - Le nom de la base de donnees est correct
    echo - L'utilisateur a les droits necessaires
    echo.
)

pause

