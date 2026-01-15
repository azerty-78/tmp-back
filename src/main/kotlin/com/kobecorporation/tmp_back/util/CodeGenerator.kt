package com.kobecorporation.tmp_back.util

import java.security.SecureRandom

/**
 * Utilitaire pour générer des codes de vérification et tokens
 */
object CodeGenerator {
    
    private val random = SecureRandom()
    
    /**
     * Génère un code de vérification à 6 chiffres
     */
    fun generateVerificationCode(): String {
        return String.format("%06d", random.nextInt(1000000))
    }
    
    /**
     * Génère un token aléatoire sécurisé
     */
    fun generateSecureToken(length: Int = 32): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
}
