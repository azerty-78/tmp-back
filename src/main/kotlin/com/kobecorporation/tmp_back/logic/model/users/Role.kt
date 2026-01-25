package com.kobecorporation.tmp_back.logic.model.users

/**
 * Rôles globaux de l'application
 * 
 * Architecture multi-tenant :
 * - PLATFORM_ADMIN : Super admin qui gère TOUS les tenants (sans tenant)
 * - ROOT_ADMIN : Admin d'un tenant spécifique (créé au premier démarrage du tenant)
 * - ADMIN, EMPLOYE, USER : Rôles au sein d'un tenant
 * 
 * Hiérarchie : PLATFORM_ADMIN > ROOT_ADMIN > ADMIN > EMPLOYE > USER
 */
enum class Role(val value: String, val description: String) {
    /**
     * USER : Utilisateur standard
     * - Peut accéder aux routes publiques
     * - Accès limité aux fonctionnalités de base
     */
    USER("USER", "Utilisateur standard"),

    /**
     * EMPLOYE : Accès à l'interface de management
     * - Peut gérer le contenu (CRUD)
     * - Accès aux routes protégées de management
     * - Créé et géré par ADMIN
     */
    EMPLOYE("EMPLOYE", "Employé - Accès interface de management"),

    /**
     * ADMIN : Gestion des employés et contenu
     * - Peut créer et gérer les EMPLOYE
     * - Gère le contenu et les interfaces
     * - Accès complet sauf configuration système
     * - Créé par ROOT_ADMIN
     */
    ADMIN("ADMIN", "Administrateur - Gestion des employés et contenu"),

    /**
     * ROOT_ADMIN : Admin principal d'un tenant
     * - Accès complet au tenant
     * - Peut créer les ADMIN
     * - Créé automatiquement à la création du tenant
     */
    ROOT_ADMIN("ROOT_ADMIN", "Root Admin - Accès complet au tenant"),
    
    /**
     * PLATFORM_ADMIN : Super administrateur de la plateforme SaaS
     * - Accès à TOUS les tenants
     * - Peut créer/suspendre/supprimer des tenants
     * - Gestion globale de la plateforme
     * - N'appartient à aucun tenant (tenantId = null)
     */
    PLATFORM_ADMIN("PLATFORM_ADMIN", "Platform Admin - Super admin multi-tenant");

    companion object {
        fun fromString(value: String): Role? = entries.find { it.value == value }
        
        /**
         * Vérifie si un rôle a les permissions d'un autre rôle
         * Note: PLATFORM_ADMIN a accès à tout
         */
        fun hasPermission(userRole: Role, requiredRole: Role): Boolean {
            // Platform admin a accès à tout
            if (userRole == PLATFORM_ADMIN) return true
            
            val hierarchy = listOf(USER, EMPLOYE, ADMIN, ROOT_ADMIN, PLATFORM_ADMIN)
            val userIndex = hierarchy.indexOf(userRole)
            val requiredIndex = hierarchy.indexOf(requiredRole)
            return userIndex >= requiredIndex
        }
        
        /**
         * Vérifie si le rôle est un rôle de niveau plateforme
         */
        fun isPlatformLevel(role: Role): Boolean {
            return role == PLATFORM_ADMIN
        }
    }
}
