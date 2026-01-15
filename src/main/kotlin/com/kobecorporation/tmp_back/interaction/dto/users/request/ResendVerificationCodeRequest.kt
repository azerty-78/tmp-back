package com.kobecorporation.tmp_back.interaction.dto.users.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * DTO pour renvoyer le code de vérification
 */
data class ResendVerificationCodeRequest(
    @field:NotBlank(message = "L'email est requis")
    @field:Email(message = "L'email doit être valide")
    val email: String
)
