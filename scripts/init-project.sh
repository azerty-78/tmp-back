#!/bin/bash

# ==========================================
# Script d'initialisation automatique du projet
# ==========================================

set -e  # Arrêter en cas d'erreur

echo "🚀 Initialisation du projet template..."
echo ""

# Couleurs pour les messages
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Fonction pour demander une valeur
ask_value() {
    local prompt=$1
    local default=$2
    local var_name=$3
    
    if [ -n "$default" ]; then
        read -p "$prompt [$default]: " value
        value=${value:-$default}
    else
        read -p "$prompt: " value
    fi
    
    eval "$var_name='$value'"
}

# Demander les informations du projet
echo "📝 Veuillez fournir les informations suivantes :"
echo ""

ask_value "Nom du projet (PROJECT_NAME)" "project-name" PROJECT_NAME
ask_value "Nom de la base de données MongoDB (MONGO_DATABASE)" "$PROJECT_NAME" MONGO_DATABASE
ask_value "URL de l'API (APP_BASE_URL)" "http://localhost:8090" APP_BASE_URL
ask_value "URL du frontend (APP_FRONTEND_URL)" "http://localhost:3000" APP_FRONTEND_URL
ask_value "Origines autorisées CORS (ALLOWED_ORIGINS)" "$APP_FRONTEND_URL,http://localhost:3001" ALLOWED_ORIGINS
ask_value "Nom d'utilisateur Docker Hub (optionnel)" "" DOCKERHUB_USERNAME

echo ""
echo -e "${YELLOW}📧 Configuration SMTP (Email) :${NC}"
echo "  Pour le développement, utilisez MailHog (localhost:1025)"
echo "  Pour la production, utilisez les paramètres SMTP de votre fournisseur"
echo ""
read -p "Utiliser la configuration SMTP de production ? (o/N): " USE_PRODUCTION_SMTP
USE_PRODUCTION_SMTP=${USE_PRODUCTION_SMTP:-n}

if [ "$USE_PRODUCTION_SMTP" = "o" ] || [ "$USE_PRODUCTION_SMTP" = "O" ]; then
    echo -e "${GREEN}Configuration SMTP PRODUCTION${NC}"
    ask_value "Host SMTP (MAIL_HOST)" "smtp.lws.fr" MAIL_HOST
    ask_value "Port SMTP (MAIL_PORT)" "587" MAIL_PORT
    ask_value "Nom d'utilisateur SMTP (MAIL_USERNAME)" "" MAIL_USERNAME
    read -sp "Mot de passe SMTP (MAIL_PASSWORD): " MAIL_PASSWORD
    echo ""
    MAIL_SMTP_AUTH="true"
    MAIL_SMTP_STARTTLS="true"
else
    echo -e "${GREEN}Configuration SMTP TEST (MailHog)${NC}"
    MAIL_HOST="localhost"
    MAIL_PORT="1025"
    MAIL_USERNAME=""
    MAIL_PASSWORD=""
    MAIL_SMTP_AUTH="false"
    MAIL_SMTP_STARTTLS="false"
fi

ask_value "Adresse email expéditrice (EMAIL_FROM_ADDRESS)" "noreply@example.com" EMAIL_FROM_ADDRESS
ask_value "Nom expéditeur (EMAIL_FROM_NAME)" "KOBE Corporation" EMAIL_FROM_NAME

echo ""
echo "⏳ Configuration en cours..."

# Créer les fichiers .env
echo "📄 Création des fichiers .env..."

# Setup BD
if [ ! -f "setup-bd/.env" ]; then
    cp setup-bd/env.example setup-bd/.env
    sed -i.bak "s/PROJECT_NAME=project-name/PROJECT_NAME=$PROJECT_NAME/" setup-bd/.env
    sed -i.bak "s/MONGO_DATABASE=project-name/MONGO_DATABASE=$MONGO_DATABASE/" setup-bd/.env
    rm setup-bd/.env.bak 2>/dev/null || true
    echo "  ✅ setup-bd/.env créé"
else
    echo "  ⚠️  setup-bd/.env existe déjà, ignoré"
fi

# Setup SMTP
if [ ! -f "setup-smtp/.env" ]; then
    if [ -f "setup-smtp/env.example" ]; then
        cp setup-smtp/env.example setup-smtp/.env
        sed -i.bak "s/PROJECT_NAME=project-name/PROJECT_NAME=$PROJECT_NAME/" setup-smtp/.env
        rm setup-smtp/.env.bak 2>/dev/null || true
        echo "  ✅ setup-smtp/.env créé"
    else
        echo "  ⚠️  setup-smtp/env.example introuvable, ignoré"
    fi
else
    echo "  ⚠️  setup-smtp/.env existe déjà, ignoré"
fi

# Setup API
if [ ! -f "setup-api/.env" ]; then
    cp setup-api/env.example setup-api/.env
    
    # Remplacer PROJECT_NAME
    sed -i.bak "s/PROJECT_NAME=project-name/PROJECT_NAME=$PROJECT_NAME/" setup-api/.env
    
    # Générer l'URI MongoDB
    MONGO_URI="mongodb://root:qwerty87@${PROJECT_NAME}-mongodb:27017/${MONGO_DATABASE}?authSource=admin"
    sed -i.bak "s|SPRING_DATA_MONGODB_URI=.*|SPRING_DATA_MONGODB_URI=$MONGO_URI|" setup-api/.env
    
    # Remplacer les URLs
    sed -i.bak "s|APP_BASE_URL=.*|APP_BASE_URL=$APP_BASE_URL|" setup-api/.env
    sed -i.bak "s|APP_FRONTEND_URL=.*|APP_FRONTEND_URL=$APP_FRONTEND_URL|" setup-api/.env
    sed -i.bak "s|ALLOWED_ORIGINS=.*|ALLOWED_ORIGINS=$ALLOWED_ORIGINS|" setup-api/.env
    
    # Docker Hub username (si fourni)
    if [ -n "$DOCKERHUB_USERNAME" ]; then
        sed -i.bak "s/DOCKERHUB_USERNAME=.*/DOCKERHUB_USERNAME=$DOCKERHUB_USERNAME/" setup-api/.env
    fi
    
    # Configuration SMTP
    sed -i.bak "s|MAIL_HOST=.*|MAIL_HOST=$MAIL_HOST|" setup-api/.env
    sed -i.bak "s|MAIL_PORT=.*|MAIL_PORT=$MAIL_PORT|" setup-api/.env
    if [ -n "$MAIL_USERNAME" ]; then
        sed -i.bak "s|MAIL_USERNAME=.*|MAIL_USERNAME=$MAIL_USERNAME|" setup-api/.env
    fi
    if [ -n "$MAIL_PASSWORD" ]; then
        sed -i.bak "s|MAIL_PASSWORD=.*|MAIL_PASSWORD=$MAIL_PASSWORD|" setup-api/.env
    fi
    sed -i.bak "s|MAIL_SMTP_AUTH=.*|MAIL_SMTP_AUTH=$MAIL_SMTP_AUTH|" setup-api/.env
    sed -i.bak "s|MAIL_SMTP_STARTTLS=.*|MAIL_SMTP_STARTTLS=$MAIL_SMTP_STARTTLS|" setup-api/.env
    
    # Configuration Email Application
    sed -i.bak "s|EMAIL_FROM_ADDRESS=.*|EMAIL_FROM_ADDRESS=$EMAIL_FROM_ADDRESS|" setup-api/.env
    sed -i.bak "s|EMAIL_FROM_NAME=.*|EMAIL_FROM_NAME=$EMAIL_FROM_NAME|" setup-api/.env
    sed -i.bak "s|EMAIL_FRONTEND_URL=.*|EMAIL_FRONTEND_URL=$APP_FRONTEND_URL|" setup-api/.env
    
    rm setup-api/.env.bak 2>/dev/null || true
    echo "  ✅ setup-api/.env créé"
else
    echo "  ⚠️  setup-api/.env existe déjà, ignoré"
fi

# Créer les dossiers nécessaires
echo "📁 Création des dossiers..."
mkdir -p setup-api/uploads/users
mkdir -p setup-api/uploads/stock
mkdir -p setup-bd/init-scripts
echo "  ✅ Dossiers créés"

# Validation
echo ""
echo "🔍 Validation de la configuration..."

# Vérifier que PROJECT_NAME est identique
BD_PROJECT=$(grep "^PROJECT_NAME=" setup-bd/.env 2>/dev/null | cut -d'=' -f2 || echo "")
API_PROJECT=$(grep "^PROJECT_NAME=" setup-api/.env 2>/dev/null | cut -d'=' -f2 || echo "")

if [ "$BD_PROJECT" != "$API_PROJECT" ] && [ -n "$BD_PROJECT" ] && [ -n "$API_PROJECT" ]; then
    echo -e "  ${RED}❌ ERREUR: PROJECT_NAME différent entre setup-bd et setup-api${NC}"
    exit 1
fi

echo -e "  ${GREEN}✅ Configuration validée${NC}"

echo ""
echo -e "${GREEN}✨ Initialisation terminée avec succès !${NC}"
echo ""
echo "📋 Prochaines étapes :"
echo "  1. Vérifiez les fichiers .env dans setup-bd/, setup-api/ et setup-smtp/"
echo "  2. Si vous utilisez MailHog (mode test), démarrez-le :"
echo "     cd setup-smtp"
echo "     docker-compose up -d"
echo "  3. Lancez './scripts/start.sh' pour démarrer les services"
echo "  4. Ou utilisez 'make start' si vous avez un Makefile"
echo ""
