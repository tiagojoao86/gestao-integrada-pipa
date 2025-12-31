package br.com.grupopipa.gestaointegrada.tenant.config;

import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.com.grupopipa.gestaointegrada.tenant.entity.Tenant;
import br.com.grupopipa.gestaointegrada.tenant.enums.TenantStatus;
import br.com.grupopipa.gestaointegrada.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executa migrations pendentes em todos os tenants ativos na inicialização do sistema.
 *
 * <p>⚠️ IMPORTANTE: Quando você adicionar novas migrations em db/tenant-migrations/, este
 * componente garante que TODOS os tenants existentes recebam as atualizações, não apenas os novos
 * tenants criados após a migration.
 *
 * <p>Exemplo de cenário: 1. Sistema tem tenants: empresa_lunar, empresa_solar 2. Você adiciona
 * V20251201000000__adiciona_coluna_email.sql 3. Na próxima inicialização, esta migration é aplicada
 * em AMBOS os tenants
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantMigrationRunner {

  private final TenantRepository tenantRepository;
  private final DataSource dataSource;

  /**
   * Executa após a inicialização completa do Spring Boot. Usa ApplicationReadyEvent para garantir
   * que todos os beans estão prontos.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void migrateTenants() {
    log.info("======================================================");
    log.info("🔄 INICIANDO MIGRATIONS AUTOMÁTICAS DE TENANTS");
    log.info("======================================================");

    try {
      // Busca todos os tenants ativos
      List<Tenant> tenants = tenantRepository.findAll();

      if (tenants.isEmpty()) {
        log.info("ℹ️  Nenhum tenant encontrado. Nada a migrar.");
        return;
      }

      log.info("📋 Encontrados {} tenant(s) no sistema", tenants.size());

      int successCount = 0;
      int skipCount = 0;
      int errorCount = 0;

      for (Tenant tenant : tenants) {
        try {
          // Apenas tenants ativos ou em trial
          if (tenant.getStatus() == TenantStatus.SUSPENDED
              || tenant.getStatus() == TenantStatus.CANCELLED) {
            log.info(
                "⏭️  SKIP: {} - Status: {} (suspenso/cancelado)",
                tenant.getTenantId(),
                tenant.getStatus());
            skipCount++;
            continue;
          }

          log.info(
              "🔍 Verificando migrations para: {} (schema: {})",
              tenant.getTenantId(),
              tenant.getSchemaName());

          int migrationsExecuted = executeMigrations(tenant.getSchemaName());

          if (migrationsExecuted > 0) {
            log.info(
                "✅ SUCCESS: {} - {} migration(s) aplicada(s)",
                tenant.getTenantId(),
                migrationsExecuted);
            successCount++;
          } else {
            log.info("✓ OK: {} - Nenhuma migration pendente", tenant.getTenantId());
          }

        } catch (Exception e) {
          log.error(
              "❌ ERROR: Falha ao migrar tenant {}: {}", tenant.getTenantId(), e.getMessage(), e);
          errorCount++;
        }
      }

      log.info("======================================================");
      log.info(
          "✅ Migrations concluídas - Sucesso: {} | Skip: {} | Erro: {}",
          successCount,
          skipCount,
          errorCount);
      log.info("======================================================");

      if (errorCount > 0) {
        log.warn("⚠️  Alguns tenants falharam! Verifique os logs acima.");
      }

    } catch (Exception e) {
      log.error("❌ ERRO CRÍTICO ao executar migrations de tenants: {}", e.getMessage(), e);
      throw new RuntimeException("Falha ao migrar tenants", e);
    }
  }

  /**
   * Executa Flyway migrations em um schema específico de tenant.
   *
   * @param schemaName Nome do schema (ex: tenant_empresa_lunar)
   * @return Número de migrations executadas
   */
  private int executeMigrations(String schemaName) {
    Flyway flyway =
        Flyway.configure()
            .dataSource(dataSource)
            .schemas(schemaName)
            .locations("classpath:db/tenant-migrations")
            .baselineOnMigrate(true)
            .createSchemas(false) // Schema já existe
            .table("flyway_schema_history")
            .load();

    return flyway.migrate().migrationsExecuted;
  }
}
