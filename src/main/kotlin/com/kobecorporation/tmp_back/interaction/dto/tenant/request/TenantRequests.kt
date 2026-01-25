package com.kobecorporation.tmp_back.interaction.dto.tenant.request

import com.kobecorporation.tmp_back.logic.model.tenant.TenantRole
import com.kobecorporation.tmp_back.logic.model.tenant.TenantSettings
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Requête pour créer un nouveau tenant avec son propriétaire
 */
data class CreateTenantRequest(
    @field:NotBlank(message = "Le nom du tenant est requis")
    @field:Size(min = 2, max = 100, message = "Le nom doit faire entre 2 et 100 caractères")
    val name: String,
    
    @field:NotBlank(message = "Le slug est requis")
    @field:Size(min = 3, max = 50, message = "Le slug doit faire entre 3 et 50 caractères")
    @field:Pattern(
        regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$",
        message = "Le slug ne peut contenir que des lettres minuscules, chiffres et tirets"
    )
    val slug: String,
    
    @field:NotBlank(message = "L'email du propriétaire est requis")
    @field:Email(message = "Email invalide")
    val ownerEmail: String,
    
    @field:NotBlank(message = "Le mot de passe est requis")
    @field:Size(min = 8, message = "Le mot de passe doit faire au moins 8 caractères")
    val ownerPassword: String,
    
    @field:NotBlank(message = "Le prénom est requis")
    val ownerFirstName: String,
    
    @field:NotBlank(message = "Le nom est requis")
    val ownerLastName: String,
    
    @field:NotBlank(message = "Le nom d'utilisateur est requis")
    @field:Size(min = 3, max = 30, message = "Le nom d'utilisateur doit faire entre 3 et 30 caractères")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_-]+$",
        message = "Le nom d'utilisateur ne peut contenir que des lettres, chiffres, tirets et underscores"
    )
    val ownerUsername: String
)

/**
 * Requête pour mettre à jour un tenant
 */
data class UpdateTenantRequest(
    @field:Size(min = 2, max = 100, message = "Le nom doit faire entre 2 et 100 caractères")
    val name: String? = null,
    
    val settings: TenantSettings? = null
)

/**
 * Requête pour configurer un domaine personnalisé
 */
data class SetCustomDomainRequest(
    val customDomain: String? = null // null pour supprimer le domaine custom
)

/**
 * Requête pour inviter un membre
 */
data class InviteMemberRequest(
    @field:NotBlank(message = "L'email est requis")
    @field:Email(message = "Email invalide")
    val email: String,
    
    val role: TenantRole? = null // Par défaut MEMBER
)

/**
 * Requête pour accepter une invitation
 */
data class AcceptInvitationRequest(
    @field:NotBlank(message = "Le nom d'utilisateur est requis")
    @field:Size(min = 3, max = 30, message = "Le nom d'utilisateur doit faire entre 3 et 30 caractères")
    val username: String,
    
    @field:NotBlank(message = "Le mot de passe est requis")
    @field:Size(min = 8, message = "Le mot de passe doit faire au moins 8 caractères")
    val password: String,
    
    @field:NotBlank(message = "Le prénom est requis")
    val firstName: String,
    
    @field:NotBlank(message = "Le nom est requis")
    val lastName: String
)
