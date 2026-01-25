package com.kobecorporation.tmp_back.logic.service.tenant

import com.kobecorporation.tmp_back.interaction.exception.AuthenticationException
import com.kobecorporation.tmp_back.interaction.exception.ResourceAlreadyExistsException
import com.kobecorporation.tmp_back.interaction.exception.ResourceNotFoundException
import com.kobecorporation.tmp_back.logic.model.tenant.*
import com.kobecorporation.tmp_back.logic.model.users.Role
import com.kobecorporation.tmp_back.logic.model.users.User
import com.kobecorporation.tmp_back.logic.repository.tenant.TenantInvitationRepository
import com.kobecorporation.tmp_back.logic.repository.tenant.TenantRepository
import com.kobecorporation.tmp_back.logic.repository.users.UserRepository
import com.kobecorporation.tmp_back.logic.service.email.EmailService
import com.kobecorporation.tmp_back.util.CodeGenerator
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Service de gestion des invitations tenant
 * 
 * Permet aux admins d'inviter des utilisateurs à rejoindre leur tenant
 */
@Service
class InvitationService(
    private val invitationRepository: TenantInvitationRepository,
    private val tenantRepository: TenantRepository,
    private val userRepository: UserRepository,
    private val tenantService: TenantService,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder
) {
    
    private val logger = LoggerFactory.getLogger(InvitationService::class.java)
    
    // ===== CRÉATION D'INVITATION =====
    
    /**
     * Crée et envoie une invitation
     */
    fun createInvitation(
        tenantId: ObjectId,
        email: String,
        role: TenantRole,
        invitedBy: ObjectId
    ): Mono<TenantInvitation> {
        val normalizedEmail = email.lowercase()
        
        logger.info("📧 [INVITATION] Création d'une invitation pour $normalizedEmail dans le tenant $tenantId")
        
        return validateInvitation(tenantId, normalizedEmail, invitedBy)
            .then(Mono.defer {
                val token = CodeGenerator.generateSecureToken()
                val expiresAt = Instant.now().plusSeconds(
                    TenantInvitation.DEFAULT_EXPIRATION_DAYS * 24 * 60 * 60
                )
                
                val invitation = TenantInvitation(
                    tenantId = tenantId,
                    email = normalizedEmail,
                    role = role,
                    token = token,
                    invitedBy = invitedBy,
                    expiresAt = expiresAt
                )
                
                invitationRepository.save(invitation)
            })
            .flatMap { invitation ->
                // Récupérer le tenant pour l'email
                tenantRepository.findById(tenantId)
                    .flatMap { tenant ->
                        // Récupérer l'inviteur pour l'email
                        userRepository.findById(invitedBy)
                            .flatMap { inviter ->
                                // Envoyer l'email d'invitation
                                sendInvitationEmail(invitation, tenant, inviter)
                                    .thenReturn(invitation)
                            }
                    }
            }
            .doOnSuccess { 
                logger.info("✅ [INVITATION] Invitation créée et envoyée à ${it.email}")
            }
    }
    
    /**
     * Valide qu'une invitation peut être créée
     */
    private fun validateInvitation(
        tenantId: ObjectId,
        email: String,
        invitedBy: ObjectId
    ): Mono<Void> {
        return tenantService.getTenantById(tenantId)
            .flatMap { tenant ->
                // Vérifier que le tenant peut ajouter un membre
                tenantService.canAddMember(tenantId)
                    .flatMap { canAdd ->
                        if (!canAdd) {
                            Mono.error(IllegalStateException(
                                "Le tenant a atteint sa limite de membres pour ce plan. " +
                                "Passez à un plan supérieur pour inviter plus de membres."
                            ))
                        } else {
                            Mono.empty()
                        }
                    }
            }
            .then(
                // Vérifier que l'email n'est pas déjà membre du tenant
                userRepository.existsByTenantIdAndEmail(tenantId, email)
                    .flatMap { exists ->
                        if (exists) {
                            Mono.error(ResourceAlreadyExistsException(
                                "Cet utilisateur est déjà membre de ce tenant"
                            ))
                        } else {
                            Mono.empty()
                        }
                    }
            )
            .then(
                // Vérifier qu'il n'y a pas déjà une invitation en attente
                invitationRepository.existsByTenantIdAndEmailAndStatus(
                    tenantId, email, InvitationStatus.PENDING
                )
                    .flatMap { exists ->
                        if (exists) {
                            Mono.error(ResourceAlreadyExistsException(
                                "Une invitation est déjà en attente pour cet email"
                            ))
                        } else {
                            Mono.empty()
                        }
                    }
            )
    }
    
    /**
     * Envoie l'email d'invitation
     */
    private fun sendInvitationEmail(
        invitation: TenantInvitation,
        tenant: Tenant,
        inviter: User
    ): Mono<Void> {
        // TODO: Créer une méthode spécifique dans EmailService pour les invitations
        // Pour l'instant, on utilise un log
        logger.info("📧 [INVITATION] Email envoyé à ${invitation.email}")
        logger.info("   Tenant: ${tenant.name}")
        logger.info("   Invité par: ${inviter.fullName}")
        logger.info("   Token: ${invitation.token}")
        logger.info("   Lien: https://${tenant.activeDomain}/invitation?token=${invitation.token}")
        
        return Mono.empty()
    }
    
    // ===== ACCEPTATION D'INVITATION =====
    
    /**
     * Accepte une invitation et crée l'utilisateur
     */
    fun acceptInvitation(
        token: String,
        username: String,
        password: String,
        firstName: String,
        lastName: String
    ): Mono<User> {
        logger.info("🎉 [INVITATION] Acceptation de l'invitation avec token: ${token.take(10)}...")
        
        return getValidInvitation(token)
            .flatMap { invitation ->
                // Vérifier que le username n'est pas déjà pris dans ce tenant
                userRepository.existsByTenantIdAndUsername(invitation.tenantId, username.lowercase())
                    .flatMap { exists ->
                        if (exists) {
                            Mono.error(ResourceAlreadyExistsException(
                                "Ce nom d'utilisateur est déjà pris dans ce tenant"
                            ))
                        } else {
                            // Créer l'utilisateur
                            val user = User(
                                tenantId = invitation.tenantId,
                                tenantRole = invitation.role,
                                username = username.lowercase(),
                                email = invitation.email,
                                password = passwordEncoder.encode(password),
                                firstName = firstName,
                                lastName = lastName,
                                role = Role.USER,
                                isEmailVerified = true // L'email est déjà vérifié via l'invitation
                            )
                            
                            userRepository.save(user)
                                .flatMap { savedUser ->
                                    // Marquer l'invitation comme acceptée
                                    val updatedInvitation = invitation.copy(
                                        status = InvitationStatus.ACCEPTED,
                                        acceptedAt = Instant.now()
                                    )
                                    invitationRepository.save(updatedInvitation)
                                        .thenReturn(savedUser)
                                }
                        }
                    }
            }
            .doOnSuccess { 
                logger.info("✅ [INVITATION] Utilisateur créé : ${it.email} (${it.username})")
            }
    }
    
    /**
     * Récupère une invitation valide par son token
     */
    fun getValidInvitation(token: String): Mono<TenantInvitation> {
        return invitationRepository.findByToken(token)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Invitation non trouvée")))
            .flatMap { invitation ->
                when {
                    invitation.status == InvitationStatus.ACCEPTED -> {
                        Mono.error(AuthenticationException("Cette invitation a déjà été utilisée"))
                    }
                    invitation.status == InvitationStatus.CANCELLED -> {
                        Mono.error(AuthenticationException("Cette invitation a été annulée"))
                    }
                    invitation.isExpired() -> {
                        // Mettre à jour le statut
                        val updated = invitation.copy(status = InvitationStatus.EXPIRED)
                        invitationRepository.save(updated)
                            .flatMap { Mono.error(AuthenticationException("Cette invitation a expiré")) }
                    }
                    else -> Mono.just(invitation)
                }
            }
    }
    
    /**
     * Récupère les infos d'une invitation (pour affichage avant acceptation)
     */
    fun getInvitationInfo(token: String): Mono<Map<String, Any>> {
        return getValidInvitation(token)
            .flatMap { invitation ->
                tenantRepository.findById(invitation.tenantId)
                    .map { tenant ->
                        mapOf(
                            "email" to invitation.email,
                            "role" to invitation.role.displayName,
                            "tenantName" to tenant.name,
                            "tenantLogo" to (tenant.settings.logo ?: ""),
                            "expiresAt" to invitation.expiresAt.toString()
                        )
                    }
            }
    }
    
    // ===== GESTION DES INVITATIONS =====
    
    /**
     * Liste les invitations en attente d'un tenant
     */
    fun getPendingInvitations(tenantId: ObjectId): Flux<TenantInvitation> {
        return invitationRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(
            tenantId, InvitationStatus.PENDING
        )
    }
    
    /**
     * Liste toutes les invitations d'un tenant
     */
    fun getAllInvitations(tenantId: ObjectId): Flux<TenantInvitation> {
        return invitationRepository.findByTenantId(tenantId)
    }
    
    /**
     * Annule une invitation
     */
    fun cancelInvitation(invitationId: ObjectId, cancelledBy: ObjectId): Mono<TenantInvitation> {
        return invitationRepository.findById(invitationId)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Invitation non trouvée")))
            .flatMap { invitation ->
                if (invitation.status != InvitationStatus.PENDING) {
                    Mono.error(IllegalStateException("Cette invitation ne peut pas être annulée"))
                } else {
                    val updated = invitation.copy(
                        status = InvitationStatus.CANCELLED,
                        cancelledAt = Instant.now()
                    )
                    invitationRepository.save(updated)
                }
            }
            .doOnSuccess { logger.info("✅ [INVITATION] Invitation annulée : ${it.email}") }
    }
    
    /**
     * Renvoie l'email d'invitation
     */
    fun resendInvitation(invitationId: ObjectId): Mono<TenantInvitation> {
        return invitationRepository.findById(invitationId)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("Invitation non trouvée")))
            .flatMap { invitation ->
                if (!invitation.canResend()) {
                    Mono.error(IllegalStateException(
                        "Cette invitation ne peut plus être renvoyée (max ${TenantInvitation.MAX_RESEND_COUNT} envois)"
                    ))
                } else {
                    // Prolonger l'expiration
                    val newExpiresAt = Instant.now().plusSeconds(
                        TenantInvitation.DEFAULT_EXPIRATION_DAYS * 24 * 60 * 60
                    )
                    val updated = invitation.copy(
                        expiresAt = newExpiresAt,
                        emailsSent = invitation.emailsSent + 1
                    )
                    
                    invitationRepository.save(updated)
                        .flatMap { savedInvitation ->
                            // Renvoyer l'email
                            tenantRepository.findById(invitation.tenantId)
                                .flatMap { tenant ->
                                    userRepository.findById(invitation.invitedBy)
                                        .flatMap { inviter ->
                                            sendInvitationEmail(savedInvitation, tenant, inviter)
                                                .thenReturn(savedInvitation)
                                        }
                                }
                        }
                }
            }
            .doOnSuccess { logger.info("✅ [INVITATION] Email renvoyé à ${it.email}") }
    }
}
