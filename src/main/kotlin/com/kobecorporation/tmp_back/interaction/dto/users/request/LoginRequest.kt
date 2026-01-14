package com.kobecorporation.tmp_back.interaction.dto.users.request

import jakarta.validation.constraints.NotBlank

/**
 * DTO pour la connexion
 */
data class LoginRequest(
    @field:NotBlank(message = "L'email ou le nom d'utilisateur est requis")
    val emailOrUsername: String,
    
    @field:NotBlank(message = "Le mot de passe est requis")
    val password: String
)
