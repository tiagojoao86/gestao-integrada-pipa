package br.com.grupopipa.gestaointegrada.config.security;

import org.springframework.context.annotation.Configuration;

/**
 * Configuração de auditoria JPA.
 * A auditoria agora é feita manualmente através do CustomAuditingEntityListener,
 * que tem controle total sobre quando atualizar os campos de auditoria.
 */
@Configuration
public class JpaAuditingConfiguration {
    // JPA Auditing desabilitado - usando CustomAuditingEntityListener
}
