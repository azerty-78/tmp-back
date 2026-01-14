package com.kobecorporation.tmp_back.interaction.dto.users.request

import jakarta.validation.constraints.NotBlank

/**
 * DTO pour le rafraîchissement du token
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "Le refresh token est requis")
    val refreshToken: String
)
