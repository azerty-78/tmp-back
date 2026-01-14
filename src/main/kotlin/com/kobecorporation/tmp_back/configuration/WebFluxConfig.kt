package com.kobecorporation.tmp_back.configuration

import com.kobecorporation.tmp_back.configuration.security.jwt.OptionalAuthenticationPrincipalResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer

/**
 * Configuration WebFlux pour les resolvers d'arguments personnalisés
 */
@Configuration
class WebFluxConfig : WebFluxConfigurer {

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(
            OptionalAuthenticationPrincipalResolver()
        )
    }
}
