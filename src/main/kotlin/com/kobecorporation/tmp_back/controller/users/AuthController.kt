package com.kobecorporation.tmp_back.controller.users

import com.kobecorporation.tmp_back.interaction.dto.users.request.LoginRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.RefreshTokenRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.RegisterRequest
import com.kobecorporation.tmp_back.interaction.dto.users.response.AuthResponse
import com.kobecorporation.tmp_back.interaction.exception.AuthenticationException
import com.kobecorporation.tmp_back.interaction.exception.ResourceAlreadyExistsException
import com.kobecorporation.tmp_back.logic.service.users.AuthService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

/**
 * Controller pour l'authentification
 * 
 * Endpoints :
 * - POST /api/auth/register : Inscription
 * - POST /api/auth/login : Connexion
 * - POST /api/auth/refresh : Rafraîchissement de token
 * - POST /api/auth/logout : Déconnexion (côté client)
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    /**
     * Inscription d'un nouvel utilisateur
     */
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody registerRequest: RegisterRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val requestId = UUID.randomUUID().toString()
        logger.info("[$requestId] Register request for email: ${registerRequest.email}")

        return authService.register(registerRequest)
            .map { authResponse ->
                logger.info("[$requestId] User registered successfully: ${authResponse.user.username}")
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(mapOf(
                        "success" to true,
                        "message" to "Inscription réussie",
                        "data" to authResponse,
                        "requestId" to requestId
                    ))
            }
            .onErrorResume(ResourceAlreadyExistsException::class.java) { e ->
                logger.warn("[$requestId] Registration failed: ${e.message}")
                Mono.just(
                    ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(mapOf(
                            "success" to false,
                            "message" to (e.message ?: "Cette ressource existe déjà"),
                            "errorCode" to "RESOURCE_ALREADY_EXISTS",
                            "requestId" to requestId
                        ))
                )
            }
            .onErrorResume { e ->
                logger.error("[$requestId] Registration error: ${e.message}", e)
                Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(mapOf(
                            "success" to false,
                            "message" to "Erreur lors de l'inscription",
                            "errorCode" to "INTERNAL_ERROR",
                            "requestId" to requestId
                        ))
                )
            }
    }

    /**
     * Connexion d'un utilisateur
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        @RequestParam(value = "rememberMe", defaultValue = "false") rememberMe: Boolean
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val requestId = UUID.randomUUID().toString()
        logger.info("[$requestId] Login request for: ${loginRequest.emailOrUsername}")

        return authService.login(loginRequest, rememberMe)
            .map { authResponse ->
                logger.info("[$requestId] User logged in successfully: ${authResponse.user.username}")
                ResponseEntity.ok(
                    mapOf(
                        "success" to true,
                        "message" to "Connexion réussie",
                        "data" to authResponse,
                        "requestId" to requestId
                    )
                )
            }
            .onErrorResume(AuthenticationException::class.java) { e ->
                logger.warn("[$requestId] Authentication failed: ${e.message}")
                Mono.just(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(mapOf(
                            "success" to false,
                            "message" to (e.message ?: "Identifiants invalides"),
                            "errorCode" to "AUTHENTICATION_FAILED",
                            "requestId" to requestId
                        ))
                )
            }
            .onErrorResume { e ->
                logger.error("[$requestId] Login error: ${e.message}", e)
                Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(mapOf(
                            "success" to false,
                            "message" to "Erreur lors de la connexion",
                            "errorCode" to "INTERNAL_ERROR",
                            "requestId" to requestId
                        ))
                )
            }
    }

    /**
     * Rafraîchissement du token d'accès
     */
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody refreshRequest: RefreshTokenRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val requestId = UUID.randomUUID().toString()
        logger.info("[$requestId] Refresh token request")

        return authService.refreshToken(refreshRequest)
            .map { authResponse ->
                logger.info("[$requestId] Token refreshed successfully for user: ${authResponse.user.username}")
                ResponseEntity.ok(
                    mapOf(
                        "success" to true,
                        "message" to "Token refreshed successfully",
                        "data" to authResponse,
                        "requestId" to requestId
                    )
                )
            }
            .onErrorResume(AuthenticationException::class.java) { e ->
                logger.warn("[$requestId] Refresh token failed: ${e.message}")
                Mono.just(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(mapOf(
                            "success" to false,
                            "message" to (e.message ?: "Token invalide"),
                            "errorCode" to "AUTHENTICATION_FAILED",
                            "requestId" to requestId
                        ))
                )
            }
            .onErrorResume { e ->
                logger.error("[$requestId] Refresh token error: ${e.message}", e)
                Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(mapOf(
                            "success" to false,
                            "message" to "Erreur lors du rafraîchissement",
                            "errorCode" to "INTERNAL_ERROR",
                            "requestId" to requestId
                        ))
                )
            }
    }

    /**
     * Déconnexion
     * 
     * Avec JWT, le logout est géré côté client (suppression du token)
     * Optionnellement, on peut invalider le refresh token côté serveur
     */
    @PostMapping("/logout")
    fun logout(): Mono<ResponseEntity<Map<String, Any>>> {
        val requestId = UUID.randomUUID().toString()
        logger.info("[$requestId] Logout request")

        // Avec JWT, le logout est géré côté client (suppression du token)
        // Pour une invalidation côté serveur, il faudrait supprimer le refreshToken de l'utilisateur
        return Mono.just(
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Logout successful. Please remove the token from client side.",
                    "requestId" to requestId
                )
            )
        )
    }
}
