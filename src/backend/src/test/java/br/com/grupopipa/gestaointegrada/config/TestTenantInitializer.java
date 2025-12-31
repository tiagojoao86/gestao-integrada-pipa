package br.com.grupopipa.gestaointegrada.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

/**
 * Configuração para inicializar tenant de teste automaticamente.
 *
 * <p>Cria o schema 'teste_tenant' e executa todas as migrations de tenant nele, simulando o
 * ambiente real de produção.
 *
 * <p>IMPORTANTE: Usa flag estática para garantir que a inicialização ocorra apenas uma vez por
 * execução de testes, mesmo com @DirtiesContext.
 */
@TestConfiguration
@Profile("test")
@Order(1)
public class TestTenantInitializer {

  private static final String TEST_TENANT_SCHEMA = "teste_tenant";

  /**
   * Bean que inicializa o tenant de teste. Flyway é idempotente, então não há problema em chamar
   * múltiplas vezes.
   */
  @Bean
  public TestTenantSetup testTenantSetup(DataSource dataSource) {
    setupTestTenant(dataSource);
    return new TestTenantSetup();
  }

  private void setupTestTenant(DataSource dataSource) {
    try {
      System.out.println("🔄 Inicializando tenant de teste '" + TEST_TENANT_SCHEMA + "'...");

      // 1. Criar o schema público e migrations
      Flyway flywayPublic =
          Flyway.configure()
              .dataSource(dataSource)
              .locations("classpath:db/migration")
              .schemas("public")
              .baselineOnMigrate(true)
              .load();

      flywayPublic.migrate();

      // 2. Limpar e recriar schema do tenant (para garantir estado limpo em testes)
      try (var connection = dataSource.getConnection();
          var statement = connection.createStatement()) {
        // Drop cascade para remover todas as dependências
        statement.execute("DROP SCHEMA IF EXISTS " + TEST_TENANT_SCHEMA + " CASCADE");
        statement.execute("CREATE SCHEMA " + TEST_TENANT_SCHEMA);
      }

      // 3. Executar migrations do tenant
      Flyway flywayTenant =
          Flyway.configure()
              .dataSource(dataSource)
              .locations("classpath:db/tenant-migrations")
              .schemas(TEST_TENANT_SCHEMA)
              .defaultSchema(TEST_TENANT_SCHEMA)
              .baselineOnMigrate(true)
              .load();

      flywayTenant.migrate();

      System.out.println(
          "✅ Tenant de teste '" + TEST_TENANT_SCHEMA + "' inicializado com sucesso!");

    } catch (Exception e) {
      throw new RuntimeException("Erro ao inicializar tenant de teste", e);
    }
  }

  /** Classe auxiliar para marcar que o setup foi concluído */
  public static class TestTenantSetup {
    public String getTenantSchema() {
      return TEST_TENANT_SCHEMA;
    }
  }
}
