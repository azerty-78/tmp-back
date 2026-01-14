#!/bin/bash

# Script pour créer le réseau Docker avant de démarrer les services
# Ce script doit être exécuté avant docker-compose up

# Charger les variables d'environnement
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

PROJECT_NAME=${PROJECT_NAME:-project-name}
NETWORK_NAME="${PROJECT_NAME}-network"

echo "🔧 Création du réseau Docker: $NETWORK_NAME"

# Vérifier si le réseau existe déjà
if docker network ls | grep -q "$NETWORK_NAME"; then
    echo "✓ Le réseau $NETWORK_NAME existe déjà"
else
    # Créer le réseau
    docker network create "$NETWORK_NAME"
    echo "✅ Réseau $NETWORK_NAME créé avec succès"
fi

echo ""
echo "📋 Vous pouvez maintenant lancer: docker-compose up -d"
