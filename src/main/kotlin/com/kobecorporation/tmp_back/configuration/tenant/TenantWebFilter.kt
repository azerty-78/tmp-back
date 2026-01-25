package com.kobecorporation.tmp_back.configuration.tenant

import com.kobecorporation.tmp_back.logic.model.tenant.Tenant
import com.kobecorporation.tmp_back.logic.repository.tenant.TenantRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * WebFilter pour la résolution automatique du tenant
 * 
 * Stratégie de résolution :
 * 1. Domaine personnalisé : app.cliententreprise.fr → tenant avec customDomain
 * 2. Sous-domaine par défaut : kb-saas-acme.kobecorporation.com → tenant avec slug "acme"
 * 3. Header X-Tenant-ID : Pour les tests ou API directe
 * 
 * Routes exclues (sans tenant requis) :
 * - /actuator/** : Health checks
 * - /api/platform/** : Routes Platform Admin
 * - /api/tenants/signup : Création de tenant
 * - /api/tenants/check-slug/** : Vérification disponibilité slug
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // Après CORS, avant Security
class TenantWebFilter(
    private val tenantRepository: TenantRepository,
    @Value("\${tenant.platform-domain:kobecorporation.com}") 
    private val platformDomain: String,
    @Value("\${tenant.subdomain-prefix:kb-saas-}") 
    private val subdomainPrefix: String,
    @Value("\${tenant.header-name:X-Tenant-ID}") 
    private val tenantHeaderName: String
) : WebFilter {
    
    private val logger = LoggerFactory.getLogger(TenantWebFilter::class.java)
    
    /**
     * Routes qui ne nécessitent pas de tenant
     */
    private val excludedPaths = listOf(
        "/actuator",
        "/health",
        "/api/platform",
        "/api/tenants/signup",
        "/api/tenants/check-slug",
        "/swagger-ui",
        "/v3/api-docs"
    )
    
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.value()
        
        // Vérifier si la route est exclue
        if (isExcludedPath(path)) {
            logger.debug("🏠 Route exclue du tenant filter: $path")
            return chain.filter(exchange)
        }
        
        val host = exchange.request.headers.host?.hostString ?: ""
        val tenantHeader = exchange.request.headers.getFirst(tenantHeaderName)
        
        logger.debug("🔍 Résolution tenant - Host: $host, Header: $tenantHeader, Path: $path")
        
        return resolveTenant(host, tenantHeader)
            .flatMap { tenant ->
                // Vérifier que le tenant est accessible
                if (!tenant.isAccessible()) {
                    logger.warn("⛔ Tenant '${tenant.slug}' non accessible (status: ${tenant.status})")
                    return@flatMap Mono.error<Void>(
                        TenantNotAccessibleException(tenant.slug)
                    )
                }
                
                logger.debug("✅ Tenant résolu: ${tenant.slug} (${tenant.name})")
                
                // Continuer la chaîne avec le tenant dans le contexte
                chain.filter(exchange)
                    .contextWrite(TenantContext.withTenant(tenant))
            }
            .switchIfEmpty(
                // Aucun tenant trouvé - laisser passer (sera géré par la sécurité)
                chain.filter(exchange)
            )
            .onErrorResume(TenantNotAccessibleException::class.java) { ex ->
                // Tenant suspendu ou inaccessible
                exchange.response.statusCode = HttpStatus.FORBIDDEN
                exchange.response.headers.add("X-Tenant-Error", "TENANT_NOT_ACCESSIBLE")
                exchange.response.setComplete()
            }
            .onErrorResume(TenantResolutionException::class.java) { ex ->
                // Domaine ne correspond à aucun tenant
                logger.warn("❌ ${ex.message}")
                exchange.response.statusCode = HttpStatus.NOT_FOUND
                exchange.response.headers.add("X-Tenant-Error", "TENANT_NOT_FOUND")
                exchange.response.setComplete()
            }
    }
    
    /**
     * Résout le tenant à partir du host ou du header
     */
    private fun resolveTenant(host: String, tenantHeader: String?): Mono<Tenant> {
        // 1. Priorité au header (pour les tests et API)
        if (!tenantHeader.isNullOrBlank()) {
            logger.debug("🔑 Résolution par header: $tenantHeader")
            return tenantRepository.findBySlug(tenantHeader)
        }
        
        // 2. Vérifier si c'est un domaine personnalisé
        if (!isDefaultSubdomain(host)) {
            logger.debug("🌐 Résolution par domaine custom: $host")
            return tenantRepository.findByCustomDomain(host)
        }
        
        // 3. Extraire le slug du sous-domaine par défaut
        val slug = extractSlugFromHost(host)
        if (slug != null) {
            logger.debug("📛 Résolution par slug: $slug")
            return tenantRepository.findBySlug(slug)
        }
        
        // Aucune résolution possible
        return Mono.empty()
    }
    
    /**
     * Vérifie si le host est un sous-domaine par défaut de la plateforme
     * Ex: kb-saas-acme.kobecorporation.com → true
     * Ex: app.cliententreprise.fr → false
     */
    private fun isDefaultSubdomain(host: String): Boolean {
        return host.endsWith(".$platformDomain") || host == platformDomain
    }
    
    /**
     * Extrait le slug depuis un host de type kb-saas-{slug}.kobecorporation.com
     */
    private fun extractSlugFromHost(host: String): String? {
        // Retirer le domaine de la plateforme
        val subdomain = host.removeSuffix(".$platformDomain")
        
        // Vérifier que c'est bien un sous-domaine avec notre préfixe
        if (subdomain.startsWith(subdomainPrefix)) {
            return subdomain.removePrefix(subdomainPrefix)
        }
        
        // Cas spécial: localhost ou IP (pour le dev)
        if (host.startsWith("localhost") || host.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+.*"))) {
            return null // Pas de tenant en local sans header
        }
        
        return null
    }
    
    /**
     * Vérifie si le path est exclu de la résolution tenant
     */
    private fun isExcludedPath(path: String): Boolean {
        return excludedPaths.any { excluded -> 
            path.startsWith(excluded) || path == excluded
        }
    }
}
