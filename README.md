# 🚀 Template Spring Boot - API Backend

> **Template générique et réutilisable** pour créer rapidement des APIs Spring Boot avec MongoDB, JWT, et Docker pour vos nouveaux clients.

## 📋 Table des matières

- [Présentation](#présentation)
- [Fonctionnalités](#fonctionnalités)
- [Prérequis](#prérequis)
- [Installation Rapide](#installation-rapide)
- [Configuration](#configuration)
- [Démarrage](#démarrage)
- [Tests de l'API](#tests-de-lapi)
- [Architecture](#architecture)
- [Structure du Projet](#structure-du-projet)
- [Développement](#développement)
- [Production](#production)
- [Dépannage](#dépannage)

---

## 🎯 Présentation

Ce template est conçu pour **accélérer le démarrage de nouveaux projets clients**. Il fournit une base solide avec :

- ✅ **Spring Boot 4.0** avec **Kotlin 2.2**
- ✅ **WebFlux Reactive** (non-bloquant)
- ✅ **MongoDB** (Reactive) avec authentification
- ✅ **Spring Security** avec **JWT** (Access + Refresh Token)
- ✅ **Docker & Docker Compose** (prêt pour la production)
- ✅ **Gestion des fichiers** (images users/stock)
- ✅ **4 rôles utilisateurs** : USER, EMPLOYE, ADMIN, ROOT_ADMIN
- ✅ **CORS configuré**
- ✅ **Swagger/OpenAPI** intégré

---

## ✨ Fonctionnalités

### 🔐 Authentification & Sécurité

- **Inscription** (`POST /api/auth/register`)
- **Connexion** (`POST /api/auth/login`) avec support `rememberMe`
- **Rafraîchissement de token** (`POST /api/auth/refresh`)
- **Déconnexion** (`POST /api/auth/logout`)
- **JWT** avec Access Token (1h) et Refresh Token (7j ou 30j)
- **Refresh Token par utilisateur** (stocké en base, renouvelé toutes les heures)

### 👥 Gestion des Utilisateurs

- **4 rôles hiérarchiques** :
  - `USER` : Utilisateur public (accès sans authentification)
  - `EMPLOYE` : Employé (accès interface de management)
  - `ADMIN` : Administrateur (gestion des employés et contenu)
  - `ROOT_ADMIN` : Root Admin (accès complet système)
- **Compte ROOT_ADMIN** créé automatiquement au démarrage
- **Verrouillage de compte** après tentatives échouées
- **Vérification d'email** (champ `isEmailVerified`)

### 📁 Stockage de Fichiers

- **Dossiers obligatoires** :
  - `/uploads/users/` : Images de profil utilisateur
  - `/uploads/stock/` : Images de produits/articles (e-commerce)
- **Serving statique** : Fichiers accessibles via `/uploads/**`
- **Validation des types** : JPEG, PNG, GIF, WebP

---

## 📦 Prérequis

### Obligatoires

- **Java 21** (JDK)
- **Docker** et **Docker Compose**
- **Git**

### Optionnels (pour le développement local)

- **Gradle** (ou utilisez `./gradlew`)
- **MongoDB Compass** (pour visualiser la base de données)
- **Postman** ou **Insomnia** (pour tester l'API)

---

## 🚀 Installation Rapide

### 1. Cloner le projet

```bash
git clone <url-du-repo>
cd tmp-back
```

### 2. Configuration automatique (Recommandé) ⚡

Utilisez les scripts d'initialisation qui configurent tout automatiquement :

```bash
# Linux/Mac
./scripts/init-project.sh

# Windows PowerShell
.\scripts\init-project.ps1
```

Le script vous demandera :
- `PROJECT_NAME` : Nom de votre projet (ex: `mon-client-api`)
- `MONGO_DATABASE` : Nom de votre base de données (ex: `mon-client-db`)
- `APP_BASE_URL` : URL de votre API (ex: `http://localhost:8090`)
- `APP_FRONTEND_URL` : URL de votre frontend (ex: `http://localhost:3000`)

---

## ⚙️ Configuration

### Variables à Personnaliser (Minimum)

#### Base de Données (`setup-bd/.env`)

```env
PROJECT_NAME=project-name              # ⚠️ À modifier
MONGO_DATABASE=project-name            # ⚠️ À modifier
MONGO_ROOT_USERNAME=root               # Déjà configuré
MONGO_ROOT_PASSWORD=qwerty87           # Déjà configuré
MONGO_PORT=27017                       # Déjà configuré
```

#### API (`setup-api/.env`)

```env
# ⚠️ À modifier
PROJECT_NAME=project-name
APP_BASE_URL=http://localhost:8090
APP_FRONTEND_URL=http://localhost:3000
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,http://localhost:5174

# ⚠️ À modifier : Remplacez "project-name" par votre PROJECT_NAME (2 fois)
SPRING_DATA_MONGODB_URI=mongodb://root:qwerty87@project-name-mongodb:27017/project-name?authSource=admin

# Optionnel (valeurs par défaut fonctionnelles)
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production-min-256-bits
ADMIN_EMAIL=bendjibril789@gmail.com
ADMIN_PASSWORD=Root@dmin789!
ADMIN_USERNAME=azerty-78
```

**⚠️ Important** : `PROJECT_NAME` doit être **identique** dans `setup-bd/.env` et `setup-api/.env`

### Profils Spring Boot

- **`default`** : Configuration locale (port 8090)
- **`ngrok`** : Configuration pour tests avec ngrok
- **`prod`** : Configuration production (Docker)

Pour utiliser un profil :

```bash
./gradlew bootRun -Dspring.profiles.active=ngrok
```

---

## 🏃 Démarrage

### Méthode 1 : Scripts automatisés (Recommandé)

```bash
# Démarrage complet (MongoDB + API)
make start

# Ou avec les scripts
./scripts/start.sh        # Linux/Mac
.\scripts\start.ps1       # Windows PowerShell
```

### Méthode 2 : Docker Compose manuel

```bash
# 1. Démarrer MongoDB (crée le réseau Docker automatiquement)
cd setup-bd
docker-compose up -d

# 2. Attendre que MongoDB soit prêt (environ 10-20 secondes)
docker-compose ps

# 3. Démarrer l'API
cd ../setup-api
docker-compose up -d
```

### Méthode 3 : Développement local (sans Docker)

```bash
# 1. Démarrer MongoDB (via Docker ou local)
cd setup-bd
docker-compose up -d

# 2. Lancer l'application Spring Boot
./gradlew bootRun

# Ou avec un profil spécifique
./gradlew bootRun -Dspring.profiles.active=ngrok
```

### Vérification

Une fois démarré, vérifiez que tout fonctionne :

```bash
# Vérifier les conteneurs
docker ps

# Tester l'API
curl http://localhost:8090/actuator/health
```

**Réponse attendue** :
```json
{
  "status": "UP",
  "components": {
    "mongo": {
      "status": "UP",
      "details": {
        "databases": ["admin", "project-name", "config", "local"]
      }
    }
  }
}
```

---

## 🧪 Tests de l'API

### 1. Health Check

```bash
curl http://localhost:8090/actuator/health
```

### 2. Inscription

```bash
curl -X POST http://localhost:8090/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Réponse attendue** :
```json
{
  "success": true,
  "message": "Inscription réussie",
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "eyJhbGciOiJIUzM4NCJ9...",
    "expiresIn": 3600,
    "refreshExpiresIn": 604800,
    "user": {
      "id": "...",
      "username": "testuser",
      "email": "test@example.com",
      "role": "USER"
    }
  }
}
```

### 3. Connexion

```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "test@example.com",
    "password": "Test123!"
  }'
```

### 4. Connexion ROOT_ADMIN

Au premier démarrage, un compte ROOT_ADMIN est créé automatiquement :

```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "bendjibril789@gmail.com",
    "password": "Root@dmin789!"
  }'
```

**Identifiants par défaut** :
- **Email** : `bendjibril789@gmail.com`
- **Password** : `Root@dmin789!`
- **Username** : `azerty-78`

### 5. Rafraîchissement de Token

```bash
curl -X POST http://localhost:8090/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "VOTRE_REFRESH_TOKEN_ICI"
  }'
```

### 6. Utiliser un Token pour une Route Protégée

```bash
curl -X GET http://localhost:8090/api/users/me \
  -H "Authorization: Bearer VOTRE_ACCESS_TOKEN_ICI"
```

### Documentation Swagger

Une fois l'application démarrée, accédez à :

```
http://localhost:8090/swagger-ui.html
```

---

## 🏗️ Architecture

### Stack Technique

- **Backend** : Spring Boot 4.0 + Kotlin 2.2
- **Framework Web** : Spring WebFlux (Reactive)
- **Base de données** : MongoDB (Reactive)
- **Sécurité** : Spring Security + JWT
- **Containerisation** : Docker + Docker Compose
- **Build** : Gradle (Kotlin DSL)

### Architecture en Couches

```
┌─────────────────────────────────────┐
│         Controller Layer             │  ← REST Endpoints
│    (AuthController, UserController)  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Interaction Layer               │  ← DTOs, Mappers, Exceptions
│  (DTOs, Mappers, Validation)        │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Logic Layer                  │  ← Business Logic
│  (Services, Repositories, Models)    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         MongoDB (Reactive)          │  ← Database
└─────────────────────────────────────┘
```

### Flux de Données (Reactive)

```
Request → Controller → Service (Mono/Flux) → Repository → MongoDB
         ↓
    Response (Mono/Flux)
```

---

## 📁 Structure du Projet

```
tmp-back/
├── src/main/kotlin/com/kobecorporation/tmp_back/
│   ├── TmpBackApplication.kt          # Point d'entrée
│   │
│   ├── logic/                          # Couche logique métier
│   │   ├── model/users/               # Modèles (User, Role, Gender)
│   │   ├── repository/users/          # Repositories MongoDB (Reactive)
│   │   └── service/users/             # Services métier (AuthService)
│   │
│   ├── interaction/                    # Couche d'interaction
│   │   ├── dto/users/                 # DTOs (Request/Response)
│   │   ├── mapper/users/              # Mappers Entity ↔ DTO
│   │   └── exception/                 # Exceptions personnalisées
│   │
│   ├── controller/users/               # Controllers REST
│   │
│   ├── configuration/                  # Configuration Spring
│   │   ├── security/                  # Security, JWT
│   │   ├── fileStorage/                # Configuration stockage fichiers
│   │   └── MongoConfig.kt              # Configuration MongoDB
│   │
│   └── util/                           # Utilitaires
│
├── src/main/resources/
│   ├── application.properties          # Configuration par défaut
│   ├── application-ngrok.properties    # Configuration ngrok
│   └── application-prod.properties     # Configuration production
│
├── setup-bd/                           # Configuration MongoDB Docker
│   ├── docker-compose.yaml
│   ├── .env
│   └── init-scripts/
│       └── 01-init-database.js         # Script d'initialisation DB
│
├── setup-api/                          # Configuration API Docker
│   ├── docker-compose.yaml
│   ├── Dockerfile
│   └── .env
│
├── scripts/                            # Scripts d'automatisation
│   ├── init-project.sh / .ps1         # Initialisation projet
│   ├── start.sh / .ps1                 # Démarrage services
│   └── stop.sh / .ps1                  # Arrêt services
│
├── Makefile                            # Commandes simplifiées
├── build.gradle.kts                    # Dépendances Gradle
└── README.md                           # Ce fichier
```

---

## 💻 Développement

### Commandes Utiles

```bash
# Démarrer les services
make start

# Arrêter les services
make stop

# Voir les logs
make logs

# Rebuild l'API
make build

# Initialiser le projet
make init

# Voir le statut
make status
```

### Développement Local

```bash
# Lancer en mode développement
./gradlew bootRun

# Lancer avec un profil spécifique
./gradlew bootRun -Dspring.profiles.active=ngrok

# Build le JAR
./gradlew bootJar

# Tests
./gradlew test
```

### Connexion MongoDB

```bash
# Via MongoDB Compass
mongodb://root:qwerty87@localhost:27017/?authSource=admin

# Via mongosh
docker exec -it project-name-mongodb mongosh -u root -p qwerty87 --authenticationDatabase admin
```

---

## 🚀 Production

### Build et Déploiement

```bash
# 1. Build l'image Docker
cd setup-api
docker build -t your-username/project-name-api:latest .

# 2. Push vers Docker Hub (optionnel)
docker push your-username/project-name-api:latest

# 3. Déployer avec Docker Compose
docker-compose up -d
```

### Checklist Production

- [ ] Changer `JWT_SECRET` (minimum 256 bits)
- [ ] Configurer `APP_BASE_URL` et `APP_FRONTEND_URL`
- [ ] Configurer `ALLOWED_ORIGINS` (CORS)
- [ ] Changer les identifiants MongoDB
- [ ] Configurer les identifiants ROOT_ADMIN
- [ ] Activer HTTPS
- [ ] Configurer les logs (niveau INFO/WARN)
- [ ] Configurer les backups MongoDB
- [ ] Limiter l'accès au port MongoDB (firewall)

---

## 🐛 Dépannage

### Le réseau Docker n'existe pas

```bash
# Le réseau est créé automatiquement par Docker Compose
# Si erreur, supprimez et recréez :
docker network rm project-name-network
cd setup-bd
docker-compose up -d
```

### MongoDB ne démarre pas

```bash
# Vérifier les logs
docker-compose -f setup-bd/docker-compose.yaml logs

# Vérifier les ports
docker ps | grep mongodb
```

### L'API ne peut pas se connecter à MongoDB

Vérifiez que :
1. MongoDB est démarré et healthy
2. Le `SPRING_DATA_MONGODB_URI` dans `setup-api/.env` est correct
3. Le nom du conteneur correspond à `PROJECT_NAME-mongodb`
4. Les credentials sont corrects (`root:qwerty87`)

### Erreur : "Command find requires authentication"

**Solution** : Vérifiez que l'URI MongoDB contient les credentials :

```properties
spring.data.mongodb.uri=mongodb://root:qwerty87@localhost:27017/project-name?authSource=admin
```

### Erreur : "Connection refused"

**Solution** : Vérifiez que MongoDB est démarré :

```bash
docker ps | grep mongodb
```

### La base de données n'existe pas

La base de données est créée automatiquement au premier démarrage via le script `setup-bd/init-scripts/01-init-database.js`.

Si elle n'existe pas, connectez-vous manuellement :

```bash
docker exec -it project-name-mongodb mongosh -u root -p qwerty87 --authenticationDatabase admin
```

Puis dans mongosh :
```javascript
use project-name
db.createCollection("_init")
```

---

## 📝 Notes Importantes

- ⚠️ **Ne commitez jamais** les fichiers `.env` (déjà dans `.gitignore`)
- ⚠️ **Changez les secrets** en production (JWT_SECRET, passwords)
- ⚠️ **Le ROOT_ADMIN** est créé uniquement si l'email n'existe pas déjà
- ✅ **Le réseau Docker** est créé automatiquement par Docker Compose
- ✅ **Les dossiers** `users/` et `stock/` sont créés automatiquement
- ✅ **La base de données** est créée automatiquement au premier démarrage

---

## 📄 Licence

Template interne - KOBE CORPORATION

---

**Dernière mise à jour** : Janvier 2025
