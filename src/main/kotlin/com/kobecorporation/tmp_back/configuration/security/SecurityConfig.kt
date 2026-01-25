package com.kobecorporation.tmp_back.configuration.security

import com.kobecorporation.tmp_back.configuration.security.jwt.JwtAuthenticationManager
import com.kobecorporation.tmp_back.configuration.security.jwt.JwtServerAuthenticationConverter
import com.kobecorporation.tmp_back.configuration.security.jwt.JwtServerSecurityContextRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

/**
 * Configuration Spring Security pour WebFlux Reactive
 * 
 * Architecture Multi-Tenant SaaS :
 * - PLATFORM_ADMIN : Super admin qui gère tous les tenants
 * - ROOT_ADMIN : Admin principal d'un tenant
 * - ADMIN : Gestion des employés et contenu (au sein d'un tenant)
 * - EMPLOYE : Interface de management (au sein d'un tenant)
 * - USER : Utilisateur standard
 * 
 * TenantRole (au sein d'un tenant) :
 * - OWNER : Propriétaire du tenant
 * - ADMIN : Administrateur du tenant
 * - MEMBER : Membre standard
 * - GUEST : Accès limité
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationManager: JwtAuthenticationManager,
    private val jwtServerAuthenticationConverter: JwtServerAuthenticationConverter,
    private val jwtServerSecurityContextRepository: JwtServerSecurityContextRepository,

    // URLs configurables depuis application.properties
    @Value("\${app.frontend-url:}") private val frontendUrl: String,
    @Value("\${app.allowed-origins:http://localhost:3000,http://localhost:5174}") private val allowedOrigins: String,
    @Value("\${tenant.platform-domain:kobecorporation.com}") private val platformDomain: String
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authenticationWebFilter = AuthenticationWebFilter(jwtAuthenticationManager).apply {
            setServerAuthenticationConverter(jwtServerAuthenticationConverter)
            setSecurityContextRepository(jwtServerSecurityContextRepository)
        }

        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .securityContextRepository(jwtServerSecurityContextRepository)
            .authorizeExchange { exchanges ->
                exchanges
                    // ===== ROUTES PUBLIQUES (Pas d'authentification) =====
                    
                    // Authentification classique
                    .pathMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                    .pathMatchers("/api/auth/verify-email", "/api/auth/resend-verification-code").permitAll()
                    .pathMatchers("/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                    
                    // === MULTI-TENANT : Routes publiques ===
                    // Création de tenant (signup)
                    .pathMatchers(HttpMethod.POST, "/api/tenants/signup").permitAll()
                    // Vérification disponibilité slug
                    .pathMatchers(HttpMethod.GET, "/api/tenants/check-slug/**").permitAll()
                    // Accepter une invitation
                    .pathMatchers(HttpMethod.GET, "/api/invitations/**").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/invitations/*/accept").permitAll()
                    
                    // Health check et documentation
                    .pathMatchers("/actuator/health", "/health", "/actuator/info").permitAll()
                    .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                    
                    // Lecture publique (e-commerce - produits, articles, etc.)
                    .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/content/**").permitAll()
                    
                    // Images publiques (lecture)
                    .pathMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/uploads/**").permitAll()
                    
                    // Recherche publique
                    .pathMatchers(HttpMethod.GET, "/api/search/**").permitAll()
                    
                    // Profils utilisateurs publics (lecture)
                    .pathMatchers(HttpMethod.GET, "/api/users/profile/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/users/username/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/users/{userId}/public").permitAll()

                    // ===== ROUTES PLATFORM ADMIN (Super Admin Multi-Tenant) =====
                    // Gestion de tous les tenants
                    .pathMatchers("/api/platform/**").hasRole("PLATFORM_ADMIN")
                    .pathMatchers("/api/platform/admin/**").hasRole("PLATFORM_ADMIN")
                    .pathMatchers("/api/platform/tenants/**").hasRole("PLATFORM_ADMIN")
                    .pathMatchers("/api/platform/stats/**").hasRole("PLATFORM_ADMIN")

                    // ===== ROUTES TENANT (Authentifié + dans un tenant) =====
                    // Informations du tenant courant
                    .pathMatchers(HttpMethod.GET, "/api/tenants/me").authenticated()
                    .pathMatchers(HttpMethod.PUT, "/api/tenants/me").authenticated()
                    .pathMatchers(HttpMethod.PUT, "/api/tenants/me/domain").authenticated()
                    // Membres du tenant
                    .pathMatchers(HttpMethod.GET, "/api/tenants/me/members").authenticated()
                    // Invitations (réservé aux admins du tenant - vérifié dans le controller)
                    .pathMatchers("/api/tenants/me/invitations/**").authenticated()

                    // ===== ROUTES EMPLOYE (Interface de Management) =====
                    // Gestion du contenu (CRUD)
                    .pathMatchers(HttpMethod.POST, "/api/content/**").hasAnyRole("EMPLOYE", "ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")
                    .pathMatchers(HttpMethod.PUT, "/api/content/**").hasAnyRole("EMPLOYE", "ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")
                    .pathMatchers(HttpMethod.DELETE, "/api/content/**").hasAnyRole("EMPLOYE", "ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")
                    
                    // Gestion des produits/articles (CRUD)
                    .pathMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("EMPLOYE", "ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")
                    .pathMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("EMPLOYE", "ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")
                    .pathMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("EMPLOYE", "ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")
                    
                    // Upload d'images (authentifié)
                    .pathMatchers(HttpMethod.POST, "/api/uploads/**").hasAnyRole("EMPLOYE", "ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")
                    .pathMatchers(HttpMethod.DELETE, "/api/uploads/**").hasAnyRole("EMPLOYE", "ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")

                    // ===== ROUTES ADMIN (Gestion des Employés dans le tenant) =====
                    // Gestion des employés (création, modification, suppression)
                    .pathMatchers("/api/admin/employees/**").hasAnyRole("ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")
                    .pathMatchers("/api/admin/users/**").hasAnyRole("ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")
                    .pathMatchers("/api/admin/stats/**").hasAnyRole("ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")
                    
                    // Promotion/rétrogradation des rôles
                    .pathMatchers("/api/users/{userId}/role/**").hasAnyRole("ADMIN", "ROOT_ADMIN", "PLATFORM_ADMIN")

                    // ===== ROUTES ROOT_ADMIN (Configuration du tenant) =====
                    // Gestion des ADMIN (création par ROOT_ADMIN uniquement)
                    .pathMatchers("/api/root/admin/**").hasAnyRole("ROOT_ADMIN", "PLATFORM_ADMIN")
                    .pathMatchers("/api/root/system/**").hasAnyRole("ROOT_ADMIN", "PLATFORM_ADMIN")
                    .pathMatchers("/api/root/config/**").hasAnyRole("ROOT_ADMIN", "PLATFORM_ADMIN")

                    // ===== ROUTES UTILISATEUR AUTHENTIFIÉ =====
                    // Gestion du profil utilisateur
                    .pathMatchers("/api/users/me", "/api/users/me/**").authenticated()
                    .pathMatchers(HttpMethod.PUT, "/api/users/me/**").authenticated()
                    .pathMatchers(HttpMethod.DELETE, "/api/users/me/**").authenticated()
                    
                    // Actions utilisateur authentifié
                    .pathMatchers("/api/users/follow/**").authenticated()
                    .pathMatchers("/api/users/unfollow/**").authenticated()
                    .pathMatchers("/api/favorites/**").authenticated()
                    .pathMatchers("/api/cart/**").authenticated()

                    // Toutes les autres routes nécessitent une authentification
                    .anyExchange().authenticated()
            }
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        // Parser les origins depuis la propriété (séparés par des virgules)
        val originsFromConfig = allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toMutableSet()

        // Ajouter l'URL frontend explicite si définie
        if (frontendUrl.isNotBlank()) {
            originsFromConfig.add(frontendUrl.trim())
        }

        println("🌐 CORS - Allowed Origins: $originsFromConfig")
        println("🌐 CORS - Frontend URL: $frontendUrl")
        println("🌐 CORS - Platform Domain: $platformDomain")

        val configuration = CorsConfiguration().apply {
            // Utiliser les origins configurés (+ frontendUrl)
            allowedOrigins = originsFromConfig.toList()
            
            // Patterns pour les sous-domaines du platform domain (multi-tenant)
            // Accepte tous les sous-domaines de kobecorporation.com
            allowedOriginPatterns = listOf(
                "https://*.${platformDomain}",
                "http://*.${platformDomain}",
                "http://localhost:*",           // Dev local
                "http://127.0.0.1:*"            // Dev local
            )

            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization", "X-Total-Count", "X-Tenant-ID", "X-Tenant-Error")
            allowCredentials = true
            maxAge = 3600
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
