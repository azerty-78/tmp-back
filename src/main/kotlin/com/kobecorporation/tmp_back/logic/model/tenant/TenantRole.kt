package com.kobecorporation.tmp_back.logic.model.tenant

/**
 * Rôles au sein d'un tenant
 * 
 * Ces rôles définissent les permissions d'un utilisateur dans un tenant spécifique.
 * Un utilisateur peut avoir des rôles différents dans différents tenants.
 * 
 * Hiérarchie : OWNER > ADMIN > MEMBER > GUEST
 */
enum class TenantRole(
    val displayName: String,
    val level: Int,
    val permissions: Set<TenantPermission>
) {
    /**
     * Propriétaire du tenant (créateur)
     * - Accès complet à toutes les fonctionnalités
     * - Peut supprimer le tenant
     * - Peut transférer la propriété
     * - Ne peut pas être supprimé
     */
    OWNER(
        displayName = "Propriétaire",
        level = 100,
        permissions = TenantPermission.entries.toSet()
    ),
    
    /**
     * Administrateur du tenant
     * - Peut gérer les membres (inviter, modifier rôles, supprimer)
     * - Peut modifier les paramètres du tenant
     * - Ne peut pas supprimer le tenant
     */
    ADMIN(
        displayName = "Administrateur",
        level = 75,
        permissions = setOf(
            TenantPermission.VIEW_MEMBERS,
            TenantPermission.INVITE_MEMBERS,
            TenantPermission.MANAGE_MEMBERS,
            TenantPermission.VIEW_SETTINGS,
            TenantPermission.EDIT_SETTINGS,
            TenantPermission.VIEW_ANALYTICS,
            TenantPermission.MANAGE_CONTENT,
            TenantPermission.MANAGE_INTEGRATIONS
        )
    ),
    
    /**
     * Membre standard
     * - Accès aux fonctionnalités de base
     * - Peut voir les autres membres
     * - Peut gérer son propre contenu
     */
    MEMBER(
        displayName = "Membre",
        level = 50,
        permissions = setOf(
            TenantPermission.VIEW_MEMBERS,
            TenantPermission.VIEW_SETTINGS,
            TenantPermission.MANAGE_OWN_CONTENT
        )
    ),
    
    /**
     * Invité / Accès limité
     * - Accès en lecture seule
     * - Fonctionnalités très limitées
     */
    GUEST(
        displayName = "Invité",
        level = 10,
        permissions = setOf(
            TenantPermission.VIEW_CONTENT
        )
    );
    
    /**
     * Vérifie si ce rôle a une permission spécifique
     */
    fun hasPermission(permission: TenantPermission): Boolean {
        return permissions.contains(permission)
    }
    
    /**
     * Vérifie si ce rôle est supérieur ou égal à un autre
     */
    fun isAtLeast(other: TenantRole): Boolean {
        return this.level >= other.level
    }
    
    /**
     * Vérifie si ce rôle peut modifier le rôle d'un autre utilisateur
     */
    fun canModifyRole(targetRole: TenantRole): Boolean {
        // On ne peut modifier que les rôles inférieurs au sien
        // Et OWNER ne peut être modifié par personne
        return this.level > targetRole.level && targetRole != OWNER
    }
}

/**
 * Permissions granulaires au sein d'un tenant
 */
enum class TenantPermission {
    // Membres
    VIEW_MEMBERS,           // Voir la liste des membres
    INVITE_MEMBERS,         // Inviter de nouveaux membres
    MANAGE_MEMBERS,         // Modifier/supprimer des membres
    
    // Paramètres
    VIEW_SETTINGS,          // Voir les paramètres du tenant
    EDIT_SETTINGS,          // Modifier les paramètres
    EDIT_BILLING,           // Modifier la facturation
    
    // Contenu
    VIEW_CONTENT,           // Voir le contenu
    MANAGE_OWN_CONTENT,     // Gérer son propre contenu
    MANAGE_CONTENT,         // Gérer tout le contenu
    
    // Analytics
    VIEW_ANALYTICS,         // Voir les statistiques
    
    // Intégrations
    MANAGE_INTEGRATIONS,    // Gérer les intégrations (webhooks, API keys)
    
    // Administration
    DELETE_TENANT,          // Supprimer le tenant
    TRANSFER_OWNERSHIP      // Transférer la propriété
}
