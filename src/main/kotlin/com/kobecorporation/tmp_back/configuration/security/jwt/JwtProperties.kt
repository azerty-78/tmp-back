package com.kobecorporation.tmp_back.configuration.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Propriétés de configuration JWT depuis application.properties
 */
@Component
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var secret: String = "",
    var accessTokenExpiration: Long = 3600000, // 1 heure par défaut
    var refreshTokenExpiration: Long = 604800000, // 7 jours par défaut
    // Durée de vie étendue pour le refresh token quand "Rester connecté" est activé
    var rememberMeRefreshTokenExpiration: Long = 2592000000 // 30 jours
)
