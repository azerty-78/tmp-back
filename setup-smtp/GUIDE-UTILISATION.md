# Guide d'utilisation - Système d'envoi d'emails

## 📋 Vue d'ensemble

Le système d'authentification inclut maintenant :
1. **Vérification d'email** lors de l'inscription
2. **Réinitialisation de mot de passe** par email

## 🚀 Démarrage du serveur SMTP de test (MailHog)

### En mode développement/test

```bash
cd setup-smtp
docker-compose up -d
```

Le serveur MailHog sera accessible :
- **Interface Web** : http://localhost:8025 (pour visualiser les emails)
- **Serveur SMTP** : localhost:1025 (pour l'application)

### Configuration automatique

Le fichier `application.properties` est déjà configuré pour utiliser MailHog en mode développement :
```properties
spring.mail.host=localhost
spring.mail.port=1025
```

## 📧 Workflow de vérification d'email

### 1. Inscription
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Réponse :**
```json
{
  "success": true,
  "message": "Inscription réussie. Un code de vérification a été envoyé à votre adresse email.",
  "email": "john@example.com",
  "emailVerified": false
}
```

➡️ Un email avec un code à 6 chiffres est envoyé automatiquement.

### 2. Vérification de l'email

#### Option A : Vérifier avec le code reçu
```http
POST /api/auth/verify-email
Content-Type: application/json

{
  "email": "john@example.com",
  "code": "123456"
}
```

**Réponse (succès) :**
```json
{
  "success": true,
  "message": "Email vérifié avec succès",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "user": {...}
  }
}
```

#### Option B : Renvoyer le code
```http
POST /api/auth/resend-verification-code
Content-Type: application/json

{
  "email": "john@example.com"
}
```

### 3. Connexion (après vérification)
```http
POST /api/auth/login
Content-Type: application/json

{
  "emailOrUsername": "john@example.com",
  "password": "SecurePass123!"
}
```

⚠️ **Important** : La connexion ne fonctionne que si l'email a été vérifié.

## 🔑 Workflow de réinitialisation de mot de passe

### 1. Demander une réinitialisation
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "john@example.com"
}
```

**Réponse :**
```json
{
  "success": true,
  "message": "Si cette adresse email existe, un lien de réinitialisation a été envoyé."
}
```

➡️ Un email avec un lien de réinitialisation est envoyé.

### 2. Réinitialiser le mot de passe
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "token-du-lien-dans-l-email",
  "newPassword": "NewSecurePass123!"
}
```

**Réponse :**
```json
{
  "success": true,
  "message": "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter."
}
```

## 🧪 Tester en mode développement

### 1. Démarrer MailHog
```bash
cd setup-smtp
docker-compose up -d
```

### 2. Démarrer l'application
```bash
./gradlew bootRun
```

### 3. Tester l'inscription
Utilisez Postman ou curl pour créer un compte. L'email sera capturé par MailHog.

### 4. Visualiser l'email
Ouvrez http://localhost:8025 dans votre navigateur pour voir :
- Le code de vérification (pour l'inscription)
- Le lien de réinitialisation (pour le mot de passe)

## 🔧 Configuration en production

### Variables d'environnement requises

Dans votre fichier `.env` ou variables d'environnement Docker :

```bash
# SMTP Configuration
MAIL_HOST=smtp.lws.fr
MAIL_PORT=587
MAIL_USERNAME=votre-email@votre-domaine.com
MAIL_PASSWORD=votre-mot-de-passe
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

# Email de l'application
EMAIL_FROM_ADDRESS=noreply@votre-domaine.com
EMAIL_FROM_NAME=KOBE Corporation
EMAIL_FRONTEND_URL=https://votre-domaine.com
```

### Configuration LWS

Si vous utilisez LWS email pro, les paramètres SMTP sont généralement :
- **Host** : `smtp.lws.fr`
- **Port** : `587` (avec STARTTLS) ou `465` (SSL)
- **Authentification** : Oui
- **Username** : Votre adresse email complète
- **Password** : Le mot de passe de votre compte email

## 📝 Notes importantes

1. **Expiration des codes** :
   - Code de vérification : 15 minutes (configurable)
   - Token de réinitialisation : 30 minutes (configurable)

2. **Sécurité** :
   - Pour des raisons de sécurité, le message de "forgot password" ne révèle pas si l'email existe
   - Les codes sont générés de manière sécurisée
   - Les tokens de réinitialisation sont uniques et utilisables une seule fois

3. **Email vérifié requis** :
   - Les utilisateurs ne peuvent pas se connecter tant que leur email n'est pas vérifié
   - La réinitialisation de mot de passe nécessite un email vérifié

## 🔍 Dépannage

### L'application ne peut pas envoyer d'emails
- Vérifiez que MailHog est démarré : `docker ps | grep mailhog`
- Vérifiez les logs de l'application pour les erreurs SMTP
- Vérifiez que le port 1025 n'est pas déjà utilisé

### Les emails n'apparaissent pas dans MailHog
- Vérifiez que l'application utilise bien `localhost:1025`
- Vérifiez les logs de l'application
- Redémarrez MailHog : `docker-compose restart`

### En production, les emails ne partent pas
- Vérifiez les variables d'environnement
- Testez la connexion SMTP avec un client email externe
- Vérifiez les logs de l'application pour les erreurs d'authentification SMTP
