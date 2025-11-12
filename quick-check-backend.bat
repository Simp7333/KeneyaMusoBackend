@echo off
chcp 65001 >nul
echo.
echo ════════════════════════════════════════════════════════════════════════
echo   VÉRIFICATION RAPIDE DU BACKEND
echo ════════════════════════════════════════════════════════════════════════
echo.

echo [TEST 1] Vérification du port 8080...
netstat -an | findstr ":8080.*LISTENING" >nul
if errorlevel 1 (
    echo [❌] Le port 8080 n'est PAS en écoute
    echo [ACTION] Démarrez le backend avec start-backend.bat
    goto :end
) else (
    echo [✅] Le port 8080 est en écoute
    netstat -an | findstr ":8080.*LISTENING"
)
echo.

echo [TEST 2] Test de connexion locale...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/api-docs' -UseBasicParsing -TimeoutSec 3; Write-Host '[✅] Backend répond (Status:' $response.StatusCode ')' } catch { Write-Host '[❌] Backend ne répond pas:' $_.Exception.Message }"
echo.

echo [TEST 3] Vérification depuis l'émulateur Android...
adb devices | findstr "device$" >nul
if errorlevel 1 (
    echo [⚠️] Aucun émulateur Android connecté
) else (
    echo [✅] Émulateur Android détecté
    echo [INFO] Test depuis l'émulateur:
    adb shell "curl -s -m 3 http://10.0.2.2:8080/api-docs 2>&1 | head -c 100"
)
echo.

:end
pause

