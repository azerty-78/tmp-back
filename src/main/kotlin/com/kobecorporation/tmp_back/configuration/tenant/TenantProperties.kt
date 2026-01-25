package com.kobecorporation.tmp_back.configuration.tenant

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Propriétés de configuration pour le multi-tenant
 */
@ConfigurationProperties(prefix = "tenant")
data class TenantProperties(
    /**
     * Domaine principal de la plateforme
     * Ex: kobecorporation.com
     */
    val platformDomain: String = "kobecorporation.com",
    
    /**
     * Préfixe pour les sous-domaines par défaut
     * Ex: kb-saas- → kb-saas-acme.kobecorporation.com
     */
    val subdomainPrefix: String = "kb-saas-",
    
    /**
     * Nom du header HTTP pour spécifier le tenant
     * Utile pour les tests et les appels API directs
     */
    val headerName: String = "X-Tenant-ID",
    
    /**
     * Plan par défaut pour les nouveaux tenants
     */
    val defaultPlan: String = "FREE",
    
    /**
     * Nombre de jours d'essai gratuit
     */
    val trialDays: Int = 14,
    
    /**
     * Nombre maximum de tenants qu'un utilisateur peut créer
     * -1 = illimité
     */
    val maxTenantsPerUser: Int = 3,
    
    /**
     * Activer la vérification DNS pour les domaines personnalisés
     */
    val verifyCustomDomainDns: Boolean = true
)
