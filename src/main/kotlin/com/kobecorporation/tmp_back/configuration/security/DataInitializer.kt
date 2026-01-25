package com.kobecorporation.tmp_back.configuration.security

import com.kobecorporation.tmp_back.logic.model.tenant.TenantRole
import com.kobecorporation.tmp_back.logic.model.users.Gender
import com.kobecorporation.tmp_back.logic.model.users.Role
import com.kobecorporation.tmp_back.logic.model.users.User
import com.kobecorporation.tmp_back.logic.repository.users.UserRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Initialise les données par défaut au démarrage de l'application
 * 
 * Architecture Multi-Tenant :
 * - Crée le PLATFORM_ADMIN (super admin sans tenant) au premier démarrage
 * - Le PLATFORM_ADMIN peut gérer tous les tenants
 */
@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    
    // Configuration Platform Admin (super admin multi-tenant)
    @Value("\${platform.admin.email:admin@kobecorporation.com}") 
    private val platformAdminEmail: String,
    @Value("\${platform.admin.password:Platform@dmin789!}") 
    private val platformAdminPassword: String,
    @Value("\${platform.admin.username:platform-admin}") 
    private val platformAdminUsername: String,
    @Value("\${platform.admin.firstname:Platform}") 
    private val platformAdminFirstName: String,
    @Value("\${platform.admin.lastname:Admin}") 
    private val platformAdminLastName: String,
    
    // Ancienne configuration (gardée pour compatibilité)
    @Value("\${admin.email:bendjibril789@gmail.com}") 
    private val legacyAdminEmail: String,
    @Value("\${admin.password:Root@dmin789!}") 
    private val legacyAdminPassword: String,
    @Value("\${admin.username:azerty-78}") 
    private val legacyAdminUsername: String,
    @Value("\${admin.firstname:Ben}") 
    private val legacyAdminFirstName: String,
    @Value("\${admin.lastname:Djibril}") 
    private val legacyAdminLastName: String,
) {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    @PostConstruct
    fun init() {
        // Créer le Platform Admin (super admin)
        createPlatformAdmin()
            .subscribe(
                { user ->
                    logger.info("✅ Platform Admin initialized: ${user.email} (Role: ${user.role})")
                },
                { error ->
                    logger.error("❌ Failed to initialize Platform Admin: ${error.message}", error)
                }
            )
    }

    /**
     * Crée le PLATFORM_ADMIN au premier démarrage
     * 
     * Le PLATFORM_ADMIN :
     * - N'appartient à aucun tenant (tenantId = null)
     * - Peut gérer tous les tenants de la plateforme
     * - Est créé automatiquement au démarrage
     */
    private fun createPlatformAdmin(): Mono<User> {
        // Chercher un PLATFORM_ADMIN existant (par email ou par rôle)
        return userRepository.findByTenantIdIsNullAndRole(Role.PLATFORM_ADMIN)
            .next() // Prendre le premier s'il y en a plusieurs
            .switchIfEmpty(
                // Chercher aussi par email
                userRepository.findByEmail(platformAdminEmail)
            )
            .switchIfEmpty(
                Mono.defer {
                    logger.info("🔧 Creating Platform Admin (Super Admin Multi-Tenant)...")
                    logger.info("   Email: $platformAdminEmail")
                    logger.info("   Username: $platformAdminUsername")
                    
                    val platformAdmin = User(
                        tenantId = null, // ⚠️ Pas de tenant pour le Platform Admin
                        tenantRole = TenantRole.OWNER, // Techniquement pas utilisé, mais on met OWNER
                        username = platformAdminUsername,
                        email = platformAdminEmail,
                        password = passwordEncoder.encode(platformAdminPassword),
                        firstName = platformAdminFirstName,
                        lastName = platformAdminLastName,
                        role = Role.PLATFORM_ADMIN, // ⚠️ PLATFORM_ADMIN
                        isEmailVerified = true,
                        isActive = true,
                        bio = "Platform Administrator - Super Admin Multi-Tenant",
                        gender = Gender.MALE
                    )
                    userRepository.save(platformAdmin)
                }
            )
            .doOnSuccess { user ->
                if (user != null && user.role == Role.PLATFORM_ADMIN) {
                    logger.info("ℹ️ Platform Admin already exists: ${user.email}")
                }
            }
    }
}
