package br.com.grupopipa.gestaointegrada.tenant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.tenant.entity.Tenant;
import br.com.grupopipa.gestaointegrada.tenant.enums.TenantPlano;
import br.com.grupopipa.gestaointegrada.tenant.enums.TenantStatus;
import br.com.grupopipa.gestaointegrada.tenant.repository.TenantRepository;

import javax.sql.DataSource;
import java.time.LocalDateTime;

/**
 * Serviço para gerenciar tenants
 * Utiliza Flyway para executar migrations em cada schema de tenant
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    /**
     * Cria um novo tenant
     * 1. Salva no banco (schema public)
     * 2. Cria o schema do tenant (FORA da transação)
     * 3. Executa migrations no schema do tenant
     */
    public Tenant criarTenant(String tenantId, String nome, String numeroDocumento, TenantPlano plano) {
        // Validar se tenant já existe
        if (tenantRepository.existsByTenantId(tenantId)) {
            throw new IllegalArgumentException("Tenant já existe: " + tenantId);
        }

        // Criar entidade Tenant
        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);
        tenant.setNome(nome);
        tenant.setNumeroDocumento(numeroDocumento);
        tenant.setPlano(plano != null ? plano : TenantPlano.BASIC);
        tenant.setStatus(TenantStatus.TRIAL);
        tenant.setDataCriacao(LocalDateTime.now());
        tenant.setDataExpiracao(LocalDateTime.now().plusMonths(1)); // 1 mês de trial

        // Gerar schema name
        String schemaName = "tenant_" + tenantId.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        tenant.setSchemaName(schemaName);

        // Salvar no banco (dentro de transação)
        tenant = salvarTenant(tenant);

        // Criar schema no PostgreSQL (FORA de transação para evitar rollback)
        criarSchema(schemaName);

        // Executar migrations do tenant
        executarMigrationsTenant(schemaName);

        log.info("Tenant criado com sucesso: {} (schema: {})", tenantId, schemaName);
        
        return tenant;
    }

    /**
     * Salva o tenant no banco (método separado para controlar transação)
     */
    @Transactional
    private Tenant salvarTenant(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    /**
     * Cria o schema no PostgreSQL
     * Usa conexão em AUTOCOMMIT para garantir que o schema seja criado imediatamente
     */
    private void criarSchema(String schemaName) {
        try {
            // Verificar se schema já existe
            String checkSql = "SELECT schema_name FROM information_schema.schemata WHERE schema_name = ?";
            String existingSchema = jdbcTemplate.query(checkSql, 
                (rs, rowNum) -> rs.getString("schema_name"), 
                schemaName)
                .stream()
                .findFirst()
                .orElse(null);
            
            if (existingSchema != null) {
                log.warn("Schema já existe: {}", schemaName);
                return;
            }
            
            // Criar schema usando uma nova conexão em AUTOCOMMIT
            try (var connection = dataSource.getConnection()) {
                boolean originalAutoCommit = connection.getAutoCommit();
                try {
                    connection.setAutoCommit(true); // Força commit imediato
                    
                    try (var statement = connection.createStatement()) {
                        String createSql = String.format("CREATE SCHEMA %s", schemaName);
                        statement.execute(createSql);
                        log.info("Schema criado com sucesso: {}", schemaName);
                    }
                } finally {
                    connection.setAutoCommit(originalAutoCommit);
                }
            }
            
            // Validar que foi criado
            String validationSql = "SELECT schema_name FROM information_schema.schemata WHERE schema_name = ?";
            String createdSchema = jdbcTemplate.query(validationSql, 
                (rs, rowNum) -> rs.getString("schema_name"), 
                schemaName)
                .stream()
                .findFirst()
                .orElse(null);
            
            if (createdSchema == null) {
                throw new RuntimeException("Schema não foi criado: " + schemaName);
            }
            
        } catch (Exception e) {
            log.error("Erro ao criar schema {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Erro ao criar schema do tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Executa migrations do Flyway no schema do tenant
     * Usa as mesmas migrations que estão em db/migration/tenant
     * Elimina duplicação: migrations são mantidas apenas em um lugar!
     */
    private void executarMigrationsTenant(String schemaName) {
        try {
            log.info("Executando Flyway migrations no schema: {}", schemaName);
            
            Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)  // Define o schema do tenant
                .locations("classpath:db/tenant-migrations") // Migrations específicas de tenant (pasta separada)
                .baselineOnMigrate(true) // Permite migrations em schemas existentes
                .createSchemas(false) // NÃO criar schema - já foi criado manualmente
                .table("flyway_schema_history") // Tabela de histórico do Flyway
                .load();

            // Executa migrations
            int migrationsExecuted = flyway.migrate().migrationsExecuted;
            
            log.info("Flyway executou {} migrations no schema: {}", migrationsExecuted, schemaName);
        } catch (Exception e) {
            log.error("Erro ao executar Flyway migrations no schema {}: {}", schemaName, e.getMessage(), e);
            throw new RuntimeException("Erro ao executar migrations do tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Busca tenant por ID
     */
    public Tenant buscarPorTenantId(String tenantId) {
        return tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant não encontrado: " + tenantId));
    }

    /**
     * Ativa um tenant
     */
    @Transactional
    public void ativarTenant(String tenantId) {
        Tenant tenant = buscarPorTenantId(tenantId);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenantRepository.save(tenant);
        log.info("Tenant ativado: {}", tenantId);
    }

    /**
     * Suspende um tenant
     */
    @Transactional
    public void suspenderTenant(String tenantId) {
        Tenant tenant = buscarPorTenantId(tenantId);
        tenant.setStatus(TenantStatus.SUSPENDED);
        tenantRepository.save(tenant);
        log.info("Tenant suspenso: {}", tenantId);
    }
}
