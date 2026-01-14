#!/bin/bash

# ==========================================
# Script d'arrêt des services
# ==========================================

echo "🛑 Arrêt des services..."
echo ""

cd setup-api
docker-compose down
cd ..

cd setup-bd
docker-compose down
cd ..

echo ""
echo "✅ Services arrêtés"
