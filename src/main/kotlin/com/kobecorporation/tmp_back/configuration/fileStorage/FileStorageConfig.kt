package com.kobecorporation.tmp_back.configuration.fileStorage

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

/**
 * Configuration pour servir les fichiers statiques (images)
 * 
 * Les fichiers sont accessibles via :
 * - /uploads/users/** : Images de profil utilisateur
 * - /uploads/stock/** : Images de produits/articles
 */
@Configuration
class FileStorageConfig(
    private val fileStorageProperties: FileStorageProperties
) : WebFluxConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadPath = fileStorageProperties.getBasePath().toString()

        // Servir tous les fichiers sous /uploads/**
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$uploadPath/")

        println("📂 Static file serving configured:")
        println("   URL pattern: /uploads/**")
        println("   File location: $uploadPath/")
        println("   Available paths:")
        println("   - /uploads/users/")
        println("   - /uploads/stock/")
    }
}
