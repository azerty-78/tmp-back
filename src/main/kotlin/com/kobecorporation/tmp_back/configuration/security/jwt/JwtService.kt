package com.kobecorporation.tmp_back.configuration.security.jwt

import com.kobecorporation.tmp_back.logic.model.users.Role
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey
import java.time.Instant
import java.util.Base64

/**
 * Service pour la génération et validation des tokens JWT
 * 
 * Gère :
 * - Access Token : Durée courte (15 min recommandé)
 * - Refresh Token : Durée longue (7 jours ou 30 jours avec rememberMe)
 * - Refresh Token par user : Stocké dans User.refreshToken
 */
@Service
class JwtService(
    val jwtProperties: JwtProperties // Exposé pour AuthService
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(
        Base64.getDecoder().decode(jwtProperties.secret)
    )

    /**
     * Génère un Access Token (durée courte)
     */
    fun generateAccessToken(userId: ObjectId, email: String, role: Role): String {
        val now = Instant.now()
        val expiryDate = Date.from(now.plusMillis(jwtProperties.accessTokenExpiration))

        return Jwts.builder()
            .subject(userId.toHexString())
            .claim("email", email)
            .claim("role", role.name)
            .claim("type", "ACCESS")
            .issuedAt(Date.from(now))
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Génère un Refresh Token (durée longue)
     * Le refresh token sera stocké dans User.refreshToken
     * Renouvelé toutes les heures après utilisation
     */
    fun generateRefreshToken(userId: ObjectId, rememberMe: Boolean = false): String {
        val now = Instant.now()
        val expirationMillis = if (rememberMe) {
            jwtProperties.rememberMeRefreshTokenExpiration
        } else {
            jwtProperties.refreshTokenExpiration
        }
        val expiryDate = Date.from(now.plusMillis(expirationMillis))

        return Jwts.builder()
            .subject(userId.toHexString())
            .claim("type", "REFRESH")
            .claim("rememberMe", rememberMe)
            .issuedAt(Date.from(now))
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Valide un token JWT
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Extrait l'ID utilisateur depuis le token
     */
    fun extractUserId(token: String): ObjectId {
        val claims = extractAllClaims(token)
        return ObjectId(claims.subject)
    }

    /**
     * Extrait l'email depuis le token
     */
    fun extractEmail(token: String): String {
        return extractAllClaims(token)["email"] as String
    }

    /**
     * Extrait le rôle depuis le token
     */
    fun extractRole(token: String): Role {
        val roleName = extractAllClaims(token)["role"] as String
        return Role.fromString(roleName) ?: Role.USER
    }

    /**
     * Extrait le type de token (ACCESS ou REFRESH)
     */
    fun extractTokenType(token: String): String {
        return extractAllClaims(token)["type"] as String
    }

    /**
     * Vérifie si un token est expiré
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val expiration = extractAllClaims(token).expiration
            expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Extrait tous les claims d'un token
     */
    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * Méthode interne pour accéder aux claims bruts
     * Utilisée par AuthService pour relire le flag rememberMe
     */
    internal fun extractAllClaimsInternal(token: String): Claims = extractAllClaims(token)

    /**
     * Retourne la durée de vie de l'access token en millisecondes
     */
    fun getAccessTokenExpiration(): Long = jwtProperties.accessTokenExpiration
}
