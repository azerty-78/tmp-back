# 🎨 Guide Frontend - Interfaces et Intégration API

Ce document décrit toutes les interfaces à créer, les endpoints disponibles, et les workflows complets pour intégrer le frontend avec l'API backend.

## 📋 Table des matières

- [Configuration de base](#configuration-de-base)
- [Endpoints d'authentification](#endpoints-dauthentification)
- [Interfaces à créer](#interfaces-à-créer)
- [Workflows complets](#workflows-complets)
- [Gestion des tokens](#gestion-des-tokens)
- [Gestion des erreurs](#gestion-des-erreurs)

---

## 🔧 Configuration de base

### Base URL de l'API

```
http://localhost:8090
```

### Headers requis

Pour toutes les requêtes authentifiées :
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

### Format de réponse standard

Toutes les réponses suivent ce format :

```typescript
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  errorCode?: string;
  requestId?: string;
}
```

---

## 🔐 Endpoints d'authentification

### 1. Inscription (`POST /api/auth/register`)

**Description** : Crée un nouveau compte utilisateur. Un code de vérification est envoyé par email.

**Request Body** :
```typescript
interface RegisterRequest {
  username: string;        // 3-50 caractères, lettres/chiffres/underscores uniquement
  email: string;           // Email valide
  password: string;        // 8-100 caractères
  firstName: string;       // 2-50 caractères
  lastName: string;        // 2-50 caractères
  birthDate?: string;      // Format: YYYY-MM-DD (optionnel)
  gender?: "MALE" | "FEMALE" | "OTHER" | null; // (optionnel)
}
```

**Response (201 Created)** :
```typescript
interface RegisterResponse {
  success: true;
  message: "Inscription réussie. Un code de vérification a été envoyé à votre adresse email.";
  email: string;
  emailVerified: false;
  requestId: string;
}
```

**Response (409 Conflict)** :
```typescript
{
  success: false;
  message: "Cette adresse email est déjà utilisée..." | "Ce nom d'utilisateur est déjà pris...";
  errorCode: "RESOURCE_ALREADY_EXISTS";
  requestId: string;
}
```

---

### 2. Vérification d'email (`POST /api/auth/verify-email`)

**Description** : Vérifie l'email avec le code reçu. Retourne les tokens d'authentification si le code est valide.

**Request Body** :
```typescript
interface VerifyEmailRequest {
  email: string;     // L'email utilisé lors de l'inscription
  code: string;      // Code à 6 chiffres reçu par email
}
```

**Response (200 OK)** :
```typescript
interface VerifyEmailResponse {
  success: true;
  message: "Email vérifié avec succès";
  data: AuthResponse;
  requestId: string;
}

interface AuthResponse {
  accessToken: string;      // Token JWT à utiliser pour les requêtes authentifiées
  refreshToken: string;     // Token pour rafraîchir l'accessToken
  tokenType: "Bearer";
  expiresIn: number;        // Durée de vie en secondes (3600 = 1h)
  refreshExpiresIn: number; // Durée de vie du refresh token en secondes
  user: UserResponse;
}

interface UserResponse {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  birthDate?: string;       // Format: YYYY-MM-DD
  gender?: "MALE" | "FEMALE" | "OTHER" | null;
  role: "USER" | "EMPLOYE" | "ADMIN" | "ROOT_ADMIN";
  isActive: boolean;
  isEmailVerified: boolean;
  profilePicture?: string;
  bio?: string;
  website?: string;
  socialLinks: SocialLinks;
  createdAt: string;        // ISO 8601
  updatedAt: string;        // ISO 8601
  lastLoginAt?: string;     // ISO 8601
}
```

**Response (401 Unauthorized)** :
```typescript
{
  success: false;
  message: "Code de vérification invalide ou expiré";
  errorCode: "AUTHENTICATION_FAILED";
  requestId: string;
}
```

---

### 3. Renvoyer le code de vérification (`POST /api/auth/resend-verification-code`)

**Description** : Renvoie un nouveau code de vérification à l'adresse email.

**Request Body** :
```typescript
interface ResendVerificationCodeRequest {
  email: string;
}
```

**Response (200 OK)** :
```typescript
{
  success: true;
  message: "Un nouveau code de vérification a été envoyé à votre adresse email.";
  requestId: string;
}
```

---

### 4. Connexion (`POST /api/auth/login`)

**Description** : Connecte un utilisateur et retourne les tokens d'authentification.

**Request Body** :
```typescript
interface LoginRequest {
  emailOrUsername: string;  // Email ou nom d'utilisateur
  password: string;
}

// Query Parameter (optionnel)
rememberMe?: boolean;       // Si true, refresh token valide 30 jours au lieu de 7 jours
```

**URL avec paramètre** :
```
POST /api/auth/login?rememberMe=true
```

**Response (200 OK)** :
```typescript
{
  success: true;
  message: "Connexion réussie";
  data: AuthResponse;  // Voir AuthResponse ci-dessus
  requestId: string;
}
```

**Response (401 Unauthorized)** :
```typescript
{
  success: false;
  message: "Identifiants invalides" | "Votre adresse email n'a pas été vérifiée..." | "Votre compte est temporairement verrouillé...";
  errorCode: "AUTHENTICATION_FAILED";
  requestId: string;
}
```

---

### 5. Rafraîchissement de token (`POST /api/auth/refresh`)

**Description** : Génère un nouvel access token à partir du refresh token.

**Request Body** :
```typescript
interface RefreshTokenRequest {
  refreshToken: string;
}
```

**Response (200 OK)** :
```typescript
{
  success: true;
  message: "Token refreshed successfully";
  data: AuthResponse;
  requestId: string;
}
```

**Response (401 Unauthorized)** :
```typescript
{
  success: false;
  message: "Invalid refresh token" | "Refresh token expired";
  errorCode: "AUTHENTICATION_FAILED";
  requestId: string;
}
```

---

### 6. Demander réinitialisation de mot de passe (`POST /api/auth/forgot-password`)

**Description** : Envoie un email avec un lien de réinitialisation de mot de passe.

**Request Body** :
```typescript
interface ForgotPasswordRequest {
  email: string;
}
```

**Response (200 OK)** :
```typescript
{
  success: true;
  message: "Si cette adresse email existe, un lien de réinitialisation a été envoyé.";
  requestId: string;
}
```

**⚠️ Note** : Pour des raisons de sécurité, le message est toujours le même, même si l'email n'existe pas.

---

### 7. Réinitialiser le mot de passe (`POST /api/auth/reset-password`)

**Description** : Réinitialise le mot de passe avec le token reçu par email.

**Request Body** :
```typescript
interface ResetPasswordRequest {
  token: string;          // Token du lien dans l'email
  newPassword: string;    // 8-100 caractères
}
```

**Response (200 OK)** :
```typescript
{
  success: true;
  message: "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.";
  requestId: string;
}
```

**Response (401 Unauthorized)** :
```typescript
{
  success: false;
  message: "Token de réinitialisation invalide ou expiré";
  errorCode: "AUTHENTICATION_FAILED";
  requestId: string;
}
```

---

### 8. Déconnexion (`POST /api/auth/logout`)

**Description** : Déconnexion côté client (le token doit être supprimé côté frontend).

**Response (200 OK)** :
```typescript
{
  success: true;
  message: "Logout successful. Please remove the token from client side.";
  requestId: string;
}
```

**⚠️ Note** : Cette endpoint est principalement informatif. Le logout réel consiste à supprimer les tokens côté client.

---

## 🎨 Interfaces à créer

### 1. Page d'inscription (`/register`)

**Éléments à afficher** :
- Formulaire avec les champs :
  - Nom d'utilisateur (requis, validation en temps réel)
  - Email (requis, validation format email)
  - Mot de passe (requis, minimum 8 caractères, avec indicateur de force)
  - Confirmation du mot de passe (requis, doit correspondre)
  - Prénom (requis)
  - Nom (requis)
  - Date de naissance (optionnel, date picker)
  - Genre (optionnel, dropdown/radio)
- Bouton "S'inscrire"
- Lien vers la page de connexion

**Workflow** :
1. Utilisateur remplit le formulaire
2. Validation côté client
3. Envoi de la requête `POST /api/auth/register`
4. Si succès : Redirection vers la page de vérification d'email
5. Si erreur : Affichage du message d'erreur

**Messages d'erreur à gérer** :
- Email déjà utilisé
- Nom d'utilisateur déjà pris
- Erreurs de validation (champs manquants, format invalide)

---

### 2. Page de vérification d'email (`/verify-email`)

**Éléments à afficher** :
- Message : "Un code de vérification a été envoyé à {email}"
- Champ pour saisir le code à 6 chiffres (format: 123456)
- Bouton "Vérifier"
- Lien "Renvoyer le code"
- Compte à rebours (15 minutes) pour l'expiration du code
- Message d'erreur si le code est invalide

**Workflow** :
1. Afficher le formulaire avec l'email (passé en paramètre ou depuis le state)
2. Utilisateur saisit le code
3. Envoi de la requête `POST /api/auth/verify-email`
4. Si succès :
   - Stocker les tokens (accessToken, refreshToken) dans le localStorage/sessionStorage
   - Stocker les informations utilisateur
   - Redirection vers la page d'accueil ou dashboard
5. Si erreur : Afficher le message d'erreur

**Fonctionnalité "Renvoyer le code"** :
- Envoi de `POST /api/auth/resend-verification-code`
- Réinitialiser le compte à rebours
- Afficher un message de confirmation

---

### 3. Page de connexion (`/login`)

**Éléments à afficher** :
- Formulaire avec :
  - Email ou nom d'utilisateur
  - Mot de passe
  - Case à cocher "Se souvenir de moi" (rememberMe)
- Bouton "Se connecter"
- Lien "Mot de passe oublié ?"
- Lien vers la page d'inscription

**Workflow** :
1. Utilisateur saisit ses identifiants
2. Envoi de la requête `POST /api/auth/login?rememberMe={true/false}`
3. Si succès :
   - Stocker les tokens
   - Stocker les informations utilisateur
   - Redirection vers la page d'accueil ou dashboard
4. Si erreur : Afficher le message d'erreur

**Messages d'erreur à gérer** :
- Identifiants invalides
- Email non vérifié (avec lien pour renvoyer le code)
- Compte verrouillé
- Compte désactivé

---

### 4. Page mot de passe oublié (`/forgot-password`)

**Éléments à afficher** :
- Formulaire avec :
  - Champ email
- Bouton "Envoyer le lien de réinitialisation"
- Message informatif expliquant que si l'email existe, un lien sera envoyé
- Lien retour vers la page de connexion

**Workflow** :
1. Utilisateur saisit son email
2. Envoi de la requête `POST /api/auth/forgot-password`
3. Afficher un message de confirmation (même si l'email n'existe pas, pour la sécurité)
4. Optionnel : Afficher un compte à rebours avant de pouvoir renvoyer une nouvelle demande

---

### 5. Page réinitialisation de mot de passe (`/reset-password`)

**Éléments à afficher** :
- Formulaire avec :
  - Champ nouveau mot de passe (avec indicateur de force)
  - Champ confirmation du nouveau mot de passe
- Champ caché pour le token (extrait de l'URL query parameter : `?token=...`)
- Bouton "Réinitialiser le mot de passe"

**Workflow** :
1. Vérifier que le token est présent dans l'URL
2. Si pas de token : Redirection vers `/forgot-password` avec message d'erreur
3. Utilisateur saisit le nouveau mot de passe
4. Validation que les deux mots de passe correspondent
5. Envoi de la requête `POST /api/auth/reset-password`
6. Si succès : Redirection vers `/login` avec message de confirmation
7. Si erreur : Afficher le message d'erreur (token invalide/expiré)

**URL attendue** :
```
/reset-password?token={token_du_lien_email}
```

---

### 6. Page de profil utilisateur (Authentifiée)

**Éléments à afficher** :
- Informations utilisateur :
  - Photo de profil
  - Nom d'utilisateur
  - Email (avec badge "Vérifié" si `isEmailVerified = true`)
  - Nom complet
  - Date de naissance
  - Genre
  - Rôle
- Bouton "Modifier le profil"
- Bouton "Changer le mot de passe"
- Bouton "Déconnexion"

**Token requis** : Oui, `Authorization: Bearer {accessToken}`

---

## 🔄 Workflows complets

### Workflow d'inscription complet

```
1. Page /register
   └─> Formulaire d'inscription
       └─> POST /api/auth/register
           └─> Succès: Redirection vers /verify-email?email={email}
           └─> Erreur: Afficher message d'erreur

2. Page /verify-email
   └─> Saisie du code
       └─> POST /api/auth/verify-email
           └─> Succès: Stocker tokens + Redirection vers /dashboard
           └─> Erreur: Afficher message + Option "Renvoyer le code"
               └─> POST /api/auth/resend-verification-code
```

### Workflow de connexion

```
1. Page /login
   └─> Saisie identifiants
       └─> POST /api/auth/login?rememberMe={true/false}
           └─> Succès: Stocker tokens + Redirection vers /dashboard
           └─> Erreur "Email non vérifié": Redirection vers /verify-email
           └─> Autre erreur: Afficher message
```

### Workflow réinitialisation de mot de passe

```
1. Page /forgot-password
   └─> Saisie email
       └─> POST /api/auth/forgot-password
           └─> Afficher message de confirmation

2. Utilisateur clique sur le lien dans l'email
   └─> Redirection vers /reset-password?token={token}

3. Page /reset-password
   └─> Saisie nouveau mot de passe
       └─> POST /api/auth/reset-password
           └─> Succès: Redirection vers /login
           └─> Erreur: Afficher message (token invalide/expiré)
```

---

## 🔑 Gestion des tokens

### Stockage des tokens

**Recommandation** : Utiliser `localStorage` ou `sessionStorage`

```typescript
// Stocker les tokens après connexion/vérification
localStorage.setItem('accessToken', authResponse.data.accessToken);
localStorage.setItem('refreshToken', authResponse.data.refreshToken);
localStorage.setItem('user', JSON.stringify(authResponse.data.user));

// Récupérer les tokens
const accessToken = localStorage.getItem('accessToken');
const refreshToken = localStorage.getItem('refreshToken');
```

### Ajout du token aux requêtes

```typescript
// Exemple avec fetch
fetch('http://localhost:8090/api/users/me', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
    'Content-Type': 'application/json'
  }
});
```

### Rafraîchissement automatique du token

**Stratégie recommandée** :
1. Intercepter toutes les réponses 401 (Unauthorized)
2. Si le refreshToken existe :
   - Appeler `POST /api/auth/refresh`
   - Récupérer le nouvel accessToken
   - Réessayer la requête originale avec le nouveau token
3. Si le refreshToken n'existe pas ou est invalide :
   - Rediriger vers `/login`

**Exemple avec Axios interceptor** :
```typescript
// Intercepteur pour rafraîchir le token automatiquement
axios.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const response = await axios.post('/api/auth/refresh', {
            refreshToken
          });
          const newAccessToken = response.data.data.accessToken;
          localStorage.setItem('accessToken', newAccessToken);
          // Réessayer la requête originale
          error.config.headers.Authorization = `Bearer ${newAccessToken}`;
          return axios.request(error.config);
        } catch (refreshError) {
          // Refresh token invalide, rediriger vers login
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);
```

### Déconnexion

```typescript
// Supprimer les tokens et rediriger
localStorage.removeItem('accessToken');
localStorage.removeItem('refreshToken');
localStorage.removeItem('user');
// Optionnel : Appeler POST /api/auth/logout
window.location.href = '/login';
```

---

## ⚠️ Gestion des erreurs

### Codes d'erreur possibles

```typescript
type ErrorCode = 
  | "RESOURCE_ALREADY_EXISTS"    // Email/username déjà utilisé
  | "AUTHENTICATION_FAILED"      // Identifiants invalides, token invalide
  | "RESOURCE_NOT_FOUND"         // Ressource non trouvée
  | "BAD_REQUEST"                // Données invalides
  | "INTERNAL_ERROR";            // Erreur serveur
```

### Messages d'erreur à afficher

| Code | Message utilisateur |
|------|---------------------|
| `RESOURCE_ALREADY_EXISTS` | "Cette adresse email est déjà utilisée" / "Ce nom d'utilisateur est déjà pris" |
| `AUTHENTICATION_FAILED` | Afficher le message exact de l'API (ex: "Identifiants invalides", "Email non vérifié") |
| `RESOURCE_NOT_FOUND` | "Compte non trouvé" |
| `BAD_REQUEST` | "Données invalides" + afficher les erreurs de validation |
| `INTERNAL_ERROR` | "Une erreur est survenue. Veuillez réessayer plus tard." |

---

## 📝 Validations côté client

### Validation du formulaire d'inscription

```typescript
const validateRegister = (data: RegisterRequest): string[] => {
  const errors: string[] = [];
  
  if (!data.username || data.username.length < 3 || data.username.length > 50) {
    errors.push("Le nom d'utilisateur doit contenir entre 3 et 50 caractères");
  }
  if (!/^[a-zA-Z0-9_]+$/.test(data.username)) {
    errors.push("Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscores");
  }
  
  if (!data.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) {
    errors.push("L'email doit être valide");
  }
  
  if (!data.password || data.password.length < 8 || data.password.length > 100) {
    errors.push("Le mot de passe doit contenir entre 8 et 100 caractères");
  }
  
  if (!data.firstName || data.firstName.length < 2 || data.firstName.length > 50) {
    errors.push("Le prénom doit contenir entre 2 et 50 caractères");
  }
  
  if (!data.lastName || data.lastName.length < 2 || data.lastName.length > 50) {
    errors.push("Le nom doit contenir entre 2 et 50 caractères");
  }
  
  return errors;
};
```

### Validation du code de vérification

```typescript
const validateVerificationCode = (code: string): boolean => {
  return /^[0-9]{6}$/.test(code);
};
```

---

## 🎯 Checklist des interfaces

- [ ] Page d'inscription (`/register`)
- [ ] Page de vérification d'email (`/verify-email`)
- [ ] Page de connexion (`/login`)
- [ ] Page mot de passe oublié (`/forgot-password`)
- [ ] Page réinitialisation de mot de passe (`/reset-password?token=...`)
- [ ] Page de profil utilisateur (authentifiée)
- [ ] Gestion des tokens (stockage, rafraîchissement automatique)
- [ ] Intercepteur HTTP pour ajouter le token
- [ ] Gestion des erreurs API
- [ ] Validation des formulaires côté client
- [ ] Messages d'erreur conviviaux
- [ ] Loading states (pendant les requêtes)
- [ ] Redirections appropriées

---

## 📚 Ressources supplémentaires

- **Swagger UI** : http://localhost:8090/swagger-ui.html (quand l'API est démarrée)
- **Base URL API** : http://localhost:8090
- **Documentation backend** : Voir `README.md` et `GUIDE-DEMARRAGE.md`

---

**Dernière mise à jour** : Janvier 2025
