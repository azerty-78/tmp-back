package com.kobecorporation.tmp_back.logic.model.tenant

/**
 * Plans d'abonnement disponibles pour les tenants
 * 
 * Chaque plan définit des limites et fonctionnalités différentes
 */
enum class TenantPlan(
    val displayName: String,
    val maxUsers: Int,
    val maxStorageMB: Long,
    val monthlyPriceEuros: Int,
    val features: Set<TenantFeature>
) {
    FREE(
        displayName = "Gratuit",
        maxUsers = 3,
        maxStorageMB = 100,
        monthlyPriceEuros = 0,
        features = setOf(TenantFeature.BASIC_SUPPORT)
    ),
    
    STARTER(
        displayName = "Starter",
        maxUsers = 10,
        maxStorageMB = 1024, // 1 GB
        monthlyPriceEuros = 19,
        features = setOf(
            TenantFeature.BASIC_SUPPORT,
            TenantFeature.EMAIL_SUPPORT,
            TenantFeature.CUSTOM_BRANDING
        )
    ),
    
    PRO(
        displayName = "Pro",
        maxUsers = 50,
        maxStorageMB = 10240, // 10 GB
        monthlyPriceEuros = 49,
        features = setOf(
            TenantFeature.BASIC_SUPPORT,
            TenantFeature.EMAIL_SUPPORT,
            TenantFeature.CUSTOM_BRANDING,
            TenantFeature.PRIORITY_SUPPORT,
            TenantFeature.CUSTOM_DOMAIN,
            TenantFeature.API_ACCESS
        )
    ),
    
    ENTERPRISE(
        displayName = "Enterprise",
        maxUsers = Int.MAX_VALUE, // Illimité
        maxStorageMB = Long.MAX_VALUE, // Illimité
        monthlyPriceEuros = -1, // Sur devis
        features = TenantFeature.entries.toSet() // Toutes les fonctionnalités
    );
    
    /**
     * Vérifie si le plan permet d'ajouter un nouvel utilisateur
     */
    fun canAddUser(currentUserCount: Long): Boolean {
        return currentUserCount < maxUsers
    }
    
    /**
     * Vérifie si le plan a une fonctionnalité spécifique
     */
    fun hasFeature(feature: TenantFeature): Boolean {
        return features.contains(feature)
    }
    
    /**
     * Vérifie si ce plan est supérieur ou égal à un autre
     */
    fun isAtLeast(other: TenantPlan): Boolean {
        return this.ordinal >= other.ordinal
    }
}

/**
 * Fonctionnalités disponibles selon le plan
 */
enum class TenantFeature {
    BASIC_SUPPORT,      // Support basique (documentation)
    EMAIL_SUPPORT,      // Support par email
    PRIORITY_SUPPORT,   // Support prioritaire
    CUSTOM_BRANDING,    // Logo et couleurs personnalisées
    CUSTOM_DOMAIN,      // Domaine personnalisé
    API_ACCESS,         // Accès API avancé
    WEBHOOKS,           // Webhooks pour intégrations
    SSO,                // Single Sign-On
    AUDIT_LOGS,         // Logs d'audit détaillés
    DEDICATED_SUPPORT   // Support dédié avec account manager
}
