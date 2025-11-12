#!/bin/bash

# Script de configuration automatique pour KènèyaMuso Backend
# Usage: ./setup.sh [dev|docker|mysql]

set -e

echo "╔════════════════════════════════════════════════════════════╗"
echo "║        KènèyaMuso Backend - Script de Configuration       ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonctions utilitaires
print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Vérifier Java
check_java() {
    print_info "Vérification de Java..."
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 17 ]; then
            print_success "Java $JAVA_VERSION détecté"
            return 0
        else
            print_error "Java 17 ou supérieur est requis (trouvé: Java $JAVA_VERSION)"
            return 1
        fi
    else
        print_error "Java n'est pas installé"
        print_info "Installez Java 17+ depuis: https://adoptium.net/"
        return 1
    fi
}

# Vérifier Maven
check_maven() {
    print_info "Vérification de Maven..."
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
        print_success "Maven $MVN_VERSION détecté"
        return 0
    else
        print_error "Maven n'est pas installé"
        print_info "Installez Maven depuis: https://maven.apache.org/download.cgi"
        return 1
    fi
}

# Vérifier Docker
check_docker() {
    print_info "Vérification de Docker..."
    if command -v docker &> /dev/null && command -v docker-compose &> /dev/null; then
        print_success "Docker et Docker Compose détectés"
        return 0
    else
        print_error "Docker ou Docker Compose n'est pas installé"
        print_info "Installez Docker depuis: https://docs.docker.com/get-docker/"
        return 1
    fi
}

# Vérifier MySQL
check_mysql() {
    print_info "Vérification de MySQL..."
    if command -v mysql &> /dev/null; then
        print_success "MySQL détecté"
        return 0
    else
        print_warning "MySQL n'est pas installé ou pas dans le PATH"
        return 1
    fi
}

# Setup mode développement (H2)
setup_dev() {
    echo ""
    echo "═══════════════════════════════════════════════════════"
    echo "  Mode DÉVELOPPEMENT (H2 - Base de données en mémoire)"
    echo "═══════════════════════════════════════════════════════"
    echo ""

    check_java || exit 1
    check_maven || exit 1

    print_info "Compilation du projet..."
    mvn clean install -DskipTests

    print_success "Compilation terminée"
    echo ""
    print_info "Démarrage de l'application..."
    echo ""

    mvn spring-boot:run -Dspring-boot.run.profiles=dev
}

# Setup mode Docker
setup_docker() {
    echo ""
    echo "═══════════════════════════════════════════════════════"
    echo "  Mode DOCKER (MySQL + Application)"
    echo "═══════════════════════════════════════════════════════"
    echo ""

    check_docker || exit 1

    print_info "Construction des images Docker..."
    docker-compose build

    print_success "Images construites"
    echo ""
    print_info "Démarrage des conteneurs..."
    docker-compose up -d

    echo ""
    print_success "Conteneurs démarrés"
    echo ""
    print_info "Attente du démarrage de l'application (30 secondes)..."
    sleep 30

    echo ""
    print_info "État des conteneurs:"
    docker-compose ps

    echo ""
    print_success "Application prête!"
    echo ""
    print_info "Pour voir les logs:"
    echo "  docker-compose logs -f app"
    echo ""
    print_info "Pour arrêter:"
    echo "  docker-compose down"
}

# Setup mode MySQL
setup_mysql() {
    echo ""
    echo "═══════════════════════════════════════════════════════"
    echo "  Mode MYSQL (Base de données locale)"
    echo "═══════════════════════════════════════════════════════"
    echo ""

    check_java || exit 1
    check_maven || exit 1
    check_mysql

    echo ""
    print_warning "Configuration de MySQL requise"
    echo ""
    echo "Exécutez les commandes SQL suivantes dans MySQL:"
    echo ""
    echo -e "${YELLOW}"
    echo "CREATE DATABASE keneyamuso_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo "CREATE USER 'keneyamuso'@'localhost' IDENTIFIED BY 'keneyamuso123';"
    echo "GRANT ALL PRIVILEGES ON keneyamuso_db.* TO 'keneyamuso'@'localhost';"
    echo "FLUSH PRIVILEGES;"
    echo -e "${NC}"
    
    read -p "Avez-vous exécuté ces commandes? (o/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Oo]$ ]]; then
        print_error "Configuration annulée"
        exit 1
    fi

    print_info "Compilation du projet..."
    mvn clean install -DskipTests

    print_success "Compilation terminée"
    echo ""
    print_info "Démarrage de l'application..."
    echo ""

    mvn spring-boot:run
}

# Menu interactif
show_menu() {
    echo ""
    echo "Choisissez le mode de configuration:"
    echo ""
    echo "  1) Mode DÉVELOPPEMENT (H2 - Recommandé pour débuter)"
    echo "  2) Mode DOCKER (MySQL + Application)"
    echo "  3) Mode MYSQL (Base de données locale)"
    echo "  4) Quitter"
    echo ""
    read -p "Votre choix (1-4): " choice

    case $choice in
        1)
            setup_dev
            ;;
        2)
            setup_docker
            ;;
        3)
            setup_mysql
            ;;
        4)
            print_info "Au revoir!"
            exit 0
            ;;
        *)
            print_error "Choix invalide"
            show_menu
            ;;
    esac
}

# Point d'entrée
if [ $# -eq 0 ]; then
    # Mode interactif
    show_menu
else
    # Mode avec argument
    case $1 in
        dev)
            setup_dev
            ;;
        docker)
            setup_docker
            ;;
        mysql)
            setup_mysql
            ;;
        *)
            echo "Usage: $0 [dev|docker|mysql]"
            echo ""
            echo "Modes disponibles:"
            echo "  dev    - Mode développement avec H2"
            echo "  docker - Mode Docker avec MySQL"
            echo "  mysql  - Mode MySQL local"
            echo ""
            echo "Ou lancez sans argument pour le menu interactif"
            exit 1
            ;;
    esac
fi

# Message de fin
echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║              Application démarrée avec succès!             ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
print_info "Documentation Swagger: ${BLUE}http://localhost:8080/swagger-ui.html${NC}"
print_info "Console H2 (mode dev): ${BLUE}http://localhost:8080/h2-console${NC}"
echo ""
print_success "Bonne utilisation! 🚀"
echo ""

