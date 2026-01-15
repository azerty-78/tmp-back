package com.kobecorporation.tmp_back.logic.service.users

import com.kobecorporation.tmp_back.configuration.email.EmailProperties
import com.kobecorporation.tmp_back.interaction.dto.users.request.ForgotPasswordRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.ResetPasswordRequest
import com.kobecorporation.tmp_back.interaction.exception.AuthenticationException
import com.kobecorporation.tmp_back.interaction.exception.ResourceNotFoundException
import com.kobecorporation.tmp_back.logic.repository.users.UserRepository
import com.kobecorporation.tmp_back.logic.service.email.EmailService
import com.kobecorporation.tmp_back.util.CodeGenerator
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Service pour la réinitialisation de mot de passe
 */
@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService,
    private val emailProperties: EmailProperties
) {
    
    private val logger = LoggerFactory.getLogger(PasswordResetService::class.java)
    
    /**
     * Demande une réinitialisation de mot de passe
     * Génère un token et envoie un email avec le lien de réinitialisation
     */
    fun requestPasswordReset(request: ForgotPasswordRequest): Mono<Map<String, Any>> {
        return userRepository.findByEmail(request.email.lowercase())
            .flatMap { user ->
                if (!user.isEmailVerified) {
                    return@flatMap Mono.just<Map<String, Any>>(
                        mapOf(
                            "success" to false,
                            "message" to "Votre adresse email n'a pas été vérifiée. Veuillez d'abord vérifier votre email."
                        )
                    )
                }

                // Générer un token de réinitialisation
                val resetToken = CodeGenerator.generateSecureToken(32)
                val tokenExpiresAt = Instant.now()
                    .plusSeconds(emailProperties.passwordResetTokenExpirationMinutes * 60)

                val updatedUser = user.copy(
                    passwordResetToken = resetToken,
                    passwordResetTokenExpiresAt = tokenExpiresAt,
                    updatedAt = Instant.now()
                )

                userRepository.save(updatedUser)
                    .flatMap { savedUser ->
                        emailService.sendPasswordResetEmail(
                            to = savedUser.email,
                            resetToken = resetToken,
                            userName = savedUser.fullName
                        )
                            .then(
                                Mono.just<Map<String, Any>>(
                                    mapOf(
                                        "success" to true,
                                        "message" to "Si cette adresse email existe, un lien de réinitialisation a été envoyé."
                                    )
                                )
                            )
                    }
            }
            .switchIfEmpty(
                // Pour des raisons de sécurité, on ne révèle pas si l'email existe
                Mono.just<Map<String, Any>>(
                    mapOf(
                        "success" to true,
                        "message" to "Si cette adresse email existe, un lien de réinitialisation a été envoyé."
                    )
                )
            )
            .onErrorResume { error ->
                logger.error("Erreur lors de la demande de réinitialisation de mot de passe", error)
                Mono.just<Map<String, Any>>(
                    mapOf(
                        "success" to true,
                        "message" to "Si cette adresse email existe, un lien de réinitialisation a été envoyé."
                    )
                )
            }
    }
    
    /**
     * Réinitialise le mot de passe avec un token
     */
    fun resetPassword(request: ResetPasswordRequest): Mono<Map<String, Any>> {
        // Trouver l'utilisateur avec ce token
        return userRepository.findByPasswordResetToken(request.token)
            .switchIfEmpty(
                Mono.error(AuthenticationException("Token de réinitialisation invalide ou expiré"))
            )
            .flatMap { user ->
                // Vérifier que le token n'est pas expiré
                if (!user.hasValidPasswordResetToken(request.token)) {
                    return@flatMap Mono.error<Map<String, Any>>(
                        AuthenticationException("Token de réinitialisation invalide ou expiré")
                    )
                }
                
                // Mettre à jour le mot de passe et supprimer le token
                val updatedUser = user.copy(
                    password = passwordEncoder.encode(request.newPassword),
                    passwordResetToken = null,
                    passwordResetTokenExpiresAt = null,
                    failedLoginAttempts = 0, // Réinitialiser les tentatives échouées
                    lockedUntil = null,
                    updatedAt = Instant.now()
                )
                
                userRepository.save(updatedUser)
                    .flatMap { savedUser ->
                        logger.info("Mot de passe réinitialisé avec succès pour : ${savedUser.email}")
                        Mono.just(
                            mapOf(
                                "success" to true,
                                "message" to "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter."
                            )
                        )
                    }
            }
    }
}
