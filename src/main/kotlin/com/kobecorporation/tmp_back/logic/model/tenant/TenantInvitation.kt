package com.kobecorporation.tmp_back.logic.model.tenant

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * Invitation à rejoindre un tenant
 * 
 * Permet aux admins d'un tenant d'inviter de nouveaux membres par email.
 * L'invitation contient un token unique et expire après un certain temps.
 */
@Document(collection = "tenant_invitations")
data class TenantInvitation(
    @Id
    val id: ObjectId = ObjectId(),
    
    /**
     * ID du tenant auquel l'utilisateur est invité
     */
    @Indexed
    val tenantId: ObjectId,
    
    /**
     * Email de la personne invitée
     */
    @Indexed
    val email: String,
    
    /**
     * Rôle qui sera attribué à l'utilisateur dans le tenant
     */
    val role: TenantRole = TenantRole.MEMBER,
    
    /**
     * Token unique pour accepter l'invitation
     */
    @Indexed(unique = true)
    val token: String,
    
    /**
     * ID de l'utilisateur qui a envoyé l'invitation
     */
    val invitedBy: ObjectId,
    
    /**
     * Date d'expiration de l'invitation
     */
    val expiresAt: Instant,
    
    /**
     * Date d'acceptation (null si pas encore acceptée)
     */
    val acceptedAt: Instant? = null,
    
    /**
     * Date d'annulation (null si pas annulée)
     */
    val cancelledAt: Instant? = null,
    
    /**
     * Statut de l'invitation
     */
    val status: InvitationStatus = InvitationStatus.PENDING,
    
    /**
     * Nombre d'emails envoyés (pour les renvois)
     */
    val emailsSent: Int = 1,
    
    /**
     * Date de création
     */
    val createdAt: Instant = Instant.now()
) {
    
    /**
     * Vérifie si l'invitation est valide (non expirée, non utilisée)
     */
    fun isValid(): Boolean {
        return status == InvitationStatus.PENDING && 
               expiresAt.isAfter(Instant.now())
    }
    
    /**
     * Vérifie si l'invitation est expirée
     */
    fun isExpired(): Boolean {
        return expiresAt.isBefore(Instant.now())
    }
    
    /**
     * Vérifie si l'invitation peut être renvoyée
     */
    fun canResend(): Boolean {
        return status == InvitationStatus.PENDING && emailsSent < MAX_RESEND_COUNT
    }
    
    companion object {
        /**
         * Durée de validité par défaut (7 jours)
         */
        const val DEFAULT_EXPIRATION_DAYS = 7L
        
        /**
         * Nombre maximum de renvois d'email
         */
        const val MAX_RESEND_COUNT = 5
    }
}

/**
 * Statuts possibles pour une invitation
 */
enum class InvitationStatus {
    /**
     * Invitation envoyée, en attente d'acceptation
     */
    PENDING,
    
    /**
     * Invitation acceptée, utilisateur créé
     */
    ACCEPTED,
    
    /**
     * Invitation expirée
     */
    EXPIRED,
    
    /**
     * Invitation annulée par l'admin
     */
    CANCELLED,
    
    /**
     * Invitation refusée par l'invité
     */
    DECLINED
}
