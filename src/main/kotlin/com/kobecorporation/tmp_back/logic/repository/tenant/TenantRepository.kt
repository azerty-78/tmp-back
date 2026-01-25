package com.kobecorporation.tmp_back.logic.repository.tenant

import com.kobecorporation.tmp_back.logic.model.tenant.Tenant
import com.kobecorporation.tmp_back.logic.model.tenant.TenantPlan
import com.kobecorporation.tmp_back.logic.model.tenant.TenantStatus
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Repository réactif pour les tenants
 */
@Repository
interface TenantRepository : ReactiveMongoRepository<Tenant, ObjectId> {
    
    // ===== RECHERCHE PAR IDENTIFIANT =====
    
    /**
     * Trouve un tenant par son slug unique
     * Ex: "acme" pour kb-saas-acme.kobecorporation.com
     */
    fun findBySlug(slug: String): Mono<Tenant>
    
    /**
     * Trouve un tenant par son domaine personnalisé
     * Ex: "app.cliententreprise.fr"
     */
    fun findByCustomDomain(customDomain: String): Mono<Tenant>
    
    /**
     * Vérifie si un slug existe déjà
     */
    fun existsBySlug(slug: String): Mono<Boolean>
    
    /**
     * Vérifie si un domaine personnalisé existe déjà
     */
    fun existsByCustomDomain(customDomain: String): Mono<Boolean>
    
    // ===== RECHERCHE PAR PROPRIÉTAIRE =====
    
    /**
     * Trouve tous les tenants d'un propriétaire
     * (Un utilisateur peut posséder plusieurs tenants)
     */
    fun findByOwnerId(ownerId: ObjectId): Flux<Tenant>
    
    /**
     * Compte les tenants d'un propriétaire
     */
    fun countByOwnerId(ownerId: ObjectId): Mono<Long>
    
    // ===== RECHERCHE PAR STATUT =====
    
    /**
     * Trouve tous les tenants avec un statut donné
     */
    fun findByStatus(status: TenantStatus): Flux<Tenant>
    
    /**
     * Trouve tous les tenants actifs
     */
    fun findByStatusIn(statuses: Collection<TenantStatus>): Flux<Tenant>
    
    /**
     * Compte les tenants par statut
     */
    fun countByStatus(status: TenantStatus): Mono<Long>
    
    // ===== RECHERCHE PAR PLAN =====
    
    /**
     * Trouve tous les tenants avec un plan donné
     */
    fun findByPlan(plan: TenantPlan): Flux<Tenant>
    
    /**
     * Compte les tenants par plan
     */
    fun countByPlan(plan: TenantPlan): Mono<Long>
    
    // ===== RECHERCHE COMBINÉE =====
    
    /**
     * Trouve les tenants actifs par plan
     */
    fun findByStatusAndPlan(status: TenantStatus, plan: TenantPlan): Flux<Tenant>
    
    /**
     * Recherche par nom (contient, insensible à la casse)
     */
    fun findByNameContainingIgnoreCase(name: String): Flux<Tenant>
}
