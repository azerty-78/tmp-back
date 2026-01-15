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
- **Vérification d'email obligatoire** : Code à 6 chiffres envoyé par email lors de l'inscription
- **Réinitialisation de mot de passe** : Lien sécurisé envoyé par email

### 📧 Système d'envoi d'emails

- **Vérification d'email** : Code à 6 chiffres envoyé lors de l'inscription (valide 15 minutes)
- **Réinitialisation de mot de passe** : Token sécurisé envoyé par email (valide 30 minutes)
- **Mode test** : MailHog (capture tous les emails, interface web sur port 8025)
- **Mode production** : Configuration SMTP réelle (LWS, Gmail, etc.)

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

### 📧 Configuration Email / SMTP

#### Comment fonctionne la vérification d'email ?

**Oui, le système vérifie que l'utilisateur possède réellement l'adresse email** en utilisant un système de **code de vérification** :

1. **Lors de l'inscription** :
   - L'utilisateur saisit son email
   - Un code à **6 chiffres** est généré automatiquement
   - Ce code est **envoyé par email** à l'adresse fournie
   - Le code est valide pendant **15 minutes** (configurable)

2. **Vérification** :
   - L'utilisateur doit saisir le code reçu dans sa boîte mail
   - Si le code est correct, l'email est marqué comme vérifié (`isEmailVerified = true`)
   - L'utilisateur peut alors se connecter

3. **Sécurité** :
   - Si l'email n'est pas réel, l'utilisateur ne recevra jamais le code
   - Le code expire après 15 minutes
   - Possibilité de renvoyer un nouveau code si nécessaire

**⚠️ Important** : L'utilisateur **ne peut pas se connecter** tant que son email n'est pas vérifié.

#### Configuration en mode TEST (Développement)

Pour le développement local, utilisez **MailHog** (serveur SMTP de test) :

1. **Démarrer MailHog** :
   ```bash
   cd setup-smtp
   docker-compose up -d
   ```

2. **Configuration dans `setup-api/.env`** (déjà configuré par défaut) :
   ```env
   MAIL_HOST=localhost
   MAIL_PORT=1025
   MAIL_USERNAME=
   MAIL_PASSWORD=
   MAIL_SMTP_AUTH=false
   MAIL_SMTP_STARTTLS=false
   ```

3. **Visualiser les emails** : http://localhost:8025

#### Configuration en mode PRODUCTION (Vrai email)

Quand vous personnalisez le projet pour un client avec un vrai email professionnel :

1. **Mettre à jour `setup-api/.env`** :
   ```env
   # Configuration SMTP (exemple avec LWS email pro)
   MAIL_HOST=smtp.lws.fr
   MAIL_PORT=587
   MAIL_USERNAME=votre-email@votre-domaine.com
   MAIL_PASSWORD=votre-mot-de-passe-email
   MAIL_SMTP_AUTH=true
   MAIL_SMTP_STARTTLS=true
   MAIL_CONNECTION_TIMEOUT=5000
   MAIL_TIMEOUT=5000
   MAIL_WRITE_TIMEOUT=5000
   
   # Email de l'application (adresse expéditrice)
   EMAIL_FROM_ADDRESS=noreply@votre-domaine.com
   EMAIL_FROM_NAME=Nom de votre entreprise
   EMAIL_FRONTEND_URL=https://votre-domaine.com
   ```

2. **Mettre à jour `src/main/resources/application.properties`** (optionnel, pour développement local) :
   ```properties
   # Si vous lancez en local avec ./gradlew bootRun
   spring.mail.host=${MAIL_HOST:localhost}
   spring.mail.port=${MAIL_PORT:1025}
   spring.mail.username=${MAIL_USERNAME:}
   spring.mail.password=${MAIL_PASSWORD:}
   spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH:false}
   spring.mail.properties.mail.smtp.starttls.enable=${MAIL_SMTP_STARTTLS:false}
   ```

3. **Mettre à jour `src/main/resources/application-prod.properties`** (pour production) :
   ```properties
   # Configuration SMTP Production
   spring.mail.host=${MAIL_HOST:smtp.lws.fr}
   spring.mail.port=${MAIL_PORT:587}
   spring.mail.username=${MAIL_USERNAME}
   spring.mail.password=${MAIL_PASSWORD}
   spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH:true}
   spring.mail.properties.mail.smtp.starttls.enable=${MAIL_SMTP_STARTTLS:true}
   
   # Email Application
   app.email.from-address=${EMAIL_FROM_ADDRESS:noreply@votre-domaine.com}
   app.email.from-name=${EMAIL_FROM_NAME:Nom de votre entreprise}
   app.email.frontend-url=${EMAIL_FRONTEND_URL:${app.frontend-url}}
   ```

#### Guide complet de configuration Email

Voir le fichier **`setup-smtp/GUIDE-UTILISATION.md`** pour :
- Configuration détaillée MailHog (test)
- Configuration SMTP production (LWS, Gmail, etc.)
- Dépannage des problèmes d'envoi d'emails

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
  "message": "Inscription réussie. Un code de vérification a été envoyé à votre adresse email.",
  "email": "test@example.com",
  "emailVerified": false
}
```

**⚠️ Important** : L'inscription ne retourne **pas** de tokens. L'utilisateur doit d'abord vérifier son email avec le code reçu.

### 2.1. Vérification d'email

```bash
# Récupérer le code depuis MailHog (http://localhost:8025) ou votre boîte mail
curl -X POST http://localhost:8090/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "code": "123456"
  }'
```

**Réponse attendue** :
```json
{
  "success": true,
  "message": "Email vérifié avec succès",
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "eyJhbGciOiJIUzM4NCJ9...",
    "expiresIn": 3600,
    "refreshExpiresIn": 604800,
    "user": {
      "id": "...",
      "username": "testuser",
      "email": "test@example.com",
      "emailVerified": true,
      "role": "USER"
    }
  }
}
```

### 3. Connexion

**⚠️ Important** : La connexion ne fonctionne que si l'email a été vérifié.

```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "test@example.com",
    "password": "Test123!"
  }'
```

**Si l'email n'est pas vérifié**, vous recevrez :
```json
{
  "success": false,
  "message": "Votre adresse email n'a pas été vérifiée. Veuillez vérifier votre email ou demander un nouveau code.",
  "errorCode": "AUTHENTICATION_FAILED"
}
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

### 7. Réinitialisation de mot de passe

```bash
# 1. Demander une réinitialisation
curl -X POST http://localhost:8090/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'

# 2. Récupérer le token depuis l'email (MailHog ou boîte mail)

# 3. Réinitialiser le mot de passe
curl -X POST http://localhost:8090/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "TOKEN_DU_LIEN_EMAIL",
    "newPassword": "NewPassword123!"
  }'
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
- [ ] **Configurer l'email SMTP de production** (voir section Configuration Email)
- [ ] **Configurer `EMAIL_FROM_ADDRESS`** avec l'adresse email professionnelle
- [ ] **Tester l'envoi d'emails** avant la mise en production
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
- ⚠️ **Vérification d'email obligatoire** : Les utilisateurs ne peuvent pas se connecter tant que leur email n'est pas vérifié
- ⚠️ **Configuration SMTP** : En production, configurez un vrai serveur SMTP (pas MailHog)
- ✅ **Le réseau Docker** est créé automatiquement par Docker Compose
- ✅ **Les dossiers** `users/` et `stock/` sont créés automatiquement
- ✅ **La base de données** est créée automatiquement au premier démarrage
- ✅ **MailHog** : Utilisez-le uniquement en développement/test (jamais en production)

## 🔧 Guide de Personnalisation du Projet

### Mise à jour des fichiers de configuration

Quand vous personnalisez ce template pour un nouveau client, voici les fichiers à modifier :

#### 1. Fichiers `.env` (via script d'initialisation)

Utilisez le script d'initialisation qui configure automatiquement tous les `.env` :

```bash
# Windows PowerShell
.\scripts\init-project.ps1

# Linux/Mac
./scripts/init-project.sh
```

Le script configure :
- `setup-bd/.env` : Configuration MongoDB
- `setup-smtp/.env` : Configuration MailHog
- `setup-api/.env` : Configuration API + SMTP

#### 2. Fichier `application.properties` (développement local)

**Fichier** : `src/main/resources/application.properties`

**À modifier si vous lancez en local avec `./gradlew bootRun`** :

```properties
# Email / SMTP (pour développement local avec MailHog)
spring.mail.host=${MAIL_HOST:localhost}
spring.mail.port=${MAIL_PORT:1025}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH:false}
spring.mail.properties.mail.smtp.starttls.enable=${MAIL_SMTP_STARTTLS:false}

# Email Application
app.email.from-address=${EMAIL_FROM_ADDRESS:noreply@example.com}
app.email.from-name=${EMAIL_FROM_NAME:KOBE Corporation}
```

**⚠️ Note** : Les valeurs par défaut (`:localhost`, `:1025`, etc.) sont utilisées si les variables d'environnement ne sont pas définies. Pour le développement local, vous pouvez laisser ces valeurs par défaut.

#### 3. Fichier `application-prod.properties` (production)

**Fichier** : `src/main/resources/application-prod.properties`

**À modifier pour la production** :

```properties
# Email / SMTP Production
spring.mail.host=${MAIL_HOST:smtp.lws.fr}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH:true}
spring.mail.properties.mail.smtp.starttls.enable=${MAIL_SMTP_STARTTLS:true}

# Email Application Production
app.email.from-address=${EMAIL_FROM_ADDRESS:noreply@votre-domaine.com}
app.email.from-name=${EMAIL_FROM_NAME:Nom de votre entreprise}
```

**⚠️ Important** : Les valeurs sont lues depuis les variables d'environnement définies dans `setup-api/.env` ou `docker-compose.yaml`.

#### 4. Résumé : Quelle configuration utiliser ?

| Environnement | Fichier à modifier | Configuration SMTP |
|---------------|-------------------|-------------------|
| **Développement local** (`./gradlew bootRun`) | `application.properties` | MailHog (localhost:1025) |
| **Docker (test)** | `setup-api/.env` | MailHog (localhost:1025) |
| **Docker (production)** | `setup-api/.env` | Vrai SMTP (smtp.lws.fr, etc.) |
| **Production (JAR)** | Variables d'environnement système | Vrai SMTP |

#### 5. Exemple complet : Configuration pour un client

**Scénario** : Client avec domaine `monclient.com` et email LWS

1. **Exécuter le script d'initialisation** :
   ```bash
   .\scripts\init-project.ps1
   ```
   - Choisir "Configuration SMTP de production"
   - Entrer les paramètres SMTP LWS

2. **Vérifier `setup-api/.env`** :
   ```env
   MAIL_HOST=smtp.lws.fr
   MAIL_PORT=587
   MAIL_USERNAME=contact@monclient.com
   MAIL_PASSWORD=le-mot-de-passe-email
   MAIL_SMTP_AUTH=true
   MAIL_SMTP_STARTTLS=true
   
   EMAIL_FROM_ADDRESS=noreply@monclient.com
   EMAIL_FROM_NAME=Mon Client
   EMAIL_FRONTEND_URL=https://www.monclient.com
   ```

3. **Tester l'envoi d'emails** :
   - Créer un compte de test
   - Vérifier que l'email de vérification arrive bien
   - Vérifier que le code fonctionne

4. **Déployer en production** :
   - Les variables d'environnement dans `setup-api/.env` seront utilisées automatiquement
   - L'application utilisera le vrai serveur SMTP

---

## 📄 Licence

Template interne - KOBE CORPORATION

---

---

## 📚 Documentation Complémentaire

- **`GUIDE-DEMARRAGE.md`** : Guide rapide pour lancer et tester le projet
- **`GUIDE-FRONTEND.md`** : Documentation complète pour intégrer le frontend
- **`setup-smtp/GUIDE-UTILISATION.md`** : Guide détaillé du système d'envoi d'emails

---

**Dernière mise à jour** : Janvier 2025
