# Configuration des fichiers application.properties

Ce projet utilise 3 fichiers de configuration Spring Boot pour différents environnements :

## Fichiers disponibles

### 1. `application.properties` (par défaut)
- **Usage** : Configuration de base avec valeurs par défaut
- **Profil actif par défaut** : `ngrok`
- **Utilisation** : Développement local ou tests
- **Variables d'environnement** : Toutes les valeurs peuvent être surchargées via variables d'env

### 2. `application-ngrok.properties` (profil `ngrok`)
- **Usage** : Tests avec ngrok pour exposer l'API localement
- **⚠️ Important** : Mettre à jour les URLs ngrok à chaque redémarrage de ngrok
- **Activation** : `./gradlew bootRun -Dspring.profiles.active=ngrok`
- **MongoDB** : Utilise la base locale (localhost:27017)

### 3. `application-prod.properties` (profil `prod`)
- **Usage** : Production dans le conteneur Docker
- **Activation** : Automatique via `SPRING_PROFILE=prod` dans Docker
- **Variables d'environnement** : Toutes les valeurs sensibles doivent être définies dans le `.env` Docker
- **MongoDB** : Utilise les variables d'environnement (peut être Atlas ou local)

## Personnalisation minimale

Pour un nouveau projet, modifiez uniquement :

1. **Dans `application.properties`** :
   - `spring.application.name` : Nom de votre application
   - Les valeurs par défaut des URLs si nécessaire

2. **Dans `application-ngrok.properties`** :
   - `app.base-url` : Votre URL ngrok API
   - `app.frontend-url` : Votre URL ngrok frontend
   - `app.allowed-origins` : Ajoutez vos URLs ngrok

3. **Dans `application-prod.properties`** :
   - Les valeurs par défaut des URLs (seront surchargées par les variables d'env Docker)

## Structure des dossiers de stockage

Les fichiers sont stockés dans :
- `users/` : Images de profil utilisateur
- `stock/` : Images de produits/articles (e-commerce)

Ces chemins sont configurables via :
- `file.storage.users-path`
- `file.storage.stock-path`

## Commandes utiles

```bash
# Lancer en local (profil ngrok par défaut)
./gradlew bootRun

# Lancer avec profil ngrok explicitement
./gradlew bootRun -Dspring.profiles.active=ngrok

# Lancer avec profil prod (pour tester la config prod)
./gradlew bootRun -Dspring.profiles.active=prod

# Dans Docker, le profil est défini par SPRING_PROFILE dans .env
```

## Variables d'environnement importantes

Toutes les valeurs peuvent être surchargées via variables d'environnement définies dans :
- `setup-api/.env` pour Docker
- Variables système pour le développement local

Les variables principales sont documentées dans `setup-api/env.example`.
