# 🚀 Guide de Démarrage Rapide

Ce guide vous explique comment lancer et tester rapidement le projet avec toutes ses fonctionnalités.

## 📋 Prérequis

- Docker et Docker Compose installés
- Ports disponibles : 8090 (API), 27017 (MongoDB), 1025 (SMTP test), 8025 (MailHog UI)

## 🏁 Démarrage Rapide

### Étape 1 : Initialisation du projet

Si c'est la première fois que vous lancez le projet :

```bash
# Windows PowerShell
.\scripts\init-project.ps1

# Linux/Mac
./scripts/init-project.sh
```

Le script vous demandera :
- Nom du projet
- Configuration SMTP (test ou production)
- Paramètres de l'application

### Étape 2 : Démarrer MailHog (Serveur SMTP de test)

**⚠️ Important** : MailHog doit être démarré AVANT l'API pour tester l'envoi d'emails.

```bash
cd setup-smtp
docker-compose up -d
```

Vérifiez que MailHog est démarré :
```bash
docker ps | grep mailhog
```

L'interface web sera disponible sur : **http://localhost:8025**

### Étape 3 : Démarrer MongoDB

```bash
cd setup-bd
docker-compose up -d
```

Attendez que MongoDB soit "healthy" (environ 10-20 secondes) :
```bash
docker-compose ps
```

### Étape 4 : Démarrer l'API

**Option A : Avec Docker (Production-like)**
```bash
cd setup-api
docker-compose up -d
```

**Option B : En mode développement local**
```bash
# Depuis la racine du projet
./gradlew bootRun
```

### Étape 5 : Vérifier que tout fonctionne

```bash
# Tester l'API
curl http://localhost:8090/actuator/health

# Réponse attendue :
# {"status":"UP"}
```

## 🧪 Tester le système complet

### 1. Test d'inscription avec vérification d'email

#### a) Inscription
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

#### b) Vérifier l'email dans MailHog
1. Ouvrez votre navigateur
2. Allez sur **http://localhost:8025**
3. Vous devriez voir l'email avec un code à 6 chiffres

#### c) Vérifier l'email avec le code
```bash
curl -X POST http://localhost:8090/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "code": "123456"
  }'
```

**Remplacez `123456` par le code réel reçu dans MailHog.**

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

### 2. Test de connexion

```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "test@example.com",
    "password": "Test123!"
  }'
```

**⚠️ Note** : La connexion ne fonctionnera que si l'email a été vérifié.

### 3. Test de réinitialisation de mot de passe

#### a) Demander une réinitialisation
```bash
curl -X POST http://localhost:8090/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

#### b) Vérifier l'email dans MailHog
- Allez sur http://localhost:8025
- Ouvrez l'email de réinitialisation
- Copiez le token du lien (ou l'URL complète)

#### c) Réinitialiser le mot de passe
```bash
curl -X POST http://localhost:8090/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "TOKEN_DU_LIEN_DANS_L_EMAIL",
    "newPassword": "NewPassword123!"
  }'
```

## 📊 Commandes Utiles

### Voir le statut des services
```bash
docker ps --filter "name=project-name"
```

### Voir les logs
```bash
# Logs MongoDB
docker-compose -f setup-bd/docker-compose.yaml logs -f

# Logs API
docker-compose -f setup-api/docker-compose.yaml logs -f

# Logs MailHog
docker-compose -f setup-smtp/docker-compose.yaml logs -f
```

### Arrêter les services
```bash
# Windows PowerShell
.\scripts\stop.ps1

# Linux/Mac
./scripts/stop.sh

# Ou manuellement
cd setup-api && docker-compose down
cd ../setup-bd && docker-compose down
cd ../setup-smtp && docker-compose down
```

### Redémarrer un service
```bash
cd setup-api
docker-compose restart
```

## 🔍 Dépannage

### MailHog ne démarre pas
```bash
# Vérifier que le port n'est pas utilisé
netstat -ano | findstr :8025  # Windows
lsof -i :8025                  # Linux/Mac

# Redémarrer MailHog
cd setup-smtp
docker-compose restart
```

### L'API ne peut pas envoyer d'emails
1. Vérifiez que MailHog est démarré : `docker ps | grep mailhog`
2. Vérifiez que l'API utilise bien `localhost:1025` dans `setup-api/.env`
3. Vérifiez les logs de l'API pour les erreurs SMTP

### Les emails n'apparaissent pas dans MailHog
1. Vérifiez que MailHog est accessible : http://localhost:8025
2. Vérifiez les logs de MailHog : `docker-compose -f setup-smtp/docker-compose.yaml logs`
3. Vérifiez les logs de l'API pour voir si l'email a été envoyé

### L'inscription fonctionne mais pas la connexion
- Vérifiez que l'email a été vérifié (isEmailVerified = true)
- Vérifiez que vous utilisez le bon email/mot de passe
- Vérifiez les logs de l'API pour les erreurs d'authentification

## 📝 Endpoints Disponibles

Voir le fichier **`GUIDE-FRONTEND.md`** pour la documentation complète des endpoints et des interfaces à créer.

## 🔗 URLs Importantes

- **API** : http://localhost:8090
- **Swagger/OpenAPI** : http://localhost:8090/swagger-ui.html
- **Health Check** : http://localhost:8090/actuator/health
- **MailHog (Emails de test)** : http://localhost:8025
- **MongoDB** : localhost:27017

## ✅ Checklist de Démarrage

- [ ] Docker et Docker Compose installés
- [ ] Projet initialisé avec `init-project.ps1` ou `init-project.sh`
- [ ] MailHog démarré (port 8025 accessible)
- [ ] MongoDB démarré et healthy
- [ ] API démarrée et répond sur /actuator/health
- [ ] Test d'inscription réussi
- [ ] Email visible dans MailHog
- [ ] Vérification d'email réussie
- [ ] Connexion réussie

---

**Prêt pour le développement !** 🎉

Voir **`GUIDE-FRONTEND.md`** pour la documentation complète des interfaces à créer.
