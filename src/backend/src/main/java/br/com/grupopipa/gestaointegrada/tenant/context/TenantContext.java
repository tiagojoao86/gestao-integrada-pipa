package br.com.grupopipa.gestaointegrada.tenant.context;

/**
 * Contexto para armazenar o tenant atual na thread Usa ThreadLocal para isolar
 * o tenant por
 * requisição
 *
 * <p>
 * IMPORTANTE: Armazena o SCHEMA_NAME (tenant_empresa_solar), não o tenant_id
 * (empresa-solar) O
 * Hibernate usa este valor para definir o search_path do PostgreSQL
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT_SCHEMA = new ThreadLocal<>();

    /**
     * Define o schema do tenant atual para a thread
     *
     * @param schemaName Nome do schema (ex: tenant_empresa_solar)
     */
    public static void setTenantId(String schemaName) {
        CURRENT_TENANT_SCHEMA.set(schemaName);
    }

    /**
     * Retorna o schema do tenant atual da thread
     *
     * @return Nome do schema (ex: tenant_empresa_solar) ou null se não definido
     */
    public static String getTenantId() {
        return CURRENT_TENANT_SCHEMA.get();
    }

    /** Limpa o tenant da thread */
    public static void clear() {
        CURRENT_TENANT_SCHEMA.remove();
    }
}
