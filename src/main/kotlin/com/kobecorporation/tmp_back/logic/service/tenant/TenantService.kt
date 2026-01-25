package com.kobecorporation.tmp_back.logic.service.tenant

import com.kobecorporation.tmp_back.configuration.tenant.TenantContext
import com.kobecorporation.tmp_back.configuration.tenant.TenantNotFoundException
import com.kobecorporation.tmp_back.interaction.exception.ResourceAlreadyExistsException
import com.kobecorporation.tmp_back.interaction.exception.ResourceNotFoundException
import com.kobecorporation.tmp_back.logic.model.tenant.*
import com.kobecorporation.tmp_back.logic.model.users.Role
import com.kobecorporation.tmp_back.logic.model.users.User
import com.kobecorporation.tmp_back.logic.repository.tenant.TenantRepository
import com.kobecorporation.tmp_back.logic.repository.users.UserRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Service de gestion des tenants
 * 
 * Responsabilités :
 * - Création de tenant avec owner
 * - Mise à jour des paramètres
 * - Gestion des domaines personnalisés
 * - Vérification des limites et quotas
 */
@Service
class TenantService(
    private val tenantRepository: TenantRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${tenant.trial-days:14}") private val trialDays: Int,
    @Value("\${tenant.platform-domain:kobecorporation.com}") private val platformDomain: String
) {
    
    private val logger = LoggerFactory.getLogger(TenantService::class.java)
    
    // ===== CRÉATION DE TENANT =====
    
    /**
     * Crée un nouveau tenant avec son propriétaire (owner)
     * 
     * Flow :
     * 1. Valider le slug (format, unicité, mots réservés)
     * 2. Créer l'utilisateur owner
     * 3. Créer le tenant
     * 4. Lier l'owner au tenant
     * 
     * @return Pair<Tenant, User> le tenant créé et son propriétaire
     */
    fun createTenantWithOwner(
        tenantName: String,
        slug: String,
        ownerEmail: String,
        ownerPassword: String,
        ownerFirstName: String,
        ownerLastName: String,
        ownerUsername: String
    ): Mono<Pair<Tenant, User>> {
        logger.info("🏢 [TENANT] Création d'un nouveau tenant : $tenantName (slug: $slug)")
        
        return validateSlug(slug)
            .then(checkSlugAvailability(slug))
            .then(checkEmailAvailability(ownerEmail))
            .then(checkUsernameAvailability(ownerUsername))
            .then(Mono.defer {
                // Créer d'abord un ID pour le tenant
                val tenantId = ObjectId()
                
                // Créer l'utilisateur owner
                val owner = User(
                    tenantId = tenantId,
                    tenantRole = TenantRole.OWNER,
                    username = ownerUsername.lowercase(),
                    email = ownerEmail.lowercase(),
                    password = passwordEncoder.encode(ownerPassword),
                    firstName = ownerFirstName,
                    lastName = ownerLastName,
                    role = Role.ROOT_ADMIN, // Le owner est ROOT_ADMIN de son tenant
                    isEmailVerified = false // Devra vérifier son email
                )
                
                userRepository.save(owner)
                    .flatMap { savedOwner ->
                        // Créer le tenant avec l'ID du owner
                        val trialEndsAt = Instant.now().plusSeconds(trialDays.toLong() * 24 * 60 * 60)
                        
                        val tenant = Tenant(
                            id = tenantId,
                            name = tenantName,
                            slug = slug.lowercase(),
                            plan = TenantPlan.FREE,
                            status = TenantStatus.TRIAL,
                            ownerId = savedOwner.id,
                            trialEndsAt = trialEndsAt
                        )
                        
                        tenantRepository.save(tenant)
                            .map { savedTenant ->
                                logger.info("✅ [TENANT] Tenant créé : ${savedTenant.name} (ID: ${savedTenant.id})")
                                logger.info("✅ [TENANT] Owner : ${savedOwner.email} (ID: ${savedOwner.id})")
                                Pair(savedTenant, savedOwner)
                            }
                    }
            })
    }
    
    // ===== RECHERCHE DE TENANT =====
    
    /**
     * Trouve un tenant par son ID
     */
    fun getTenantById(tenantId: ObjectId): Mono<Tenant> {
        return tenantRepository.findById(tenantId)
            .switchIfEmpty(Mono.error(TenantNotFoundException("Tenant non trouvé : $tenantId")))
    }
    
    /**
     * Trouve un tenant par son slug
     */
    fun getTenantBySlug(slug: String): Mono<Tenant> {
        return tenantRepository.findBySlug(slug.lowercase())
            .switchIfEmpty(Mono.error(TenantNotFoundException("Tenant non trouvé : $slug")))
    }
    
    /**
     * Trouve un tenant par son domaine personnalisé
     */
    fun getTenantByCustomDomain(domain: String): Mono<Tenant> {
        return tenantRepository.findByCustomDomain(domain.lowercase())
            .switchIfEmpty(Mono.error(TenantNotFoundException("Tenant non trouvé pour le domaine : $domain")))
    }
    
    /**
     * Résout un tenant depuis un host (domaine custom ou sous-domaine par défaut)
     */
    fun resolveTenantFromHost(host: String): Mono<Tenant> {
        val hostLower = host.lowercase()
        
        // Vérifier si c'est un domaine personnalisé
        return if (!hostLower.endsWith(".$platformDomain") && hostLower != platformDomain) {
            // Domaine personnalisé
            getTenantByCustomDomain(hostLower)
        } else {
            // Sous-domaine par défaut : kb-saas-{slug}.kobecorporation.com
            val subdomain = hostLower.removeSuffix(".$platformDomain")
            val slug = subdomain.removePrefix("kb-saas-")
            if (slug.isNotBlank() && slug != subdomain) {
                getTenantBySlug(slug)
            } else {
                Mono.empty()
            }
        }
    }
    
    /**
     * Récupère le tenant courant depuis le contexte
     */
    fun getCurrentTenant(): Mono<Tenant> {
        return TenantContext.getCurrentTenant()
    }
    
    // ===== MISE À JOUR DE TENANT =====
    
    /**
     * Met à jour les informations d'un tenant
     */
    fun updateTenant(
        tenantId: ObjectId,
        name: String? = null,
        settings: TenantSettings? = null
    ): Mono<Tenant> {
        return getTenantById(tenantId)
            .flatMap { tenant ->
                val updated = tenant.copy(
                    name = name ?: tenant.name,
                    settings = settings ?: tenant.settings,
                    updatedAt = Instant.now()
                )
                tenantRepository.save(updated)
            }
            .doOnSuccess { logger.info("✅ [TENANT] Tenant mis à jour : ${it.name}") }
    }
    
    /**
     * Configure un domaine personnalisé pour le tenant
     */
    fun setCustomDomain(tenantId: ObjectId, customDomain: String?): Mono<Tenant> {
        return getTenantById(tenantId)
            .flatMap { tenant ->
                // Vérifier que le plan permet les domaines personnalisés
                if (customDomain != null && !tenant.canUseCustomDomain()) {
                    return@flatMap Mono.error<Tenant>(
                        IllegalStateException("Votre plan ne permet pas les domaines personnalisés. Passez au plan Pro ou supérieur.")
                    )
                }
                
                // Vérifier que le domaine n'est pas déjà utilisé
                if (customDomain != null) {
                    return@flatMap tenantRepository.existsByCustomDomain(customDomain.lowercase())
                        .flatMap { exists ->
                            if (exists) {
                                Mono.error(ResourceAlreadyExistsException("Ce domaine est déjà utilisé par un autre tenant"))
                            } else {
                                val updated = tenant.copy(
                                    customDomain = customDomain.lowercase(),
                                    updatedAt = Instant.now()
                                )
                                tenantRepository.save(updated)
                            }
                        }
                } else {
                    // Supprimer le domaine personnalisé
                    val updated = tenant.copy(
                        customDomain = null,
                        updatedAt = Instant.now()
                    )
                    tenantRepository.save(updated)
                }
            }
            .doOnSuccess { logger.info("✅ [TENANT] Domaine personnalisé mis à jour : ${it.customDomain}") }
    }
    
    /**
     * Met à jour le statut d'un tenant
     */
    fun updateTenantStatus(tenantId: ObjectId, status: TenantStatus): Mono<Tenant> {
        return getTenantById(tenantId)
            .flatMap { tenant ->
                val updated = tenant.copy(
                    status = status,
                    updatedAt = Instant.now()
                )
                tenantRepository.save(updated)
            }
            .doOnSuccess { logger.info("✅ [TENANT] Statut mis à jour : ${it.slug} → ${it.status}") }
    }
    
    // ===== MEMBRES DU TENANT =====
    
    /**
     * Récupère tous les membres d'un tenant
     */
    fun getTenantMembers(tenantId: ObjectId): Flux<User> {
        return userRepository.findByTenantId(tenantId)
    }
    
    /**
     * Compte les membres d'un tenant
     */
    fun getMemberCount(tenantId: ObjectId): Mono<Long> {
        return userRepository.countByTenantId(tenantId)
    }
    
    /**
     * Vérifie si le tenant peut ajouter un nouveau membre
     */
    fun canAddMember(tenantId: ObjectId): Mono<Boolean> {
        return getTenantById(tenantId)
            .flatMap { tenant ->
                getMemberCount(tenantId)
                    .map { count -> tenant.canAddUser(count) }
            }
    }
    
    // ===== VALIDATION =====
    
    /**
     * Valide le format du slug
     */
    private fun validateSlug(slug: String): Mono<Void> {
        return if (!Tenant.isValidSlug(slug)) {
            Mono.error(IllegalArgumentException(
                "Le slug '$slug' est invalide. Il doit contenir entre 3 et 50 caractères alphanumériques et tirets, " +
                "ne pas commencer/finir par un tiret, et ne pas être un mot réservé."
            ))
        } else {
            Mono.empty()
        }
    }
    
    /**
     * Vérifie que le slug n'est pas déjà utilisé
     */
    private fun checkSlugAvailability(slug: String): Mono<Void> {
        return tenantRepository.existsBySlug(slug.lowercase())
            .flatMap { exists ->
                if (exists) {
                    Mono.error(ResourceAlreadyExistsException("Ce slug est déjà utilisé : $slug"))
                } else {
                    Mono.empty()
                }
            }
    }
    
    /**
     * Vérifie la disponibilité d'un slug (exposé pour le controller)
     */
    fun isSlugAvailable(slug: String): Mono<Boolean> {
        return if (!Tenant.isValidSlug(slug)) {
            Mono.just(false)
        } else {
            tenantRepository.existsBySlug(slug.lowercase())
                .map { exists -> !exists }
        }
    }
    
    /**
     * Vérifie que l'email n'est pas déjà utilisé
     */
    private fun checkEmailAvailability(email: String): Mono<Void> {
        return userRepository.existsByEmail(email.lowercase())
            .flatMap { exists ->
                if (exists) {
                    Mono.error(ResourceAlreadyExistsException("Cette adresse email est déjà utilisée"))
                } else {
                    Mono.empty()
                }
            }
    }
    
    /**
     * Vérifie que le username n'est pas déjà utilisé
     */
    private fun checkUsernameAvailability(username: String): Mono<Void> {
        return userRepository.existsByUsername(username.lowercase())
            .flatMap { exists ->
                if (exists) {
                    Mono.error(ResourceAlreadyExistsException("Ce nom d'utilisateur est déjà pris"))
                } else {
                    Mono.empty()
                }
            }
    }
    
    // ===== SUPPRESSION =====
    
    /**
     * Supprime un tenant et tous ses utilisateurs
     * ⚠️ Action irréversible - à utiliser avec précaution
     */
    fun deleteTenant(tenantId: ObjectId): Mono<Void> {
        return getTenantById(tenantId)
            .flatMap { tenant ->
                logger.warn("⚠️ [TENANT] Suppression du tenant : ${tenant.name} (${tenant.id})")
                
                // Supprimer tous les utilisateurs du tenant
                userRepository.findByTenantId(tenantId)
                    .flatMap { user -> userRepository.delete(user) }
                    .then(tenantRepository.delete(tenant))
            }
            .doOnSuccess { logger.info("✅ [TENANT] Tenant supprimé") }
    }
    
    // ===== PLATFORM ADMIN =====
    
    /**
     * Liste tous les tenants (pour Platform Admin)
     */
    fun getAllTenants(): Flux<Tenant> {
        return tenantRepository.findAll()
    }
    
    /**
     * Liste les tenants par statut (pour Platform Admin)
     */
    fun getTenantsByStatus(status: TenantStatus): Flux<Tenant> {
        return tenantRepository.findByStatus(status)
    }
    
    /**
     * Recherche de tenants par nom (pour Platform Admin)
     */
    fun searchTenants(query: String): Flux<Tenant> {
        return tenantRepository.findByNameContainingIgnoreCase(query)
    }
}
