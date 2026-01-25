package com.kobecorporation.tmp_back.logic.model.users

import com.kobecorporation.tmp_back.logic.model.tenant.TenantRole
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate

/**
 * Modèle utilisateur avec support multi-tenant
 * 
 * Architecture multi-tenant :
 * - tenantId : Identifie le tenant auquel appartient l'utilisateur
 *   - null = Platform Admin (accès à tous les tenants)
 * - tenantRole : Rôle de l'utilisateur au sein de son tenant
 * - role : Rôle global (pour compatibilité et platform admin)
 * 
 * Index composés pour garantir l'unicité par tenant :
 * - (tenantId, email) : Un email unique par tenant
 * - (tenantId, username) : Un username unique par tenant
 */
@Document(collection = "users")
@CompoundIndexes(
    CompoundIndex(name = "tenant_email_idx", def = "{'tenantId': 1, 'email': 1}", unique = true),
    CompoundIndex(name = "tenant_username_idx", def = "{'tenantId': 1, 'username': 1}", unique = true)
)
data class User(
    @Id
    val id: ObjectId = ObjectId(),

    // ===== MULTI-TENANT =====
    /**
     * ID du tenant auquel appartient l'utilisateur
     * null = Platform Admin (super admin sans tenant)
     */
    @Indexed
    val tenantId: ObjectId? = null,
    
    /**
     * Rôle de l'utilisateur au sein de son tenant
     * OWNER, ADMIN, MEMBER, GUEST
     */
    val tenantRole: TenantRole = TenantRole.MEMBER,

    // ===== IDENTIFIANTS =====
    /**
     * Username unique au sein du tenant
     */
    val username: String,
    
    /**
     * Email unique au sein du tenant
     */
    val email: String,
    val password: String?,

    val firstName: String,
    val lastName: String,

    val birthDate: LocalDate? = null,
    val gender: Gender? = null,

    /**
     * Rôle global (pour compatibilité et distinction platform admin)
     * PLATFORM_ADMIN = Super admin qui gère tous les tenants
     */
    val role: Role = Role.USER,

    val isActive: Boolean = true,
    val isEmailVerified: Boolean = false,

    val profilePicture: String? = null,
    val bio: String? = null,
    val website: String? = null,
    val socialLinks: SocialLinks = SocialLinks(),
    
    // Refresh token pour gérer les sessions (stocké par user)
    val refreshToken: String? = null,
    val refreshTokenExpiresAt: Instant? = null,
    
    // Vérification d'email
    val emailVerificationCode: String? = null,
    val emailVerificationCodeExpiresAt: Instant? = null,
    
    // Réinitialisation de mot de passe
    val passwordResetToken: String? = null,
    val passwordResetTokenExpiresAt: Instant? = null,
    
    // Tracking
    @Indexed
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val lastLoginAt: Instant? = null,
    
    // Pour le tracking des tentatives de connexion
    val failedLoginAttempts: Int = 0,
    val lockedUntil: Instant? = null
) {
    /**
     * Vérifie si le compte est verrouillé
     */
    fun isLocked(): Boolean {
        return lockedUntil != null && lockedUntil!!.isAfter(Instant.now())
    }
    
    /**
     * Vérifie si l'utilisateur peut se connecter
     */
    fun canLogin(): Boolean {
        return isActive && !isLocked() && isEmailVerified
    }
    
    /**
     * Vérifie si l'utilisateur a un refresh token valide
     */
    fun hasValidRefreshToken(): Boolean {
        return refreshToken != null && 
               refreshTokenExpiresAt != null && 
               refreshTokenExpiresAt!!.isAfter(Instant.now())
    }
    
    /**
     * Vérifie si le code de vérification d'email est valide
     */
    fun hasValidVerificationCode(code: String): Boolean {
        return emailVerificationCode != null &&
               emailVerificationCode == code &&
               emailVerificationCodeExpiresAt != null &&
               emailVerificationCodeExpiresAt!!.isAfter(Instant.now())
    }
    
    /**
     * Vérifie si le token de réinitialisation de mot de passe est valide
     */
    fun hasValidPasswordResetToken(token: String): Boolean {
        return passwordResetToken != null &&
               passwordResetToken == token &&
               passwordResetTokenExpiresAt != null &&
               passwordResetTokenExpiresAt!!.isAfter(Instant.now())
    }
    
    /**
     * Nom complet de l'utilisateur
     */
    val fullName: String
        get() = "$firstName $lastName"
    
    /**
     * Vérifie si l'utilisateur est un Platform Admin (sans tenant)
     */
    fun isPlatformAdmin(): Boolean {
        return tenantId == null && role == Role.PLATFORM_ADMIN
    }
    
    /**
     * Vérifie si l'utilisateur est propriétaire de son tenant
     */
    fun isTenantOwner(): Boolean {
        return tenantRole == TenantRole.OWNER
    }
    
    /**
     * Vérifie si l'utilisateur peut gérer les membres de son tenant
     */
    fun canManageMembers(): Boolean {
        return tenantRole.isAtLeast(TenantRole.ADMIN)
    }
    
    /**
     * Vérifie si l'utilisateur appartient à un tenant spécifique
     */
    fun belongsToTenant(otherTenantId: ObjectId): Boolean {
        return tenantId == otherTenantId
    }
}
