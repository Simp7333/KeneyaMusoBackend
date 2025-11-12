@echo off
REM Script de test automatique des endpoints - KènèyaMuso
REM Lance le script PowerShell pour tester tous les endpoints

echo ============================================
echo   KènèyaMuso - Test des Endpoints
echo ============================================
echo.

REM Vérifier si l'application est lancée
echo Verification de l'application...
curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo ERREUR : L'application n'est pas demarree !
    echo.
    echo Veuillez d'abord lancer l'application :
    echo   - Option 1 : setup.bat dev
    echo   - Option 2 : mvn spring-boot:run
    echo   - Option 3 : Depuis votre IDE
    echo.
    pause
    exit /b 1
)

echo OK - L'application est en ligne !
echo.

REM Lancer le script PowerShell
echo Lancement des tests...
echo.
powershell -ExecutionPolicy Bypass -File test-all-endpoints.ps1

echo.
echo ============================================
echo   Tests terminés !
echo ============================================
echo.
pause

