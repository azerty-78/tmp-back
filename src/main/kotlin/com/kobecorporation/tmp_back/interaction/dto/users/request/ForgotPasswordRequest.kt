package com.kobecorporation.tmp_back.interaction.dto.users.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * DTO pour demander une réinitialisation de mot de passe
 */
data class ForgotPasswordRequest(
    @field:NotBlank(message = "L'email est requis")
    @field:Email(message = "L'email doit être valide")
    val email: String
)
