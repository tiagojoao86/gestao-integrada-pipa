package br.com.grupopipa.gestaointegrada.tenant.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

import br.com.grupopipa.gestaointegrada.tenant.context.TenantContext;

/**
 * Resolver para identificar o tenant atual baseado no TenantContext
 */
@Slf4j
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_TENANT = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        String resolved = tenantId != null ? tenantId : DEFAULT_TENANT;
        log.debug("Resolvendo tenant identifier: {} (contexto: {})", resolved, tenantId);
        return resolved;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
