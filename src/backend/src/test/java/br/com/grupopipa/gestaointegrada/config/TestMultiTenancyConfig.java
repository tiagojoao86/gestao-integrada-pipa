package br.com.grupopipa.gestaointegrada.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import br.com.grupopipa.gestaointegrada.tenant.context.TenantContext;

/**
 * Configuração de multi-tenancy para testes.
 *
 * <p>
 * Define o tenant padrão como 'teste_tenant' para todos os testes, garantindo
 * isolamento e
 * consistência com o ambiente de produção.
 */
@TestConfiguration
@Profile("test")
public class TestMultiTenancyConfig {

    private static final String TEST_TENANT_SCHEMA = "teste_tenant";

    /**
     * Configura o TenantContext sempre que o contexto Spring é carregado ou
     * recarregado. Isso é
     * crucial para testes com @DirtiesContext, pois o contexto é recriado.
     */
    @EventListener
    public void onContextRefreshed(ContextRefreshedEvent event) {
        TenantContext.setTenantId(TEST_TENANT_SCHEMA);
        System.out.println(
                "🔧 TenantContext configurado após refresh do contexto: " + TEST_TENANT_SCHEMA);
    }

    @Bean
    public TestTenantContextInitializer testTenantContextInitializer() {
        return new TestTenantContextInitializer();
    }

    /** Classe auxiliar para inicializar o TenantContext com o tenant de teste */
    public static class TestTenantContextInitializer {
        public TestTenantContextInitializer() {
            // Define o schema do tenant de teste como padrão
            TenantContext.setTenantId(TEST_TENANT_SCHEMA);
            System.out.println("🔧 TenantContext configurado para testes: " + TEST_TENANT_SCHEMA);
        }

        public String getTestTenantSchema() {
            return TEST_TENANT_SCHEMA;
        }
    }
}
