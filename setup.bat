@echo off
REM Script de configuration automatique pour KènèyaMuso Backend (Windows)
REM Usage: setup.bat [dev|docker|mysql]

setlocal enabledelayedexpansion

echo ╔════════════════════════════════════════════════════════════╗
echo ║        KènèyaMuso Backend - Script de Configuration       ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Vérifier Java
:check_java
echo [INFO] Verification de Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Java n'est pas installe
    echo [INFO] Installez Java 17+ depuis: https://adoptium.net/
    pause
    exit /b 1
)
echo [OK] Java detecte
echo.

REM Vérifier Maven
:check_maven
echo [INFO] Verification de Maven...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Maven n'est pas installe
    echo [INFO] Installez Maven depuis: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
echo [OK] Maven detecte
echo.

REM Mode développement
if "%1"=="dev" goto setup_dev
if "%1"=="docker" goto setup_docker
if "%1"=="mysql" goto setup_mysql
if "%1"=="" goto show_menu

echo Usage: %0 [dev^|docker^|mysql]
echo.
echo Modes disponibles:
echo   dev    - Mode developpement avec H2
echo   docker - Mode Docker avec MySQL
echo   mysql  - Mode MySQL local
echo.
pause
exit /b 1

:show_menu
echo.
echo Choisissez le mode de configuration:
echo.
echo   1) Mode DEVELOPPEMENT (H2 - Recommande pour debuter)
echo   2) Mode DOCKER (MySQL + Application)
echo   3) Mode MYSQL (Base de donnees locale)
echo   4) Quitter
echo.
set /p choice="Votre choix (1-4): "

if "%choice%"=="1" goto setup_dev
if "%choice%"=="2" goto setup_docker
if "%choice%"=="3" goto setup_mysql
if "%choice%"=="4" goto end
echo [ERREUR] Choix invalide
goto show_menu

:setup_dev
echo.
echo ═══════════════════════════════════════════════════════
echo   Mode DEVELOPPEMENT (H2 - Base de donnees en memoire)
echo ═══════════════════════════════════════════════════════
echo.

echo [INFO] Compilation du projet...
call mvn clean install -DskipTests
if errorlevel 1 (
    echo [ERREUR] Echec de la compilation
    pause
    exit /b 1
)

echo [OK] Compilation terminee
echo.
echo [INFO] Demarrage de l'application...
echo.

call mvn spring-boot:run -Dspring-boot.run.profiles=dev
goto end

:setup_docker
echo.
echo ═══════════════════════════════════════════════════════
echo   Mode DOCKER (MySQL + Application)
echo ═══════════════════════════════════════════════════════
echo.

echo [INFO] Verification de Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Docker n'est pas installe
    echo [INFO] Installez Docker depuis: https://docs.docker.com/get-docker/
    pause
    exit /b 1
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Docker Compose n'est pas installe
    pause
    exit /b 1
)

echo [OK] Docker detecte
echo.

echo [INFO] Construction des images Docker...
docker-compose build

echo [OK] Images construites
echo.
echo [INFO] Demarrage des conteneurs...
docker-compose up -d

echo.
echo [OK] Conteneurs demarres
echo.
echo [INFO] Attente du demarrage (30 secondes)...
timeout /t 30 /nobreak >nul

echo.
echo [INFO] Etat des conteneurs:
docker-compose ps

echo.
echo [OK] Application prete!
echo.
echo [INFO] Pour voir les logs:
echo   docker-compose logs -f app
echo.
echo [INFO] Pour arreter:
echo   docker-compose down
goto end

:setup_mysql
echo.
echo ═══════════════════════════════════════════════════════
echo   Mode MYSQL (Base de donnees locale)
echo ═══════════════════════════════════════════════════════
echo.

echo [ATTENTION] Configuration de MySQL requise
echo.
echo Executez les commandes SQL suivantes dans MySQL:
echo.
echo CREATE DATABASE keneyamuso_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
echo CREATE USER 'keneyamuso'@'localhost' IDENTIFIED BY 'keneyamuso123';
echo GRANT ALL PRIVILEGES ON keneyamuso_db.* TO 'keneyamuso'@'localhost';
echo FLUSH PRIVILEGES;
echo.

set /p confirm="Avez-vous execute ces commandes? (o/n): "
if /i not "%confirm%"=="o" (
    echo [INFO] Configuration annulee
    pause
    exit /b 1
)

echo [INFO] Compilation du projet...
call mvn clean install -DskipTests
if errorlevel 1 (
    echo [ERREUR] Echec de la compilation
    pause
    exit /b 1
)

echo [OK] Compilation terminee
echo.
echo [INFO] Demarrage de l'application...
echo.

call mvn spring-boot:run
goto end

:end
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║              Application demarree avec succes!             ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
echo [INFO] Documentation Swagger: http://localhost:8080/swagger-ui.html
echo [INFO] Console H2 (mode dev): http://localhost:8080/h2-console
echo.
echo [OK] Bonne utilisation! 🚀
echo.
pause

