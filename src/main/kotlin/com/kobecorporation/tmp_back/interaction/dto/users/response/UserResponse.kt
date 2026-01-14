package com.kobecorporation.tmp_back.interaction.dto.users.response

import com.kobecorporation.tmp_back.domain.model.users.Gender
import com.kobecorporation.tmp_back.domain.model.users.Role
import com.kobecorporation.tmp_back.domain.model.users.SocialLinks
import java.time.Instant
import java.time.LocalDate

/**
 * DTO de réponse pour un utilisateur (sans informations sensibles)
 */
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val role: Role,
    val isActive: Boolean,
    val isEmailVerified: Boolean,
    val profilePicture: String? = null,
    val bio: String? = null,
    val website: String? = null,
    val socialLinks: SocialLinks,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastLoginAt: Instant? = null
)
