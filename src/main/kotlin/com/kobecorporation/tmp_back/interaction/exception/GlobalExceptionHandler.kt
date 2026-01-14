package com.kobecorporation.tmp_back.interaction.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import reactor.core.publisher.Mono
import java.util.*

/**
 * Handler global pour la gestion des exceptions
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(e: AuthenticationException): Mono<ResponseEntity<Map<String, Any>>> {
        logger.warn("Authentication error: ${e.message}")
        return Mono.just(
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Erreur d'authentification"),
                    "errorCode" to "AUTHENTICATION_FAILED",
                    "timestamp" to Date()
                ))
        )
    }

    @ExceptionHandler(ResourceAlreadyExistsException::class)
    fun handleResourceAlreadyExistsException(e: ResourceAlreadyExistsException): Mono<ResponseEntity<Map<String, Any>>> {
        logger.warn("Resource already exists: ${e.message}")
        return Mono.just(
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Cette ressource existe déjà"),
                    "errorCode" to "RESOURCE_ALREADY_EXISTS",
                    "timestamp" to Date()
                ))
        )
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(e: ResourceNotFoundException): Mono<ResponseEntity<Map<String, Any>>> {
        logger.warn("Resource not found: ${e.message}")
        return Mono.just(
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Ressource non trouvée"),
                    "errorCode" to "RESOURCE_NOT_FOUND",
                    "timestamp" to Date()
                ))
        )
    }

    @ExceptionHandler(FileStorageException::class)
    fun handleFileStorageException(e: FileStorageException): Mono<ResponseEntity<Map<String, Any>>> {
        logger.error("File storage error: ${e.message}", e)
        return Mono.just(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf(
                    "success" to false,
                    "message" to (e.message ?: "Erreur de stockage de fichier"),
                    "errorCode" to "FILE_STORAGE_ERROR",
                    "timestamp" to Date()
                ))
        )
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationException(e: WebExchangeBindException): Mono<ResponseEntity<Map<String, Any>>> {
        logger.warn("Validation error: ${e.message}")
        val errors = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return Mono.just(
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf(
                    "success" to false,
                    "message" to "Erreurs de validation",
                    "errorCode" to "VALIDATION_ERROR",
                    "errors" to errors,
                    "timestamp" to Date()
                ))
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): Mono<ResponseEntity<Map<String, Any>>> {
        logger.error("Unexpected error: ${e.message}", e)
        return Mono.just(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf(
                    "success" to false,
                    "message" to "Une erreur interne s'est produite",
                    "errorCode" to "INTERNAL_ERROR",
                    "timestamp" to Date()
                ))
        )
    }
}
