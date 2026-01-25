package com.kobecorporation.tmp_back.logic.repository.base

import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Interface de base pour les repositories tenant-aware
 * 
 * Cette interface définit les méthodes communes pour les entités
 * qui appartiennent à un tenant et doivent être filtrées automatiquement.
 * 
 * Usage :
 * ```kotlin
 * interface ProductRepository : ReactiveMongoRepository<Product, ObjectId>, 
 *                               TenantAwareRepository<Product, ObjectId>
 * ```
 * 
 * @param T Type de l'entité
 * @param ID Type de l'identifiant (généralement ObjectId)
 */
interface TenantAwareRepository<T, ID> {
    
    /**
     * Trouve toutes les entités d'un tenant
     */
    fun findAllByTenantId(tenantId: ObjectId): Flux<T>
    
    /**
     * Trouve une entité par son ID dans un tenant spécifique
     * Garantit l'isolation : ne retourne l'entité que si elle appartient au tenant
     */
    fun findByIdAndTenantId(id: ID, tenantId: ObjectId): Mono<T>
    
    /**
     * Supprime une entité par son ID dans un tenant spécifique
     * Garantit l'isolation : ne supprime que si l'entité appartient au tenant
     */
    fun deleteByIdAndTenantId(id: ID, tenantId: ObjectId): Mono<Void>
    
    /**
     * Compte les entités d'un tenant
     */
    fun countByTenantId(tenantId: ObjectId): Mono<Long>
    
    /**
     * Vérifie si une entité existe dans un tenant
     */
    fun existsByIdAndTenantId(id: ID, tenantId: ObjectId): Mono<Boolean>
}

/**
 * Interface marqueur pour les entités qui appartiennent à un tenant
 * 
 * Toute entité implémentant cette interface doit avoir un champ tenantId
 */
interface TenantAwareEntity {
    val tenantId: ObjectId
}

/**
 * Classe abstraite de base pour les documents MongoDB tenant-aware
 * 
 * Usage :
 * ```kotlin
 * @Document(collection = "products")
 * data class Product(
 *     @Id val id: ObjectId = ObjectId(),
 *     override val tenantId: ObjectId,
 *     val name: String,
 *     // ... autres champs
 * ) : TenantAwareDocument()
 * ```
 */
abstract class TenantAwareDocument : TenantAwareEntity {
    abstract override val tenantId: ObjectId
}
