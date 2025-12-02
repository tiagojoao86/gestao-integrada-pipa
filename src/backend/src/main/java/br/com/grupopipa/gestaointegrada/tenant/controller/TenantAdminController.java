package br.com.grupopipa.gestaointegrada.tenant.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.grupopipa.gestaointegrada.tenant.entity.Tenant;
import br.com.grupopipa.gestaointegrada.tenant.enums.TenantPlano;
import br.com.grupopipa.gestaointegrada.tenant.service.TenantService;

/**
 * Controller ADMINISTRATIVO para gerenciamento de tenants
 * 
 * SEGURANÇA: Este endpoint requer um token administrativo secreto
 * O token deve ser configurado em application.properties e mantido em segredo
 * 
 * Uso:
 * POST /admin/tenants
 * Header: X-Admin-Token: {seu-token-secreto}
 */
@Slf4j
@RestController
@RequestMapping("/admin/tenants")
@RequiredArgsConstructor
public class TenantAdminController {

    private final TenantService tenantService;

    @Value("${app.admin.token:CHANGE_ME_IN_PRODUCTION}")
    private String adminToken;

    /**
     * Criar novo tenant
     * Requer token administrativo no header X-Admin-Token
     */
    @PostMapping
    public ResponseEntity<?> criarTenant(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @RequestBody CriarTenantRequest request) {
        
        // Validar token administrativo
        if (!validarToken(token)) {
            log.warn("Tentativa de criação de tenant sem token válido. IP: {}", getClientIp());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Token administrativo inválido ou ausente"));
        }

        try {
            log.info("Iniciando criação de tenant: {} (solicitado por admin)", request.getTenantId());
            
            Tenant tenant = tenantService.criarTenant(
                request.getTenantId(),
                request.getNome(),
                request.getNumeroDocumento(),
                request.getPlano()
            );
            
            log.info("Tenant criado com sucesso: {}", tenant.getTenantId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(tenant);
        } catch (IllegalArgumentException e) {
            log.warn("Erro de validação ao criar tenant: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao criar tenant: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Erro ao criar tenant: " + e.getMessage()));
        }
    }

    /**
     * Buscar tenant por ID
     * Requer token administrativo
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<?> buscarTenant(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable String tenantId) {
        
        if (!validarToken(token)) {
            log.warn("Tentativa de busca de tenant sem token válido");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Token administrativo inválido ou ausente"));
        }

        try {
            Tenant tenant = tenantService.buscarPorTenantId(tenantId);
            return ResponseEntity.ok(tenant);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ativar tenant
     * Requer token administrativo
     */
    @PostMapping("/{tenantId}/ativar")
    public ResponseEntity<?> ativarTenant(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable String tenantId) {
        
        if (!validarToken(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Token administrativo inválido ou ausente"));
        }

        try {
            tenantService.ativarTenant(tenantId);
            log.info("Tenant ativado: {}", tenantId);
            return ResponseEntity.ok().body(new SuccessResponse("Tenant ativado com sucesso"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Suspender tenant
     * Requer token administrativo
     */
    @PostMapping("/{tenantId}/suspender")
    public ResponseEntity<?> suspenderTenant(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable String tenantId) {
        
        if (!validarToken(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Token administrativo inválido ou ausente"));
        }

        try {
            tenantService.suspenderTenant(tenantId);
            log.info("Tenant suspenso: {}", tenantId);
            return ResponseEntity.ok().body(new SuccessResponse("Tenant suspenso com sucesso"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Valida o token administrativo
     */
    private boolean validarToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // Validação simples: compara com token configurado
        boolean isValid = adminToken.equals(token);
        
        if (!isValid) {
            log.warn("Token administrativo inválido fornecido");
        }
        
        return isValid;
    }

    /**
     * Obtém IP do cliente (para logs de segurança)
     */
    private String getClientIp() {
        // Em produção, considerar X-Forwarded-For se estiver atrás de proxy
        return "unknown";
    }

    // DTOs
    
    @Data
    public static class CriarTenantRequest {
        private String tenantId;
        private String nome;
        private String numeroDocumento;
        private TenantPlano plano;
    }

    @Data
    public static class ErrorResponse {
        private final String error;
    }

    @Data
    public static class SuccessResponse {
        private final String message;
    }
}
