# Structure du Projet

## Architecture Spring Boot - WebFlux Reactive

```
src/main/kotlin/com/kobecorporation/tmp_back/
├── TmpBackApplication.kt          # Point d'entrée de l'application
│
├── configuration/                   # Classes de configuration Spring
│   ├── SecurityConfig.kt            # Configuration Spring Security
│   ├── CorsConfig.kt                # Configuration CORS
│   ├── WebFluxConfig.kt             # Configuration WebFlux
│   ├── MongoConfig.kt               # Configuration MongoDB (optionnel)
│   └── security/                    # Composants de sécurité
│       ├── JwtTokenProvider.kt      # Génération et validation JWT
│       ├── JwtAuthenticationFilter.kt # Filtre d'authentification JWT
│       ├── SecurityContextRepository.kt # Gestion du contexte de sécurité
│       └── UserDetailsServiceImpl.kt # Service de détails utilisateur
│
├── domain/                          # Couche domaine (Business Logic)
│   ├── model/                       # Data Models / Entities (MongoDB Documents)
│   │   ├── User.kt                  # Modèle utilisateur
│   │   └── ...
│   ├── repository/                  # MongoDB Repositories (Reactive)
│   │   ├── UserRepository.kt       # Repository utilisateur
│   │   └── ...
│   └── service/                     # Business Logic (Services métier)
│       ├── AuthService.kt            # Service d'authentification
│       ├── UserService.kt           # Service utilisateur
│       └── ...
│
├── interaction/                     # Couche d'interaction (API)
│   ├── controller/                  # REST Controllers (Endpoints API)
│   │   ├── AuthController.kt        # Authentification (login, register)
│   │   ├── UserController.kt        # Gestion des utilisateurs
│   │   └── ...
│   ├── dto/                         # Data Transfer Objects
│   │   ├── request/                 # DTOs pour les requêtes
│   │   │   ├── LoginRequest.kt
│   │   │   ├── RegisterRequest.kt
│   │   │   └── ...
│   │   └── response/                # DTOs pour les réponses
│   │       ├── AuthResponse.kt
│   │       ├── UserResponse.kt
│   │       └── ...
│   └── mapper/                      # Mappers (conversions Entity <-> DTO)
│       ├── UserMapper.kt            # Mapper utilisateur
│       └── ...
│
├── exception/                       # Gestion des exceptions
│   ├── GlobalExceptionHandler.kt    # Handler global des exceptions
│   ├── ResourceNotFoundException.kt # Exception personnalisée
│   ├── BadRequestException.kt       # Exception personnalisée
│   └── ...
│
├── util/                            # Classes utilitaires
│   ├── FileStorageUtil.kt           # Utilitaires de stockage de fichiers
│   └── ...
│
├── validation/                      # Validateurs personnalisés
│   └── ...
│
└── config/                          # Configuration applicative
    ├── AppProperties.kt              # Propriétés de l'application
    └── ...
```

## Structure des ressources

```
src/main/resources/
├── application.properties           # Configuration par défaut
├── application-ngrok.properties     # Configuration ngrok
├── application-prod.properties       # Configuration production
└── APPLICATION-PROPERTIES-README.md  # Documentation des properties
```

## Flux de données (Reactive)

```
Interaction Layer
    ↓
Controller (WebFlux)
    ↓
Domain Layer
    ↓
Service (Reactive - Mono/Flux)
    ↓
Repository (ReactiveMongoRepository)
    ↓
MongoDB
```

## Architecture en couches

1. **Configuration** : Configuration Spring (Security, CORS, etc.)
2. **Domain** : Logique métier, modèles, repositories
3. **Interaction** : Controllers, DTOs, Mappers (couche API)
4. **Exception** : Gestion centralisée des erreurs
5. **Util** : Classes utilitaires
6. **Validation** : Validateurs personnalisés
7. **Config** : Configuration applicative

## Sécurité (JWT)

```
Request
    ↓
JwtAuthenticationFilter (extrait le token)
    ↓
SecurityContextRepository (valide et charge l'utilisateur)
    ↓
Controller (accès sécurisé)
```

## Bonnes pratiques

1. **Controllers** : Minces, délèguent aux services
2. **Services** : Contiennent la logique métier
3. **Repositories** : Accès aux données uniquement
4. **DTOs** : Séparation entre API et modèle interne
5. **Mappers** : Conversions explicites entre Entity et DTO
6. **Exceptions** : Gestion centralisée dans GlobalExceptionHandler
7. **Security** : Configuration centralisée dans SecurityConfig

## Conventions de nommage

- **Controllers** : `*Controller.kt`
- **Services** : `*Service.kt` (interface) et `*ServiceImpl.kt` (implémentation)
- **Repositories** : `*Repository.kt`
- **Models** : Nom au singulier (ex: `User.kt`, `Product.kt`)
- **DTOs** : `*Request.kt`, `*Response.kt`
- **Configurations** : `*Config.kt`
