@echo off
chcp 65001 >nul
echo ========================================
echo Test d'approbation de formulaire
echo ========================================
echo.

REM Configuration
set BASE_URL=http://localhost:8080/api
set MEDECIN_TELEPHONE=73337988
set SUBMISSION_ID=1

echo 📝 Étape 1: Connexion du médecin...
curl -X POST "%BASE_URL%/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"telephone\":\"%MEDECIN_TELEPHONE%\",\"motDePasse\":\"password123\"}" ^
  -c cookies.txt ^
  -s | jq .

echo.
echo.
echo 📋 Étape 2: Récupération des soumissions en attente...
curl -X GET "%BASE_URL%/professionnels/soumissions" ^
  -b cookies.txt ^
  -s | jq .

echo.
echo.
echo ✅ Étape 3: Approbation de la soumission %SUBMISSION_ID%...
curl -X POST "%BASE_URL%/professionnels/soumissions/%SUBMISSION_ID%/approuver" ^
  -H "Content-Type: application/json" ^
  -b cookies.txt ^
  -d "{\"commentaire\":\"Formulaire approuvé pour test\"}" ^
  -v

echo.
echo.
echo 🔍 Étape 4: Vérification de la soumission...
curl -X GET "%BASE_URL%/professionnels/soumissions/%SUBMISSION_ID%" ^
  -b cookies.txt ^
  -s | jq .

echo.
echo ========================================
echo Test terminé
echo ========================================
pause

