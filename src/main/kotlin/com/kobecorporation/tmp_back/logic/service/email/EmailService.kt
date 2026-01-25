package com.kobecorporation.tmp_back.logic.service.email

import com.kobecorporation.tmp_back.configuration.email.EmailProperties
import com.kobecorporation.tmp_back.configuration.tenant.TenantContext
import com.kobecorporation.tmp_back.logic.model.tenant.Tenant
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
 * Architecture Multi-Tenant :
 * - Supporte le branding personnalisé par tenant
 * - Utilise le nom et email du tenant si configuré
 * - Fallback sur la configuration par défaut
 * 
 * Gère l'envoi d'emails pour :
 * - Vérification d'email lors de l'inscription
 * - Réinitialisation de mot de passe
 * - Invitations à rejoindre un tenant
 * - Bienvenue dans un tenant
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
        logger.info("📧 [EMAIL] sendVerificationEmail() appelé")
        logger.info("📧 [EMAIL] Destinataire : $to")
        logger.info("📧 [EMAIL] Code : $code")
        logger.info("📧 [EMAIL] Nom utilisateur : $userName")
        
        val subject = "Vérification de votre adresse email - ${emailProperties.fromName}"
        logger.info("📧 [EMAIL] Sujet : $subject")
        
        val message = buildVerificationEmailMessage(code, userName)
        logger.info("📧 [EMAIL] Message construit (${message.length} caractères)")
        
        logger.info("📧 [EMAIL] Appel de sendEmail()...")
        return sendEmail(to, subject, message)
            .doOnSuccess {
                logger.info("✅ [EMAIL] Email de vérification envoyé avec succès à : $to")
            }
            .doOnError { error ->
                logger.error("❌ [EMAIL] Erreur lors de l'envoi de l'email de vérification à : $to", error)
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
        logger.info("📮 [SEND_EMAIL] Début de l'envoi d'email")
        logger.info("📮 [SEND_EMAIL] Configuration SMTP :")
        logger.info("   - From: ${emailProperties.fromName} <${emailProperties.fromAddress}>")
        logger.info("   - To: $to")
        logger.info("   - Subject: $subject")
        logger.info("   - Content length: ${content.length} caractères")
        
        return Mono.fromCallable {
            logger.info("📮 [SEND_EMAIL] Création du message SimpleMailMessage...")
            val message = SimpleMailMessage()
            message.setFrom("${emailProperties.fromName} <${emailProperties.fromAddress}>")
            message.setTo(to)
            message.setSubject(subject)
            message.setText(content)
            
            logger.info("📮 [SEND_EMAIL] Message créé. Tentative d'envoi via JavaMailSender...")
            logger.info("📮 [SEND_EMAIL] mailSender.send() appelé...")
            
            try {
                mailSender.send(message)
                logger.info("✅ [SEND_EMAIL] mailSender.send() réussi !")
            } catch (e: MailException) {
                logger.error("❌ [SEND_EMAIL] EXCEPTION lors de l'envoi de l'email", e)
                logger.error("❌ [SEND_EMAIL] Type d'exception : ${e.javaClass.simpleName}")
                logger.error("❌ [SEND_EMAIL] Message : ${e.message}")
                if (e.cause != null) {
                    logger.error("❌ [SEND_EMAIL] Cause : ${e.cause?.javaClass?.simpleName} - ${e.cause?.message}")
                }
                throw RuntimeException("Impossible d'envoyer l'email", e)
            }
        }
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSubscribe {
            logger.info("📮 [SEND_EMAIL] Subscription sur boundedElastic scheduler")
        }
        .doOnError { error ->
            logger.error("❌ [SEND_EMAIL] Erreur dans le Mono : ${error.javaClass.simpleName} - ${error.message}")
        }
        .then()
        .doOnSuccess {
            logger.info("✅ [SEND_EMAIL] Mono terminé avec succès")
        }
        .doOnError { error ->
            logger.error("❌ [SEND_EMAIL] Mono terminé avec erreur : ${error.javaClass.simpleName}", error)
        }
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
            "PLATFORM_ADMIN" -> "Administrateur Plateforme"
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
    
    // ===== MÉTHODES MULTI-TENANT =====
    
    /**
     * Envoie un email d'invitation à rejoindre un tenant
     */
    fun sendTenantInvitationEmail(
        to: String,
        inviterName: String,
        tenant: Tenant,
        invitationToken: String,
        role: String
    ): Mono<Void> {
        val fromName = getFromName(tenant)
        val subject = "Invitation à rejoindre $fromName"
        val invitationUrl = "https://${tenant.activeDomain}/invitation?token=$invitationToken"
        
        val message = buildInvitationEmailMessage(inviterName, tenant, invitationUrl, role)
        
        return sendEmailWithTenantBranding(to, subject, message, tenant)
            .doOnSuccess {
                logger.info("✅ Email d'invitation envoyé à $to pour le tenant ${tenant.name}")
            }
    }
    
    /**
     * Envoie un email de bienvenue dans un tenant
     */
    fun sendTenantWelcomeEmail(
        to: String,
        userName: String,
        tenant: Tenant,
        role: String
    ): Mono<Void> {
        val fromName = getFromName(tenant)
        val subject = "Bienvenue sur $fromName !"
        
        val message = buildTenantWelcomeMessage(userName, tenant, role)
        
        return sendEmailWithTenantBranding(to, subject, message, tenant)
            .doOnSuccess {
                logger.info("✅ Email de bienvenue envoyé à $to pour le tenant ${tenant.name}")
            }
    }
    
    /**
     * Envoie un email de vérification avec branding du tenant
     */
    fun sendVerificationEmailWithTenant(
        to: String,
        code: String,
        userName: String,
        tenant: Tenant?
    ): Mono<Void> {
        return if (tenant != null) {
            val fromName = getFromName(tenant)
            val subject = "Vérification de votre adresse email - $fromName"
            val message = buildVerificationEmailMessageWithTenant(code, userName, tenant)
            sendEmailWithTenantBranding(to, subject, message, tenant)
        } else {
            sendVerificationEmail(to, code, userName)
        }
    }
    
    /**
     * Envoie un email avec le branding du tenant
     */
    private fun sendEmailWithTenantBranding(
        to: String,
        subject: String,
        content: String,
        tenant: Tenant
    ): Mono<Void> {
        val fromName = getFromName(tenant)
        val fromAddress = getFromAddress(tenant)
        
        logger.info("📮 [TENANT_EMAIL] Envoi avec branding tenant: $fromName <$fromAddress>")
        
        return Mono.fromCallable {
            val message = SimpleMailMessage()
            message.setFrom("$fromName <$fromAddress>")
            message.setTo(to)
            message.setSubject(subject)
            message.setText(content)
            
            try {
                mailSender.send(message)
                logger.info("✅ [TENANT_EMAIL] Email envoyé avec succès")
            } catch (e: MailException) {
                logger.error("❌ [TENANT_EMAIL] Erreur lors de l'envoi", e)
                throw RuntimeException("Impossible d'envoyer l'email", e)
            }
        }
        .subscribeOn(Schedulers.boundedElastic())
        .then()
    }
    
    /**
     * Récupère le nom d'expéditeur (tenant custom ou défaut)
     */
    private fun getFromName(tenant: Tenant?): String {
        return tenant?.settings?.emailFromName 
            ?: tenant?.name 
            ?: emailProperties.fromName
    }
    
    /**
     * Récupère l'adresse email d'expéditeur (tenant custom ou défaut)
     */
    private fun getFromAddress(tenant: Tenant?): String {
        return tenant?.settings?.emailFromAddress 
            ?: emailProperties.fromAddress
    }
    
    /**
     * Construit le message d'invitation
     */
    private fun buildInvitationEmailMessage(
        inviterName: String,
        tenant: Tenant,
        invitationUrl: String,
        role: String
    ): String {
        val roleDisplayName = when (role.uppercase()) {
            "OWNER" -> "Propriétaire"
            "ADMIN" -> "Administrateur"
            "MEMBER" -> "Membre"
            "GUEST" -> "Invité"
            else -> role
        }
        
        return """
            Bonjour,
            
            $inviterName vous invite à rejoindre ${tenant.name} !
            
            Vous avez été invité en tant que : $roleDisplayName
            
            Pour accepter cette invitation et créer votre compte, cliquez sur le lien ci-dessous :
            
            $invitationUrl
            
            Ce lien est valide pendant 7 jours.
            
            Si vous n'avez pas demandé cette invitation, vous pouvez ignorer cet email.
            
            Cordialement,
            L'équipe ${getFromName(tenant)}
        """.trimIndent()
    }
    
    /**
     * Construit le message de bienvenue dans un tenant
     */
    private fun buildTenantWelcomeMessage(
        userName: String,
        tenant: Tenant,
        role: String
    ): String {
        val roleDisplayName = when (role.uppercase()) {
            "OWNER" -> "Propriétaire"
            "ADMIN" -> "Administrateur"
            "MEMBER" -> "Membre"
            "GUEST" -> "Invité"
            else -> role
        }
        
        return """
            Bonjour $userName,
            
            Bienvenue sur ${tenant.name} !
            
            Votre compte a été créé avec succès.
            
            Vos informations :
            - Rôle : $roleDisplayName
            - Espace : ${tenant.name}
            - URL : https://${tenant.activeDomain}
            
            Vous pouvez maintenant vous connecter et commencer à utiliser la plateforme.
            
            Si vous avez des questions, n'hésitez pas à contacter l'administrateur de votre espace.
            
            Bienvenue parmi nous !
            
            Cordialement,
            L'équipe ${getFromName(tenant)}
        """.trimIndent()
    }
    
    /**
     * Construit le message de vérification avec branding tenant
     */
    private fun buildVerificationEmailMessageWithTenant(
        code: String,
        userName: String,
        tenant: Tenant
    ): String {
        return """
            Bonjour $userName,
            
            Bienvenue sur ${tenant.name} !
            
            Pour vérifier votre adresse email et activer votre compte, veuillez utiliser le code de vérification suivant :
            
            Code : $code
            
            ⚠️ Ce code est valide pendant 10 minutes seulement.
            
            Si vous n'avez pas créé de compte, veuillez ignorer cet email.
            
            Cordialement,
            L'équipe ${getFromName(tenant)}
        """.trimIndent()
    }
}
