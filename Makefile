# ==========================================
# Makefile pour commandes courantes
# ==========================================

.PHONY: help init start stop restart logs status clean rebuild validate

# Charger les variables d'environnement
-include setup-bd/.env
-include setup-api/.env

PROJECT_NAME ?= project-name

help: ## Affiche l'aide
	@echo "📋 Commandes disponibles :"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

init: ## Initialise le projet (copie les .env et configure)
	@echo "🚀 Initialisation du projet..."
	@if [ -f scripts/init-project.sh ]; then \
		chmod +x scripts/init-project.sh && ./scripts/init-project.sh; \
	else \
		echo "⚠️  Script d'initialisation non trouvé. Copiez manuellement les env.example vers .env"; \
	fi

start: ## Démarre tous les services
	@echo "🚀 Démarrage des services..."
	@echo "📦 Démarrage de MongoDB (le réseau sera créé automatiquement)..."
	@cd setup-bd && docker-compose up -d
	@sleep 5
	@echo "📦 Démarrage de l'API..."
	@cd setup-api && docker-compose up -d
	@echo "✅ Services démarrés"
	@$(MAKE) status

stop: ## Arrête tous les services
	@echo "🛑 Arrêt des services..."
	@cd setup-api && docker-compose down
	@cd setup-bd && docker-compose down
	@echo "✅ Services arrêtés"

restart: stop start ## Redémarre tous les services

logs: ## Affiche les logs de tous les services
	@docker-compose -f setup-bd/docker-compose.yaml -f setup-api/docker-compose.yaml logs -f

logs-api: ## Affiche les logs de l'API uniquement
	@cd setup-api && docker-compose logs -f

logs-db: ## Affiche les logs de MongoDB uniquement
	@cd setup-bd && docker-compose logs -f

status: ## Affiche le statut des conteneurs
	@echo "📊 Statut des services :"
	@docker ps --filter "name=$(PROJECT_NAME)" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

rebuild: ## Rebuild l'image de l'API
	@echo "🔨 Rebuild de l'image API..."
	@cd setup-api && docker-compose build --no-cache
	@echo "✅ Build terminé"

clean: ## Arrête et supprime les volumes (⚠️ supprime les données)
	@echo "🧹 Nettoyage complet..."
	@cd setup-api && docker-compose down -v
	@cd setup-bd && docker-compose down -v
	@echo "✅ Nettoyage terminé"

validate: ## Valide la configuration (.env)
	@echo "🔍 Validation de la configuration..."
	@if [ ! -f setup-bd/.env ]; then \
		echo "❌ setup-bd/.env manquant"; exit 1; \
	fi
	@if [ ! -f setup-api/.env ]; then \
		echo "❌ setup-api/.env manquant"; exit 1; \
	fi
	@BD_PROJECT=$$(grep "^PROJECT_NAME=" setup-bd/.env | cut -d'=' -f2); \
	API_PROJECT=$$(grep "^PROJECT_NAME=" setup-api/.env | cut -d'=' -f2); \
	if [ "$$BD_PROJECT" != "$$API_PROJECT" ]; then \
		echo "❌ PROJECT_NAME différent entre setup-bd et setup-api"; exit 1; \
	fi
	@echo "✅ Configuration valide"

dev: ## Lance l'application en mode développement (Gradle)
	@./gradlew bootRun

test: ## Lance les tests
	@./gradlew test

build: ## Build l'application
	@./gradlew clean build
