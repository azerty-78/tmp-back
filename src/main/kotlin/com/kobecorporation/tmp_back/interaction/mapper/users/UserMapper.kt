package com.kobecorporation.tmp_back.interaction.mapper.users

import com.kobecorporation.tmp_back.logic.model.users.User
import com.kobecorporation.tmp_back.interaction.dto.users.response.UserResponse

/**
 * Mapper pour convertir entre User (Entity) et UserResponse (DTO)
 */
object UserMapper {
    
    /**
     * Convertit un User en UserResponse (sans informations sensibles)
     */
    fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id.toString(),
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            fullName = user.fullName,
            birthDate = user.birthDate,
            gender = user.gender,
            role = user.role,
            isActive = user.isActive,
            isEmailVerified = user.isEmailVerified,
            profilePicture = user.profilePicture,
            bio = user.bio,
            website = user.website,
            socialLinks = user.socialLinks,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            lastLoginAt = user.lastLoginAt
        )
    }
    
    /**
     * Convertit une liste d'utilisateurs en liste de réponses
     */
    fun toResponseList(users: List<User>): List<UserResponse> {
        return users.map { toResponse(it) }
    }
}
