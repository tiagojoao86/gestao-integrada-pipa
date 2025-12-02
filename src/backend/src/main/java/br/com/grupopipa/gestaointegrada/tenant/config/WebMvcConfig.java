package br.com.grupopipa.gestaointegrada.tenant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração MVC
 * 
 * NOTA: TenantInterceptor foi substituído por TenantFilter
 * O Filter executa ANTES do Spring Security, garantindo que o tenant
 * seja definido antes da autenticação
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    // TenantInterceptor não é mais necessário
    // O TenantFilter (com @Order(1)) executa antes do Spring Security
    
}
