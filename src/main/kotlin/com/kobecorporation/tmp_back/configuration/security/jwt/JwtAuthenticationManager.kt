package com.kobecorporation.tmp_back.configuration.security.jwt

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Gestionnaire d'authentification JWT pour WebFlux Reactive
 * 
 * Valide le token JWT et crée l'objet Authentication avec les autorités
 */
@Component
class JwtAuthenticationManager(
    private val jwtService: JwtService
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .map { it.credentials.toString() }
            .filter { token ->
                // Vérifier que c'est un token ACCESS (pas REFRESH)
                jwtService.validateToken(token) && 
                !jwtService.isTokenExpired(token) &&
                jwtService.extractTokenType(token) == "ACCESS"
            }
            .map { token ->
                val userId = jwtService.extractUserId(token)
                val role = jwtService.extractRole(token)
                val email = jwtService.extractEmail(token)

                // Créer les autorités avec le préfixe ROLE_ (requis par Spring Security)
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

                UsernamePasswordAuthenticationToken(
                    userId.toHexString(),
                    token,
                    authorities
                ) as Authentication
            }
            .switchIfEmpty(Mono.error(RuntimeException("Invalid or expired JWT token")))
    }
}
