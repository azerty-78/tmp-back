package com.kobecorporation.tmp_back.configuration

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

/**
 * Configuration MongoDB personnalisée pour forcer l'utilisation des credentials
 * 
 * Cette configuration garantit que les credentials sont correctement utilisés
 * même si Spring Boot ne les parse pas correctement depuis l'URI.
 */
@Configuration
class MongoConfig(
    @Value("\${spring.data.mongodb.uri:mongodb://root:qwerty87@localhost:27017/project-name?authSource=admin}")
    private val mongoUri: String
) : AbstractReactiveMongoConfiguration() {

    override fun getDatabaseName(): String {
        // Extraire le nom de la base de données depuis l'URI
        return try {
            val connectionString = ConnectionString(mongoUri)
            connectionString.database ?: "project-name"
        } catch (e: Exception) {
            "project-name"
        }
    }

    @Bean
    override fun reactiveMongoClient(): MongoClient {
        println("🔧 Configuration MongoDB personnalisée")
        println("   URI: ${mongoUri.replace(Regex(":[^:@]+@"), ":****@")}") // Masquer le mot de passe dans les logs
        
        return try {
            val connectionString = ConnectionString(mongoUri)
            val settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build()
            
            println("   ✅ Credentials détectés: ${connectionString.credential != null}")
            if (connectionString.credential != null) {
                println("   ✅ Username: ${connectionString.credential?.userName}")
                println("   ✅ Auth Database: ${connectionString.credential?.source}")
            } else {
                println("   ⚠️  Aucun credential détecté dans l'URI!")
            }
            
            MongoClients.create(settings)
        } catch (e: Exception) {
            println("   ❌ Erreur lors de la création du MongoClient: ${e.message}")
            throw e
        }
    }

    // Spring Boot créera automatiquement le ReactiveMongoTemplate
    // à partir du MongoClient configuré ci-dessus
}
