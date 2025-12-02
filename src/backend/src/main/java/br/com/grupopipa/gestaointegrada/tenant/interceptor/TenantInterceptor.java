package br.com.grupopipa.gestaointegrada.tenant.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import br.com.grupopipa.gestaointegrada.tenant.context.TenantContext;

import java.io.IOException;

/**
 * Interceptor para identificar e configurar o tenant atual
 * Pode ser identificado por:
 * - Header: X-Tenant-ID
 * - Subdomínio: tenant.gestao-solar.com
 * - Token JWT (claim tenant_id)
 * 
 * IMPORTANTE: Requisições sem tenant serão REJEITADAS (exceto rotas públicas)
 */
@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String requestPath = request.getRequestURI();
        
        // Rotas que NÃO precisam de tenant (públicas/administrativas)
        if (isPublicRoute(requestPath)) {
            log.debug("Rota pública acessada, tenant não obrigatório: {}", requestPath);
            return true;
        }

        // 1. Tentar obter tenant do header
        String tenantId = request.getHeader(TENANT_HEADER);

        // 2. Se não encontrou no header, pode tentar extrair do JWT (implementar depois)
        // String token = extractTokenFromRequest(request);
        // if (token != null) {
        //     tenantId = extractTenantFromJWT(token);
        // }

        // 3. Se não encontrou no JWT, pode tentar extrair do subdomínio
        // if (tenantId == null) {
        //     tenantId = extractTenantFromSubdomain(request);
        // }

        // Validar se tenant foi identificado
        if (tenantId == null || tenantId.isEmpty()) {
            log.warn("Requisição sem tenant identificado: {} {}", request.getMethod(), requestPath);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Tenant não identificado. Header X-Tenant-ID é obrigatório.\"}");
            return false; // Bloqueia requisição
        }

        // Define o tenant no contexto
        // Converter tenant_id para schema_name (precisa buscar no banco)
        // Por enquanto, normaliza: se não tem prefixo tenant_, adiciona
        String schemaName = tenantId.startsWith("tenant_") ? tenantId : "tenant_" + tenantId.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        TenantContext.setTenantId(schemaName);
        
        log.debug("Tenant identificado: {} (schema: {}) para requisição: {} {}", tenantId, schemaName, request.getMethod(), requestPath);

        return true;
    }

    /**
     * Verifica se a rota é pública (não requer tenant)
     */
    private boolean isPublicRoute(String path) {
        // Rotas administrativas (criação de tenants)
        if (path.contains("/admin/tenants")) {
            return true;
        }
        
        // Rotas de autenticação (login pode ser público ou pode exigir tenant, você decide)
        if (path.contains("/auth/login") || path.contains("/auth/register")) {
            return false; // Login EXIGE tenant (usuário pertence a um tenant)
        }
        
        // Health check, actuator, etc
        if (path.contains("/actuator") || path.contains("/health")) {
            return true;
        }
        
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // Opcional: adicionar tenant ao response header para debug
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            response.setHeader(TENANT_HEADER, tenantId);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Limpar o contexto do tenant ao finalizar a requisição
        TenantContext.clear();
    }

    /**
     * Extrai o tenant do subdomínio
     * Exemplo: empresa-abc.gestao-solar.com -> empresa-abc
     */
    private String extractTenantFromSubdomain(HttpServletRequest request) {
        String serverName = request.getServerName();
        if (serverName != null && serverName.contains(".")) {
            String[] parts = serverName.split("\\.");
            if (parts.length >= 3) {
                // primeiro segmento é o tenant
                return parts[0];
            }
        }
        return null;
    }
}
