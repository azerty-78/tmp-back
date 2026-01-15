package com.kobecorporation.tmp_back.interaction.dto.users.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO pour réinitialiser le mot de passe avec un token
 */
data class ResetPasswordRequest(
    @field:NotBlank(message = "Le token est requis")
    val token: String,
    
    @field:NotBlank(message = "Le nouveau mot de passe est requis")
    @field:Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    val newPassword: String
)
