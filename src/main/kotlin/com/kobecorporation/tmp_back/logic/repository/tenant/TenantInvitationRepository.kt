package com.kobecorporation.tmp_back.logic.repository.tenant

import com.kobecorporation.tmp_back.logic.model.tenant.InvitationStatus
import com.kobecorporation.tmp_back.logic.model.tenant.TenantInvitation
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Repository pour les invitations tenant
 */
@Repository
interface TenantInvitationRepository : ReactiveMongoRepository<TenantInvitation, ObjectId> {
    
    /**
     * Trouve une invitation par son token
     */
    fun findByToken(token: String): Mono<TenantInvitation>
    
    /**
     * Trouve toutes les invitations d'un tenant
     */
    fun findByTenantId(tenantId: ObjectId): Flux<TenantInvitation>
    
    /**
     * Trouve les invitations d'un tenant par statut
     */
    fun findByTenantIdAndStatus(tenantId: ObjectId, status: InvitationStatus): Flux<TenantInvitation>
    
    /**
     * Trouve les invitations en attente d'un tenant
     */
    fun findByTenantIdAndStatusOrderByCreatedAtDesc(
        tenantId: ObjectId, 
        status: InvitationStatus = InvitationStatus.PENDING
    ): Flux<TenantInvitation>
    
    /**
     * Trouve une invitation par email et tenant
     */
    fun findByTenantIdAndEmail(tenantId: ObjectId, email: String): Mono<TenantInvitation>
    
    /**
     * Trouve une invitation en attente par email et tenant
     */
    fun findByTenantIdAndEmailAndStatus(
        tenantId: ObjectId, 
        email: String, 
        status: InvitationStatus
    ): Mono<TenantInvitation>
    
    /**
     * Vérifie si une invitation existe déjà pour cet email dans ce tenant
     */
    fun existsByTenantIdAndEmailAndStatus(
        tenantId: ObjectId, 
        email: String, 
        status: InvitationStatus
    ): Mono<Boolean>
    
    /**
     * Compte les invitations en attente d'un tenant
     */
    fun countByTenantIdAndStatus(tenantId: ObjectId, status: InvitationStatus): Mono<Long>
    
    /**
     * Trouve les invitations envoyées par un utilisateur
     */
    fun findByInvitedBy(invitedBy: ObjectId): Flux<TenantInvitation>
    
    /**
     * Supprime les invitations expirées
     */
    fun deleteByStatusAndExpiresAtBefore(status: InvitationStatus, expiresAt: java.time.Instant): Mono<Long>
}
