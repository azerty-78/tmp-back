package com.kobecorporation.tmp_back.configuration.email

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Configuration pour l'envoi d'emails
 */
@Configuration
@ConfigurationProperties(prefix = "app.email")
data class EmailProperties(
    /**
     * Adresse email de l'expéditeur (nom de domaine de l'application)
     */
    var fromAddress: String = "noreply@example.com",
    /**
     * Nom de l'expéditeur
     */
    var fromName: String = "KOBE Corporation",
    
    /**
     * URL du frontend pour les liens dans les emails
     */
    var frontendUrl: String = "http://localhost:3000",
    
    /**
     * Durée de validité du code de vérification (en minutes)
     */
    var verificationCodeExpirationMinutes: Long = 15,
    
    /**
     * Durée de validité du token de réinitialisation (en minutes)
     */
    var passwordResetTokenExpirationMinutes: Long = 30
)
