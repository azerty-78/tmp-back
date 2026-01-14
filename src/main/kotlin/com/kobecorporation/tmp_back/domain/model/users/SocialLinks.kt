package com.kobecorporation.tmp_back.domain.model.users

/**
 * Liens sociaux de l'utilisateur
 */
data class SocialLinks(
    val facebook: String? = null,
    val twitter: String? = null,
    val instagram: String? = null,
    val linkedin: String? = null,
    val github: String? = null,
    val youtube: String? = null
)
