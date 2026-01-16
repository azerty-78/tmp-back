# 🧪 Commandes cURL pour Tester l'API

Guide complet avec toutes les commandes curl pour tester le système d'authentification et d'envoi d'emails.

## 📋 Prérequis

1. **L'API doit être démarrée** : `./gradlew bootRun` ou `docker-compose up -d` dans `setup-api`
2. **Port de l'API** : 8090 (par défaut)
3. **Base URL** : `http://localhost:8090`

---

## 🚀 Tests d'Authentification

### 1. Health Check (Vérifier que l'API fonctionne)

```bash
curl http://localhost:8090/actuator/health
```

**Réponse attendue** :
```json
{
  "status": "UP"
}
```

---

### 2. Inscription (`POST /api/auth/register`)

**Description** : Crée un compte et envoie un code de vérification par email.

```bash
curl -X POST http://localhost:8090/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"testuser\",
    \"email\": \"votre-email@gmail.com\",
    \"password\": \"Test123!\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\"
  }"
```

**Avec PowerShell** (Windows) :
```powershell
curl.exe -X POST http://localhost:8090/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"testuser\",\"email\":\"votre-email@gmail.com\",\"password\":\"Test123!\",\"firstName\":\"Test\",\"lastName\":\"User\"}'
```

**Réponse attendue (201 Created)** :
```json
{
  "success": true,
  "message": "Inscription réussie. Un code de vérification a été envoyé à votre adresse email.",
  "email": "votre-email@gmail.com",
  "emailVerified": false,
  "requestId": "..."
}
```

**⚠️ Important** :
- Allez dans votre boîte Gmail
- Vérifiez les spams si nécessaire
- Vous devriez recevoir un email avec un code à 6 chiffres (ex: `123456`)

---

### 3. Vérification d'email (`POST /api/auth/verify-email`)

**Description** : Vérifie l'email avec le code reçu. Retourne les tokens d'authentification.

```bash
curl -X POST http://localhost:8090/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"votre-email@gmail.com\",
    \"code\": \"123456\"
  }"
```

**Avec PowerShell** :
```powershell
curl.exe -X POST http://localhost:8090/api/auth/verify-email `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"votre-email@gmail.com\",\"code\":\"123456\"}'
```

**⚠️ Remplacez `123456` par le code réel reçu dans Gmail !**

**Réponse attendue (200 OK)** :
```json
{
  "success": true,
  "message": "Email vérifié avec succès",
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "eyJhbGciOiJIUzM4NCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "refreshExpiresIn": 604800,
    "user": {
      "id": "...",
      "username": "testuser",
      "email": "votre-email@gmail.com",
      "emailVerified": true,
      "role": "USER",
      ...
    }
  },
  "requestId": "..."
}
```

**💾 Note** : Sauvegardez le `accessToken` et `refreshToken` pour les tests suivants.

---

### 4. Renvoyer le code de vérification (`POST /api/auth/resend-verification-code`)

**Description** : Renvoie un nouveau code de vérification.

```bash
curl -X POST http://localhost:8090/api/auth/resend-verification-code \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"votre-email@gmail.com\"
  }"
```

**Avec PowerShell** :
```powershell
curl.exe -X POST http://localhost:8090/api/auth/resend-verification-code `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"votre-email@gmail.com\"}'
```

**Réponse attendue (200 OK)** :
```json
{
  "success": true,
  "message": "Un nouveau code de vérification a été envoyé à votre adresse email.",
  "requestId": "..."
}
```

---

### 5. Connexion (`POST /api/auth/login`)

**Description** : Connecte un utilisateur et retourne les tokens. **L'email doit être vérifié.**

```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"emailOrUsername\": \"votre-email@gmail.com\",
    \"password\": \"Test123!\"
  }"
```

**Avec "Se souvenir de moi"** :
```bash
curl -X POST "http://localhost:8090/api/auth/login?rememberMe=true" \
  -H "Content-Type: application/json" \
  -d "{
    \"emailOrUsername\": \"votre-email@gmail.com\",
    \"password\": \"Test123!\"
  }"
```

**Avec PowerShell** :
```powershell
curl.exe -X POST http://localhost:8090/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"emailOrUsername\":\"votre-email@gmail.com\",\"password\":\"Test123!\"}'
```

**Réponse attendue (200 OK)** :
```json
{
  "success": true,
  "message": "Connexion réussie",
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "eyJhbGciOiJIUzM4NCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "refreshExpiresIn": 604800,
    "user": { ... }
  },
  "requestId": "..."
}
```

**Réponse si email non vérifié (401 Unauthorized)** :
```json
{
  "success": false,
  "message": "Votre adresse email n'a pas été vérifiée. Veuillez vérifier votre email ou demander un nouveau code.",
  "errorCode": "AUTHENTICATION_FAILED",
  "requestId": "..."
}
```

---

### 6. Rafraîchissement de token (`POST /api/auth/refresh`)

**Description** : Génère un nouvel access token à partir du refresh token.

```bash
curl -X POST http://localhost:8090/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"VOTRE_REFRESH_TOKEN_ICI\"
  }"
```

**Avec PowerShell** :
```powershell
curl.exe -X POST http://localhost:8090/api/auth/refresh `
  -H "Content-Type: application/json" `
  -d '{\"refreshToken\":\"VOTRE_REFRESH_TOKEN_ICI\"}'
```

**Réponse attendue (200 OK)** :
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "eyJhbGciOiJIUzM4NCJ9...",
    ...
  },
  "requestId": "..."
}
```

---

### 7. Demander réinitialisation de mot de passe (`POST /api/auth/forgot-password`)

**Description** : Envoie un email avec un lien de réinitialisation.

```bash
curl -X POST http://localhost:8090/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"votre-email@gmail.com\"
  }"
```

**Avec PowerShell** :
```powershell
curl.exe -X POST http://localhost:8090/api/auth/forgot-password `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"votre-email@gmail.com\"}'
```

**Réponse attendue (200 OK)** :
```json
{
  "success": true,
  "message": "Si cette adresse email existe, un lien de réinitialisation a été envoyé.",
  "requestId": "..."
}
```

**⚠️ Important** :
- Allez dans votre boîte Gmail
- Ouvrez l'email de réinitialisation
- Le lien contient un token (ex: `http://localhost:3000/reset-password?token=abc123...`)
- Copiez le token pour l'étape suivante

---

### 8. Réinitialiser le mot de passe (`POST /api/auth/reset-password`)

**Description** : Réinitialise le mot de passe avec le token du lien email.

```bash
curl -X POST http://localhost:8090/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"TOKEN_DU_LIEN_DANS_L_EMAIL\",
    \"newPassword\": \"NewPassword123!\"
  }"
```

**Avec PowerShell** :
```powershell
curl.exe -X POST http://localhost:8090/api/auth/reset-password `
  -H "Content-Type: application/json" `
  -d '{\"token\":\"TOKEN_DU_LIEN_DANS_L_EMAIL\",\"newPassword\":\"NewPassword123!\"}'
```

**Réponse attendue (200 OK)** :
```json
{
  "success": true,
  "message": "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.",
  "requestId": "..."
}
```

---

### 9. Déconnexion (`POST /api/auth/logout`)

**Description** : Déconnexion côté client (informatif).

```bash
curl -X POST http://localhost:8090/api/auth/logout
```

**Réponse attendue (200 OK)** :
```json
{
  "success": true,
  "message": "Logout successful. Please remove the token from client side.",
  "requestId": "..."
}
```

---

## 🔄 Workflow de Test Complet

### Test 1 : Inscription → Vérification → Connexion

```bash
# 1. Inscription
curl -X POST http://localhost:8090/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"testuser\",
    \"email\": \"votre-email@gmail.com\",
    \"password\": \"Test123!\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\"
  }"

# 2. Vérifier votre Gmail pour le code

# 3. Vérification d'email (remplacez 123456 par le code réel)
curl -X POST http://localhost:8090/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"votre-email@gmail.com\",
    \"code\": \"123456\"
  }"

# 4. Connexion
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"emailOrUsername\": \"votre-email@gmail.com\",
    \"password\": \"Test123!\"
  }"
```

---

### Test 2 : Réinitialisation de Mot de Passe

```bash
# 1. Demander réinitialisation
curl -X POST http://localhost:8090/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"votre-email@gmail.com\"
  }"

# 2. Vérifier votre Gmail pour le lien avec le token

# 3. Réinitialiser (remplacez TOKEN par le token du lien)
curl -X POST http://localhost:8090/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"TOKEN_DU_LIEN\",
    \"newPassword\": \"NewPassword123!\"
  }"

# 4. Se connecter avec le nouveau mot de passe
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"emailOrUsername\": \"votre-email@gmail.com\",
    \"password\": \"NewPassword123!\"
  }"
```

---

## ⚠️ Gestion des Erreurs

### Erreur : Email déjà utilisé (409 Conflict)

```json
{
  "success": false,
  "message": "Cette adresse email est déjà utilisée. Veuillez utiliser une autre adresse email ou vous connecter.",
  "errorCode": "RESOURCE_ALREADY_EXISTS"
}
```

### Erreur : Code de vérification invalide (401 Unauthorized)

```json
{
  "success": false,
  "message": "Code de vérification invalide ou expiré",
  "errorCode": "AUTHENTICATION_FAILED"
}
```

### Erreur : Email non vérifié (401 Unauthorized)

```json
{
  "success": false,
  "message": "Votre adresse email n'a pas été vérifiée. Veuillez vérifier votre email ou demander un nouveau code.",
  "errorCode": "AUTHENTICATION_FAILED"
}
```

---

## 🔧 Variables à Personnaliser

Dans les commandes ci-dessus, remplacez :
- `votre-email@gmail.com` : Par votre vraie adresse Gmail
- `123456` : Par le code réel reçu dans Gmail
- `TOKEN_DU_LIEN_DANS_L_EMAIL` : Par le token du lien dans l'email de réinitialisation
- `VOTRE_REFRESH_TOKEN_ICI` : Par le refresh token reçu après connexion/vérification

---

## 📝 Script de Test Complet (Bash)

Créez un fichier `test-api.sh` :

```bash
#!/bin/bash

BASE_URL="http://localhost:8090"
EMAIL="votre-email@gmail.com"
USERNAME="testuser"
PASSWORD="Test123!"

echo "1. Health Check..."
curl -s $BASE_URL/actuator/health | jq

echo -e "\n2. Inscription..."
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"email\": \"$EMAIL\",
    \"password\": \"$PASSWORD\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\"
  }")
echo $REGISTER_RESPONSE | jq

echo -e "\n3. Vérifiez votre Gmail pour le code, puis appuyez sur Entrée..."
read

echo -e "\n4. Entrez le code reçu : "
read CODE

echo -e "\n5. Vérification d'email..."
VERIFY_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$EMAIL\",
    \"code\": \"$CODE\"
  }")
echo $VERIFY_RESPONSE | jq

echo -e "\n6. Connexion..."
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"emailOrUsername\": \"$EMAIL\",
    \"password\": \"$PASSWORD\"
  }")
echo $LOGIN_RESPONSE | jq

echo -e "\n✅ Tests terminés !"
```

---

## 📝 Script de Test Complet (PowerShell)

Créez un fichier `test-api.ps1` :

```powershell
$BASE_URL = "http://localhost:8090"
$EMAIL = "votre-email@gmail.com"
$USERNAME = "testuser"
$PASSWORD = "Test123!"

Write-Host "1. Health Check..." -ForegroundColor Cyan
curl.exe -s $BASE_URL/actuator/health | ConvertFrom-Json | ConvertTo-Json

Write-Host "`n2. Inscription..." -ForegroundColor Cyan
$registerBody = @{
    username = $USERNAME
    email = $EMAIL
    password = $PASSWORD
    firstName = "Test"
    lastName = "User"
} | ConvertTo-Json

$registerResponse = curl.exe -s -X POST "$BASE_URL/api/auth/register" `
    -H "Content-Type: application/json" `
    -d $registerBody
$registerResponse | ConvertFrom-Json | ConvertTo-Json

Write-Host "`n3. Vérifiez votre Gmail pour le code, puis appuyez sur Entrée..." -ForegroundColor Yellow
Read-Host

Write-Host "`n4. Entrez le code reçu : " -ForegroundColor Cyan
$CODE = Read-Host

Write-Host "`n5. Vérification d'email..." -ForegroundColor Cyan
$verifyBody = @{
    email = $EMAIL
    code = $CODE
} | ConvertTo-Json

$verifyResponse = curl.exe -s -X POST "$BASE_URL/api/auth/verify-email" `
    -H "Content-Type: application/json" `
    -d $verifyBody
$verifyResponse | ConvertFrom-Json | ConvertTo-Json

Write-Host "`n6. Connexion..." -ForegroundColor Cyan
$loginBody = @{
    emailOrUsername = $EMAIL
    password = $PASSWORD
} | ConvertTo-Json

$loginResponse = curl.exe -s -X POST "$BASE_URL/api/auth/login" `
    -H "Content-Type: application/json" `
    -d $loginBody
$loginResponse | ConvertFrom-Json | ConvertTo-Json

Write-Host "`n✅ Tests terminés !" -ForegroundColor Green
```

---

## 🎯 Checklist de Test

- [ ] Health check fonctionne
- [ ] Inscription réussie
- [ ] Email reçu dans Gmail avec code
- [ ] Vérification d'email réussie
- [ ] Tokens reçus après vérification
- [ ] Connexion réussie (après vérification)
- [ ] Réinitialisation de mot de passe fonctionne
- [ ] Email de réinitialisation reçu
- [ ] Nouveau mot de passe fonctionne

---

**Note** : Si vous utilisez **MailHog** au lieu de Gmail, remplacez "vérifier Gmail" par "voir http://localhost:8025" dans les instructions.
