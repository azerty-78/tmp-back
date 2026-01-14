package com.kobecorporation.tmp_back.interaction.dto.users.response

/**
 * Réponse d'authentification avec tokens
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long, // Durée de vie en secondes
    val refreshExpiresIn: Long, // Durée de vie du refresh token en secondes
    val user: UserResponse
)
