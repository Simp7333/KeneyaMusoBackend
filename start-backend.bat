@echo off
chcp 65001 >nul
echo.
echo ════════════════════════════════════════════════════════════════════════
echo   DÉMARRAGE DU BACKEND KènèyaMuso
echo ════════════════════════════════════════════════════════════════════════
echo.

REM Vérifier si Java est installé
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Java n'est pas installé ou pas dans le PATH
    echo [INFO] Installez Java 17+ depuis: https://adoptium.net/
    pause
    exit /b 1
)

REM Vérifier si Maven est installé
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Maven n'est pas installé ou pas dans le PATH
    echo [INFO] Installez Maven depuis: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo [OK] Java et Maven détectés
echo.

REM Vérifier si le port 8080 est déjà utilisé
netstat -an | findstr ":8080.*LISTENING" >nul
if not errorlevel 1 (
    echo [ATTENTION] Le port 8080 est déjà utilisé!
    echo [INFO] Arrêtez l'application qui utilise ce port ou modifiez server.port
    echo.
    echo Voulez-vous continuer quand même? (O/N)
    set /p continue=
    if /i not "%continue%"=="O" (
        exit /b 1
    )
)

echo [INFO] Démarrage du backend Spring Boot...
echo [INFO] Configuration:
echo        - Port: 8080
echo        - Adresse: 0.0.0.0 (accessible depuis toutes les interfaces)
echo        - Swagger UI: http://localhost:8080/swagger-ui.html
echo.
echo [INFO] Pour les tests depuis Flutter, utilisez votre IP: http://[VOTRE_IP]:8080/api
echo.

REM Obtenir l'adresse IP locale
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4"') do (
    set ip=%%a
    set ip=!ip:~1!
    goto :ip_found
)

:ip_found
echo [INFO] Votre adresse IP locale semble être: %ip%
echo [INFO] URL pour Flutter: http://%ip%:8080/api
echo.
echo ════════════════════════════════════════════════════════════════════════
echo   DÉMARRAGE EN COURS...
echo   Appuyez sur Ctrl+C pour arrêter le serveur
echo ════════════════════════════════════════════════════════════════════════
echo.

REM Démarrer le backend
mvn spring-boot:run

pause

