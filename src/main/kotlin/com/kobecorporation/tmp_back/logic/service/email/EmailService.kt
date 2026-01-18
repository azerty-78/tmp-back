package com.kobecorporation.tmp_back.logic.service.email

import com.kobecorporation.tmp_back.configuration.email.EmailProperties
import com.kobecorporation.tmp_back.interaction.exception.AuthenticationException
import org.slf4j.LoggerFactory
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Service pour l'envoi d'emails
 * 
 * Gère l'envoi d'emails pour :
 * - Vérification d'email lors de l'inscription
 * - Réinitialisation de mot de passe
 */
@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val emailProperties: EmailProperties
) {
    
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    
    /**
     * Envoie un email de vérification avec un code
     * L'erreur est propagée pour faire échouer l'inscription si l'email ne peut pas être envoyé
     */
    fun sendVerificationEmail(to: String, code: String, userName: String): Mono<Void> {
        val subject = "Vérification de votre adresse email - ${emailProperties.fromName}"
        val message = buildVerificationEmailMessage(code, userName)
        
        return sendEmail(to, subject, message)
            .doOnSuccess {
                logger.info("Email de vérification envoyé avec succès à : $to")
            }
            .doOnError { error ->
                logger.error("Erreur lors de l'envoi de l'email de vérification à : $to", error)
            }
    }
    
    /**
     * Envoie un email de réinitialisation de mot de passe
     * FAIT ÉCHOUER le flux si l'email ne peut pas être envoyé
     */
    fun sendPasswordResetEmail(to: String, resetToken: String, userName: String): Mono<Void> {
        val subject = "Réinitialisation de votre mot de passe - ${emailProperties.fromName}"
        val resetUrl = "${emailProperties.frontendUrl}/reset-password?token=$resetToken"
        val message = buildPasswordResetEmailMessage(resetUrl, userName)
        
        return sendEmail(to, subject, message)
            .doOnSuccess {
                logger.info("✅ Email de réinitialisation de mot de passe envoyé avec succès à : $to")
            }
            .doOnError { error ->
                logger.error("❌ Erreur lors de l'envoi de l'email de réinitialisation à : $to", error)
            }
    }
    
    /**
     * Envoie un email de confirmation de création de compte
     * Inclut le rôle et le nom de l'utilisateur
     */
    fun sendAccountConfirmationEmail(to: String, userName: String, role: String): Mono<Void> {
        val subject = "Bienvenue sur ${emailProperties.fromName} - Votre compte a été créé"
        val message = buildAccountConfirmationMessage(userName, role)
        
        return sendEmail(to, subject, message)
            .doOnSuccess {
                logger.info("✅ Email de confirmation de compte envoyé avec succès à : $to")
            }
            .doOnError { error ->
                logger.error("❌ Erreur lors de l'envoi de l'email de confirmation à : $to", error)
            }
    }
    
    /**
     * Envoie un email générique
     */
    private fun sendEmail(to: String, subject: String, content: String): Mono<Void> {
        return Mono.fromCallable {
            val message = SimpleMailMessage()
            message.setFrom("${emailProperties.fromName} <${emailProperties.fromAddress}>")
            message.setTo(to)
            message.setSubject(subject)
            message.setText(content)
            
            try {
                mailSender.send(message)
            } catch (e: MailException) {
                logger.error("Erreur lors de l'envoi de l'email", e)
                throw RuntimeException("Impossible d'envoyer l'email", e)
            }
        }
        .subscribeOn(Schedulers.boundedElastic())
        .then()
    }
    
    /**
     * Construit le message d'email de vérification
     * Le code est toujours valide pendant 10 minutes
     */
    private fun buildVerificationEmailMessage(code: String, userName: String): String {
        return """
            Bonjour $userName,
            
            Bienvenue sur ${emailProperties.fromName} !
            
            Pour vérifier votre adresse email et activer votre compte, veuillez utiliser le code de vérification suivant :
            
            Code : $code
            
            ⚠️ Ce code est valide pendant 10 minutes seulement.
            
            Si vous n'avez pas créé de compte, veuillez ignorer cet email.
            
            Cordialement,
            L'équipe ${emailProperties.fromName}
        """.trimIndent()
    }
    
    /**
     * Construit le message d'email de réinitialisation de mot de passe
     */
    private fun buildPasswordResetEmailMessage(resetUrl: String, userName: String): String {
        return """
            Bonjour $userName,
            
            Vous avez demandé à réinitialiser votre mot de passe sur ${emailProperties.fromName}.
            
            Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :
            
            $resetUrl
            
            Ce lien est valide pendant ${emailProperties.passwordResetTokenExpirationMinutes} minutes.
            
            Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.
            Votre mot de passe ne sera pas modifié.
            
            Cordialement,
            L'équipe ${emailProperties.fromName}
        """.trimIndent()
    }
    
    /**
     * Construit le message d'email de confirmation de compte
     */
    private fun buildAccountConfirmationMessage(userName: String, role: String): String {
        val roleDisplayName = when (role.uppercase()) {
            "USER" -> "Utilisateur"
            "EMPLOYE" -> "Employé"
            "ADMIN" -> "Administrateur"
            "ROOT_ADMIN" -> "Administrateur Principal"
            else -> role
        }
        
        return """
            Bonjour $userName,
            
            Félicitations ! Votre compte a été créé avec succès sur ${emailProperties.fromName}.
            
            Votre compte :
            - Nom : $userName
            - Rôle : $roleDisplayName
            
            Vous pouvez maintenant vous connecter et profiter de tous nos services.
            
            Si vous avez des questions ou besoin d'aide, n'hésitez pas à nous contacter.
            
            Bienvenue parmi nous !
            
            Cordialement,
            L'équipe ${emailProperties.fromName}
        """.trimIndent()
    }
}
