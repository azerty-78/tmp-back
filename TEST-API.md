# 🧪 Guide de Test de l'API

## Prérequis

1. **MongoDB doit être démarré** avec authentification :
   ```bash
   cd setup-bd
   docker-compose up -d
   ```

2. **Vérifier que MongoDB est accessible** :
   ```bash
   docker ps | grep mongodb
   ```

3. **L'application doit être démarrée** :
   ```bash
   ./gradlew bootRun
   # ou avec un profil spécifique
   ./gradlew bootRun -Dspring.profiles.active=ngrok
   ```

## ⚠️ Important : Configuration MongoDB

Assurez-vous que votre URI MongoDB dans `application-ngrok.properties` ou `application.properties` contient les credentials :

```properties
spring.data.mongodb.uri=mongodb://root:qwerty87@localhost:27017/project-name?authSource=admin
```

---

## 🔐 Tests d'Authentification

### 1. Inscription (Register)

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
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "refreshExpiresIn": 604800,
    "user": {
      "id": "...",
      "username": "testuser",
      "email": "test@example.com",
      "firstName": "Test",
      "lastName": "User",
      "role": "USER"
    }
  },
  "requestId": "..."
}
```

### 2. Connexion (Login)

```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "test@example.com",
    "password": "Test123!",
    "rememberMe": false
  }'
```

**Avec rememberMe** :
```bash
curl -X POST "http://localhost:8090/api/auth/login?rememberMe=true" \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "test@example.com",
    "password": "Test123!"
  }'
```

**Réponse attendue** :
```json
{
  "success": true,
  "message": "Connexion réussie",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "refreshExpiresIn": 604800,
    "user": {
      "id": "...",
      "username": "testuser",
      "email": "test@example.com",
      "firstName": "Test",
      "lastName": "User",
      "role": "USER"
    }
  },
  "requestId": "..."
}
```

### 3. Connexion ROOT_ADMIN

```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "bendjibril789@gmail.com",
    "password": "Root@dmin789!",
    "rememberMe": false
  }'
```

### 4. Rafraîchissement de Token (Refresh Token)

```bash
# Remplacez YOUR_REFRESH_TOKEN par le refresh token reçu lors du login
curl -X POST http://localhost:8090/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

**Réponse attendue** :
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "refreshExpiresIn": 604800,
    "user": {
      "id": "...",
      "username": "testuser",
      "email": "test@example.com",
      "role": "USER"
    }
  },
  "requestId": "..."
}
```

### 5. Déconnexion (Logout)

```bash
curl -X POST http://localhost:8090/api/auth/logout \
  -H "Content-Type: application/json"
```

---

## 🔍 Tests de Santé (Health Check)

### Health Check

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

## 📚 Documentation API (Swagger)

Une fois l'application démarrée, accédez à :

```
http://localhost:8090/swagger-ui.html
```

Ou l'API OpenAPI JSON :

```bash
curl http://localhost:8090/v3/api-docs
```

---

## 🧪 Scripts de Test Complets

### Script Bash (Linux/Mac)

```bash
#!/bin/bash

BASE_URL="http://localhost:8090"

echo "🧪 Test d'inscription..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User"
  }')

echo "$REGISTER_RESPONSE" | jq '.'

# Extraire le refresh token
REFRESH_TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.data.refreshToken')

echo ""
echo "🧪 Test de connexion..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "test@example.com",
    "password": "Test123!"
  }')

echo "$LOGIN_RESPONSE" | jq '.'

# Extraire l'access token
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken')

echo ""
echo "🧪 Test de rafraîchissement de token..."
curl -s -X POST "$BASE_URL/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}" | jq '.'

echo ""
echo "🧪 Test de health check..."
curl -s "$BASE_URL/actuator/health" | jq '.'
```

### Script PowerShell (Windows)

```powershell
$baseUrl = "http://localhost:8090"

Write-Host "🧪 Test d'inscription..." -ForegroundColor Cyan
$registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" `
  -Method POST `
  -ContentType "application/json" `
  -Body (@{
    username = "testuser"
    email = "test@example.com"
    password = "Test123!"
    firstName = "Test"
    lastName = "User"
  } | ConvertTo-Json)

$registerResponse | ConvertTo-Json -Depth 10

$refreshToken = $registerResponse.data.refreshToken

Write-Host "`n🧪 Test de connexion..." -ForegroundColor Cyan
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body (@{
    emailOrUsername = "test@example.com"
    password = "Test123!"
  } | ConvertTo-Json)

$loginResponse | ConvertTo-Json -Depth 10

$accessToken = $loginResponse.data.accessToken

Write-Host "`n🧪 Test de rafraîchissement de token..." -ForegroundColor Cyan
$refreshResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/refresh" `
  -Method POST `
  -ContentType "application/json" `
  -Body (@{
    refreshToken = $refreshToken
  } | ConvertTo-Json)

$refreshResponse | ConvertTo-Json -Depth 10

Write-Host "`n🧪 Test de health check..." -ForegroundColor Cyan
Invoke-RestMethod -Uri "$baseUrl/actuator/health" | ConvertTo-Json
```

---

## 🔑 Variables d'Environnement pour les Tests

Vous pouvez sauvegarder les tokens dans des variables :

```bash
# Après le login
export ACCESS_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
export REFRESH_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Utiliser dans les requêtes suivantes
curl -X GET http://localhost:8090/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

---

## 📝 Notes

- **Access Token** : Durée de vie de 1 heure (3600 secondes)
- **Refresh Token** : Durée de vie de 7 jours (604800 secondes) ou 30 jours avec `rememberMe`
- **Format des dates** : ISO-8601 (ex: `2025-01-14T17:45:32.123Z`)
- **CORS** : Configuré pour `http://localhost:3000`, `http://localhost:3001`, `http://localhost:5174`

---

## 🐛 Dépannage

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

### Erreur : "Invalid credentials"

**Solution** : Vérifiez que l'utilisateur existe et que le mot de passe est correct.
