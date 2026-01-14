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

## Personnalisation Rapide (3 étapes)

### 1. Configuration Base de Données

Dans `setup-bd/` :

```bash
cd setup-bd
cp env.example .env
```

Modifiez `.env` avec les valeurs de votre projet :
- `PROJECT_NAME` : Nom de votre projet (ex: `mon-client-api`)
- `MONGO_ROOT_PASSWORD` : Mot de passe root MongoDB
- `MONGO_DATABASE` : Nom de la base de données

### 2. Configuration API

Dans `setup-api/` :

```bash
cd setup-api
cp env.example .env
```

Modifiez `.env` avec les valeurs de votre projet :
- `PROJECT_NAME` : **Même nom que dans setup-bd**
- `DOCKERHUB_USERNAME` : Votre nom d'utilisateur Docker Hub
- `SPRING_DATA_MONGODB_URI` : URI MongoDB (ajustez avec le PROJECT_NAME)
- `JWT_SECRET` : Clé secrète JWT (changez-la !)
- Toutes les autres variables selon vos besoins

### 3. Démarrage

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

## Variables Clés à Personnaliser

### Obligatoires
- `PROJECT_NAME` : Utilisé pour nommer les conteneurs, volumes et réseaux
- `MONGO_ROOT_PASSWORD` : Mot de passe MongoDB
- `JWT_SECRET` : Clé secrète JWT

### Recommandées
- `ADMIN_EMAIL`, `ADMIN_PASSWORD` : Compte admin par défaut
- `APP_BASE_URL`, `APP_FRONTEND_URL` : URLs de l'application
- `DOCKERHUB_USERNAME` : Pour pousser l'image Docker

## Notes Importantes

1. **PROJECT_NAME** : Doit être identique dans `setup-bd/.env` et `setup-api/.env`
2. **Ordre de démarrage** : Toujours démarrer `setup-bd` avant `setup-api`
3. **Réseau Docker** : Créé automatiquement par `setup-bd`, utilisé par `setup-api`
4. **Sécurité** : Changez tous les mots de passe par défaut en production !

## Commandes Utiles

```bash
# Voir les logs
docker-compose logs -f

# Arrêter les services
docker-compose down

# Arrêter et supprimer les volumes (⚠️ supprime les données)
docker-compose down -v

# Rebuild l'image API
cd setup-api
docker-compose build
```

## Support

Consultez les README.md dans chaque dossier pour plus de détails.
