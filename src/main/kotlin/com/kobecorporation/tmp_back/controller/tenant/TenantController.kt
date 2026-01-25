package com.kobecorporation.tmp_back.controller.tenant

import com.kobecorporation.tmp_back.configuration.tenant.TenantContext
import com.kobecorporation.tmp_back.interaction.dto.tenant.request.*
import com.kobecorporation.tmp_back.interaction.dto.tenant.response.*
import com.kobecorporation.tmp_back.logic.model.tenant.TenantRole
import com.kobecorporation.tmp_back.logic.model.users.User
import com.kobecorporation.tmp_back.logic.service.tenant.InvitationService
import com.kobecorporation.tmp_back.logic.service.tenant.TenantService
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * Controller pour la gestion des tenants
 * 
 * Routes :
 * - POST /api/tenants/signup : Créer un nouveau tenant avec owner
 * - GET /api/tenants/check-slug/{slug} : Vérifier disponibilité d'un slug
 * - GET /api/tenants/me : Informations du tenant courant
 * - PUT /api/tenants/me : Mettre à jour le tenant courant
 * - PUT /api/tenants/me/domain : Configurer le domaine personnalisé
 * - GET /api/tenants/me/members : Liste des membres
 * - POST /api/tenants/me/invitations : Inviter un membre
 * - GET /api/tenants/me/invitations : Liste des invitations
 * - DELETE /api/tenants/me/invitations/{id} : Annuler une invitation
 * - POST /api/tenants/me/invitations/{id}/resend : Renvoyer une invitation
 */
@RestController
@RequestMapping("/api/tenants")
class TenantController(
    private val tenantService: TenantService,
    private val invitationService: InvitationService
) {
    
    private val logger = LoggerFactory.getLogger(TenantController::class.java)
    
    // ===== CRÉATION DE TENANT (PUBLIC) =====
    
    /**
     * Crée un nouveau tenant avec son propriétaire
     * Route publique - pas besoin d'authentification
     */
    @PostMapping("/signup")
    fun signup(
        @RequestBody request: CreateTenantRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val requestId = UUID.randomUUID().toString().take(8)
        logger.info("🏢 [$requestId] POST /api/tenants/signup - Création tenant : ${request.name}")
        
        return tenantService.createTenantWithOwner(
            tenantName = request.name,
            slug = request.slug,
            ownerEmail = request.ownerEmail,
            ownerPassword = request.ownerPassword,
            ownerFirstName = request.ownerFirstName,
            ownerLastName = request.ownerLastName,
            ownerUsername = request.ownerUsername
        )
            .map { (tenant, owner) ->
                ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
                    "success" to true,
                    "message" to "Tenant créé avec succès. Un email de vérification a été envoyé.",
                    "tenant" to TenantResponse.fromTenant(tenant),
                    "owner" to mapOf(
                        "id" to owner.id.toHexString(),
                        "email" to owner.email,
                        "username" to owner.username
                    )
                ))
            }
            .onErrorResume { error ->
                logger.error("❌ [$requestId] Erreur lors de la création du tenant", error)
                Mono.just(ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "message" to (error.message ?: "Erreur lors de la création du tenant")
                )))
            }
    }
    
    /**
     * Vérifie la disponibilité d'un slug
     * Route publique
     */
    @GetMapping("/check-slug/{slug}")
    fun checkSlugAvailability(
        @PathVariable slug: String
    ): Mono<ResponseEntity<Map<String, Any>>> {
        return tenantService.isSlugAvailable(slug)
            .map { available ->
                ResponseEntity.ok(mapOf(
                    "slug" to slug,
                    "available" to available,
                    "domain" to "kb-saas-$slug.kobecorporation.com"
                ))
            }
    }
    
    // ===== INFORMATIONS DU TENANT COURANT =====
    
    /**
     * Récupère les informations du tenant courant
     */
    @GetMapping("/me")
    fun getCurrentTenant(
        @AuthenticationPrincipal user: User
    ): Mono<ResponseEntity<TenantResponse>> {
        return TenantContext.requireCurrentTenant()
            .flatMap { tenant ->
                tenantService.getMemberCount(tenant.id)
                    .map { count ->
                        ResponseEntity.ok(TenantResponse.fromTenant(tenant, count))
                    }
            }
    }
    
    /**
     * Met à jour le tenant courant
     */
    @PutMapping("/me")
    fun updateCurrentTenant(
        @AuthenticationPrincipal user: User,
        @RequestBody request: UpdateTenantRequest
    ): Mono<ResponseEntity<TenantResponse>> {
        // Vérifier que l'utilisateur est OWNER ou ADMIN
        if (!user.tenantRole.isAtLeast(TenantRole.ADMIN)) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build())
        }
        
        return TenantContext.requireCurrentTenantId()
            .flatMap { tenantId ->
                tenantService.updateTenant(
                    tenantId = tenantId,
                    name = request.name,
                    settings = request.settings
                )
            }
            .flatMap { tenant ->
                tenantService.getMemberCount(tenant.id)
                    .map { count ->
                        ResponseEntity.ok(TenantResponse.fromTenant(tenant, count))
                    }
            }
    }
    
    /**
     * Configure le domaine personnalisé
     */
    @PutMapping("/me/domain")
    fun setCustomDomain(
        @AuthenticationPrincipal user: User,
        @RequestBody request: SetCustomDomainRequest
    ): Mono<ResponseEntity<TenantResponse>> {
        // Seul le OWNER peut configurer le domaine
        if (user.tenantRole != TenantRole.OWNER) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build())
        }
        
        return TenantContext.requireCurrentTenantId()
            .flatMap { tenantId ->
                tenantService.setCustomDomain(tenantId, request.customDomain)
            }
            .flatMap { tenant ->
                tenantService.getMemberCount(tenant.id)
                    .map { count ->
                        ResponseEntity.ok(TenantResponse.fromTenant(tenant, count))
                    }
            }
    }
    
    // ===== MEMBRES =====
    
    /**
     * Liste les membres du tenant
     */
    @GetMapping("/me/members")
    fun getMembers(
        @AuthenticationPrincipal user: User
    ): Mono<ResponseEntity<List<MemberResponse>>> {
        return TenantContext.requireCurrentTenantId()
            .flatMap { tenantId ->
                tenantService.getTenantMembers(tenantId)
                    .map { member -> MemberResponse.fromUser(member) }
                    .collectList()
                    .map { members -> ResponseEntity.ok(members) }
            }
    }
    
    // ===== INVITATIONS =====
    
    /**
     * Invite un nouveau membre
     */
    @PostMapping("/me/invitations")
    fun inviteMember(
        @AuthenticationPrincipal user: User,
        @RequestBody request: InviteMemberRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        // Vérifier que l'utilisateur peut inviter
        if (!user.tenantRole.isAtLeast(TenantRole.ADMIN)) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build())
        }
        
        return TenantContext.requireCurrentTenantId()
            .flatMap { tenantId ->
                invitationService.createInvitation(
                    tenantId = tenantId,
                    email = request.email,
                    role = request.role ?: TenantRole.MEMBER,
                    invitedBy = user.id
                )
            }
            .map { invitation ->
                ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
                    "success" to true,
                    "message" to "Invitation envoyée à ${invitation.email}",
                    "invitation" to InvitationResponse.fromInvitation(invitation)
                ))
            }
            .onErrorResume { error ->
                Mono.just(ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "message" to (error.message ?: "Erreur lors de l'envoi de l'invitation")
                )))
            }
    }
    
    /**
     * Liste les invitations du tenant
     */
    @GetMapping("/me/invitations")
    fun getInvitations(
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = "false") all: Boolean
    ): Mono<ResponseEntity<List<InvitationResponse>>> {
        // Vérifier que l'utilisateur peut voir les invitations
        if (!user.tenantRole.isAtLeast(TenantRole.ADMIN)) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build())
        }
        
        return TenantContext.requireCurrentTenantId()
            .flatMap { tenantId ->
                val invitations = if (all) {
                    invitationService.getAllInvitations(tenantId)
                } else {
                    invitationService.getPendingInvitations(tenantId)
                }
                
                invitations
                    .map { invitation -> InvitationResponse.fromInvitation(invitation) }
                    .collectList()
                    .map { list -> ResponseEntity.ok(list) }
            }
    }
    
    /**
     * Annule une invitation
     */
    @DeleteMapping("/me/invitations/{invitationId}")
    fun cancelInvitation(
        @AuthenticationPrincipal user: User,
        @PathVariable invitationId: String
    ): Mono<ResponseEntity<Map<String, Any>>> {
        if (!user.tenantRole.isAtLeast(TenantRole.ADMIN)) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build())
        }
        
        return invitationService.cancelInvitation(
            invitationId = ObjectId(invitationId),
            cancelledBy = user.id
        )
            .map {
                ResponseEntity.ok(mapOf<String, Any>(
                    "success" to true,
                    "message" to "Invitation annulée"
                ))
            }
            .onErrorResume { error ->
                Mono.just(ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "message" to (error.message ?: "Erreur")
                )))
            }
    }
    
    /**
     * Renvoie une invitation
     */
    @PostMapping("/me/invitations/{invitationId}/resend")
    fun resendInvitation(
        @AuthenticationPrincipal user: User,
        @PathVariable invitationId: String
    ): Mono<ResponseEntity<Map<String, Any>>> {
        if (!user.tenantRole.isAtLeast(TenantRole.ADMIN)) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build())
        }
        
        return invitationService.resendInvitation(ObjectId(invitationId))
            .map { invitation ->
                ResponseEntity.ok(mapOf<String, Any>(
                    "success" to true,
                    "message" to "Invitation renvoyée à ${invitation.email}"
                ))
            }
            .onErrorResume { error ->
                Mono.just(ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "message" to (error.message ?: "Erreur")
                )))
            }
    }
}

/**
 * Controller pour accepter les invitations (public)
 */
@RestController
@RequestMapping("/api/invitations")
class InvitationController(
    private val invitationService: InvitationService
) {
    
    private val logger = LoggerFactory.getLogger(InvitationController::class.java)
    
    /**
     * Récupère les infos d'une invitation (pour affichage)
     */
    @GetMapping("/{token}")
    fun getInvitationInfo(
        @PathVariable token: String
    ): Mono<ResponseEntity<Map<String, Any>>> {
        return invitationService.getInvitationInfo(token)
            .map { info -> ResponseEntity.ok(info) }
            .onErrorResume { error ->
                Mono.just(ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "message" to (error.message ?: "Invitation invalide")
                )))
            }
    }
    
    /**
     * Accepte une invitation et crée le compte
     */
    @PostMapping("/{token}/accept")
    fun acceptInvitation(
        @PathVariable token: String,
        @RequestBody request: AcceptInvitationRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        logger.info("🎉 POST /api/invitations/$token/accept")
        
        return invitationService.acceptInvitation(
            token = token,
            username = request.username,
            password = request.password,
            firstName = request.firstName,
            lastName = request.lastName
        )
            .map { user ->
                ResponseEntity.status(HttpStatus.CREATED).body(mapOf(
                    "success" to true,
                    "message" to "Bienvenue ! Votre compte a été créé.",
                    "user" to mapOf(
                        "id" to user.id.toHexString(),
                        "email" to user.email,
                        "username" to user.username,
                        "tenantRole" to user.tenantRole.displayName
                    )
                ))
            }
            .onErrorResume { error ->
                logger.error("❌ Erreur lors de l'acceptation de l'invitation", error)
                Mono.just(ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "message" to (error.message ?: "Erreur lors de l'acceptation")
                )))
            }
    }
}
