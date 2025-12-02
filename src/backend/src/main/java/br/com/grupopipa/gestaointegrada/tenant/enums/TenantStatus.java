package br.com.grupopipa.gestaointegrada.tenant.enums;

/**
 * Status do tenant
 */
public enum TenantStatus {
    /**
     * Tenant ativo e operacional
     */
    ACTIVE,
    
    /**
     * Tenant suspenso (falta de pagamento, violação de termos, etc)
     */
    SUSPENDED,
    
    /**
     * Tenant inativo (desativado temporariamente)
     */
    INACTIVE,
    
    /**
     * Tenant em período de trial
     */
    TRIAL,
    
    /**
     * Tenant cancelado (pode ser reativado)
     */
    CANCELLED
}
