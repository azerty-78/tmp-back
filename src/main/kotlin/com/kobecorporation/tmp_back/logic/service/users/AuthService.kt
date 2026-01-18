package com.kobecorporation.tmp_back.logic.service.users

import com.kobecorporation.tmp_back.configuration.email.EmailProperties
import com.kobecorporation.tmp_back.configuration.security.jwt.JwtService
import com.kobecorporation.tmp_back.interaction.dto.users.request.LoginRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.RefreshTokenRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.RegisterRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.ResendVerificationCodeRequest
import com.kobecorporation.tmp_back.interaction.dto.users.request.VerifyEmailRequest
import com.kobecorporation.tmp_back.interaction.dto.users.response.AuthResponse
import com.kobecorporation.tmp_back.interaction.exception.AuthenticationException
import com.kobecorporation.tmp_back.interaction.exception.ResourceAlreadyExistsException
import com.kobecorporation.tmp_back.interaction.exception.ResourceNotFoundException
import com.kobecorporation.tmp_back.interaction.mapper.users.UserMapper
import com.kobecorporation.tmp_back.logic.model.users.Role
import com.kobecorporation.tmp_back.logic.model.users.User
import com.kobecorporation.tmp_back.logic.repository.users.UserRepository
import com.kobecorporation.tmp_back.logic.service.email.EmailService
import com.kobecorporation.tmp_back.util.CodeGenerator
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDate

/**
 * Service d'authentification
 * 
 * Gère :
 * - Inscription (register)
 * - Connexion (login)
 * - Rafraîchissement de token (refreshToken)
 * - Gestion du refresh token par utilisateur (renouvelé toutes les heures)
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val emailService: EmailService,
    private val emailProperties: EmailProperties
) {
    
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    /**
     * Inscription d'un nouvel utilisateur
     * 
     * IMPORTANT : L'utilisateur n'est créé QUE si l'email est envoyé avec succès
     * Un code de vérification est envoyé par email (valide 10 minutes)
     * Le compte n'est activé qu'après vérification du code
     */
    fun register(registerRequest: RegisterRequest): Mono<Map<String, Any>> {
        logger.info("📝 [REGISTER] Début de l'inscription pour : ${registerRequest.email}")
        
        return checkUserExists(registerRequest.email, registerRequest.username)
            .doOnSuccess {
                logger.info("✅ [REGISTER] Vérification email/username : OK (pas de doublon)")
            }
            .then(
                Mono.fromCallable {
                    logger.info("🔧 [REGISTER] Génération du code de vérification...")
                    val birthDate = registerRequest.birthDate?.let { LocalDate.parse(it) }
                    val verificationCode = CodeGenerator.generateVerificationCode()
                    // Code valide pendant 10 minutes
                    val codeExpiresAt = Instant.now().plusSeconds(600) // 10 minutes = 600 secondes
                    logger.info("✅ [REGISTER] Code généré : $verificationCode (expire dans 10 minutes)")

                    User(
                        username = registerRequest.username.lowercase(),
                        email = registerRequest.email.lowercase(),
                        password = passwordEncoder.encode(registerRequest.password),
                        firstName = registerRequest.firstName,
                        lastName = registerRequest.lastName,
                        birthDate = birthDate,
                        gender = registerRequest.gender,
                        role = Role.USER, // Par défaut, tous les nouveaux utilisateurs sont USER
                        isEmailVerified = false,
                        emailVerificationCode = verificationCode,
                        emailVerificationCodeExpiresAt = codeExpiresAt
                    )
                }
            )
            .flatMap { user ->
                logger.info("📧 [REGISTER] Tentative d'envoi de l'email AVANT création de l'utilisateur...")
                logger.info("📧 [REGISTER] Email destinataire : ${user.email}")
                logger.info("📧 [REGISTER] Code à envoyer : ${user.emailVerificationCode}")
                
                // Capturer le code et le nom avant de sauvegarder
                val verificationCode = user.emailVerificationCode ?: ""
                val userName = user.fullName
                
                // Envoyer l'email de vérification AVANT de créer l'utilisateur
                // Si l'email ne peut pas être envoyé, l'inscription échouera et l'utilisateur ne sera pas créé
                emailService.sendVerificationEmail(
                    to = user.email,
                    code = verificationCode,
                    userName = userName
                )
                .doOnSuccess {
                    logger.info("✅ [REGISTER] Email envoyé avec succès ! Création de l'utilisateur...")
                }
                .doOnError { error ->
                    logger.error("❌ [REGISTER] ÉCHEC de l'envoi d'email. L'utilisateur NE SERA PAS créé.", error)
                }
                .flatMap {
                    // SEULEMENT si l'email est envoyé avec succès, créer l'utilisateur
                    logger.info("💾 [REGISTER] Sauvegarde de l'utilisateur dans la base de données...")
                    userRepository.save(user)
                        .map { savedUser ->
                            logger.info("✅ [REGISTER] Utilisateur créé avec succès : ${savedUser.email} (ID: ${savedUser.id})")
                            mapOf(
                                "success" to true,
                                "message" to "Inscription réussie. Un code de vérification a été envoyé à votre adresse email (valide 10 minutes).",
                                "email" to savedUser.email,
                                "emailVerified" to false
                            )
                        }
                }
            }
    }
    
    /**
     * Vérifie l'email avec le code de vérification
     * 
     * IMPORTANT : Le compte n'est créé/activé qu'après vérification du code
     * Un email de confirmation est envoyé avec le rôle et le nom
     */
    fun verifyEmail(verifyEmailRequest: VerifyEmailRequest): Mono<AuthResponse> {
        return userRepository.findByEmail(verifyEmailRequest.email.lowercase())
            .switchIfEmpty(
                Mono.error(ResourceNotFoundException("Aucun compte trouvé avec cet email"))
            )
            .flatMap { user ->
                if (user.isEmailVerified) {
                    return@flatMap Mono.error<AuthResponse>(
                        AuthenticationException("Cette adresse email est déjà vérifiée")
                    )
                }
                
                if (!user.hasValidVerificationCode(verifyEmailRequest.code)) {
                    return@flatMap Mono.error<AuthResponse>(
                        AuthenticationException("Code de vérification invalide ou expiré. Le code est valide pendant 10 minutes.")
                    )
                }
                
                // Marquer l'email comme vérifié et supprimer le code
                val updatedUser = user.copy(
                    isEmailVerified = true,
                    emailVerificationCode = null,
                    emailVerificationCodeExpiresAt = null,
                    updatedAt = Instant.now()
                )
                
                userRepository.save(updatedUser)
                    .flatMap { savedUser ->
                        logger.info("✅ Email vérifié avec succès pour : ${savedUser.email}")
                        
                        // Envoyer un email de confirmation avec le rôle et le nom
                        emailService.sendAccountConfirmationEmail(
                            to = savedUser.email,
                            userName = savedUser.fullName,
                            role = savedUser.role.name
                        )
                        .then(
                            // Générer la réponse d'authentification (tokens JWT)
                            generateAuthResponse(savedUser, rememberMe = false)
                        )
                    }
            }
    }
    
    /**
     * Renvoie le code de vérification
     */
    fun resendVerificationCode(resendRequest: ResendVerificationCodeRequest): Mono<Map<String, Any>> {
        return userRepository.findByEmail(resendRequest.email.lowercase())
            .switchIfEmpty(
                Mono.error(ResourceNotFoundException("Aucun compte trouvé avec cet email"))
            )
            .flatMap { user ->
                if (user.isEmailVerified) {
                    return@flatMap Mono.error<Map<String, Any>>(
                        AuthenticationException("Cette adresse email est déjà vérifiée")
                    )
                }
                
                // Générer un nouveau code (valide 10 minutes)
                val verificationCode = CodeGenerator.generateVerificationCode()
                val codeExpiresAt = Instant.now().plusSeconds(600) // 10 minutes = 600 secondes
                
                val updatedUser = user.copy(
                    emailVerificationCode = verificationCode,
                    emailVerificationCodeExpiresAt = codeExpiresAt,
                    updatedAt = Instant.now()
                )
                
                userRepository.save(updatedUser)
                    .flatMap { savedUser ->
                        emailService.sendVerificationEmail(
                            to = savedUser.email,
                            code = verificationCode,
                            userName = savedUser.fullName
                        )
                        .then(
                            Mono.just(mapOf(
                                "success" to true,
                                "message" to "Un nouveau code de vérification a été envoyé à votre adresse email."
                            ))
                        )
                    }
            }
    }

    /**
     * Connexion d'un utilisateur
     */
    fun login(loginRequest: LoginRequest, rememberMe: Boolean = false): Mono<AuthResponse> {
        return findUserByEmailOrUsername(loginRequest.emailOrUsername)
            .flatMap { user ->
                // Vérifier que le compte est actif
                if (!user.isActive) {
                    return@flatMap Mono.error<AuthResponse>(
                        AuthenticationException("Votre compte a été désactivé. Veuillez contacter le support.")
                    )
                }
                
                // Vérifier que l'email est vérifié
                if (!user.isEmailVerified) {
                    return@flatMap Mono.error<AuthResponse>(
                        AuthenticationException("Votre adresse email n'a pas été vérifiée. Veuillez vérifier votre email ou demander un nouveau code.")
                    )
                }

                // Vérifier que le compte n'est pas verrouillé
                if (user.isLocked()) {
                    return@flatMap Mono.error<AuthResponse>(
                        AuthenticationException("Votre compte est temporairement verrouillé. Veuillez réessayer plus tard.")
                    )
                }

                // Vérifier le mot de passe
                if (!passwordEncoder.matches(loginRequest.password, user.password)) {
                    return@flatMap Mono.error<AuthResponse>(
                        AuthenticationException("Identifiants invalides")
                    )
                }

                // Mettre à jour lastLoginAt et réinitialiser les tentatives échouées
                val updatedUser = user.copy(
                    lastLoginAt = Instant.now(),
                    failedLoginAttempts = 0,
                    lockedUntil = null
                )

                userRepository.save(updatedUser)
                    .flatMap { savedUser ->
                        generateAuthResponse(savedUser, rememberMe)
                    }
            }
    }

    /**
     * Rafraîchit le token d'accès
     * 
     * Le refresh token est renouvelé avec une nouvelle expiration (maintenant + 1h)
     * pour gérer la session utilisateur
     */
    fun refreshToken(refreshTokenRequest: RefreshTokenRequest): Mono<AuthResponse> {
        val refreshToken = refreshTokenRequest.refreshToken

        // Valider le token
        return if (!jwtService.validateToken(refreshToken)) {
            Mono.error(AuthenticationException("Invalid refresh token"))
        } else if (jwtService.isTokenExpired(refreshToken)) {
            Mono.error(AuthenticationException("Refresh token expired"))
        } else {
            val tokenType = jwtService.extractTokenType(refreshToken)
            if (tokenType != "REFRESH") {
                Mono.error(AuthenticationException("Invalid token type"))
            } else {
                val userId = jwtService.extractUserId(refreshToken)
                processRefreshToken(userId, refreshToken)
            }
        }
    }

    private fun processRefreshToken(userId: org.bson.types.ObjectId, refreshToken: String): Mono<AuthResponse> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(AuthenticationException("User not found")))
            .flatMap { user ->
                // Vérifier que le refresh token stocké correspond
                if (user.refreshToken != refreshToken) {
                    return@flatMap Mono.error<AuthResponse>(
                        AuthenticationException("Invalid refresh token")
                    )
                }

                // Vérifier que le refresh token n'est pas expiré
                if (!user.hasValidRefreshToken()) {
                    return@flatMap Mono.error<AuthResponse>(
                        AuthenticationException("Refresh token expired")
                    )
                }

                // Récupérer le flag rememberMe depuis le token
                val rememberMe = try {
                    (jwtService.extractAllClaimsInternal(refreshToken)["rememberMe"] as? Boolean) ?: false
                } catch (e: Exception) {
                    false
                }

                // Générer de nouveaux tokens et mettre à jour le refresh token dans la base
                generateAuthResponse(user, rememberMe)
            }
    }

    /**
     * Génère la réponse d'authentification avec les tokens
     * 
     * Le refresh token est stocké dans User.refreshToken avec une expiration de 1h
     * (renouvelé à chaque utilisation)
     */
    private fun generateAuthResponse(user: User, rememberMe: Boolean = false): Mono<AuthResponse> {
        val accessToken = jwtService.generateAccessToken(user.id, user.email, user.role)
        val refreshToken = jwtService.generateRefreshToken(user.id, rememberMe)

        // Calculer les durées de vie en secondes
        val expiresIn = jwtService.getAccessTokenExpiration() / 1000
        val refreshExpiresIn = if (rememberMe) {
            jwtService.jwtProperties.rememberMeRefreshTokenExpiration / 1000
        } else {
            jwtService.jwtProperties.refreshTokenExpiration / 1000
        }

        // Mettre à jour le refresh token dans l'utilisateur avec expiration de 1h
        val refreshTokenExpiresAt = Instant.now().plusSeconds(3600) // 1 heure

        val updatedUser = user.copy(
            refreshToken = refreshToken,
            refreshTokenExpiresAt = refreshTokenExpiresAt,
            updatedAt = Instant.now()
        )

        return userRepository.save(updatedUser)
            .map { savedUser ->
                AuthResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = expiresIn,
                    refreshExpiresIn = refreshExpiresIn,
                    user = UserMapper.toResponse(savedUser)
                )
            }
    }

    /**
     * Vérifie si un email ou username existe déjà
     */
    private fun checkUserExists(email: String, username: String): Mono<Void> {
        return userRepository.existsByEmail(email.lowercase())
            .flatMap { emailExists ->
                if (emailExists) {
                    Mono.error<Void>(
                        ResourceAlreadyExistsException(
                            "Cette adresse email est déjà utilisée. Veuillez utiliser une autre adresse email ou vous connecter."
                        )
                    )
                } else {
                    userRepository.existsByUsername(username.lowercase())
                        .flatMap { usernameExists ->
                            if (usernameExists) {
                                Mono.error<Void>(
                                    ResourceAlreadyExistsException(
                                        "Ce nom d'utilisateur est déjà pris. Veuillez choisir un autre nom d'utilisateur."
                                    )
                                )
                            } else {
                                Mono.empty()
                            }
                        }
                }
            }
    }

    /**
     * Trouve un utilisateur par email ou username
     */
    private fun findUserByEmailOrUsername(emailOrUsername: String): Mono<User> {
        val normalized = emailOrUsername.lowercase()

        return if (normalized.contains("@")) {
            userRepository.findByEmail(normalized)
        } else {
            userRepository.findByUsername(normalized)
        }
        .switchIfEmpty(
            Mono.error(AuthenticationException("Identifiants invalides"))
        )
    }
    
    /**
     * Trouve un utilisateur par email
     */
    private fun findUserByEmail(email: String): Mono<User> {
        return userRepository.findByEmail(email.lowercase())
            .switchIfEmpty(
                Mono.error(ResourceNotFoundException("Aucun compte trouvé avec cet email"))
            )
    }
}
