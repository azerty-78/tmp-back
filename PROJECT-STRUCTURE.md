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
│   └── MongoConfig.kt               # Configuration MongoDB (optionnel)
│
├── controller/                      # REST Controllers (Endpoints API)
│   ├── AuthController.kt            # Authentification (login, register)
│   ├── UserController.kt            # Gestion des utilisateurs
│   └── ...
│
├── service/                         # Business Logic (Services métier)
│   ├── AuthService.kt               # Service d'authentification
│   ├── UserService.kt               # Service utilisateur
│   └── ...
│
├── repository/                      # MongoDB Repositories (Reactive)
│   ├── UserRepository.kt            # Repository utilisateur
│   └── ...
│
├── model/                           # Data Models / Entities (MongoDB Documents)
│   ├── User.kt                      # Modèle utilisateur
│   └── ...
│
├── dto/                             # Data Transfer Objects
│   ├── request/                     # DTOs pour les requêtes
│   │   ├── LoginRequest.kt
│   │   ├── RegisterRequest.kt
│   │   └── ...
│   └── response/                    # DTOs pour les réponses
│       ├── AuthResponse.kt
│       ├── UserResponse.kt
│       └── ...
│
├── security/                        # Composants de sécurité
│   ├── JwtTokenProvider.kt          # Génération et validation JWT
│   ├── JwtAuthenticationFilter.kt    # Filtre d'authentification JWT
│   ├── SecurityContextRepository.kt # Gestion du contexte de sécurité
│   └── UserDetailsServiceImpl.kt    # Service de détails utilisateur
│
├── exception/                       # Gestion des exceptions
│   ├── GlobalExceptionHandler.kt    # Handler global des exceptions
│   ├── ResourceNotFoundException.kt # Exception personnalisée
│   ├── BadRequestException.kt       # Exception personnalisée
│   └── ...
│
├── mapper/                          # Mappers (conversions Entity <-> DTO)
│   ├── UserMapper.kt                 # Mapper utilisateur
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
Controller (WebFlux)
    ↓
Service (Reactive - Mono/Flux)
    ↓
Repository (ReactiveMongoRepository)
    ↓
MongoDB
```

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
