#!/bin/bash

# ==========================================
# Script de démarrage unifié des services
# ==========================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "🚀 Démarrage des services..."
echo ""

# Vérifier que les fichiers .env existent
if [ ! -f "setup-bd/.env" ]; then
    echo -e "${RED}❌ Erreur: setup-bd/.env n'existe pas${NC}"
    echo "   Lancez d'abord: ./scripts/init-project.sh"
    exit 1
fi

if [ ! -f "setup-api/.env" ]; then
    echo -e "${RED}❌ Erreur: setup-api/.env n'existe pas${NC}"
    echo "   Lancez d'abord: ./scripts/init-project.sh"
    exit 1
fi

# Charger les variables d'environnement
source setup-bd/.env
source setup-api/.env

echo "📦 Démarrage de MongoDB..."
cd setup-bd
# Le réseau sera créé automatiquement par Docker Compose
docker-compose up -d

# Attendre que MongoDB soit healthy
echo "⏳ Attente que MongoDB soit prêt..."
timeout=60
counter=0
while [ $counter -lt $timeout ]; do
    if docker-compose ps | grep -q "healthy"; then
        echo -e "${GREEN}✅ MongoDB est prêt${NC}"
        break
    fi
    sleep 2
    counter=$((counter + 2))
    echo -n "."
done

if [ $counter -ge $timeout ]; then
    echo -e "${RED}❌ Timeout: MongoDB n'est pas prêt après ${timeout}s${NC}"
    exit 1
fi

cd ..

echo ""
echo "📦 Démarrage de l'API..."
cd setup-api
docker-compose up -d

cd ..

echo ""
echo -e "${GREEN}✨ Services démarrés avec succès !${NC}"
echo ""
echo "📊 Statut des services :"
docker ps --filter "name=${PROJECT_NAME:-project-name}" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""
echo "📋 Commandes utiles :"
echo "  - Voir les logs: docker-compose -f setup-bd/docker-compose.yaml -f setup-api/docker-compose.yaml logs -f"
echo "  - Arrêter: ./scripts/stop.sh"
echo ""
