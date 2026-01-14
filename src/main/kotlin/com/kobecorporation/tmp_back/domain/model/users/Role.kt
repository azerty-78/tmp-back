package com.kobecorporation.tmp_back.domain.model.users

/**
 * Rôles de l'application avec hiérarchie de permissions
 * 
 * Hiérarchie : ROOT_ADMIN > ADMIN > EMPLOYE > USER
 */
enum class Role(val value: String, val description: String) {
    /**
     * USER : Accès public sans authentification
     * - Peut accéder aux routes publiques uniquement
     * - Pas d'authentification requise
     */
    USER("USER", "Utilisateur public - Accès sans authentification"),

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
     * ROOT_ADMIN : Accès complet système
     * - Accès à tout (dépannage, configuration)
     * - Peut créer les ADMIN
     * - Gestion de toutes les plateformes
     * - Créé manuellement ou au premier démarrage
     */
    ROOT_ADMIN("ROOT_ADMIN", "Root Admin - Accès complet système");

    companion object {
        fun fromString(value: String): Role? = values().find { it.value == value }
        
        /**
         * Vérifie si un rôle a les permissions d'un autre rôle
         */
        fun hasPermission(userRole: Role, requiredRole: Role): Boolean {
            val hierarchy = listOf(USER, EMPLOYE, ADMIN, ROOT_ADMIN)
            val userIndex = hierarchy.indexOf(userRole)
            val requiredIndex = hierarchy.indexOf(requiredRole)
            return userIndex >= requiredIndex
        }
    }
}
