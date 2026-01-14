package com.kobecorporation.tmp_back.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * Configuration Jackson pour la sérialisation/désérialisation JSON
 * 
 * Gère les dates Java 8+ (LocalDate, Instant, etc.)
 * 
 * Note: Jackson 3.x sérialise les dates en ISO-8601 par défaut (plus besoin de WRITE_DATES_AS_TIMESTAMPS)
 * Pour WebFlux, Spring Boot configure automatiquement Jackson via les propriétés dans application.properties
 */
@Configuration
class JacksonConfig {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
            // Jackson 3.x sérialise les dates en ISO-8601 par défaut, pas besoin de désactiver WRITE_DATES_AS_TIMESTAMPS
        }
    }
}
