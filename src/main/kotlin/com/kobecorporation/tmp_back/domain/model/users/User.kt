package com.kobecorporation.tmp_back.domain.model.users
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate

/**
 * Modèle utilisateur avec support des rôles et authentification
 * 
 * Améliorations apportées :
 * - Refresh token stocké pour gérer les sessions
 * - Champs de tracking améliorés
 * - Support des rôles avec hiérarchie
 */
@Document(collection = "users")
data class User(
    @Id
    val id: ObjectId = ObjectId(),

    @Indexed(unique = true)
    val username: String,
    @Indexed(unique = true)
    val email: String,
    val password: String,
    
    val firstName: String,
    val lastName: String,
    
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    
    val role: Role = Role.USER,
    
    val isActive: Boolean = true,
    val isEmailVerified: Boolean = false,
    
    val profilePicture: String? = null,
    val bio: String? = null,
    val website: String? = null,
    val socialLinks: SocialLinks = SocialLinks(),
    
    // Refresh token pour gérer les sessions (stocké par user)
    val refreshToken: String? = null,
    val refreshTokenExpiresAt: Instant? = null,
    
    // Tracking
    @Indexed
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val lastLoginAt: Instant? = null,
    
    // Pour le tracking des tentatives de connexion
    val failedLoginAttempts: Int = 0,
    val lockedUntil: Instant? = null
) {
    /**
     * Vérifie si le compte est verrouillé
     */
    fun isLocked(): Boolean {
        return lockedUntil != null && lockedUntil!!.isAfter(Instant.now())
    }
    
    /**
     * Vérifie si l'utilisateur peut se connecter
     */
    fun canLogin(): Boolean {
        return isActive && !isLocked() && isEmailVerified
    }
    
    /**
     * Vérifie si l'utilisateur a un refresh token valide
     */
    fun hasValidRefreshToken(): Boolean {
        return refreshToken != null && 
               refreshTokenExpiresAt != null && 
               refreshTokenExpiresAt!!.isAfter(Instant.now())
    }
    
    /**
     * Nom complet de l'utilisateur
     */
    val fullName: String
        get() = "$firstName $lastName"
}
