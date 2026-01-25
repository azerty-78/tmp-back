package com.kobecorporation.tmp_back.logic.repository.users

import com.kobecorporation.tmp_back.logic.model.tenant.TenantRole
import com.kobecorporation.tmp_back.logic.model.users.Role
import com.kobecorporation.tmp_back.logic.model.users.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Repository réactif pour les utilisateurs
 * 
 * Architecture multi-tenant :
 * - Les méthodes "ByTenantId..." filtrent par tenant pour l'isolation des données
 * - Les méthodes sans TenantId sont pour les Platform Admin ou la recherche globale
 */
@Repository
interface UserRepository : ReactiveMongoRepository<User, ObjectId> {
    
    // ===== RECHERCHE GLOBALE (sans tenant - pour Platform Admin) =====
    
    /**
     * Trouve un utilisateur par email (global - tous tenants)
     * ⚠️ Utiliser findByTenantIdAndEmail pour les opérations normales
     */
    fun findByEmail(email: String): Mono<User>
    
    /**
     * Trouve un utilisateur par username (global - tous tenants)
     * ⚠️ Utiliser findByTenantIdAndUsername pour les opérations normales
     */
    fun findByUsername(username: String): Mono<User>
    
    /**
     * Vérifie si un email existe (global)
     */
    fun existsByEmail(email: String): Mono<Boolean>
    
    /**
     * Vérifie si un username existe (global)
     */
    fun existsByUsername(username: String): Mono<Boolean>
    
    /**
     * Trouve un utilisateur par email ou username (global)
     */
    fun findByEmailOrUsername(email: String, username: String): Mono<User>
    
    /**
     * Trouve tous les utilisateurs par rôle global
     */
    fun findByRole(role: Role): Flux<User>
    
    /**
     * Trouve un utilisateur par refresh token
     */
    fun findByRefreshToken(refreshToken: String): Mono<User>
    
    /**
     * Trouve tous les utilisateurs actifs
     */
    fun findByIsActiveTrue(): Flux<User>
    
    /**
     * Trouve un utilisateur par token de réinitialisation de mot de passe
     */
    fun findByPasswordResetToken(token: String): Mono<User>
    
    // ===== RECHERCHE PAR TENANT (pour isolation des données) =====
    
    /**
     * Trouve un utilisateur par email dans un tenant spécifique
     */
    fun findByTenantIdAndEmail(tenantId: ObjectId, email: String): Mono<User>
    
    /**
     * Trouve un utilisateur par username dans un tenant spécifique
     */
    fun findByTenantIdAndUsername(tenantId: ObjectId, username: String): Mono<User>
    
    /**
     * Trouve un utilisateur par email OU username dans un tenant
     */
    fun findByTenantIdAndEmailOrTenantIdAndUsername(
        tenantId1: ObjectId, email: String,
        tenantId2: ObjectId, username: String
    ): Mono<User>
    
    /**
     * Vérifie si un email existe dans un tenant
     */
    fun existsByTenantIdAndEmail(tenantId: ObjectId, email: String): Mono<Boolean>
    
    /**
     * Vérifie si un username existe dans un tenant
     */
    fun existsByTenantIdAndUsername(tenantId: ObjectId, username: String): Mono<Boolean>
    
    /**
     * Trouve tous les utilisateurs d'un tenant
     */
    fun findByTenantId(tenantId: ObjectId): Flux<User>
    
    /**
     * Compte les utilisateurs d'un tenant
     */
    fun countByTenantId(tenantId: ObjectId): Mono<Long>
    
    /**
     * Trouve les utilisateurs actifs d'un tenant
     */
    fun findByTenantIdAndIsActiveTrue(tenantId: ObjectId): Flux<User>
    
    /**
     * Compte les utilisateurs actifs d'un tenant
     */
    fun countByTenantIdAndIsActiveTrue(tenantId: ObjectId): Mono<Long>
    
    /**
     * Trouve les utilisateurs d'un tenant par rôle tenant
     */
    fun findByTenantIdAndTenantRole(tenantId: ObjectId, tenantRole: TenantRole): Flux<User>
    
    /**
     * Trouve le propriétaire d'un tenant (OWNER)
     */
    fun findByTenantIdAndTenantRole(tenantId: ObjectId, tenantRole: TenantRole = TenantRole.OWNER): Mono<User>
    
    // ===== PLATFORM ADMIN =====
    
    /**
     * Trouve tous les Platform Admins (tenantId = null et role = PLATFORM_ADMIN)
     */
    fun findByTenantIdIsNullAndRole(role: Role): Flux<User>
    
    /**
     * Trouve un Platform Admin par email
     */
    fun findByTenantIdIsNullAndEmail(email: String): Mono<User>
}
