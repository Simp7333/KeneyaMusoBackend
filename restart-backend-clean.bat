@echo off
chcp 65001 >nul
echo.
echo ════════════════════════════════════════════════════════════════════════
echo   REDÉMARRAGE PROPRE DU BACKEND
echo ════════════════════════════════════════════════════════════════════════
echo.

echo [ETAPE 1/4] Arrêt de tous les processus Java...
taskkill /F /IM java.exe >nul 2>&1
if errorlevel 1 (
    echo [INFO] Aucun processus Java trouvé
) else (
    echo [OK] Processus Java arrêtés
)
timeout /t 3 /nobreak >nul
echo.

echo [ETAPE 2/4] Vérification que le port 8080 est libre...
netstat -an | findstr ":8080.*LISTENING" >nul
if not errorlevel 1 (
    echo [ATTENTION] Le port 8080 est toujours utilisé!
    echo [INFO] Attente de 5 secondes supplémentaires...
    timeout /t 5 /nobreak >nul
) else (
    echo [OK] Port 8080 libre
)
echo.

echo [ETAPE 3/4] Démarrage du backend en mode développement (H2)...
echo [INFO] Mode H2 = Base de données en mémoire (plus rapide, pas besoin de MySQL)
echo [INFO] Cela prend généralement 30-60 secondes...
echo.
echo ════════════════════════════════════════════════════════════════════════
echo   LOGS DU BACKEND (Appuyez sur Ctrl+C pour arrêter)
echo ════════════════════════════════════════════════════════════════════════
echo.

REM Démarrer le backend avec H2
mvn spring-boot:run -Dspring-boot.run.profiles=dev

pause

