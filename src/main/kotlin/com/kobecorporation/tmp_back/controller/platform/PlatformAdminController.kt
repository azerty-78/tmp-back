package com.kobecorporation.tmp_back.controller.platform

import com.kobecorporation.tmp_back.interaction.dto.tenant.response.MemberResponse
import com.kobecorporation.tmp_back.interaction.dto.tenant.response.TenantResponse
import com.kobecorporation.tmp_back.logic.model.tenant.TenantStatus
import com.kobecorporation.tmp_back.logic.model.users.User
import com.kobecorporation.tmp_back.logic.service.tenant.TenantService
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * Controller pour l'administration de la plateforme (Platform Admin)
 * 
 * Routes réservées au PLATFORM_ADMIN (super admin sans tenant)
 * Permet de gérer tous les tenants de la plateforme
 * 
 * Routes :
 * - GET /api/platform/admin/tenants : Liste tous les tenants
 * - GET /api/platform/admin/tenants/{id} : Détails d'un tenant
 * - PUT /api/platform/admin/tenants/{id}/status : Changer le statut
 * - DELETE /api/platform/admin/tenants/{id} : Supprimer un tenant
 * - GET /api/platform/admin/tenants/{id}/members : Membres d'un tenant
 * - GET /api/platform/admin/stats : Statistiques globales
 */
@RestController
@RequestMapping("/api/platform/admin")
class PlatformAdminController(
    private val tenantService: TenantService
) {
    
    private val logger = LoggerFactory.getLogger(PlatformAdminController::class.java)
    
    // ===== GESTION DES TENANTS =====
    
    /**
     * Liste tous les tenants de la plateforme
     */
    @GetMapping("/tenants")
    fun getAllTenants(
        @AuthenticationPrincipal user: User,
        @RequestParam(required = false) status: TenantStatus?
    ): Mono<ResponseEntity<List<TenantResponse>>> {
        logger.info("👑 [PLATFORM] GET /api/platform/admin/tenants par ${user.email}")
        
        val tenantsFlux = if (status != null) {
            tenantService.getTenantsByStatus(status)
        } else {
            tenantService.getAllTenants()
        }
        
        return tenantsFlux
            .flatMap { tenant ->
                tenantService.getMemberCount(tenant.id)
                    .map { count -> TenantResponse.fromTenant(tenant, count) }
            }
            .collectList()
            .map { tenants -> ResponseEntity.ok(tenants) }
    }
    
    /**
     * Récupère les détails d'un tenant
     */
    @GetMapping("/tenants/{tenantId}")
    fun getTenant(
        @AuthenticationPrincipal user: User,
        @PathVariable tenantId: String
    ): Mono<ResponseEntity<TenantResponse>> {
        logger.info("👑 [PLATFORM] GET /api/platform/admin/tenants/$tenantId par ${user.email}")
        
        return tenantService.getTenantById(ObjectId(tenantId))
            .flatMap { tenant ->
                tenantService.getMemberCount(tenant.id)
                    .map { count -> TenantResponse.fromTenant(tenant, count) }
            }
            .map { response -> ResponseEntity.ok(response) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }
    
    /**
     * Change le statut d'un tenant (suspendre, activer, etc.)
     */
    @PutMapping("/tenants/{tenantId}/status")
    fun updateTenantStatus(
        @AuthenticationPrincipal user: User,
        @PathVariable tenantId: String,
        @RequestBody request: UpdateStatusRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        logger.info("👑 [PLATFORM] PUT /api/platform/admin/tenants/$tenantId/status → ${request.status} par ${user.email}")
        
        return tenantService.updateTenantStatus(ObjectId(tenantId), request.status)
            .flatMap { tenant ->
                tenantService.getMemberCount(tenant.id)
                    .map { count ->
                        ResponseEntity.ok(mapOf<String, Any>(
                            "success" to true,
                            "message" to "Statut du tenant mis à jour",
                            "tenant" to TenantResponse.fromTenant(tenant, count)
                        ))
                    }
            }
            .onErrorResume { error ->
                Mono.just(ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "message" to (error.message ?: "Erreur")
                )))
            }
    }
    
    /**
     * Supprime un tenant et tous ses utilisateurs
     * ⚠️ Action irréversible
     */
    @DeleteMapping("/tenants/{tenantId}")
    fun deleteTenant(
        @AuthenticationPrincipal user: User,
        @PathVariable tenantId: String
    ): Mono<ResponseEntity<Map<String, Any>>> {
        logger.warn("👑 [PLATFORM] DELETE /api/platform/admin/tenants/$tenantId par ${user.email}")
        
        return tenantService.deleteTenant(ObjectId(tenantId))
            .then(Mono.just(ResponseEntity.ok(mapOf<String, Any>(
                "success" to true,
                "message" to "Tenant supprimé avec succès"
            ))))
            .onErrorResume { error ->
                Mono.just(ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "message" to (error.message ?: "Erreur lors de la suppression")
                )))
            }
    }
    
    /**
     * Liste les membres d'un tenant
     */
    @GetMapping("/tenants/{tenantId}/members")
    fun getTenantMembers(
        @AuthenticationPrincipal user: User,
        @PathVariable tenantId: String
    ): Mono<ResponseEntity<List<MemberResponse>>> {
        logger.info("👑 [PLATFORM] GET /api/platform/admin/tenants/$tenantId/members par ${user.email}")
        
        return tenantService.getTenantMembers(ObjectId(tenantId))
            .map { member -> MemberResponse.fromUser(member) }
            .collectList()
            .map { members -> ResponseEntity.ok(members) }
    }
    
    // ===== STATISTIQUES =====
    
    /**
     * Statistiques globales de la plateforme
     */
    @GetMapping("/stats")
    fun getPlatformStats(
        @AuthenticationPrincipal user: User
    ): Mono<ResponseEntity<Map<String, Any>>> {
        logger.info("👑 [PLATFORM] GET /api/platform/admin/stats par ${user.email}")
        
        return tenantService.getAllTenants()
            .collectList()
            .map { tenants ->
                val stats = mapOf(
                    "totalTenants" to tenants.size,
                    "byStatus" to tenants.groupBy { it.status.name }.mapValues { it.value.size },
                    "byPlan" to tenants.groupBy { it.plan.name }.mapValues { it.value.size },
                    "trialTenants" to tenants.count { it.status == TenantStatus.TRIAL },
                    "activeTenants" to tenants.count { it.status == TenantStatus.ACTIVE },
                    "suspendedTenants" to tenants.count { it.status == TenantStatus.SUSPENDED }
                )
                ResponseEntity.ok<Map<String, Any>>(stats)
            }
    }
    
    // ===== RECHERCHE =====
    
    /**
     * Recherche de tenants par nom
     */
    @GetMapping("/tenants/search")
    fun searchTenants(
        @AuthenticationPrincipal user: User,
        @RequestParam query: String
    ): Mono<ResponseEntity<List<TenantResponse>>> {
        logger.info("👑 [PLATFORM] GET /api/platform/admin/tenants/search?query=$query par ${user.email}")
        
        return tenantService.searchTenants(query)
            .flatMap { tenant ->
                tenantService.getMemberCount(tenant.id)
                    .map { count -> TenantResponse.fromTenant(tenant, count) }
            }
            .collectList()
            .map { tenants -> ResponseEntity.ok(tenants) }
    }
}

/**
 * Requête pour changer le statut d'un tenant
 */
data class UpdateStatusRequest(
    val status: TenantStatus
)
