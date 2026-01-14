# 🚀 Template Spring Boot - API Backend

> **Template générique et réutilisable** pour créer rapidement des APIs Spring Boot avec MongoDB, JWT, et Docker pour vos nouveaux clients.

## 📋 Table des matières

- [Présentation](#présentation)
- [Fonctionnalités](#fonctionnalités)
- [Prérequis](#prérequis)
- [Installation et Configuration](#installation-et-configuration)
- [Démarrage](#démarrage)
- [Architecture](#architecture)
- [Structure du Projet](#structure-du-projet)
- [Configuration](#configuration)
- [Utilisation](#utilisation)
- [Documentation API](#documentation-api)
- [Développement](#développement)
- [Production](#production)

---

## 🎯 Présentation

Ce template est conçu pour **accélérer le démarrage de nouveaux projets clients**. Il fournit une base solide avec :

- ✅ **Spring Boot 4.0** avec **Kotlin 2.2**
- ✅ **WebFlux Reactive** (non-bloquant)
- ✅ **MongoDB** (Reactive)
- ✅ **Spring Security** avec **JWT**
- ✅ **Docker & Docker Compose** (prêt pour la production)
- ✅ **Gestion des fichiers** (images users/stock)
- ✅ **4 rôles utilisateurs** : USER, EMPLOYE, ADMIN, ROOT_ADMIN
- ✅ **Refresh Token** avec gestion de session
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
  - `USER` : Utilisateur public
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

## 🚀 Installation et Configuration

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

### 3. Configuration manuelle

Si vous préférez configurer manuellement :

#### a) Configuration Base de Données (`setup-bd/.env`)

```bash
cd setup-bd
# Le fichier .env devrait déjà exister, sinon créez-le
```

Modifiez uniquement :
- `PROJECT_NAME=project-name` → Votre nom de projet
- `MONGO_DATABASE=project-name` → Votre nom de base de données

#### b) Configuration API (`setup-api/.env`)

```bash
cd setup-api
```

Modifiez :
- `PROJECT_NAME` : **Même nom que dans setup-bd**
- `SPRING_DATA_MONGODB_URI` : Remplacez `project-name` par votre `PROJECT_NAME` (2 fois : conteneur et base)
- `APP_BASE_URL` : URL de votre API
- `APP_FRONTEND_URL` : URL de votre frontend
- `ALLOWED_ORIGINS` : Domaines autorisés pour CORS

#### c) Configuration ROOT_ADMIN (`src/main/resources/application.properties`)

Les valeurs par défaut sont déjà configurées. Pour les modifier :

```properties
admin.email=${ADMIN_EMAIL:bendjibril789@gmail.com}
admin.password=${ADMIN_PASSWORD:Root@dmin789!}
admin.username=${ADMIN_USERNAME:azerty-78}
admin.firstname=${ADMIN_FIRSTNAME:Ben}
admin.lastname=${ADMIN_LASTNAME:Djibril}
```

Ou via variables d'environnement dans `setup-api/.env` :
```env
ADMIN_EMAIL=bendjibril789@gmail.com
ADMIN_PASSWORD=Root@dmin789!
ADMIN_USERNAME=azerty-78
ADMIN_FIRSTNAME=Ben
ADMIN_LASTNAME=Djibril
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

# Vérifier les logs
docker-compose -f setup-bd/docker-compose.yaml logs -f
docker-compose -f setup-api/docker-compose.yaml logs -f

# Tester l'API
curl http://localhost:8090/actuator/health
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
│   ├── logic/                         # Couche logique métier
│   │   ├── model/users/              # Modèles (User, Role, Gender, etc.)
│   │   ├── repository/users/         # Repositories MongoDB (Reactive)
│   │   └── service/users/            # Services métier (AuthService)
│   │
│   ├── interaction/                   # Couche d'interaction
│   │   ├── dto/users/                # DTOs (Request/Response)
│   │   ├── mapper/users/             # Mappers Entity ↔ DTO
│   │   └── exception/                # Exceptions personnalisées
│   │
│   ├── controller/users/              # Controllers REST
│   │
│   ├── configuration/                 # Configuration Spring
│   │   ├── security/                 # Security, JWT
│   │   └── fileStorage/               # Configuration stockage fichiers
│   │
│   └── util/                          # Utilitaires
│
├── src/main/resources/
│   ├── application.properties         # Configuration par défaut
│   ├── application-ngrok.properties  # Configuration ngrok
│   └── application-prod.properties    # Configuration production
│
├── setup-bd/                          # Configuration MongoDB Docker
│   ├── docker-compose.yaml
│   └── .env
│
├── setup-api/                         # Configuration API Docker
│   ├── docker-compose.yaml
│   ├── Dockerfile
│   └── .env
│
├── scripts/                          # Scripts d'automatisation
│   ├── init-project.sh / .ps1         # Initialisation projet
│   ├── start.sh / .ps1                # Démarrage services
│   └── stop.sh / .ps1                 # Arrêt services
│
├── Makefile                           # Commandes simplifiées
├── build.gradle.kts                   # Dépendances Gradle
└── README.md                          # Ce fichier
```

Pour plus de détails, consultez [`PROJECT-STRUCTURE.md`](./PROJECT-STRUCTURE.md).

---

## ⚙️ Configuration

### Variables d'Environnement Principales

#### Base de Données (`setup-bd/.env`)

```env
PROJECT_NAME=project-name              # Nom du projet
MONGO_DATABASE=project-name            # Nom de la base de données
MONGO_ROOT_USERNAME=root               # Utilisateur MongoDB
MONGO_ROOT_PASSWORD=qwerty87            # Mot de passe MongoDB
MONGO_PORT=27017                        # Port MongoDB
```

#### API (`setup-api/.env`)

```env
# Projet
PROJECT_NAME=project-name
DOCKERHUB_USERNAME=your-username

# URLs
APP_BASE_URL=http://localhost:8090
APP_FRONTEND_URL=http://localhost:3000
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080

# MongoDB
SPRING_DATA_MONGODB_URI=mongodb://root:qwerty87@project-name-mongodb:27017/project-name?authSource=admin

# JWT
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production-min-256-bits
JWT_ACCESS_TOKEN_EXPIRATION=3600000      # 1 heure
JWT_REFRESH_TOKEN_EXPIRATION=604800000   # 7 jours

# Admin (ROOT_ADMIN)
ADMIN_EMAIL=bendjibril789@gmail.com
ADMIN_PASSWORD=Root@dmin789!
ADMIN_USERNAME=azerty-78
ADMIN_FIRSTNAME=Ben
ADMIN_LASTNAME=Djibril
```

### Profils Spring Boot

- **`default`** : Configuration locale (port 8090)
- **`ngrok`** : Configuration pour tests avec ngrok
- **`prod`** : Configuration production (Docker)

Pour utiliser un profil :

```bash
./gradlew bootRun -Dspring.profiles.active=ngrok
```

---

## 🎮 Utilisation

### Endpoints API Principaux

#### Authentification

```bash
# Inscription
POST /api/auth/register
Content-Type: application/json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}

# Connexion
POST /api/auth/login
Content-Type: application/json
{
  "emailOrUsername": "john@example.com",
  "password": "SecurePass123!",
  "rememberMe": false
}

# Rafraîchissement de token
POST /api/auth/refresh
Content-Type: application/json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

# Déconnexion
POST /api/auth/logout
Authorization: Bearer <access_token>
```

### Connexion ROOT_ADMIN

Au premier démarrage, un compte ROOT_ADMIN est créé automatiquement avec les identifiants configurés dans `application.properties` :

- **Email** : `bendjibril789@gmail.com`
- **Password** : `Root@dmin789!`
- **Username** : `azerty-78`

### Accès aux Fichiers

Les fichiers uploadés sont accessibles via :

```
GET /uploads/users/<filename>      # Images de profil
GET /uploads/stock/<filename>      # Images de produits
```

---

## 📚 Documentation API

Une fois l'application démarrée, accédez à la documentation Swagger :

```
http://localhost:8090/swagger-ui.html
```

Ou l'API OpenAPI JSON :

```
http://localhost:8090/v3/api-docs
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

## 📖 Documentation Complémentaire

- [`SETUP-TEMPLATE.md`](./SETUP-TEMPLATE.md) : Guide de personnalisation détaillé
- [`PROJECT-STRUCTURE.md`](./PROJECT-STRUCTURE.md) : Structure du projet
- [`setup-bd/README.md`](./setup-bd/README.md) : Configuration MongoDB
- [`setup-api/README.md`](./setup-api/README.md) : Configuration API

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

---

## 📝 Notes Importantes

- ⚠️ **Ne commitez jamais** les fichiers `.env` (déjà dans `.gitignore`)
- ⚠️ **Changez les secrets** en production (JWT_SECRET, passwords)
- ⚠️ **Le ROOT_ADMIN** est créé uniquement si l'email n'existe pas déjà
- ✅ **Le réseau Docker** est créé automatiquement par Docker Compose
- ✅ **Les dossiers** `users/` et `stock/` sont créés automatiquement

---

## 🤝 Support

Pour toute question ou problème, consultez la documentation dans les dossiers `setup-*/README.md` ou les fichiers markdown à la racine.

---

## 📄 Licence

Template interne - KOBE CORPORATION

---

**Dernière mise à jour** : Janvier 2025
