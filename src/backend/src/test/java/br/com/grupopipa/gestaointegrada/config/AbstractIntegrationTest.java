package br.com.grupopipa.gestaointegrada.config;

import br.com.grupopipa.gestaointegrada.tenant.context.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Classe base para testes de integração com Testcontainers.
 * 
 * Esta classe configura:
 * - Container PostgreSQL reutilizável
 * - Tenant de teste 'teste_tenant' com todas as migrations
 * - Multi-tenancy configurado para usar o tenant de teste
 * - Security desabilitado para facilitar os testes
 * 
 * Para usar, basta estender esta classe nos seus testes de integração:
 * 
 * <pre>
 * {@code
 * class MeuRepositoryTest extends AbstractIntegrationTest {
 *     @Autowired
 *     private MeuRepository repository;
 *     
 *     @Test
 *     void deveFazerAlgo() {
 *         // seu teste aqui - rodará no schema 'teste_tenant'
 *     }
 * }
 * }
 * </pre>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
@Import({
    TestTenantInitializer.class,
    TestMultiTenancyConfig.class
})
public abstract class AbstractIntegrationTest {

    /**
     * Container PostgreSQL compartilhado entre todos os testes.
     * O uso de um container estático garante que ele seja iniciado
     * apenas uma vez para toda a suite de testes, economizando tempo.
     * 
     * Nota: O warning de "resource leak" é um falso positivo. O Testcontainers
     * gerencia automaticamente o ciclo de vida do container através da anotação @Container.
     */
    @Container
    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER = 
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // Reutiliza o container entre execuções

    protected static final String TEST_TENANT_SCHEMA = "teste_tenant";

    /**
     * Garante que o TenantContext seja configurado antes de cada teste.
     * Isso é crucial para testes com @DirtiesContext, pois o contexto
     * é recriado e o TenantContext precisa ser redefinido.
     */
    @BeforeEach
    void setupTenantContext() {
        TenantContext.setTenantId(TEST_TENANT_SCHEMA);
        System.out.println("🔧 TenantContext configurado para: " + TEST_TENANT_SCHEMA);
    }

    /**
     * Configura as propriedades do Spring dinamicamente com base
     * nas configurações do container PostgreSQL.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        
        // Configurações específicas para testes
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.clean-disabled", () -> "false");
        
        // Multi-tenancy configurado para usar tenant de teste
        registry.add("app.multitenancy.default-schema", () -> TEST_TENANT_SCHEMA);
    }
}
