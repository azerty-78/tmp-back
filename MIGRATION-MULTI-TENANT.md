# 🚀 Migration vers Architecture Multi-Tenant SaaS

> **Objectif** : Transformer le template en une vraie plateforme SaaS multi-tenant

---

## 📋 Checklist des Tâches

### 🏗️ Phase 1 : Architecture & Décisions Fondamentales

- [ ] **1.1 Choisir la stratégie multi-tenant**
  - **Database per tenant** : Chaque tenant a sa propre base de données (isolation forte)
  - **Row-level tenant** : Tous les tenants partagent les mêmes collections avec un champ `tenantId` (recommandé)

- [ ] **1.2 Définir le modèle Tenant**
  - Créer l'entité `Tenant` avec : id, name, slug, domain, subdomain, plan, status, settings, createdAt, updatedAt

- [ ] **1.3 Définir la stratégie d'identification du tenant**
  - Option A : Subdomain (`client1.votreapp.com`)
  - Option B : Header personnalisé (`X-Tenant-ID`)
  - Option C : Path (`/api/tenant1/...`)

- [ ] **1.4 Décider de la gestion des plans/abonnements**
  - Définir les plans : Free, Pro, Enterprise
  - Définir les quotas et limites par plan

---

### 📦 Phase 2 : Modèles de Données

- [ ] **2.1 Créer le modèle `Tenant`**
  ```kotlin
  // logic/model/tenant/Tenant.kt
  @Document(collection = "tenants")
  data class Tenant(
      @Id val id: ObjectId = ObjectId(),
      val name: String,
      @Indexed(unique = true) val slug: String,
      val domain: String? = null,
      val subdomain: String,
      val plan: TenantPlan = TenantPlan.FREE,
      val status: TenantStatus = TenantStatus.ACTIVE,
      val settings: TenantSettings = TenantSettings(),
      val ownerId: ObjectId,
      val createdAt: Instant = Instant.now(),
      val updatedAt: Instant = Instant.now()
  )
  ```

- [ ] **2.2 Créer le modèle `Subscription`**
  ```kotlin
  // logic/model/subscription/Subscription.kt
  @Document(collection = "subscriptions")
  data class Subscription(
      @Id val id: ObjectId = ObjectId(),
      @Indexed val tenantId: ObjectId,
      val plan: TenantPlan,
      val status: SubscriptionStatus,
      val currentPeriodStart: Instant,
      val currentPeriodEnd: Instant,
      val stripeSubscriptionId: String? = null,
      val stripeCustomerId: String? = null
  )
  ```

- [ ] **2.3 Ajouter `tenantId` au modèle `User`**
  ```kotlin
  // Modifier User.kt
  @Indexed val tenantId: ObjectId,
  val tenantRole: TenantRole = TenantRole.MEMBER
  ```

- [ ] **2.4 Créer une classe de base `TenantAwareDocument`**
  ```kotlin
  // logic/model/base/TenantAwareDocument.kt
  abstract class TenantAwareDocument {
      @Indexed
      abstract val tenantId: ObjectId
  }
  ```

- [ ] **2.5 Mettre à jour tous les modèles existants**
  - Ajouter `tenantId` à toutes les collections futures

- [ ] **2.6 Ajouter les index composés**
  - Index sur `(tenantId, email)` pour User
  - Index sur `(tenantId, username)` pour User
  - Index composés pour toutes les collections tenant-aware

---

### 🔒 Phase 3 : Sécurité & Isolation des Données

- [ ] **3.1 Créer un `TenantContext`**
  ```kotlin
  // configuration/tenant/TenantContext.kt
  object TenantContext {
      private val currentTenant = ThreadLocal<Tenant?>()
      
      fun setCurrentTenant(tenant: Tenant?) { currentTenant.set(tenant) }
      fun getCurrentTenant(): Tenant? = currentTenant.get()
      fun clear() { currentTenant.remove() }
  }
  
  // Version Reactive avec Reactor Context
  object ReactiveTenantContext {
      val TENANT_KEY = "CURRENT_TENANT"
  }
  ```

- [ ] **3.2 Créer un `TenantWebFilter`**
  ```kotlin
  // configuration/tenant/TenantWebFilter.kt
  @Component
  class TenantWebFilter(
      private val tenantService: TenantService
  ) : WebFilter {
      // Extraire le tenant du subdomain ou header
      // Injecter dans le Reactor Context
  }
  ```

- [ ] **3.3 Modifier le `JwtService`**
  - Ajouter `tenantId` dans les claims du JWT
  - Valider le tenant lors de la vérification du token

- [ ] **3.4 Créer un `TenantAwareRepository`**
  - Interface de base pour filtrer automatiquement par tenant
  - Intercepteur MongoDB pour injecter le tenantId

- [ ] **3.5 Validation cross-tenant**
  - Vérifier que l'utilisateur appartient bien au tenant
  - Empêcher l'accès aux données d'autres tenants

- [ ] **3.6 Mettre à jour `SecurityConfig`**
  - Routes publiques pour onboarding tenant
  - Routes protégées pour admin tenant

---

### 👥 Phase 4 : Gestion des Utilisateurs Multi-Tenant

- [ ] **4.1 Créer l'enum `TenantRole`**
  ```kotlin
  // logic/model/tenant/TenantRole.kt
  enum class TenantRole {
      OWNER,      // Propriétaire du tenant (créateur)
      ADMIN,      // Administrateur du tenant
      MEMBER,     // Membre standard
      GUEST       // Accès limité
  }
  ```

- [ ] **4.2 Modifier le système de rôles**
  - `Role` = Rôle global (USER, EMPLOYE, ADMIN, ROOT_ADMIN, PLATFORM_ADMIN)
  - `TenantRole` = Rôle au sein d'un tenant

- [ ] **4.3 Modifier `AuthService`**
  - Registration avec création de tenant OU invitation
  - Login avec résolution du tenant
  - Vérification de l'appartenance au tenant

- [ ] **4.4 Créer le système d'invitation**
  ```kotlin
  // logic/model/tenant/TenantInvitation.kt
  @Document(collection = "tenant_invitations")
  data class TenantInvitation(
      @Id val id: ObjectId = ObjectId(),
      val tenantId: ObjectId,
      val email: String,
      val role: TenantRole,
      val token: String,
      val invitedBy: ObjectId,
      val expiresAt: Instant,
      val acceptedAt: Instant? = null
  )
  ```

- [ ] **4.5 Créer `InvitationService`**
  - Envoyer une invitation par email
  - Accepter une invitation (créer le user dans le tenant)

- [ ] **4.6 Super Admin (Platform Level)**
  - Rôle `PLATFORM_ADMIN` pour gérer tous les tenants
  - Interface d'administration globale

---

### 🚀 Phase 5 : Onboarding & Création de Tenant

- [ ] **5.1 Créer `TenantService`**
  ```kotlin
  // logic/service/tenant/TenantService.kt
  @Service
  class TenantService(
      private val tenantRepository: TenantRepository,
      private val userRepository: UserRepository
  ) {
      suspend fun createTenant(request: CreateTenantRequest, owner: User): Tenant
      suspend fun getTenantBySlug(slug: String): Tenant?
      suspend fun getTenantBySubdomain(subdomain: String): Tenant?
      suspend fun updateTenant(tenantId: ObjectId, request: UpdateTenantRequest): Tenant
      suspend fun deleteTenant(tenantId: ObjectId)
  }
  ```

- [ ] **5.2 Créer `TenantController`**
  ```kotlin
  // controller/tenant/TenantController.kt
  @RestController
  @RequestMapping("/api/tenants")
  class TenantController {
      // POST /api/tenants - Créer un tenant
      // GET /api/tenants/me - Tenant courant
      // PUT /api/tenants/me - Mettre à jour
      // GET /api/tenants/me/members - Liste des membres
      // POST /api/tenants/me/invitations - Inviter un membre
  }
  ```

- [ ] **5.3 Créer le flow d'inscription tenant**
  ```
  1. User arrive sur la landing page
  2. Clique sur "Créer mon espace"
  3. Remplit : email, mot de passe, nom entreprise, subdomain
  4. Création du Tenant + User (OWNER)
  5. Email de vérification
  6. Redirection vers dashboard
  ```

- [ ] **5.4 Validation du subdomain**
  - Unicité du subdomain
  - Format valide (alphanumeric + tirets)
  - Mots réservés : admin, api, www, app, dashboard, etc.

- [ ] **5.5 Configuration initiale du tenant**
  - Données par défaut
  - Configuration (logo, couleurs, timezone)
  - Limites selon le plan

---

### 💳 Phase 6 : Facturation & Abonnements

- [ ] **6.1 Intégration Stripe**
  - Ajouter les dépendances Stripe
  - Configuration des clés API

- [ ] **6.2 Créer les plans tarifaires**
  ```kotlin
  enum class TenantPlan {
      FREE,       // 0€ - 3 users, 100MB
      STARTER,    // 19€/mois - 10 users, 1GB
      PRO,        // 49€/mois - 50 users, 10GB
      ENTERPRISE  // Sur devis - Illimité
  }
  ```

- [ ] **6.3 Créer `SubscriptionService`**
  - Créer un abonnement Stripe
  - Gérer les upgrades/downgrades
  - Annuler un abonnement

- [ ] **6.4 Webhooks Stripe**
  - `invoice.paid` - Paiement réussi
  - `invoice.payment_failed` - Échec de paiement
  - `customer.subscription.deleted` - Annulation
  - `customer.subscription.updated` - Modification

- [ ] **6.5 Quotas et limites**
  ```kotlin
  data class PlanLimits(
      val maxUsers: Int,
      val maxStorageMB: Long,
      val maxApiCallsPerDay: Int,
      val features: Set<Feature>
  )
  ```

- [ ] **6.6 Portail de facturation**
  - Historique des factures
  - Modifier le plan
  - Mettre à jour la carte

---

### 🗄️ Phase 7 : Base de Données

- [ ] **7.1 Mettre à jour `MongoConfig`**
  - Support de la résolution dynamique du tenant
  - Intercepteur pour ajouter automatiquement le tenantId

- [ ] **7.2 Créer les index multi-tenant**
  ```javascript
  // Index composés recommandés
  db.users.createIndex({ "tenantId": 1, "email": 1 }, { unique: true })
  db.users.createIndex({ "tenantId": 1, "username": 1 }, { unique: true })
  db.users.createIndex({ "tenantId": 1, "role": 1 })
  ```

- [ ] **7.3 Script de migration des données**
  - Ajouter un tenant par défaut pour les données existantes
  - Migrer les users existants vers ce tenant

- [ ] **7.4 Créer `TenantRepository`**
  ```kotlin
  @Repository
  interface TenantRepository : ReactiveMongoRepository<Tenant, ObjectId> {
      fun findBySlug(slug: String): Mono<Tenant>
      fun findBySubdomain(subdomain: String): Mono<Tenant>
      fun existsBySlug(slug: String): Mono<Boolean>
      fun existsBySubdomain(subdomain: String): Mono<Boolean>
  }
  ```

---

### 🌐 Phase 8 : Configuration & Infrastructure

- [ ] **8.1 Configuration DNS wildcard**
  - Configurer `*.votreapp.com` vers le serveur
  - Alternative : Custom domains par tenant

- [ ] **8.2 CORS dynamique**
  ```kotlin
  // Accepter dynamiquement les origins des tenants
  fun corsConfigurationSource(): CorsConfigurationSource {
      // Récupérer les domains de tous les tenants
      // Ou utiliser un pattern wildcard
  }
  ```

- [ ] **8.3 Certificat SSL wildcard**
  - Let's Encrypt avec wildcard
  - Ou certificat commercial wildcard

- [ ] **8.4 Nouvelles variables d'environnement**
  ```env
  # Multi-tenant
  TENANT_DEFAULT_PLAN=FREE
  TENANT_TRIAL_DAYS=14
  PLATFORM_DOMAIN=votreapp.com
  
  # Stripe
  STRIPE_API_KEY=sk_live_xxx
  STRIPE_WEBHOOK_SECRET=whsec_xxx
  STRIPE_PRICE_STARTER=price_xxx
  STRIPE_PRICE_PRO=price_xxx
  ```

- [ ] **8.5 Docker Compose multi-service**
  - Service API
  - Service Worker (jobs async)
  - Redis (cache, sessions)

---

### 📧 Phase 9 : Emails Multi-Tenant

- [ ] **9.1 Templates email personnalisables**
  - Logo du tenant dans les emails
  - Couleurs personnalisées
  - Nom de l'entreprise

- [ ] **9.2 From address dynamique**
  - `noreply@{tenant-slug}.votreapp.com`
  - Ou email custom du tenant

- [ ] **9.3 Nouveaux templates email**
  - Bienvenue dans le tenant
  - Invitation à rejoindre
  - Notification de paiement
  - Changement de plan

---

### 📊 Phase 10 : Monitoring & Administration

- [ ] **10.1 Dashboard Super Admin**
  - Liste de tous les tenants
  - Créer/suspendre/supprimer un tenant
  - Impersonate un user

- [ ] **10.2 Métriques par tenant**
  - Nombre d'utilisateurs actifs
  - Stockage utilisé
  - Appels API

- [ ] **10.3 Logs tenant-aware**
  - Ajouter `tenantId` dans tous les logs
  - Filtrer les logs par tenant

- [ ] **10.4 Health check par tenant**
  - Statut de chaque tenant
  - Alertes si problème

---

## 🎯 Stratégie Recommandée

### Row-Level Tenant (Recommandé)

**Avantages :**
- ✅ Simple à implémenter
- ✅ Une seule base de données à gérer
- ✅ Scalabilité horizontale facile
- ✅ Moins coûteux en ressources
- ✅ Requêtes cross-tenant possibles (pour admin)

**Inconvénients :**
- ⚠️ Isolation moins forte (risque de fuite si bug)
- ⚠️ Index plus volumineux

### Identification par Subdomain (Recommandé)

```
https://client1.votreapp.com → Tenant "client1"
https://client2.votreapp.com → Tenant "client2"
https://app.votreapp.com → Landing page / Signup
```

---

## 📅 Ordre de Réalisation Suggéré

1. **Phase 1** - Décisions fondamentales (discussion)
2. **Phase 2** - Modèles de données
3. **Phase 3** - Sécurité & Isolation
4. **Phase 5** - Onboarding (création de tenant)
5. **Phase 4** - Gestion utilisateurs multi-tenant
6. **Phase 7** - Base de données & Migration
7. **Phase 8** - Infrastructure
8. **Phase 9** - Emails
9. **Phase 6** - Facturation (peut être fait plus tard)
10. **Phase 10** - Monitoring

---

## 📝 Notes

- Chaque tâche cochée ✅ signifie qu'elle est terminée
- Les phases peuvent être réalisées en parallèle selon les dépendances
- La Phase 6 (Facturation) peut être reportée après le MVP

---

**Dernière mise à jour** : Janvier 2025
