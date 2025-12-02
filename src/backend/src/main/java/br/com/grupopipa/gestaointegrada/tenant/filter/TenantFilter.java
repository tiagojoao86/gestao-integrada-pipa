package br.com.grupopipa.gestaointegrada.tenant.filter;

import br.com.grupopipa.gestaointegrada.tenant.context.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter para definir o tenant ANTES do Spring Security processar a requisição
 * Ordem: HIGHEST_PRECEDENCE + 1 (executa logo após o primeiro filtro do Spring)
 * 
 * IMPORTANTE: Este filter executa ANTES da autenticação do Spring Security!
 * Também valida que o tenant do JWT corresponde ao tenant do header
 */
@Slf4j
@Component
@Order(1)
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    
    // Decodificador JWT para extrair tenant_id do token
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;
    
    public TenantFilter(org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }
    
    private String extractTenantIdFromToken(String token) {
        try {
            return jwtDecoder.decode(token).getClaimAsString("tenant_id");
        } catch (Exception e) {
            log.warn("Erro ao decodificar token: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestPath = httpRequest.getRequestURI();
        
        try {
            // Rotas que NÃO precisam de tenant (públicas/administrativas)
            if (isPublicRoute(requestPath)) {
                log.debug("Rota pública acessada, tenant não obrigatório: {}", requestPath);
                chain.doFilter(request, response);
                return;
            }

            // Obter tenant do header
            String tenantId = httpRequest.getHeader(TENANT_HEADER);

            // Validar se tenant foi identificado
            if (tenantId == null || tenantId.trim().isEmpty()) {
                log.warn("Requisição sem tenant identificado: {} {}", httpRequest.getMethod(), requestPath);
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Tenant não identificado. Header X-Tenant-ID é obrigatório.\"}");
                return;
            }

            // Converter tenant_id para schema_name
            String schemaName = tenantId.startsWith("tenant_") 
                ? tenantId 
                : "tenant_" + tenantId.toLowerCase().replaceAll("[^a-z0-9_]", "_");
            
            TenantContext.setTenantId(schemaName);
            
            log.info("✅ TENANT DEFINIDO - Header: '{}', Schema: '{}', Path: {} {}", 
                tenantId, schemaName, httpRequest.getMethod(), requestPath);

            // Validar JWT se presente (exceto no login)
            if (!requestPath.contains("/authenticate") && !requestPath.contains("/admin/tenants")) {
                String authHeader = httpRequest.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    String tokenTenantId = extractTenantIdFromToken(token);
                    
                    if (tokenTenantId != null && !tokenTenantId.equals(schemaName)) {
                        log.warn("❌ TENANT MISMATCH - Token tenant: '{}', Header tenant: '{}'", tokenTenantId, schemaName);
                        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        httpResponse.setContentType("application/json");
                        httpResponse.getWriter().write("{\"error\":\"Token não pertence a este tenant. Acesso negado.\"}");
                        return;
                    }
                    
                    log.debug("✅ Token validado para tenant: {}", tokenTenantId);
                }
            }

            // Adicionar header de resposta para debug
            httpResponse.setHeader(TENANT_HEADER, schemaName);

            // Continuar a cadeia de filtros
            chain.doFilter(request, response);
            
        } finally {
            // Limpar o contexto do tenant ao finalizar a requisição
            TenantContext.clear();
        }
    }

    /**
     * Verifica se a rota é pública (não requer tenant)
     */
    private boolean isPublicRoute(String path) {
        // Rotas administrativas (criação de tenants)
        if (path.contains("/admin/tenants")) {
            return true;
        }
        
        // Health check, actuator, etc
        if (path.contains("/actuator") || path.contains("/health")) {
            return true;
        }
        
        // Autenticação EXIGE tenant (usuário pertence a um tenant)
        return false;
    }
}
