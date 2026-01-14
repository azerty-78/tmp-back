# Setup API - Configuration Docker

Ce dossier contient la configuration Docker pour l'API du projet.

## Fichiers

- `docker-compose.yaml` : Configuration Docker Compose pour l'API
- `Dockerfile` : Image Docker pour l'application Spring Boot
- `.env.example` : Exemple de fichier de configuration d'environnement

## Personnalisation rapide

1. **Copiez le fichier `env.example` vers `.env`** :
   ```bash
   cp env.example .env
   ```

2. **Modifiez uniquement ces valeurs dans `.env`** :
   - `PROJECT_NAME` : Nom de votre projet (identique à celui dans setup-bd)
   - `SPRING_DATA_MONGODB_URI` : Remplacez dans l'URI :
     - `project-name-mongodb` → `{PROJECT_NAME}-mongodb` (nom du conteneur)
     - `project-name` (avant `?authSource`) → votre `MONGO_DATABASE` (nom de la base)
   - `APP_BASE_URL` : URL de votre API (ex: `https://api.mondomaine.com`)
   - `APP_FRONTEND_URL` : URL de votre frontend (ex: `https://mondomaine.com`)
   - `ALLOWED_ORIGINS` : Ajoutez vos domaines autorisés
   - `DOCKERHUB_USERNAME` : Votre nom d'utilisateur Docker Hub (optionnel)

3. **Assurez-vous que le réseau Docker existe** :
   Le réseau `${PROJECT_NAME}-network` doit être créé par `setup-bd` avant de démarrer l'API.

## Dossiers obligatoires

L'application crée automatiquement les dossiers suivants dans le conteneur :
- `/app/uploads/users` : Pour les images de profil utilisateur
- `/app/uploads/stock` : Pour les images de produits/articles (e-commerce)

## Démarrage

```bash
# Démarrer l'API
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Arrêter l'API
docker-compose down
```

## Notes

- L'API nécessite que MongoDB soit démarré au préalable (via `setup-bd`)
- Le réseau Docker doit être créé par `setup-bd` avant de démarrer l'API
- Les volumes `api-uploads` et `api-logs` sont persistants
