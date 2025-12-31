package br.com.grupopipa.gestaointegrada.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.grupopipa.gestaointegrada.tenant.context.TenantContext;

/**
 * Classe base para testes de integração com Testcontainers.
 *
 * <p>Esta classe configura: - Container PostgreSQL reutilizável - Tenant de teste 'teste_tenant'
 * com todas as migrations - Multi-tenancy configurado para usar o tenant de teste - Security
 * desabilitado para facilitar os testes
 *
 * <p>Para usar, basta estender esta classe nos seus testes de integração:
 *
 * <pre>
 * {
 *     &#64;code
 *     class MeuRepositoryTest extends AbstractIntegrationTest {
 *         &#64;Autowired
 *         private MeuRepository repository;
 *
 *         @Test
 *         void deveFazerAlgo() {
 *             // seu teste aqui - rodará no schema 'teste_tenant'
 *         }
 *     }
 * }
 * </pre>
 */
@SuppressWarnings("resource")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
@Import({TestTenantInitializer.class, TestMultiTenancyConfig.class})
public abstract class AbstractIntegrationTest {

  /**
   * Container PostgreSQL compartilhado entre todos os testes. O container é iniciado manualmente no
   * bloco static e fica ativo durante toda a execução da suite de testes. Não usamos @Container
   * aqui porque queremos controlar manualmente o ciclo de vida do container para evitar que ele
   * seja parado entre classes de teste.
   *
   * <p>Nota: O warning de "resource leak" é um falso positivo. O Testcontainers gerencia
   * automaticamente o cleanup através do Ryuk container.
   */
  protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

  static {
    POSTGRES_CONTAINER =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // Reutiliza o container entre execuções
    POSTGRES_CONTAINER.start(); // Inicia o container uma vez para todos os testes
  }

  protected static final String TEST_TENANT_SCHEMA = "teste_tenant";

  /**
   * Garante que o TenantContext seja configurado antes de cada teste. Isso é crucial para testes
   * com @DirtiesContext, pois o contexto é recriado e o TenantContext precisa ser redefinido.
   */
  @BeforeEach
  void setupTenantContext() {
    TenantContext.setTenantId(TEST_TENANT_SCHEMA);
    System.out.println("🔧 TenantContext configurado para: " + TEST_TENANT_SCHEMA);
  }

  /**
   * Configura as propriedades do Spring dinamicamente com base nas configurações do container
   * PostgreSQL.
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
