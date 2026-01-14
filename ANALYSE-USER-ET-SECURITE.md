# Analyse du Modèle User et Recommandations Sécurité

## 📊 Analyse du Modèle User Fourni

### ✅ Points Positifs

1. **Structure MongoDB solide** : Utilisation correcte de `@Document` et `@Indexed`
2. **Champs essentiels présents** : username, email, password, rôles
3. **Tracking temporel** : createdAt, updatedAt, lastLoginAt
4. **Flexibilité** : Champs optionnels pour profil utilisateur

### 🔧 Améliorations Apportées

#### 1. **Système de Refresh Token par User**
```kotlin
val refreshToken: String? = null
val refreshTokenExpiresAt: Instant? = null
```
- **Pourquoi** : Permet de gérer les sessions utilisateur
- **Avantage** : Un refresh token par utilisateur = contrôle de session
- **Sécurité** : Permet de révoquer les tokens en invalidant le refresh token

#### 2. **Sécurité Renforcée**
```kotlin
val failedLoginAttempts: Int = 0
val lockedUntil: Instant? = null
```
- **Protection contre brute force** : Verrouillage après X tentatives
- **Temporaire** : Le compte se déverrouille automatiquement

#### 3. **Méthodes Utilitaires**
- `isLocked()` : Vérifie si le compte est verrouillé
- `canLogin()` : Vérifie toutes les conditions de connexion
- `hasValidRefreshToken()` : Vérifie la validité du refresh token
- `fullName` : Propriété calculée pour le nom complet

## 🎯 Système de Rôles - Analyse et Recommandations

### Hiérarchie des Rôles

```
ROOT_ADMIN (Niveau 4)
    ↓
ADMIN (Niveau 3)
    ↓
EMPLOYE (Niveau 2)
    ↓
USER (Niveau 1)
```

### Description des Rôles

#### 1. **USER** (Accès Public)
- **Accès** : Routes publiques uniquement
- **Authentification** : Non requise
- **Cas d'usage** : Consultation du site e-commerce, voir les produits
- **Limitation** : Pas d'actions (pas d'achat, pas de panier)

#### 2. **EMPLOYE** (Interface de Management)
- **Accès** : Interface de gestion du contenu
- **Authentification** : Requise
- **Permissions** :
  - CRUD sur les produits/articles
  - Gestion des commandes
  - Gestion du contenu public
- **Création** : Par ADMIN uniquement

#### 3. **ADMIN** (Gestion Complète)
- **Accès** : Toutes les interfaces de management
- **Authentification** : Requise
- **Permissions** :
  - Tout ce qu'un EMPLOYE peut faire
  - Créer et gérer les EMPLOYE
  - Gérer les forfaits clients
  - Accès aux statistiques
- **Création** : Par ROOT_ADMIN uniquement

#### 4. **ROOT_ADMIN** (Accès Système)
- **Accès** : Tout (dépannage, configuration)
- **Authentification** : Requise
- **Permissions** :
  - Tout ce qu'un ADMIN peut faire
  - Créer les ADMIN
  - Configuration système
  - Gestion de toutes les plateformes clients
  - Accès aux logs et métriques
- **Création** : Manuelle ou au premier démarrage

## 🏗️ Architecture des Interfaces selon Forfaits

### Forfait Basique (2 Interfaces)

```
┌─────────────────────────────────────┐
│   Interface Publique (USER)         │
│   - Accessible sans authentification│
│   - Consultation produits          │
│   - Affichage contenu              │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│   Interface Management (EMPLOYE)     │
│   - Authentification requise        │
│   - CRUD produits                   │
│   - Gestion commandes               │
└─────────────────────────────────────┘
```

**Rôles utilisés** : USER, EMPLOYE, ADMIN

### Forfait Premium (3 Interfaces)

```
┌─────────────────────────────────────┐
│   Interface Publique (USER)         │
│   - Accessible sans authentification│
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│   Interface Management (EMPLOYE)     │
│   - Gestion contenu                 │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│   Interface Admin (ADMIN)           │
│   - Gestion employés                │
│   - Gestion autres plateformes      │
│   - Statistiques                    │
└─────────────────────────────────────┘
```

**Rôles utilisés** : USER, EMPLOYE, ADMIN, ROOT_ADMIN

## 🔐 Stratégie JWT Recommandée

### Configuration des Tokens

```properties
# Access Token : Durée courte (15 minutes)
jwt.access-token-expiration=900000  # 15 min

# Refresh Token : Durée longue (7 jours)
jwt.refresh-token-expiration=604800000  # 7 jours

# Refresh Token par User : 1 heure de validité après utilisation
# (Géré dans le code)
```

### Flux d'Authentification

```
1. Login
   ↓
2. Génération Access Token (15 min) + Refresh Token (7 jours)
   ↓
3. Stockage Refresh Token dans User.refreshToken
   ↓
4. Retour des deux tokens au client
   ↓
5. Client utilise Access Token pour les requêtes
   ↓
6. Quand Access Token expire → Refresh avec Refresh Token
   ↓
7. Nouveau Access Token généré
   ↓
8. Refresh Token renouvelé (nouvelle expiration = maintenant + 1h)
```

### Avantages de cette Approche

1. **Sécurité** : Access token court = moins de risque si compromis
2. **UX** : Refresh automatique transparent pour l'utilisateur
3. **Contrôle** : Refresh token par user = possibilité de révoquer
4. **Session** : Refresh token expire après 1h d'inactivité

## 📝 Recommandations Supplémentaires

### 1. **Validation Email**
- Vérifier l'email avant d'activer le compte
- Token de vérification avec expiration
- Resend email si nécessaire

### 2. **Mot de Passe**
- Hashing avec BCrypt (Spring Security)
- Politique de complexité (min 8 caractères, majuscule, chiffre)
- Reset password avec token temporaire

### 3. **Rate Limiting**
- Limiter les tentatives de login (5 tentatives / 15 min)
- Limiter les requêtes d'API par rôle
- Protection contre les attaques DDoS

### 4. **Audit Log**
- Logger toutes les actions importantes
- Traçabilité des modifications
- Connexions/déconnexions

### 5. **2FA (Optionnel pour ADMIN/ROOT_ADMIN)**
- Authentification à deux facteurs
- SMS ou Email pour les rôles sensibles

## 🚀 Prochaines Étapes

1. ✅ Modèles créés (User, Role, Gender, SocialLinks)
2. ✅ Repository créé (UserRepository)
3. ✅ DTOs créés (RegisterRequest, LoginRequest, AuthResponse, UserResponse)
4. ✅ Mapper créé (UserMapper)
5. ⏳ Service d'authentification (AuthService)
6. ⏳ Configuration Spring Security (SecurityConfig)
7. ⏳ JWT Provider (JwtTokenProvider)
8. ⏳ Filtres de sécurité (JwtAuthenticationFilter)
9. ⏳ Controllers (AuthController)

## 💡 Points d'Attention

1. **Migration** : Si vous avez déjà des utilisateurs, prévoir une migration pour ajouter les nouveaux champs
2. **Performance** : Index sur email, username, refreshToken pour les recherches rapides
3. **Sécurité** : Ne jamais exposer le password dans les réponses
4. **Validation** : Valider tous les inputs côté serveur (ne pas faire confiance au client)
