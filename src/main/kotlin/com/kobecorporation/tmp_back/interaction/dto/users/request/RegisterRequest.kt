package com.kobecorporation.tmp_back.interaction.dto.users.request

import com.kobecorporation.tmp_back.domain.model.users.Gender
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * DTO pour l'inscription d'un nouvel utilisateur
 */
data class RegisterRequest(
    @field:NotBlank(message = "Le nom d'utilisateur est requis")
    @field:Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscores")
    val username: String,
    
    @field:NotBlank(message = "L'email est requis")
    @field:Email(message = "L'email doit être valide")
    val email: String,
    
    @field:NotBlank(message = "Le mot de passe est requis")
    @field:Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    val password: String,
    
    @field:NotBlank(message = "Le prénom est requis")
    @field:Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    val firstName: String,
    
    @field:NotBlank(message = "Le nom est requis")
    @field:Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    val lastName: String,
    
    val birthDate: String? = null, // Format: YYYY-MM-DD
    val gender: Gender? = null
)
