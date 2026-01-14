package com.kobecorporation.tmp_back.util

import com.kobecorporation.tmp_back.configuration.fileStorage.FileStorageProperties
import com.kobecorporation.tmp_back.interaction.exception.FileStorageException
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * Utilitaires pour le stockage de fichiers
 */
@Component
class FileStorageUtil(
    private val fileStorageProperties: FileStorageProperties
) {

    /**
     * Stocke un fichier dans le dossier spécifié
     * 
     * @param file Le fichier à stocker
     * @param directory Le dossier de destination (users ou stock)
     * @param entityId L'ID de l'entité (userId, productId, etc.)
     * @return L'URL relative du fichier stocké (ex: /uploads/users/user123_uuid.jpg)
     */
    fun storeFile(file: FilePart, directory: String, entityId: String): Mono<String> {
        return Mono.fromCallable {
            // Valider le type de fichier
            val contentType = file.headers().contentType?.toString() ?: ""
            if (!fileStorageProperties.isValidFileType(contentType)) {
                throw FileStorageException(
                    "File type not allowed. Allowed types: ${fileStorageProperties.allowedTypes}"
                )
            }

            // Générer un nom de fichier unique
            val fileExtension = getFileExtension(file.filename())
            val newFileName = "${entityId}_${UUID.randomUUID()}$fileExtension"

            // Chemin de destination
            val destinationPath = fileStorageProperties.getBasePath()
                .resolve(directory)
                .resolve(newFileName)

            // Créer le répertoire s'il n'existe pas
            Files.createDirectories(destinationPath.parent)

            // Sauvegarder le fichier
            file.transferTo(destinationPath).block()

            if (!Files.exists(destinationPath)) {
                throw FileStorageException("Failed to save file")
            }

            // Retourner l'URL relative
            "/uploads/$directory/$newFileName"
        }
    }

    /**
     * Supprime un fichier
     * 
     * @param fileName Le nom du fichier ou l'URL relative
     * @param directory Le dossier (users ou stock)
     * @return true si le fichier a été supprimé, false sinon
     */
    fun deleteFile(fileName: String, directory: String): Mono<Boolean> {
        return Mono.fromCallable {
            if (fileName.isBlank()) return@fromCallable false

            try {
                // Extraire le nom du fichier depuis l'URL si nécessaire
                val actualFileName = if (fileName.contains("/")) {
                    fileName.substringAfterLast("/")
                } else {
                    fileName
                }

                val filePath = fileStorageProperties.getBasePath()
                    .resolve(directory)
                    .resolve(actualFileName)

                if (Files.exists(filePath)) {
                    Files.delete(filePath)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Récupère l'extension d'un fichier
     */
    private fun getFileExtension(filename: String): String {
        return if (filename.contains(".")) {
            ".${filename.substringAfterLast(".")}"
        } else {
            ""
        }
    }

    /**
     * Vérifie si une URL est externe (http:// ou https://)
     */
    fun isExternalUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.startsWith("http://") || url.startsWith("https://")
    }

    /**
     * Vérifie si une URL est un fichier local (/uploads/)
     */
    fun isLocalFile(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.startsWith("/uploads/")
    }
}
