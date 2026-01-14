# Template de Configuration - Guide de Personnalisation

Ce projet est un template générique pour rapidement configurer un nouveau projet client avec une API Spring Boot et MongoDB.

## Structure

```
.
├── setup-api/          # Configuration Docker pour l'API
│   ├── docker-compose.yaml
│   ├── Dockerfile
│   ├── env.example
│   └── README.md
└── setup-bd/           # Configuration Docker pour MongoDB
    ├── docker-compose.yaml
    ├── env.example
    └── README.md
```

## Personnalisation Rapide (2 méthodes)

### Méthode 1 : Automatique (Recommandé) ⚡

Utilisez les scripts d'initialisation :

```bash
# Linux/Mac
./scripts/init-project.sh

# Windows PowerShell
.\scripts\init-project.ps1

# Puis démarrez
make start
# ou
./scripts/start.sh
```

Le script vous demandera toutes les informations nécessaires et configurera tout automatiquement !

### Méthode 2 : Manuelle

#### 1. Configuration Base de Données

Dans `setup-bd/` :

```bash
cd setup-bd
cp env.example .env
```

**Modifiez uniquement** :
- `PROJECT_NAME` : Nom de votre projet (ex: `mon-client-api`)
- `MONGO_DATABASE` : Nom de votre base de données (ex: `mon-client-db`)

#### 2. Configuration API

Dans `setup-api/` :

```bash
cd setup-api
cp env.example .env
```

**Modifiez uniquement** :
- `PROJECT_NAME` : **Même nom que dans setup-bd** (remplacez dans `SPRING_DATA_MONGODB_URI` pour le nom du conteneur)
- `SPRING_DATA_MONGODB_URI` : Remplacez aussi le nom de la base de données par votre `MONGO_DATABASE`
- `APP_BASE_URL` : URL de votre API (ex: `https://api.mondomaine.com` ou ngrok)
- `APP_FRONTEND_URL` : URL de votre frontend (ex: `https://mondomaine.com`)
- `ALLOWED_ORIGINS` : Ajoutez vos domaines autorisés

#### 3. Démarrage

```bash
# 1. Démarrer MongoDB (crée le réseau Docker)
cd setup-bd
docker-compose up -d

# 2. Démarrer l'API (utilise le réseau créé)
cd ../setup-api
docker-compose up -d
```

## Dossiers Obligatoires

L'application crée automatiquement dans le conteneur :
- `/app/uploads/users` : Images de profil utilisateur
- `/app/uploads/stock` : Images de produits/articles (e-commerce)

Ces dossiers sont montés via des volumes Docker persistants.

## Variables à Personnaliser (Minimum)

### Obligatoires
- `PROJECT_NAME` : Utilisé pour nommer les conteneurs, volumes et réseaux (dans setup-bd et setup-api)
- `MONGO_DATABASE` : Nom de votre base de données MongoDB
- `SPRING_DATA_MONGODB_URI` : URI MongoDB (remplacer PROJECT_NAME et MONGO_DATABASE)
- `APP_BASE_URL` : URL de votre API
- `APP_FRONTEND_URL` : URL de votre frontend
- `ALLOWED_ORIGINS` : Domaines autorisés pour CORS

### Optionnelles (valeurs par défaut fonctionnelles)
- `DOCKERHUB_USERNAME` : Pour pousser l'image Docker
- Toutes les autres variables ont des valeurs par défaut opérationnelles

## Notes Importantes

1. **PROJECT_NAME** : Doit être identique dans `setup-bd/.env` et `setup-api/.env`
2. **Ordre de démarrage** : Toujours démarrer `setup-bd` avant `setup-api`
3. **Réseau Docker** : Créé automatiquement par `setup-bd`, utilisé par `setup-api`
4. **Sécurité** : Changez tous les mots de passe par défaut en production !

## Commandes Utiles

### Avec Makefile (Recommandé)

```bash
make help      # Affiche toutes les commandes
make start     # Démarre tous les services
make stop      # Arrête tous les services
make logs      # Voir les logs
make status    # Statut des conteneurs
make rebuild   # Rebuild l'image API
make clean     # Nettoie tout (⚠️ supprime les données)
make validate  # Valide la configuration
```

### Commandes Docker manuelles

```bash
# Voir les logs
docker-compose -f setup-bd/docker-compose.yaml -f setup-api/docker-compose.yaml logs -f

# Arrêter les services
cd setup-api && docker-compose down
cd ../setup-bd && docker-compose down

# Arrêter et supprimer les volumes (⚠️ supprime les données)
cd setup-api && docker-compose down -v
cd ../setup-bd && docker-compose down -v

# Rebuild l'image API
cd setup-api && docker-compose build
```

## Support

Consultez les README.md dans chaque dossier pour plus de détails.
