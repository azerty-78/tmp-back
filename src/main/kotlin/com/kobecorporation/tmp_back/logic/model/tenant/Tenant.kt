package com.kobecorporation.tmp_back.logic.model.tenant

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * Modèle représentant un tenant (client/organisation) dans l'architecture multi-tenant
 * 
 * Chaque tenant représente une organisation distincte avec ses propres utilisateurs,
 * données et configuration.
 * 
 * Stratégie de domaine :
 * - Domaine par défaut : kb-saas-{slug}.kobecorporation.com
 * - Domaine personnalisé : {customDomain} (ex: app.cliententreprise.fr)
 */
@Document(collection = "tenants")
data class Tenant(
    @Id
    val id: ObjectId = ObjectId(),
    
    /**
     * Nom de l'organisation/entreprise
     */
    val name: String,
    
    /**
     * Identifiant unique pour l'URL (ex: "acme" → kb-saas-acme.kobecorporation.com)
     * Format: alphanumérique + tirets, minuscules, 3-50 caractères
     */
    @Indexed(unique = true)
    val slug: String,
    
    /**
     * Domaine personnalisé optionnel (ex: app.cliententreprise.fr)
     * Le client doit configurer un CNAME vers kobecorporation.com
     * Null signifie que seul le domaine par défaut est utilisé
     */
    @Indexed(unique = true, sparse = true)
    val customDomain: String? = null,
    
    /**
     * Plan d'abonnement actuel
     */
    val plan: TenantPlan = TenantPlan.FREE,
    
    /**
     * Statut du tenant
     */
    val status: TenantStatus = TenantStatus.TRIAL,
    
    /**
     * Configuration personnalisée du tenant
     */
    val settings: TenantSettings = TenantSettings(),
    
    /**
     * ID de l'utilisateur propriétaire (créateur du tenant)
     * Ce user a le rôle OWNER et ne peut pas être supprimé
     */
    @Indexed
    val ownerId: ObjectId,
    
    // ===== PÉRIODE D'ESSAI =====
    /**
     * Date de fin de la période d'essai
     * Null si le tenant n'est pas en trial
     */
    val trialEndsAt: Instant? = null,
    
    // ===== FACTURATION (pour intégration Stripe future) =====
    /**
     * ID client Stripe
     */
    val stripeCustomerId: String? = null,
    
    /**
     * ID abonnement Stripe actuel
     */
    val stripeSubscriptionId: String? = null,
    
    // ===== TRACKING =====
    /**
     * Date de création
     */
    @Indexed
    val createdAt: Instant = Instant.now(),
    
    /**
     * Date de dernière modification
     */
    val updatedAt: Instant = Instant.now(),
    
    /**
     * Date de dernière activité (login d'un user)
     */
    val lastActivityAt: Instant? = null
) {
    
    companion object {
        /**
         * Domaine de la plateforme
         */
        const val PLATFORM_DOMAIN = "kobecorporation.com"
        
        /**
         * Préfixe pour les sous-domaines par défaut
         */
        const val DEFAULT_SUBDOMAIN_PREFIX = "kb-saas-"
        
        /**
         * Slugs réservés (ne peuvent pas être utilisés)
         */
        val RESERVED_SLUGS = setOf(
            "admin", "api", "www", "app", "dashboard", "login", "signup",
            "register", "auth", "platform", "system", "root", "support",
            "help", "docs", "status", "mail", "email", "ftp", "ssh",
            "test", "demo", "staging", "dev", "prod", "kb-saas"
        )
    }
    
    /**
     * Retourne le domaine par défaut du tenant
     * Format: kb-saas-{slug}.kobecorporation.com
     */
    val defaultDomain: String
        get() = "$DEFAULT_SUBDOMAIN_PREFIX$slug.$PLATFORM_DOMAIN"
    
    /**
     * Retourne le domaine actif (custom si défini, sinon default)
     */
    val activeDomain: String
        get() = customDomain ?: defaultDomain
    
    /**
     * Vérifie si le tenant est accessible (status permet l'accès)
     */
    fun isAccessible(): Boolean {
        return status.canAccess()
    }
    
    /**
     * Vérifie si le tenant est en période d'essai et si celle-ci est encore valide
     */
    fun isTrialValid(): Boolean {
        if (status != TenantStatus.TRIAL) return false
        return trialEndsAt?.isAfter(Instant.now()) ?: false
    }
    
    /**
     * Vérifie si le tenant peut ajouter un nouvel utilisateur selon son plan
     */
    fun canAddUser(currentUserCount: Long): Boolean {
        return plan.canAddUser(currentUserCount)
    }
    
    /**
     * Vérifie si le tenant a accès à une fonctionnalité spécifique
     */
    fun hasFeature(feature: TenantFeature): Boolean {
        return plan.hasFeature(feature)
    }
    
    /**
     * Vérifie si le tenant peut utiliser un domaine personnalisé
     */
    fun canUseCustomDomain(): Boolean {
        return plan.hasFeature(TenantFeature.CUSTOM_DOMAIN)
    }
    
    /**
     * Vérifie si un slug est valide
     */
    companion object {
        fun isValidSlug(slug: String): Boolean {
            // 3-50 caractères, alphanumérique + tirets, pas de tirets au début/fin
            val regex = Regex("^[a-z0-9][a-z0-9-]{1,48}[a-z0-9]$")
            return slug.matches(regex) && !RESERVED_SLUGS.contains(slug.lowercase())
        }
    }
}
