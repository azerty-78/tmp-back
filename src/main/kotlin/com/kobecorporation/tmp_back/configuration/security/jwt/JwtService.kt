package com.kobecorporation.tmp_back.configuration.security.jwt

import com.kobecorporation.tmp_back.logic.model.tenant.TenantRole
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
 * Architecture multi-tenant :
 * - Access Token contient : userId, email, role, tenantId, tenantRole
 * - Refresh Token contient : userId, tenantId, rememberMe
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
     * Inclut les informations du tenant pour l'architecture multi-tenant
     * 
     * @param tenantId null pour les Platform Admin
     * @param tenantRole rôle au sein du tenant
     */
    fun generateAccessToken(
        userId: ObjectId, 
        email: String, 
        role: Role,
        tenantId: ObjectId? = null,
        tenantRole: TenantRole = TenantRole.MEMBER
    ): String {
        val now = Instant.now()
        val expiryDate = Date.from(now.plusMillis(jwtProperties.accessTokenExpiration))

        val builder = Jwts.builder()
            .subject(userId.toHexString())
            .claim("email", email)
            .claim("role", role.name)
            .claim("tenantRole", tenantRole.name)
            .claim("type", "ACCESS")
            .issuedAt(Date.from(now))
            .expiration(expiryDate)
        
        // Ajouter tenantId seulement s'il existe (null pour Platform Admin)
        if (tenantId != null) {
            builder.claim("tenantId", tenantId.toHexString())
        }
        
        return builder.signWith(secretKey).compact()
    }
    
    /**
     * Surcharge pour compatibilité avec l'ancien code
     * @deprecated Utiliser la version avec tenantId et tenantRole
     */
    @Deprecated("Utiliser generateAccessToken avec tenantId et tenantRole")
    fun generateAccessToken(userId: ObjectId, email: String, role: Role): String {
        return generateAccessToken(userId, email, role, null, TenantRole.MEMBER)
    }

    /**
     * Génère un Refresh Token (durée longue)
     * Le refresh token sera stocké dans User.refreshToken
     * Renouvelé toutes les heures après utilisation
     * 
     * @param tenantId inclus pour valider que le refresh est pour le bon tenant
     */
    fun generateRefreshToken(
        userId: ObjectId, 
        rememberMe: Boolean = false,
        tenantId: ObjectId? = null
    ): String {
        val now = Instant.now()
        val expirationMillis = if (rememberMe) {
            jwtProperties.rememberMeRefreshTokenExpiration
        } else {
            jwtProperties.refreshTokenExpiration
        }
        val expiryDate = Date.from(now.plusMillis(expirationMillis))

        val builder = Jwts.builder()
            .subject(userId.toHexString())
            .claim("type", "REFRESH")
            .claim("rememberMe", rememberMe)
            .issuedAt(Date.from(now))
            .expiration(expiryDate)
        
        // Ajouter tenantId pour validation lors du refresh
        if (tenantId != null) {
            builder.claim("tenantId", tenantId.toHexString())
        }
        
        return builder.signWith(secretKey).compact()
    }
    
    /**
     * Surcharge pour compatibilité
     */
    @Deprecated("Utiliser generateRefreshToken avec tenantId")
    fun generateRefreshToken(userId: ObjectId, rememberMe: Boolean = false): String {
        return generateRefreshToken(userId, rememberMe, null)
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
     * Extrait l'ID du tenant depuis le token
     * Retourne null pour les Platform Admin (pas de tenant)
     */
    fun extractTenantId(token: String): ObjectId? {
        val claims = extractAllClaims(token)
        val tenantIdStr = claims["tenantId"] as? String
        return tenantIdStr?.let { ObjectId(it) }
    }
    
    /**
     * Extrait le rôle tenant depuis le token
     */
    fun extractTenantRole(token: String): TenantRole {
        val claims = extractAllClaims(token)
        val tenantRoleName = claims["tenantRole"] as? String
        return tenantRoleName?.let { 
            try { TenantRole.valueOf(it) } catch (e: Exception) { TenantRole.MEMBER }
        } ?: TenantRole.MEMBER
    }
    
    /**
     * Vérifie si le token appartient à un Platform Admin (pas de tenantId)
     */
    fun isPlatformAdmin(token: String): Boolean {
        return extractTenantId(token) == null && extractRole(token) == Role.PLATFORM_ADMIN
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
