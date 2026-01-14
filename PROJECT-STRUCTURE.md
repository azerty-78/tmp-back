# Structure du Projet

## Architecture Spring Boot - WebFlux Reactive

```
src/main/kotlin/com/kobecorporation/tmp_back/
├── TmpBackApplication.kt          # Point d'entrée de l'application
│
├── logic/                          # Couche logique métier
│   ├── model/                      # Data Models / Entities (MongoDB Documents)
│   │   └── users/                  # Modèles liés aux utilisateurs
│   │       ├── User.kt             # Modèle utilisateur
│   │       ├── Role.kt             # Enum des rôles
│   │       ├── Gender.kt           # Enum du genre
│   │       └── SocialLinks.kt      # Liens sociaux
│   ├── repository/                 # MongoDB Repositories (Reactive)
│   │   └── users/                  # Repositories liés aux utilisateurs
│   │       └── UserRepository.kt   # Repository utilisateur
│   └── service/                    # Business Logic (Services métier)
│       └── users/                  # Services liés aux utilisateurs
│           ├── AuthService.kt      # Service d'authentification
│           └── UserService.kt      # Service utilisateur
│
├── interaction/                    # Couche d'interaction (API)
│   ├── dto/                        # Data Transfer Objects
│   │   └── users/                  # DTOs liés aux utilisateurs
│   │       ├── request/            # DTOs pour les requêtes
│   │       │   ├── LoginRequest.kt
│   │       │   ├── RegisterRequest.kt
│   │       │   └── RefreshTokenRequest.kt
│   │       └── response/           # DTOs pour les réponses
│   │           ├── AuthResponse.kt
│   │           └── UserResponse.kt
│   ├── mapper/                     # Mappers (conversions Entity <-> DTO)
│   │   └── users/                  # Mappers liés aux utilisateurs
│   │       └── UserMapper.kt       # Mapper utilisateur
│   ├── exception/                  # Gestion des exceptions
│   │   ├── GlobalExceptionHandler.kt # Handler global des exceptions
│   │   ├── ResourceNotFoundException.kt # Exception personnalisée
│   │   ├── BadRequestException.kt  # Exception personnalisée
│   │   └── ...
│   └── validation/                  # Validateurs personnalisés
│       └── ...
│
├── controller/                     # REST Controllers (Endpoints API)
│   └── users/                     # Controllers liés aux utilisateurs
│       ├── AuthController.kt      # Authentification (login, register)
│       └── UserController.kt       # Gestion des utilisateurs
│
├── util/                           # Classes utilitaires
│   ├── FileStorageUtil.kt          # Utilitaires de stockage de fichiers
│   └── ...
│
└── configuration/                  # Classes de configuration Spring
    ├── SecurityConfig.kt           # Configuration Spring Security
    ├── CorsConfig.kt               # Configuration CORS
    ├── WebFluxConfig.kt            # Configuration WebFlux
    ├── MongoConfig.kt              # Configuration MongoDB (optionnel)
    └── security/                   # Composants de sécurité
        ├── JwtTokenProvider.kt     # Génération et validation JWT
        ├── JwtAuthenticationFilter.kt # Filtre d'authentification JWT
        ├── SecurityContextRepository.kt # Gestion du contexte de sécurité
        └── UserDetailsServiceImpl.kt # Service de détails utilisateur
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
Controller (WebFlux)
    ↓
Interaction Layer (DTOs, Mappers)
    ↓
Logic Layer
    ↓
Service (Reactive - Mono/Flux)
    ↓
Repository (ReactiveMongoRepository)
    ↓
MongoDB
```

## Architecture en couches

1. **Logic** : Logique métier (model, repository, service)
2. **Interaction** : Couche d'interaction API (dto, mapper, exception, validation)
3. **Controller** : Controllers REST (endpoints API)
4. **Util** : Classes utilitaires
5. **Configuration** : Configuration Spring (Security, CORS, etc.)

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
