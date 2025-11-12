@echo off
chcp 65001 >nul
echo.
echo ════════════════════════════════════════════════════════════════════════
echo   VÉRIFICATION DU BACKEND KènèyaMuso
echo ════════════════════════════════════════════════════════════════════════
echo.

set BACKEND_IP=192.168.43.183
set BACKEND_PORT=8080

echo [INFO] Vérification de la configuration...
echo        IP configurée: السرعه %BACKEND_IP%
echo        Port: %BACKEND_PORT%
echo.

REM Obtenir l'adresse IP actuelle
echo [INFO] Détection de l'adresse IP actuelle...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4"') do (
    set CURRENT_IP=%%a
    set CURRENT_IP=!CURRENT_IP:~1!
    echo        IP détectée: !CURRENT_IP!
    goto :ip_check
)

:ip_check
echo.

REM Test 1: Vérifier que le port est en écoute
echo [TEST 1] Vérification que le port %BACKEND_PORT% est en écoute...
netstat -an | findstr ":%BACKEND_PORT%.*LISTENING" >nul
if errorlevel 1 (
    echo [❌ ÉCHEC] Le port %BACKEND_PORT% n'est pas en écoute
    echo [INFO] Le backend n'est probablement pas démarré
    echo [ACTION] Exécutez start-backend.bat pour démarrer le serveur
    goto :end
) else (
    echo [✅ OK] Le port %BACKEND_PORT% est en écoute
    netstat -an | findstr ":%BACKEND_PORT%.*LISTENING"
)
echo.

REM Test 2: Vérifier sur quelle adresse le serveur écoute
echo [TEST 2] Vérification de l'adresse d'écoute...
netstat -an | findstr "0.0.0.0:%BACKEND_PORT%.*LISTENING" >nul
if errorlevel 1 (
    netstat -an | findstr "127.0.0.1:%BACKEND_PORT%.*LISTENING" >nul
    if not errorlevel 1 (
        echo [⚠️  ATTENTION] Le serveur écoute seulement sur 127.0.0.1 (localhost)
        echo [INFO] Le serveur ne sera pas accessible depuis d'autres machines
        echo [ACTION] Vérifiez que server.address=0.0.0.0 est dans application.properties
    )
) else (
    echo [✅ OK] Le serveur écoute sur 0.0.0.0 (toutes les interfaces)
)
echo.

REM Test 3: Tester l'accès local
echo [TEST 3] Test d'accès local (localhost)...
curl -s -o nul -w "%%{http_code}" http://localhost:%BACKEND_PORT%/api-docs 2>nul
if errorlevel 1 (
    echo [❌ ÉCHEC] Impossible d'accéder au serveur sur localhost
    echo [INFO] Le serveur ne répond pas
) else (
    echo [✅ OK] Le serveur répond sur localhost
)
echo.

REM Test 4: Tester l'accès via IP
echo [TEST 4] Test d'accès via IP (%BACKEND_IP%)...
curl -s -o nul -w "%%{http_code}" http://%BACKEND_IP%:%BACKEND_PORT%/api-docs 2>nul
if errorlevel 1 (
    echo [❌ ÉCHEC] Impossible d'accéder via l'IP %BACKEND_IP%
    if not "%CURRENT_IP%"=="%BACKEND_IP%" (
        echo [INFO] L'IP configurée (%BACKEND_IP%) ne correspond pas à l'IP actuelle (!CURRENT_IP!)
        echo [ACTION] Mettez à jour l'IP dans Flutter (api_config.dart)
    )
) else (
    echo [✅ OK] Le serveur est accessible via l'IP %BACKEND_IP%
)
echo.

REM Test 5: Vérifier Swagger
echo [TEST 5] Vérification de Swagger UI...
echo        URL: http://localhost:%BACKEND_PORT%/swagger-ui.html
echo        URL IP: http://%BACKEND_IP%:%BACKEND_PORT%/swagger-ui.html
echo.

REM Résumé
echo ════════════════════════════════════════════════════════════════════════
echo   RÉSUMÉ
echo ════════════════════════════════════════════════════════════════════════
echo.
echo URLs pour tester:
echo   - Local:      http://localhost:%BACKEND_PORT%/swagger-ui.html
echo   - Via IP:     http://%BACKEND_IP%:%BACKEND_PORT%/swagger-ui.html
echo   - API Base:   http://%BACKEND_IP%:%BACKEND_PORT%/api
echo.
echo Configuration Flutter:
echo   - Modifier: Keneya_muso/lib/services/api_config.dart
echo   - Base URL:  http://%BACKEND_IP%:%BACKEND_PORT%/api
echo.

if not "%CURRENT_IP%"=="%BACKEND_IP%" (
    echo ⚠️  ATTENTION: L'IP configurée (%BACKEND_IP%) est différente de l'IP actuelle (!CURRENT_IP!)
    echo    Mettez à jour api_config.dart avec l'IP: !CURRENT_IP!
    echo.
)

:end
pause

