package com.kobecorporation.tmp_back.interaction.dto.users.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * DTO pour la vérification d'email avec code
 */
data class VerifyEmailRequest(
    @field:NotBlank(message = "L'email est requis")
    @field:Email(message = "L'email doit être valide")
    val email: String,
    
    @field:NotBlank(message = "Le code de vérification est requis")
    @field:Pattern(regexp = "^[0-9]{6}$", message = "Le code de vérification doit contenir 6 chiffres")
    val code: String
)
