# Scripts d'automatisation

Ce dossier contient des scripts pour automatiser les tâches courantes du template.

## Scripts disponibles

### Initialisation

#### `init-project.sh` / `init-project.ps1`
Initialise automatiquement un nouveau projet en :
- Demandant les informations nécessaires (PROJECT_NAME, MONGO_DATABASE, URLs)
- Créant les fichiers `.env` à partir des `env.example`
- Remplaçant automatiquement toutes les valeurs
- Générant l'URI MongoDB automatiquement
- Validant la cohérence de la configuration

**Usage :**
```bash
# Linux/Mac
./scripts/init-project.sh

# Windows PowerShell
.\scripts\init-project.ps1
```

### Démarrage

#### `start.sh` / `start.ps1`
Démarre tous les services dans le bon ordre :
- Vérifie que les `.env` existent
- Démarre MongoDB en premier
- Attend que MongoDB soit healthy
- Démarre l'API
- Affiche le statut des services

**Usage :**
```bash
# Linux/Mac
./scripts/start.sh

# Windows PowerShell
.\scripts\start.ps1
```

## Makefile

Un `Makefile` est disponible à la racine du projet avec des commandes courantes :

```bash
make help      # Affiche toutes les commandes disponibles
make init      # Initialise le projet
make start     # Démarre tous les services
make stop      # Arrête tous les services
make restart   # Redémarre tous les services
make logs      # Affiche les logs de tous les services
make logs-api  # Logs de l'API uniquement
make logs-db   # Logs de MongoDB uniquement
make status    # Statut des conteneurs
make rebuild   # Rebuild l'image API
make clean     # Nettoie tout (supprime les volumes)
make validate  # Valide la configuration
make dev       # Lance en mode développement (Gradle)
make test      # Lance les tests
make build     # Build l'application
```

## Workflow recommandé

### Pour un nouveau projet :

1. **Initialiser le projet :**
   ```bash
   make init
   # ou
   ./scripts/init-project.sh
   ```

2. **Vérifier la configuration :**
   ```bash
   make validate
   ```

3. **Démarrer les services :**
   ```bash
   make start
   # ou
   ./scripts/start.sh
   ```

4. **Voir les logs :**
   ```bash
   make logs
   ```

### Pour le développement quotidien :

```bash
make start    # Démarrer
make logs     # Voir les logs
make stop     # Arrêter
make restart  # Redémarrer
```

## Notes

- Les scripts sont compatibles Linux/Mac (`.sh`) et Windows (`.ps1`)
- Le Makefile fonctionne sur Linux/Mac (installer `make` si nécessaire)
- Sur Windows, utilisez les scripts PowerShell directement ou installez `make` via Chocolatey
