package com.kobecorporation.tmp_back.configuration.fileStorage

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class FileStorageConfig(
    private val fileStorageProperties: FileStorageProperties
) : WebFluxConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadPath = fileStorageProperties.getBasePath().toString()

        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$uploadPath/")

        println("Static file serving configured:")
        println("   URL pattern: /uploads/**")
        println("   File location: $uploadPath/")
        println("   Available paths:")
        println("   - /uploads/users/")
        println("   - /uploads/stock/")
    }
}
