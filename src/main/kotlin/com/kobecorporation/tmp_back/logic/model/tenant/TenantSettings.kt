package com.kobecorporation.tmp_back.logic.model.tenant

/**
 * Configuration personnalisable du tenant
 * 
 * Permet de personnaliser l'apparence et le comportement
 * de l'application pour chaque tenant
 */
data class TenantSettings(
    // ===== BRANDING =====
    /**
     * URL du logo du tenant (stocké dans /uploads/tenants/{tenantId}/)
     */
    val logo: String? = null,
    
    /**
     * URL du favicon
     */
    val favicon: String? = null,
    
    /**
     * Couleur principale (format hex, ex: #3B82F6)
     */
    val primaryColor: String = "#3B82F6",
    
    /**
     * Couleur secondaire
     */
    val secondaryColor: String = "#1E40AF",
    
    // ===== LOCALISATION =====
    /**
     * Fuseau horaire (ex: Europe/Paris, America/New_York)
     */
    val timezone: String = "Europe/Paris",
    
    /**
     * Langue par défaut (code ISO 639-1)
     */
    val language: String = "fr",
    
    /**
     * Format de date préféré
     */
    val dateFormat: String = "dd/MM/yyyy",
    
    // ===== EMAIL =====
    /**
     * Nom de l'expéditeur pour les emails
     * Si null, utilise le nom du tenant
     */
    val emailFromName: String? = null,
    
    /**
     * Adresse email expéditeur personnalisée
     * Requiert une vérification de domaine
     * Si null, utilise noreply@kobecorporation.com
     */
    val emailFromAddress: String? = null,
    
    /**
     * Pied de page personnalisé pour les emails
     */
    val emailFooter: String? = null,
    
    // ===== FONCTIONNALITÉS =====
    /**
     * Activer l'inscription publique pour ce tenant
     * Si false, seules les invitations permettent de rejoindre
     */
    val allowPublicSignup: Boolean = false,
    
    /**
     * Activer l'authentification à deux facteurs obligatoire
     */
    val require2FA: Boolean = false,
    
    /**
     * Durée de session en heures (défaut: 24h)
     */
    val sessionDurationHours: Int = 24,
    
    // ===== MÉTADONNÉES BUSINESS =====
    /**
     * Secteur d'activité
     */
    val industry: String? = null,
    
    /**
     * Taille de l'entreprise
     */
    val companySize: CompanySize? = null,
    
    /**
     * Pays
     */
    val country: String? = null,
    
    /**
     * Adresse
     */
    val address: String? = null,
    
    /**
     * Numéro de TVA (pour la facturation)
     */
    val vatNumber: String? = null
)

/**
 * Taille de l'entreprise
 */
enum class CompanySize(val displayName: String, val range: String) {
    SOLO("Indépendant", "1"),
    SMALL("Petite entreprise", "2-10"),
    MEDIUM("PME", "11-50"),
    LARGE("Grande entreprise", "51-200"),
    ENTERPRISE("Très grande entreprise", "200+")
}
