package com.kobecorporation.tmp_back.logic.model.tenant

/**
 * Statuts possibles pour un tenant
 */
enum class TenantStatus(
    val displayName: String,
    val isAccessible: Boolean
) {
    /**
     * Période d'essai gratuite
     * Le tenant a accès complet pendant la durée du trial
     */
    TRIAL(
        displayName = "Période d'essai",
        isAccessible = true
    ),
    
    /**
     * Tenant actif avec abonnement valide
     */
    ACTIVE(
        displayName = "Actif",
        isAccessible = true
    ),
    
    /**
     * Tenant suspendu (paiement échoué, violation des CGU, etc.)
     * Les utilisateurs ne peuvent plus se connecter
     * Les données sont conservées
     */
    SUSPENDED(
        displayName = "Suspendu",
        isAccessible = false
    ),
    
    /**
     * Tenant annulé par le propriétaire
     * Les données seront supprimées après une période de grâce
     */
    CANCELLED(
        displayName = "Annulé",
        isAccessible = false
    ),
    
    /**
     * Tenant en attente de validation (si modération requise)
     */
    PENDING(
        displayName = "En attente",
        isAccessible = false
    );
    
    /**
     * Vérifie si les utilisateurs peuvent accéder au tenant
     */
    fun canAccess(): Boolean = isAccessible
}
