# Setup BD - Configuration MongoDB

Ce dossier contient la configuration Docker pour la base de données MongoDB.

## Fichiers

- `docker-compose.yaml` : Configuration Docker Compose pour MongoDB
- `.env.example` : Exemple de fichier de configuration d'environnement

## Personnalisation rapide

1. **Copiez le fichier `env.example` vers `.env`** :
   ```bash
   cp env.example .env
   ```

2. **Modifiez ces valeurs dans `.env`** :
   - `PROJECT_NAME` : Nom de votre projet (utilisé pour les noms de conteneurs, volumes, réseaux)
   - `MONGO_DATABASE` : Nom de votre base de données MongoDB
   - Toutes les autres valeurs sont déjà configurées par défaut

## Scripts d'initialisation

Vous pouvez ajouter des scripts d'initialisation dans le dossier `init-scripts/`. 
Ces scripts seront exécutés automatiquement au premier démarrage de MongoDB.

## Démarrage

```bash
# Démarrer MongoDB
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Arrêter MongoDB
docker-compose down

# Arrêter et supprimer les volumes (⚠️ supprime toutes les données)
docker-compose down -v
```

## Connexion

### Via MongoDB Compass

```
mongodb://root:qwerty87@localhost:27017/?authSource=admin
```

### Via mongosh

```bash
docker exec -it project-name-mongodb mongosh -u root -p qwerty87 --authenticationDatabase admin
```

## Notes

- Le réseau Docker `${PROJECT_NAME}-network` est créé automatiquement
- Les volumes `mongodb-data` et `mongodb-config` sont persistants
- MongoDB est configuré avec l'authentification activée (`--auth`)
- ⚠️ En production, limitez l'accès au port MongoDB avec un firewall
