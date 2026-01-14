package com.kobecorporation.tmp_back.configuration.security.jwt

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Repository personnalisé pour stocker le SecurityContext dans les attributs de l'échange
 * 
 * Pour les applications JWT stateless (sans session), le contexte est stocké
 * dans les attributs de l'échange WebFlux
 */
@Component
class JwtServerSecurityContextRepository : ServerSecurityContextRepository {

    companion object {
        private const val SECURITY_CONTEXT_ATTR_NAME = "SECURITY_CONTEXT"
    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        return Mono.defer {
            val context = exchange.getAttribute<SecurityContext>(SECURITY_CONTEXT_ATTR_NAME)
            if (context != null) {
                Mono.just(context)
            } else {
                // Retourner un SecurityContext vide mais non-null
                Mono.just(SecurityContextImpl())
            }
        }
    }

    override fun save(exchange: ServerWebExchange, context: SecurityContext?): Mono<Void> {
        return Mono.fromRunnable {
            exchange.attributes[SECURITY_CONTEXT_ATTR_NAME] = context as Any
        }
    }
}
