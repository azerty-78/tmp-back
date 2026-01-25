# 🚀 Migration vers Architecture Multi-Tenant SaaS

> **Objectif** : Transformer le template en une vraie plateforme SaaS multi-tenant

---

## 🎯 Stratégie Choisie

### Identification des Tenants
- **Domaine personnalisé** : Chaque client peut avoir son propre domaine (`monentreprise.com`)
- **Domaine par défaut** : Pattern `kb-saas-{slug}.kobecorporation.com` pour les tests et nouveaux clients
- **Exemples** :
  - `kb-saas-01.kobecorporation.com` → Tenant de test 01
  - `kb-saas-acme.kobecorporation.com` → Tenant "acme"
  - `app.cliententreprise.fr` → Domaine custom du client

### Isolation des Données
- **Row-Level Tenant** : Tous les documents ont un champ `tenantId`
- Base de données partagée avec index composés

---

## 📋 TODO List des Tâches

### 🏗️ Phase 1 : Modèles Tenant (3 tâches)

- [ ] **1.1** Créer le modèle `Tenant`
  ```kotlin
  // logic/model/tenant/Tenant.kt
  @Document(collection = "tenants")
  data class Tenant(
      @Id val id: ObjectId = ObjectId(),
      val name: String,                              // Nom de l'entreprise
      @Indexed(unique = true) val slug: String,      // kb-saas-01, acme, etc.
      @Indexed(unique = true, sparse = true) 
      val customDomain: String? = null,              // app.cliententreprise.fr
      val plan: TenantPlan = TenantPlan.FREE,
      val status: TenantStatus = TenantStatus.ACTIVE,
      val settings: TenantSettings = TenantSettings(),
      val ownerId: ObjectId,
      val createdAt: Instant = Instant.now(),
      val updatedAt: Instant = Instant.now()
  )
  ```
  - Domaine par défaut : `kb-saas-{slug}.kobecorporation.com`
  - Domaine custom optionnel : `customDomain`

- [ ] **1.2** Créer les enums `TenantPlan` et `TenantStatus`
  ```kotlin
  // logic/model/tenant/TenantPlan.kt
  enum class TenantPlan {
      FREE,       // Gratuit - 3 users, 100MB
      STARTER,    // 19€/mois - 10 users, 1GB
      PRO,        // 49€/mois - 50 users, 10GB
      ENTERPRISE  // Sur devis - Illimité
  }
  
  // logic/model/tenant/TenantStatus.kt
  enum class TenantStatus {
      TRIAL,      // Période d'essai
      ACTIVE,     // Actif
      SUSPENDED,  // Suspendu (impayé)
      CANCELLED   // Annulé
  }
  ```

- [ ] **1.3** Créer le modèle `TenantSettings`
  ```kotlin
  // logic/model/tenant/TenantSettings.kt
  data class TenantSettings(
      val logo: String? = null,
      val primaryColor: String = "#3B82F6",
      val timezone: String = "Europe/Paris",
      val language: String = "fr",
      val emailFromName: String? = null,       // Nom expéditeur emails
      val emailFromAddress: String? = null     // Email expéditeur custom
  )
  ```

---

### 📦 Phase 2 : Modèles User & Repository (4 tâches)

- [ ] **2.1** Ajouter `tenantId` et `tenantRole` au modèle `User`
  ```kotlin
  // Modifier User.kt - ajouter ces champs
  @Indexed val tenantId: ObjectId? = null,     // null = Platform Admin
  val tenantRole: TenantRole = TenantRole.MEMBER
  ```

- [ ] **2.2** Créer l'enum `TenantRole`
  ```kotlin
  // logic/model/tenant/TenantRole.kt
  enum class TenantRole {
      OWNER,      // Propriétaire du tenant (créateur)
      ADMIN,      // Administrateur du tenant
      MEMBER,     // Membre standard
      GUEST       // Accès limité / lecture seule
  }
  ```

- [ ] **2.3** Créer `TenantRepository`
  ```kotlin
  // logic/repository/tenant/TenantRepository.kt
  @Repository
  interface TenantRepository : ReactiveMongoRepository<Tenant, ObjectId> {
      fun findBySlug(slug: String): Mono<Tenant>
      fun findByCustomDomain(customDomain: String): Mono<Tenant>
      fun existsBySlug(slug: String): Mono<Boolean>
      fun existsByCustomDomain(customDomain: String): Mono<Boolean>
      fun findByOwnerId(ownerId: ObjectId): Flux<Tenant>
      fun findByStatus(status: TenantStatus): Flux<Tenant>
  }
  ```

- [ ] **2.4** Mettre à jour `UserRepository` avec méthodes tenant-aware
  ```kotlin
  // Ajouter à UserRepository.kt
  fun findByTenantIdAndEmail(tenantId: ObjectId, email: String): Mono<User>
  fun findByTenantIdAndUsername(tenantId: ObjectId, username: String): Mono<User>
  fun existsByTenantIdAndEmail(tenantId: ObjectId, email: String): Mono<Boolean>
  fun existsByTenantIdAndUsername(tenantId: ObjectId, username: String): Mono<Boolean>
  fun findByTenantId(tenantId: ObjectId): Flux<User>
  fun countByTenantId(tenantId: ObjectId): Mono<Long>
  ```

---

### 🔒 Phase 3 : Sécurité & Isolation (4 tâches)

- [ ] **3.1** Créer `TenantContext` (Reactor Context)
  ```kotlin
  // configuration/tenant/TenantContext.kt
  object TenantContext {
      val TENANT_KEY = "CURRENT_TENANT"
      val TENANT_ID_KEY = "CURRENT_TENANT_ID"
      
      // Extension pour Mono/Flux
      fun <T> Mono<T>.withTenant(tenant: Tenant): Mono<T> =
          contextWrite { it.put(TENANT_KEY, tenant).put(TENANT_ID_KEY, tenant.id) }
      
      fun Mono<Tenant>.fromContext(): Mono<Tenant> =
          Mono.deferContextual { ctx -> Mono.justOrEmpty(ctx.getOrEmpty<Tenant>(TENANT_KEY)) }
  }
  ```

- [ ] **3.2** Créer `TenantWebFilter` (résolution par domaine)
  ```kotlin
  // configuration/tenant/TenantWebFilter.kt
  @Component
  @Order(Ordered.HIGHEST_PRECEDENCE)
  class TenantWebFilter(
      private val tenantRepository: TenantRepository
  ) : WebFilter {
      
      override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
          val host = exchange.request.headers.host?.hostString ?: ""
          
          return resolveTenant(host)
              .flatMap { tenant ->
                  chain.filter(exchange)
                      .contextWrite { it.put(TenantContext.TENANT_KEY, tenant) }
              }
              .switchIfEmpty(chain.filter(exchange)) // Routes sans tenant (platform)
      }
      
      private fun resolveTenant(host: String): Mono<Tenant> {
          // 1. Vérifier si c'est un domaine custom
          // 2. Sinon extraire le slug de kb-saas-{slug}.kobecorporation.com
          // 3. Chercher le tenant
      }
  }
  ```

- [ ] **3.3** Modifier `JwtService` pour inclure `tenantId`
  ```kotlin
  // Modifier generateToken() pour ajouter tenantId dans les claims
  .claim("tenantId", user.tenantId?.toHexString())
  .claim("tenantRole", user.tenantRole.name)
  
  // Modifier extractClaims() pour récupérer tenantId
  fun extractTenantId(token: String): String? = extractClaim(token) { it["tenantId"] as? String }
  ```

- [ ] **3.4** Créer `TenantAwareRepository` (base interface)
  ```kotlin
  // logic/repository/base/TenantAwareRepository.kt
  interface TenantAwareRepository<T, ID> {
      fun findAllByTenantId(tenantId: ObjectId): Flux<T>
      fun findByIdAndTenantId(id: ID, tenantId: ObjectId): Mono<T>
      fun deleteByIdAndTenantId(id: ID, tenantId: ObjectId): Mono<Void>
  }
  ```

---

### 🚀 Phase 4 : Services & Controllers Tenant (4 tâches)

- [ ] **4.1** Créer `TenantService`
  ```kotlin
  // logic/service/tenant/TenantService.kt
  @Service
  class TenantService(
      private val tenantRepository: TenantRepository,
      private val userRepository: UserRepository
  ) {
      fun createTenant(request: CreateTenantRequest, ownerEmail: String): Mono<Tenant>
      fun getTenantBySlug(slug: String): Mono<Tenant>
      fun getTenantByDomain(domain: String): Mono<Tenant>
      fun resolveTenant(host: String): Mono<Tenant>  // Custom domain ou kb-saas-{slug}
      fun updateTenant(tenantId: ObjectId, request: UpdateTenantRequest): Mono<Tenant>
      fun updateCustomDomain(tenantId: ObjectId, customDomain: String?): Mono<Tenant>
      fun getMembers(tenantId: ObjectId): Flux<User>
      fun getMemberCount(tenantId: ObjectId): Mono<Long>
  }
  ```

- [ ] **4.2** Créer `TenantController`
  ```kotlin
  // controller/tenant/TenantController.kt
  @RestController
  @RequestMapping("/api/tenants")
  class TenantController {
      // POST /api/tenants/signup - Créer un tenant + premier user (OWNER)
      // GET /api/tenants/me - Infos du tenant courant
      // PUT /api/tenants/me - Mettre à jour le tenant
      // PUT /api/tenants/me/domain - Configurer domaine custom
      // GET /api/tenants/me/members - Liste des membres
      // POST /api/tenants/me/invitations - Inviter un membre
      // GET /api/tenants/check-slug/{slug} - Vérifier disponibilité slug
  }
  ```

- [ ] **4.3** Modifier `AuthService` pour le flow multi-tenant
  ```kotlin
  // Modifier register() :
  // - Accepte un tenantId OU crée un nouveau tenant
  // - Gère les invitations (token d'invitation)
  
  // Modifier login() :
  // - Résout le tenant depuis le domaine/header
  // - Vérifie l'appartenance de l'user au tenant
  ```

- [ ] **4.4** Créer système d'invitation
  ```kotlin
  // logic/model/tenant/TenantInvitation.kt
  @Document(collection = "tenant_invitations")
  data class TenantInvitation(
      @Id val id: ObjectId = ObjectId(),
      @Indexed val tenantId: ObjectId,
      @Indexed val email: String,
      val role: TenantRole = TenantRole.MEMBER,
      @Indexed(unique = true) val token: String,
      val invitedBy: ObjectId,
      val expiresAt: Instant,
      val acceptedAt: Instant? = null,
      val createdAt: Instant = Instant.now()
  )
  
  // logic/service/tenant/InvitationService.kt
  @Service
  class InvitationService {
      fun createInvitation(tenantId: ObjectId, email: String, role: TenantRole, invitedBy: ObjectId): Mono<TenantInvitation>
      fun acceptInvitation(token: String, userData: RegisterRequest): Mono<User>
      fun getInvitationByToken(token: String): Mono<TenantInvitation>
      fun getPendingInvitations(tenantId: ObjectId): Flux<TenantInvitation>
  }
  ```

---

### 🛡️ Phase 5 : Sécurité & DTOs (2 tâches)

- [ ] **5.1** Mettre à jour `SecurityConfig`
  ```kotlin
  // Ajouter les nouvelles routes
  
  // Routes publiques (pas de tenant requis)
  .pathMatchers("/api/tenants/signup").permitAll()        // Création tenant
  .pathMatchers("/api/tenants/check-slug/**").permitAll() // Vérif dispo slug
  .pathMatchers("/api/invitations/accept/**").permitAll() // Accepter invitation
  
  // Routes tenant (authentifié + tenant résolu)
  .pathMatchers("/api/tenants/me/**").authenticated()
  .pathMatchers("/api/tenants/me/invitations/**").hasAnyRole("TENANT_OWNER", "TENANT_ADMIN")
  
  // Routes platform admin (super admin)
  .pathMatchers("/api/platform/admin/**").hasRole("PLATFORM_ADMIN")
  ```

- [ ] **5.2** Créer les DTOs tenant
  ```kotlin
  // interaction/dto/tenant/request/
  data class CreateTenantRequest(
      val name: String,           // Nom de l'entreprise
      val slug: String,           // kb-saas-{slug}
      // User owner
      val ownerEmail: String,
      val ownerPassword: String,
      val ownerFirstName: String,
      val ownerLastName: String
  )
  
  data class UpdateTenantRequest(
      val name: String? = null,
      val settings: TenantSettings? = null
  )
  
  data class InviteMemberRequest(
      val email: String,
      val role: TenantRole = TenantRole.MEMBER
  )
  
  // interaction/dto/tenant/response/
  data class TenantResponse(
      val id: String,
      val name: String,
      val slug: String,
      val customDomain: String?,
      val defaultDomain: String,  // kb-saas-{slug}.kobecorporation.com
      val plan: TenantPlan,
      val status: TenantStatus,
      val settings: TenantSettings,
      val memberCount: Long,
      val createdAt: Instant
  )
  ```

---

### 🗄️ Phase 6 : Base de Données & Migration (2 tâches)

- [ ] **6.1** Mettre à jour `MongoConfig` pour index composés
  ```kotlin
  // Ajouter dans MongoConfig.kt ou créer un IndexConfig.kt
  @EventListener(ApplicationReadyEvent::class)
  fun createIndexes() {
      // Index composés pour users
      mongoTemplate.indexOps("users").ensureIndex(
          Index().on("tenantId", Sort.Direction.ASC)
                 .on("email", Sort.Direction.ASC)
                 .unique()
      )
      mongoTemplate.indexOps("users").ensureIndex(
          Index().on("tenantId", Sort.Direction.ASC)
                 .on("username", Sort.Direction.ASC)
                 .unique()
      )
      
      // Index pour tenants
      mongoTemplate.indexOps("tenants").ensureIndex(
          Index().on("slug", Sort.Direction.ASC).unique()
      )
      mongoTemplate.indexOps("tenants").ensureIndex(
          Index().on("customDomain", Sort.Direction.ASC).unique().sparse()
      )
  }
  ```

- [ ] **6.2** Créer script de migration
  ```kotlin
  // scripts/migration/MigrateTenantData.kt
  // OU un ApplicationRunner qui s'exécute au démarrage
  
  // 1. Créer un tenant par défaut "kb-saas-default"
  // 2. Assigner tous les users existants à ce tenant
  // 3. Mettre le ROOT_ADMIN actuel en TENANT_OWNER
  ```

---

### 📧 Phase 7 : Emails Multi-Tenant (2 tâches)

- [ ] **7.1** Mettre à jour `EmailService` pour branding tenant
  ```kotlin
  // Modifier EmailService pour accepter le tenant
  fun sendVerificationEmail(email: String, code: String, tenant: Tenant?): Mono<Void>
  
  // Utiliser tenant.settings.emailFromName si défini
  // Sinon utiliser le nom du tenant
  // Sinon utiliser la config par défaut
  ```

- [ ] **7.2** Créer templates email personnalisables
  ```
  // Templates avec variables tenant
  - Logo : ${tenant.settings.logo} ou logo par défaut
  - Couleur : ${tenant.settings.primaryColor}
  - Nom : ${tenant.name}
  - Nouveaux templates :
    - welcome-tenant.html (bienvenue dans votre espace)
    - invitation.html (vous êtes invité à rejoindre...)
  ```

---

### 🌐 Phase 8 : Configuration Infrastructure (2 tâches)

- [ ] **8.1** Configuration CORS dynamique
  ```kotlin
  // Modifier SecurityConfig.corsConfigurationSource()
  
  // Option 1 : Wildcard pour *.kobecorporation.com
  allowedOriginPatterns = listOf(
      "https://*.kobecorporation.com",
      "http://localhost:*"  // Dev
  )
  
  // Option 2 : Charger dynamiquement depuis la DB
  // (Plus complexe mais permet les domaines custom)
  ```

- [ ] **8.2** Mettre à jour `application.properties`
  ```properties
  # Multi-tenant
  tenant.platform-domain=kobecorporation.com
  tenant.default-subdomain-pattern=kb-saas-{slug}
  tenant.default-plan=FREE
  tenant.trial-days=14
  
  # Platform admin (super admin qui gère tous les tenants)
  platform.admin.email=${PLATFORM_ADMIN_EMAIL:admin@kobecorporation.com}
  platform.admin.password=${PLATFORM_ADMIN_PASSWORD:PlatformAdmin123!}
  ```

---

### 👑 Phase 9 : Platform Admin (2 tâches)

- [ ] **9.1** Créer `PlatformAdminController`
  ```kotlin
  // controller/platform/PlatformAdminController.kt
  @RestController
  @RequestMapping("/api/platform/admin")
  class PlatformAdminController {
      // GET /tenants - Liste tous les tenants
      // GET /tenants/{id} - Détails d'un tenant
      // PUT /tenants/{id}/status - Suspendre/activer un tenant
      // DELETE /tenants/{id} - Supprimer un tenant
      // GET /tenants/{id}/members - Membres d'un tenant
      // POST /tenants/{id}/impersonate - Se connecter en tant que user
      // GET /stats - Statistiques globales
  }
  ```

- [ ] **9.2** Mettre à jour `DataInitializer`
  ```kotlin
  // Créer PLATFORM_ADMIN au démarrage (sans tenant)
  // - tenantId = null
  // - role = PLATFORM_ADMIN (nouveau rôle à ajouter dans Role.kt)
  
  // Ajouter PLATFORM_ADMIN dans l'enum Role
  enum class Role {
      USER, EMPLOYE, ADMIN, ROOT_ADMIN, PLATFORM_ADMIN
  }
  ```

---

## 📊 Résumé des Tâches

| Phase | Description | Nb Tâches |
|-------|-------------|-----------|
| 1 | Modèles Tenant | 3 |
| 2 | Modèles User & Repository | 4 |
| 3 | Sécurité & Isolation | 4 |
| 4 | Services & Controllers | 4 |
| 5 | Sécurité & DTOs | 2 |
| 6 | Base de Données & Migration | 2 |
| 7 | Emails Multi-Tenant | 2 |
| 8 | Configuration Infrastructure | 2 |
| 9 | Platform Admin | 2 |
| **Total** | | **25** |

---

## 🌐 Stratégie de Domaines

### Domaine par Défaut (tests & nouveaux clients)
```
kb-saas-{slug}.kobecorporation.com
```

**Exemples :**
- `kb-saas-01.kobecorporation.com` → Tenant de test
- `kb-saas-acme.kobecorporation.com` → Tenant "acme"
- `kb-saas-demo.kobecorporation.com` → Démo

### Domaine Personnalisé (clients en production)
```
app.cliententreprise.fr
dashboard.monentreprise.com
```

**Configuration requise :**
1. Client configure un CNAME vers `kobecorporation.com`
2. On ajoute le `customDomain` dans la collection `tenants`
3. Le `TenantWebFilter` résout le tenant via le domaine

---

## 📅 Ordre de Réalisation

```
Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6 → Phase 7 → Phase 8 → Phase 9
   │         │         │         │
   └─────────┴─────────┴─────────┘
        Peuvent être faits en parallèle
```

**Dépendances :**
- Phase 2 dépend de Phase 1 (TenantRole a besoin de Tenant)
- Phase 3 dépend de Phase 2 (TenantWebFilter a besoin de TenantRepository)
- Phase 4 dépend de Phase 3 (TenantService a besoin de TenantContext)
- Phase 5+ peuvent être faits en parallèle

---

## 📝 Notes

- Chaque tâche cochée ✅ signifie qu'elle est terminée
- La facturation (Stripe) n'est pas incluse - à faire après le MVP
- Le monitoring avancé n'est pas inclus - à faire après le MVP

---

**Dernière mise à jour** : Janvier 2025
