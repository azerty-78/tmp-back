package com.kobecorporation.tmp_back.configuration.tenant

import com.kobecorporation.tmp_back.logic.model.tenant.Tenant
import org.bson.types.ObjectId
import reactor.core.publisher.Mono
import reactor.util.context.Context

/**
 * Gestionnaire du contexte tenant pour l'architecture multi-tenant
 * 
 * Utilise le Reactor Context pour propager le tenant courant
 * de manière thread-safe à travers toute la chaîne réactive.
 * 
 * Usage:
 * ```kotlin
 * // Dans un WebFilter ou Controller
 * mono.contextWrite(TenantContext.withTenant(tenant))
 * 
 * // Pour récupérer le tenant
 * TenantContext.getCurrentTenant()
 *     .flatMap { tenant -> ... }
 * ```
 */
object TenantContext {
    
    /**
     * Clé pour stocker le tenant dans le Reactor Context
     */
    const val TENANT_KEY = "CURRENT_TENANT"
    
    /**
     * Clé pour stocker uniquement l'ID du tenant (pour les cas où on n'a pas besoin de tout le tenant)
     */
    const val TENANT_ID_KEY = "CURRENT_TENANT_ID"
    
    /**
     * Clé pour stocker le slug du tenant
     */
    const val TENANT_SLUG_KEY = "CURRENT_TENANT_SLUG"
    
    // ===== ÉCRITURE DANS LE CONTEXT =====
    
    /**
     * Crée un Context avec le tenant
     */
    fun withTenant(tenant: Tenant): Context {
        return Context.of(
            TENANT_KEY, tenant,
            TENANT_ID_KEY, tenant.id,
            TENANT_SLUG_KEY, tenant.slug
        )
    }
    
    /**
     * Crée un Context avec uniquement l'ID du tenant
     */
    fun withTenantId(tenantId: ObjectId): Context {
        return Context.of(TENANT_ID_KEY, tenantId)
    }
    
    /**
     * Ajoute le tenant à un Context existant
     */
    fun Context.putTenant(tenant: Tenant): Context {
        return this
            .put(TENANT_KEY, tenant)
            .put(TENANT_ID_KEY, tenant.id)
            .put(TENANT_SLUG_KEY, tenant.slug)
    }
    
    /**
     * Ajoute uniquement l'ID du tenant à un Context existant
     */
    fun Context.putTenantId(tenantId: ObjectId): Context {
        return this.put(TENANT_ID_KEY, tenantId)
    }
    
    // ===== LECTURE DEPUIS LE CONTEXT =====
    
    /**
     * Récupère le tenant courant depuis le Reactor Context
     * Retourne Mono.empty() si aucun tenant n'est défini
     */
    fun getCurrentTenant(): Mono<Tenant> {
        return Mono.deferContextual { ctx ->
            if (ctx.hasKey(TENANT_KEY)) {
                Mono.just(ctx.get<Tenant>(TENANT_KEY))
            } else {
                Mono.empty()
            }
        }
    }
    
    /**
     * Récupère l'ID du tenant courant depuis le Reactor Context
     * Retourne Mono.empty() si aucun tenant n'est défini
     */
    fun getCurrentTenantId(): Mono<ObjectId> {
        return Mono.deferContextual { ctx ->
            if (ctx.hasKey(TENANT_ID_KEY)) {
                Mono.just(ctx.get<ObjectId>(TENANT_ID_KEY))
            } else {
                Mono.empty()
            }
        }
    }
    
    /**
     * Récupère le slug du tenant courant
     */
    fun getCurrentTenantSlug(): Mono<String> {
        return Mono.deferContextual { ctx ->
            if (ctx.hasKey(TENANT_SLUG_KEY)) {
                Mono.just(ctx.get<String>(TENANT_SLUG_KEY))
            } else {
                Mono.empty()
            }
        }
    }
    
    /**
     * Vérifie si un tenant est présent dans le contexte
     */
    fun hasTenant(): Mono<Boolean> {
        return Mono.deferContextual { ctx ->
            Mono.just(ctx.hasKey(TENANT_KEY) || ctx.hasKey(TENANT_ID_KEY))
        }
    }
    
    // ===== UTILITAIRES =====
    
    /**
     * Exécute une action avec un tenant spécifique
     * Utile pour les opérations cross-tenant (Platform Admin)
     */
    fun <T> withTenantContext(tenant: Tenant, mono: Mono<T>): Mono<T> {
        return mono.contextWrite(withTenant(tenant))
    }
    
    /**
     * Exécute une action sans tenant (pour Platform Admin)
     */
    fun <T> withoutTenant(mono: Mono<T>): Mono<T> {
        return mono.contextWrite { ctx ->
            ctx.delete(TENANT_KEY)
                .delete(TENANT_ID_KEY)
                .delete(TENANT_SLUG_KEY)
        }
    }
    
    /**
     * Récupère le tenant ou lance une erreur si absent
     * Utile quand le tenant est obligatoire
     */
    fun requireCurrentTenant(): Mono<Tenant> {
        return getCurrentTenant()
            .switchIfEmpty(Mono.error(TenantNotFoundException("Aucun tenant dans le contexte")))
    }
    
    /**
     * Récupère l'ID du tenant ou lance une erreur si absent
     */
    fun requireCurrentTenantId(): Mono<ObjectId> {
        return getCurrentTenantId()
            .switchIfEmpty(Mono.error(TenantNotFoundException("Aucun tenant ID dans le contexte")))
    }
}

/**
 * Exception levée quand un tenant est requis mais absent
 */
class TenantNotFoundException(message: String) : RuntimeException(message)

/**
 * Exception levée quand un tenant n'est pas accessible (suspendu, etc.)
 */
class TenantNotAccessibleException(
    val tenantSlug: String,
    message: String = "Le tenant '$tenantSlug' n'est pas accessible"
) : RuntimeException(message)

/**
 * Exception levée quand le domaine ne correspond à aucun tenant
 */
class TenantResolutionException(
    val domain: String,
    message: String = "Impossible de résoudre le tenant pour le domaine '$domain'"
) : RuntimeException(message)
