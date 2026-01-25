package com.kobecorporation.tmp_back.interaction.dto.tenant.response

import com.kobecorporation.tmp_back.logic.model.tenant.*
import com.kobecorporation.tmp_back.logic.model.users.User
import java.time.Instant

/**
 * Réponse contenant les informations d'un tenant
 */
data class TenantResponse(
    val id: String,
    val name: String,
    val slug: String,
    val customDomain: String?,
    val defaultDomain: String,
    val activeDomain: String,
    val plan: PlanInfo,
    val status: StatusInfo,
    val settings: TenantSettings,
    val memberCount: Long,
    val trialEndsAt: String?,
    val createdAt: String
) {
    companion object {
        fun fromTenant(tenant: Tenant, memberCount: Long = 0): TenantResponse {
            return TenantResponse(
                id = tenant.id.toHexString(),
                name = tenant.name,
                slug = tenant.slug,
                customDomain = tenant.customDomain,
                defaultDomain = tenant.defaultDomain,
                activeDomain = tenant.activeDomain,
                plan = PlanInfo(
                    name = tenant.plan.name,
                    displayName = tenant.plan.displayName,
                    maxUsers = tenant.plan.maxUsers,
                    maxStorageMB = tenant.plan.maxStorageMB,
                    monthlyPrice = tenant.plan.monthlyPriceEuros
                ),
                status = StatusInfo(
                    name = tenant.status.name,
                    displayName = tenant.status.displayName,
                    isAccessible = tenant.status.isAccessible
                ),
                settings = tenant.settings,
                memberCount = memberCount,
                trialEndsAt = tenant.trialEndsAt?.toString(),
                createdAt = tenant.createdAt.toString()
            )
        }
    }
}

/**
 * Informations sur le plan
 */
data class PlanInfo(
    val name: String,
    val displayName: String,
    val maxUsers: Int,
    val maxStorageMB: Long,
    val monthlyPrice: Int
)

/**
 * Informations sur le statut
 */
data class StatusInfo(
    val name: String,
    val displayName: String,
    val isAccessible: Boolean
)

/**
 * Réponse contenant les informations d'un membre
 */
data class MemberResponse(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val profilePicture: String?,
    val tenantRole: TenantRoleInfo,
    val isActive: Boolean,
    val isEmailVerified: Boolean,
    val lastLoginAt: String?,
    val createdAt: String
) {
    companion object {
        fun fromUser(user: User): MemberResponse {
            return MemberResponse(
                id = user.id.toHexString(),
                username = user.username,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                fullName = user.fullName,
                profilePicture = user.profilePicture,
                tenantRole = TenantRoleInfo(
                    name = user.tenantRole.name,
                    displayName = user.tenantRole.displayName,
                    level = user.tenantRole.level
                ),
                isActive = user.isActive,
                isEmailVerified = user.isEmailVerified,
                lastLoginAt = user.lastLoginAt?.toString(),
                createdAt = user.createdAt.toString()
            )
        }
    }
}

/**
 * Informations sur le rôle tenant
 */
data class TenantRoleInfo(
    val name: String,
    val displayName: String,
    val level: Int
)

/**
 * Réponse contenant les informations d'une invitation
 */
data class InvitationResponse(
    val id: String,
    val email: String,
    val role: TenantRoleInfo,
    val status: InvitationStatusInfo,
    val expiresAt: String,
    val emailsSent: Int,
    val createdAt: String,
    val acceptedAt: String?,
    val cancelledAt: String?
) {
    companion object {
        fun fromInvitation(invitation: TenantInvitation): InvitationResponse {
            return InvitationResponse(
                id = invitation.id.toHexString(),
                email = invitation.email,
                role = TenantRoleInfo(
                    name = invitation.role.name,
                    displayName = invitation.role.displayName,
                    level = invitation.role.level
                ),
                status = InvitationStatusInfo(
                    name = invitation.status.name,
                    isValid = invitation.isValid(),
                    isExpired = invitation.isExpired()
                ),
                expiresAt = invitation.expiresAt.toString(),
                emailsSent = invitation.emailsSent,
                createdAt = invitation.createdAt.toString(),
                acceptedAt = invitation.acceptedAt?.toString(),
                cancelledAt = invitation.cancelledAt?.toString()
            )
        }
    }
}

/**
 * Informations sur le statut d'invitation
 */
data class InvitationStatusInfo(
    val name: String,
    val isValid: Boolean,
    val isExpired: Boolean
)
