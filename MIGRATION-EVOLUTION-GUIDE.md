# Guia de Evolução de Migrations Multi-Tenant

## 🎯 Objetivo

Este guia explica como adicionar novas funcionalidades ao sistema que exigem mudanças no banco de dados, garantindo que **todos os tenants** (novos e existentes) recebam as atualizações automaticamente.

---

## 🔄 Fluxo Completo de uma Nova Migration

### Cenário Real

Você precisa adicionar uma coluna `email_contato` na tabela `usuario` para implementar recuperação de senha por email.

### Passo 1: Criar a Migration

Crie um arquivo SQL em `src/backend/src/main/resources/db/tenant-migrations/`:

**Nome do arquivo:** `V{timestamp}__{descricao}.sql`

Exemplo:
```
V20251201120000__adiciona_email_contato_usuario.sql
```

**Padrão de versionamento:**
- `V` - Prefixo obrigatório do Flyway
- `20251201120000` - Timestamp: YYYYMMDDHHMMSS (ano/mês/dia/hora/min/seg)
- `__` - Dois underscores separando versão da descrição
- `adiciona_email_contato_usuario` - Descrição (use snake_case)
- `.sql` - Extensão

**Conteúdo da migration:**
```sql
-- V20251201120000__adiciona_email_contato_usuario.sql
-- Adiciona coluna email_contato para recuperação de senha

ALTER TABLE usuario 
ADD COLUMN email_contato VARCHAR(200);

COMMENT ON COLUMN usuario.email_contato 
IS 'Email de contato para recuperação de senha';

CREATE INDEX idx_usuario_email_contato ON usuario(email_contato);
```

**⚠️ Importante:**
- Use `ADD COLUMN IF NOT EXISTS` para migrations idempotentes
- Use `CREATE INDEX IF NOT EXISTS` para evitar erros em re-execuções
- Adicione comentários explicando o propósito

---

### Passo 2: Testar Localmente

**2.1. Compile o projeto:**
```bash
cd src/backend
./mvnw clean compile
```

**2.2. Reinicie o backend:**
```bash
./mvnw spring-boot:run
```

**2.3. Observe os logs:**
```
======================================================
🔄 INICIANDO MIGRATIONS AUTOMÁTICAS DE TENANTS
======================================================
📋 Encontrados 2 tenant(s) no sistema
🔍 Verificando migrations para: empresa-lunar (schema: tenant_empresa_lunar)
✅ SUCCESS: empresa-lunar - 1 migration(s) aplicada(s)
🔍 Verificando migrations para: empresa-solar (schema: tenant_empresa_solar)
✅ SUCCESS: empresa-solar - 1 migration(s) aplicada(s)
======================================================
✅ Migrations concluídas - Sucesso: 2 | Skip: 0 | Erro: 0
======================================================
```

**2.4. Valide no banco:**
```sql
-- Verificar em cada tenant
\c gestao_integrada_pipa_db
SET search_path TO tenant_empresa_lunar;
\d usuario  -- Deve mostrar a coluna email_contato

SET search_path TO tenant_empresa_solar;
\d usuario  -- Deve mostrar a coluna email_contato
```

---

### Passo 3: Commit e Deploy

**3.1. Adicione ao Git:**
```bash
git add src/backend/src/main/resources/db/tenant-migrations/V20251201120000__adiciona_email_contato_usuario.sql
git commit -m "feat: adiciona coluna email_contato para recuperação de senha"
```

**3.2. Deploy em produção:**

Ao fazer deploy da nova versão:
1. Backend inicia
2. `TenantMigrationRunner` detecta a nova migration
3. Aplica automaticamente em **todos** os tenants ativos
4. Sistema pronto para usar a nova coluna!

**Zero downtime:** As migrations são executadas de forma rápida e não bloqueiam o sistema.

---

## 🧩 Como Funciona Internamente

### Componentes Envolvidos

```
┌─────────────────────────────────────────────────────────┐
│ 1. STARTUP DO BACKEND                                   │
├─────────────────────────────────────────────────────────┤
│ Spring Boot inicializa...                               │
│ - Beans criados                                         │
│ - DataSource configurado                                │
│ - Flyway principal executa migrations do PUBLIC         │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 2. ApplicationReadyEvent                                │
├─────────────────────────────────────────────────────────┤
│ Spring dispara evento: aplicação pronta!                │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 3. TenantMigrationRunner acionado                       │
├─────────────────────────────────────────────────────────┤
│ @EventListener(ApplicationReadyEvent.class)             │
│ public void migrateTenants() {                          │
│     List<Tenant> tenants = tenantRepository.findAll(); │
│     for (Tenant tenant : tenants) {                     │
│         executeMigrations(tenant.getSchemaName());      │
│     }                                                   │
│ }                                                       │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 4. Para cada tenant (exemplo: tenant_empresa_lunar)     │
├─────────────────────────────────────────────────────────┤
│ Flyway.configure()                                      │
│   .schemas("tenant_empresa_lunar")                      │
│   .locations("classpath:db/tenant-migrations")         │
│   .migrate()                                            │
│                                                         │
│ Flyway:                                                 │
│ 1. Lê flyway_schema_history do schema                  │
│ 2. Compara com arquivos em db/tenant-migrations/       │
│ 3. Detecta V20251201120000... (nova!)                   │
│ 4. Executa: ALTER TABLE usuario ADD COLUMN...          │
│ 5. Registra em flyway_schema_history                   │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ 5. BACKEND PRONTO PARA USO                              │
├─────────────────────────────────────────────────────────┤
│ ✅ Todos os tenants atualizados                         │
│ ✅ Nova coluna disponível                               │
│ ✅ Sistema operacional                                  │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 Controle de Versões (Flyway)

### Tabela flyway_schema_history

Cada schema de tenant tem sua própria tabela de controle:

```sql
-- tenant_empresa_lunar.flyway_schema_history
SELECT version, description, installed_on, success 
FROM tenant_empresa_lunar.flyway_schema_history
ORDER BY installed_rank;

-- Resultado:
| version         | description                       | installed_on         | success |
|-----------------|-----------------------------------|----------------------|---------|
| 20250910020657  | cria tabela usuario               | 2025-11-29 23:50:00  | t       |
| 20251013003901  | cria tabela modulo perfil         | 2025-11-29 23:50:01  | t       |
| 20251201120000  | adiciona email contato usuario    | 2025-12-01 12:05:30  | t       |
```

**Flyway usa esta tabela para:**
- Saber quais migrations já foram executadas
- Detectar migrations pendentes (arquivo existe, mas não está na tabela)
- Impedir re-execução de migrations já aplicadas
- Validar integridade (checksums)

---

## 🚨 Cenários Especiais

### Cenário 1: Migration com Dados

Se sua migration precisa **popular dados** ou **transformar** dados existentes:

```sql
-- V20251205000000__migra_formato_telefone.sql
-- Converte telefone de (XX) XXXXX-XXXX para formato internacional +55XXXXXXXXXXX

UPDATE usuario 
SET telefone = '+55' || REGEXP_REPLACE(telefone, '[^0-9]', '', 'g')
WHERE telefone IS NOT NULL
  AND telefone NOT LIKE '+%';  -- Evita processar telefones já migrados

-- Adiciona validação
ALTER TABLE usuario 
ADD CONSTRAINT check_telefone_formato 
CHECK (telefone ~ '^\+[0-9]{11,15}$' OR telefone IS NULL);
```

**Boas práticas:**
- Use `WHERE` conditions para evitar afetar dados já migrados
- Teste em ambiente local com dados reais
- Considere fazer backup antes de migrations destrutivas

---

### Cenário 2: Migration Demorada

Se a migration demora muito (ex: processar milhões de registros):

**Opção A: Fazer em batches (recomendado)**
```sql
-- V20251210000000__indexa_documentos_grandes.sql
-- Cria índice de forma concorrente (não bloqueia tabela)

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_documento_status 
ON documento(status, data_criacao);
```

**Opção B: Split em múltiplas migrations**
```sql
-- V20251210000001__adiciona_coluna.sql
ALTER TABLE documento ADD COLUMN status_processamento VARCHAR(20);

-- V20251210000002__atualiza_status_lote1.sql
UPDATE documento SET status_processamento = 'PENDENTE'
WHERE id BETWEEN 1 AND 100000;

-- V20251210000003__atualiza_status_lote2.sql
UPDATE documento SET status_processamento = 'PENDENTE'
WHERE id BETWEEN 100001 AND 200000;
```

---

### Cenário 3: Rollback de Migration

**Flyway não suporta rollback automático!** Se uma migration falhar:

**Estratégia 1: Migration compensatória (recomendado)**
```sql
-- Se V20251215000000__adiciona_coluna_x.sql falhou em alguns tenants:

-- Criar: V20251215100000__remove_coluna_x_fallback.sql
ALTER TABLE tabela DROP COLUMN IF EXISTS coluna_x;
```

**Estratégia 2: Intervenção manual**
```sql
-- Identificar tenants com problema
SELECT tenant_id, schema_name 
FROM public.tenant 
WHERE schema_name NOT IN (
    SELECT DISTINCT table_schema 
    FROM information_schema.columns 
    WHERE column_name = 'coluna_x'
);

-- Corrigir manualmente cada tenant
SET search_path TO tenant_problema;
ALTER TABLE tabela DROP COLUMN coluna_x;
DELETE FROM flyway_schema_history WHERE version = '20251215000000';
```

---

## ✅ Checklist para Nova Migration

- [ ] Nome do arquivo segue padrão: `V{timestamp}__{descricao}.sql`
- [ ] Timestamp único (não conflita com migrations existentes)
- [ ] SQL testado localmente
- [ ] Usa comandos idempotentes (`IF NOT EXISTS`, `IF EXISTS`)
- [ ] Comentários explicando o propósito
- [ ] Migration rápida (< 30 segundos por tenant)
- [ ] Testado com dados reais (se aplicável)
- [ ] Rollback planejado (se aplicável)
- [ ] Logs do `TenantMigrationRunner` validados
- [ ] Commit com mensagem descritiva

---

## 🔧 Troubleshooting

### Problema: Migration não executou

**Sintomas:**
```
✓ OK: empresa-lunar - Nenhuma migration pendente
```

**Possíveis causas:**
1. **Nome do arquivo errado**
   - Verifique: `V{timestamp}__descricao.sql` (dois underscores!)
   
2. **Timestamp menor que última migration**
   ```sql
   -- Verificar última versão
   SELECT MAX(version) FROM tenant_empresa_lunar.flyway_schema_history;
   -- Se retornar 20251210000000, sua nova migration deve ser > que isso
   ```

3. **Migration já executada**
   ```sql
   -- Checar se já existe
   SELECT * FROM tenant_empresa_lunar.flyway_schema_history 
   WHERE version = '20251201120000';
   ```

---

### Problema: Migration falhou em alguns tenants

**Sintomas:**
```
❌ ERROR: Falha ao migrar tenant empresa-teste: ERROR: column "x" already exists
```

**Solução:**
1. **Identificar tenants afetados:**
   ```bash
   # Filtrar logs por "ERROR"
   grep "ERROR: Falha ao migrar tenant" logs/spring-boot.log
   ```

2. **Corrigir manualmente:**
   ```sql
   -- Conectar ao banco
   \c gestao_integrada_pipa_db
   
   -- Para cada tenant com erro
   SET search_path TO tenant_empresa_teste;
   
   -- Executar correção
   ALTER TABLE tabela DROP COLUMN IF EXISTS coluna_problema;
   
   -- Remover registro do Flyway
   DELETE FROM flyway_schema_history WHERE version = '20251201120000';
   ```

3. **Re-executar:** Reiniciar backend para tentar novamente

---

### Problema: Como forçar re-execução de migration

**Cenário:** Você corrigiu uma migration que falhou e quer re-executar.

**Solução:**
```sql
-- 1. Conectar ao tenant com problema
SET search_path TO tenant_problema;

-- 2. Reverter mudanças da migration (se possível)
-- Ex: DROP TABLE, ALTER TABLE DROP COLUMN, etc.

-- 3. Remover registro do Flyway
DELETE FROM flyway_schema_history 
WHERE version = '20251201120000';

-- 4. Reiniciar backend → migration será re-executada
```

---

## 📚 Referências

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL ALTER TABLE](https://www.postgresql.org/docs/current/sql-altertable.html)
- [MULTI-TENANT-ARCHITECTURE.md](./MULTI-TENANT-ARCHITECTURE.md) - Arquitetura completa
- [CRIAR-TENANT.md](./CRIAR-TENANT.md) - Criar novos tenants

---

**Última atualização:** 30 de novembro de 2025
