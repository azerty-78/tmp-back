package com.kobecorporation.tmp_back.controller.users

import com.kobecorporation.tmp_back.interaction.dto.users.request.ForgotPasswordRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.LoginRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.RefreshTokenRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.RegisterRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.ResendVerificationCodeRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.ResetPasswordRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.VerifyEmailRequest
import com.kobecorporation.tmp_back.interaction.dto.users.response.AuthResponse
import com.kobecorporation.tmp_back.interaction.exception.AuthenticationException
import com.kobecorporation.tmp_back.interaction.exception.ResourceAlreadyExistsException
import com.kobecorporation.tmp_back.interaction.exception.ResourceNotFoundException
import com.kobecorporation.tmp_back.logic.service.users.AuthService
import com.kobecorporation.tmp_back.logic.service.users.PasswordResetService
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
 * - POST /api/auth/verify-email : Vérification d'email avec code
 * - POST /api/auth/resend-verification-code : Renvoyer le code de vérification
 * - POST /api/auth/login : Connexion
 * - POST /api/auth/refresh : Rafraîchissement de token
 * - POST /api/auth/logout : Déconnexion (côté client)
 * - POST /api/auth/forgot-password : Demander une réinitialisation de mot de passe
 * - POST /api/auth/reset-password : Réinitialiser le mot de passe avec un token
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val passwordResetService: PasswordResetService
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
            .map { response ->
                logger.info("[$requestId] User registered successfully: ${registerRequest.email}")
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response + mapOf("requestId" to requestId))
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
     * Vérification d'email avec code
     */
    @PostMapping("/verify-email")
    fun verifyEmail(
        @Valid @RequestBody verifyEmailRequest: VerifyEmailRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val requestId = UUID.randomUUID().toString()
        logger.info("[$requestId] Email verification request for: ${verifyEmailRequest.email}")

        return authService.verifyEmail(verifyEmailRequest)
            .map { authResponse ->
                logger.info("[$requestId] Email verified successfully: ${verifyEmailRequest.email}")
                ResponseEntity.ok(
                    mapOf(
                        "success" to true,
                        "message" to "Email vérifié avec succès",
                        "data" to authResponse,
                        "requestId" to requestId
                    )
                )
            }
            .onErrorResume(AuthenticationException::class.java) { e ->
                logger.warn("[$requestId] Email verification failed: ${e.message}")
                Mono.just(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(mapOf(
                            "success" to false,
                            "message" to (e.message ?: "Code de vérification invalide"),
                            "errorCode" to "AUTHENTICATION_FAILED",
                            "requestId" to requestId
                        ))
                )
            }
            .onErrorResume(ResourceNotFoundException::class.java) { e ->
                logger.warn("[$requestId] Email verification failed: ${e.message}")
                Mono.just(
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(mapOf(
                            "success" to false,
                            "message" to (e.message ?: "Compte non trouvé"),
                            "errorCode" to "RESOURCE_NOT_FOUND",
                            "requestId" to requestId
                        ))
                )
            }
            .onErrorResume { e ->
                logger.error("[$requestId] Email verification error: ${e.message}", e)
                Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(mapOf(
                            "success" to false,
                            "message" to "Erreur lors de la vérification",
                            "errorCode" to "INTERNAL_ERROR",
                            "requestId" to requestId
                        ))
                )
            }
    }

    /**
     * Renvoyer le code de vérification
     */
    @PostMapping("/resend-verification-code")
    fun resendVerificationCode(
        @Valid @RequestBody resendRequest: ResendVerificationCodeRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val requestId = UUID.randomUUID().toString()
        logger.info("[$requestId] Resend verification code request for: ${resendRequest.email}")

        return authService.resendVerificationCode(resendRequest)
            .map { response ->
                logger.info("[$requestId] Verification code resent successfully: ${resendRequest.email}")
                ResponseEntity.ok(response + mapOf("requestId" to requestId))
            }
            .onErrorResume(ResourceNotFoundException::class.java) { e ->
                logger.warn("[$requestId] Resend verification code failed: ${e.message}")
                Mono.just(
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(mapOf(
                            "success" to false,
                            "message" to (e.message ?: "Compte non trouvé"),
                            "errorCode" to "RESOURCE_NOT_FOUND",
                            "requestId" to requestId
                        ))
                )
            }
            .onErrorResume(AuthenticationException::class.java) { e ->
                logger.warn("[$requestId] Resend verification code failed: ${e.message}")
                Mono.just(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(mapOf(
                            "success" to false,
                            "message" to (e.message ?: "Erreur lors de l'envoi"),
                            "errorCode" to "BAD_REQUEST",
                            "requestId" to requestId
                        ))
                )
            }
            .onErrorResume { e ->
                logger.error("[$requestId] Resend verification code error: ${e.message}", e)
                Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(mapOf(
                            "success" to false,
                            "message" to "Erreur lors de l'envoi du code",
                            "errorCode" to "INTERNAL_ERROR",
                            "requestId" to requestId
                        ))
                )
            }
    }

    /**
     * Demander une réinitialisation de mot de passe
     */
    @PostMapping("/forgot-password")
    fun forgotPassword(
        @Valid @RequestBody forgotPasswordRequest: ForgotPasswordRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val requestId = UUID.randomUUID().toString()
        logger.info("[$requestId] Forgot password request for: ${forgotPasswordRequest.email}")

        return passwordResetService.requestPasswordReset(forgotPasswordRequest)
            .map { response ->
                logger.info("[$requestId] Password reset request processed: ${forgotPasswordRequest.email}")
                ResponseEntity.ok(response + mapOf("requestId" to requestId))
            }
            .onErrorResume { e ->
                logger.error("[$requestId] Forgot password error: ${e.message}", e)
                Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(mapOf(
                            "success" to false,
                            "message" to "Erreur lors de la demande de réinitialisation",
                            "errorCode" to "INTERNAL_ERROR",
                            "requestId" to requestId
                        ))
                )
            }
    }

    /**
     * Réinitialiser le mot de passe avec un token
     */
    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody resetPasswordRequest: ResetPasswordRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val requestId = UUID.randomUUID().toString()
        logger.info("[$requestId] Reset password request")

        return passwordResetService.resetPassword(resetPasswordRequest)
            .map { response ->
                logger.info("[$requestId] Password reset successfully")
                ResponseEntity.ok(response + mapOf("requestId" to requestId))
            }
            .onErrorResume(AuthenticationException::class.java) { e ->
                logger.warn("[$requestId] Password reset failed: ${e.message}")
                Mono.just(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(mapOf(
                            "success" to false,
                            "message" to (e.message ?: "Token invalide ou expiré"),
                            "errorCode" to "AUTHENTICATION_FAILED",
                            "requestId" to requestId
                        ))
                )
            }
            .onErrorResume { e ->
                logger.error("[$requestId] Password reset error: ${e.message}", e)
                Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(mapOf(
                            "success" to false,
                            "message" to "Erreur lors de la réinitialisation",
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
