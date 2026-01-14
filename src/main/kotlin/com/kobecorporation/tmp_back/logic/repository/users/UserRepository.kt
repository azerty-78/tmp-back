package com.kobecorporation.tmp_back.logic.repository.users

import com.kobecorporation.tmp_back.logic.model.users.Role
import com.kobecorporation.tmp_back.logic.model.users.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

/**
 * Repository réactif pour les utilisateurs
 */
@Repository
interface UserRepository : ReactiveMongoRepository<User, ObjectId> {
    
    /**
     * Trouve un utilisateur par email
     */
    fun findByEmail(email: String): Mono<User>
    
    /**
     * Trouve un utilisateur par username
     */
    fun findByUsername(username: String): Mono<User>
    
    /**
     * Vérifie si un email existe
     */
    fun existsByEmail(email: String): Mono<Boolean>
    
    /**
     * Vérifie si un username existe
     */
    fun existsByUsername(username: String): Mono<Boolean>
    
    /**
     * Trouve un utilisateur par email ou username
     */
    fun findByEmailOrUsername(email: String, username: String): Mono<User>
    
    /**
     * Trouve tous les utilisateurs par rôle
     */
    fun findByRole(role: Role): reactor.core.publisher.Flux<User>
    
    /**
     * Trouve un utilisateur par refresh token
     */
    fun findByRefreshToken(refreshToken: String): Mono<User>
    
    /**
     * Trouve tous les utilisateurs actifs
     */
    fun findByIsActiveTrue(): reactor.core.publisher.Flux<User>
}
