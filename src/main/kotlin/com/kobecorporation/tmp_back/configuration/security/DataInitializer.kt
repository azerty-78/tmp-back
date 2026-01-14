package com.kobecorporation.tmp_back.configuration.security

import com.kobecorporation.tmp_back.logic.model.users.Gender
import com.kobecorporation.tmp_back.logic.model.users.Role
import com.kobecorporation.tmp_back.logic.model.users.User
import com.kobecorporation.tmp_back.logic.repository.users.UserRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Initialise les données par défaut au démarrage de l'application
 * Crée le ROOT_ADMIN si il n'existe pas
 */
@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${admin.email}") private val adminEmail: String,
    @Value("\${admin.password}") private val adminPassword: String,
    @Value("\${admin.username}") private val adminUsername: String,
    @Value("\${admin.firstname}") private val adminFirstName: String,
    @Value("\${admin.lastname}") private val adminLastName: String,
) {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    @PostConstruct
    fun init() {
        createRootAdmin()
            .subscribe(
                { user ->
                    logger.info("✅ Root Admin initialized: ${user.email} (Role: ${user.role})")
                },
                { error ->
                    logger.error("❌ Failed to initialize Root Admin: ${error.message}", error)
                }
            )
    }

    /**
     * Crée le ROOT_ADMIN au premier démarrage
     * Le ROOT_ADMIN est celui qui créera les ADMIN pour chaque client
     */
    private fun createRootAdmin(): Mono<User> {
        return userRepository.findByEmail(adminEmail)
            .switchIfEmpty(
                Mono.defer {
                    logger.info("🔧 Creating default Root Admin user...")
                    val rootAdmin = User(
                        username = adminUsername,
                        email = adminEmail,
                        password = passwordEncoder.encode(adminPassword),
                        firstName = adminFirstName,
                        lastName = adminLastName,
                        role = Role.ROOT_ADMIN, // ⚠️ ROOT_ADMIN
                        isEmailVerified = true,
                        isActive = true,
                        bio = "Default Root Administrator - System Owner",
                        gender = Gender.MALE
                    )
                    userRepository.save(rootAdmin)
                }
            )
            .doOnSuccess { user ->
                if (user != null) {
                    logger.info("ℹ️ Root Admin already exists: ${user.email} (Role: ${user.role})")
                }
            }
    }
}
