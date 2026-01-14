package com.kobecorporation.tmp_back.configuration.fileStorage

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Propriétés de configuration pour le stockage de fichiers
 * 
 * Dossiers obligatoires :
 * - users : Images de profil utilisateur
 * - stock : Images de produits/articles (e-commerce)
 */
@Component
@ConfigurationProperties(prefix = "file.storage")
data class FileStorageProperties(
    var basePath: String = "./uploads",
    var maxFileSize: Long = 5 * 1024 * 1024, // 5MB en bytes
    var allowedTypes: List<String> = listOf("image/jpeg", "image/png", "image/gif", "image/webp"),
    var usersPath: String = "users",
    var stockPath: String = "stock"
) {

    // Chemins absolus
    fun getBasePath(): Path = Paths.get(basePath).toAbsolutePath().normalize()
    fun getUsersPath(): Path = getBasePath().resolve(usersPath)
    fun getStockPath(): Path = getBasePath().resolve(stockPath)

    @PostConstruct
    fun init() {
        // Créer tous les répertoires au démarrage
        val directories = listOf(
            getBasePath(),
            getUsersPath(),
            getStockPath()
        )

        directories.forEach { dir ->
            if (!Files.exists(dir)) {
                Files.createDirectories(dir)
                println("✅ Created directory: $dir")
            } else {
                println("✓ Directory exists: $dir")
            }
        }

        println("📁 File storage initialized at: ${getBasePath()}")
        println("   - Users: ${getUsersPath()}")
        println("   - Stock: ${getStockPath()}")
    }

    // Méthodes utilitaires
    fun isValidFileType(contentType: String): Boolean {
        return allowedTypes.contains(contentType)
    }

    fun getMaxFileSizeMB(): Double {
        return maxFileSize / (1024.0 * 1024.0)
    }
}
