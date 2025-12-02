# Copilot/Gemini - Instruções do Projeto

Propósito: centralizar regras e contexto compartilhado para agentes (Copilot / Gemini) e referenciar guias de domínio já existentes.

Arquivos de domínio (existentes):
- src/backend/GEMINI.md  -> Contexto e comandos específicos do backend (Java / Spring).
- src/frontend/GEMINI.md -> Contexto e comandos específicos do frontend (Angular / TypeScript).
- .github/instructions/PROJECT.md -> Visão geral do projeto (contexto centralizado)
- MULTI-TENANT-ARCHITECTURE.md -> Documentação completa da arquitetura multi-tenant

Esses arquivos são complementares e não conflitam: mantenha-os como base por domínio.

## ⚠️ ARQUITETURA MULTI-TENANT

Este projeto implementa **multi-tenancy** usando a estratégia **SCHEMA-PER-TENANT** no PostgreSQL.

### Conceitos Importantes:

1. **Tenant = Cliente/Empresa**: Cada tenant é isolado em seu próprio schema PostgreSQL
2. **Header obrigatório**: Todas as requisições devem incluir `X-Tenant-ID` no header
3. **JWT com tenant**: Tokens JWT contêm `tenant_id` para prevenir uso cruzado
4. **Migrations automáticas**: Novos tenants e tenants existentes recebem migrations automaticamente

### Componentes Principais:

- **TenantFilter** (`tenant/filter/`): Valida e define tenant em TODAS as requisições
- **TenantContext** (`tenant/context/`): ThreadLocal que armazena schema atual
- **TenantConnectionProvider** (`tenant/config/`): Aplica `SET search_path` nas conexões
- **TenantIdentifierResolver** (`tenant/config/`): Informa Hibernate qual tenant usar
- **TenantMigrationRunner** (`tenant/config/`): Aplica migrations em todos tenants no startup
- **TenantService** (`tenant/service/`): Cria novos tenants (schema + migrations)

### Regras ao Modificar Código:

✅ **O que fazer:**
- Sempre incluir `X-Tenant-ID` em chamadas HTTP do frontend (via interceptor)
- Usar `TenantContext.getTenantId()` para logs e debug
- Adicionar migrations em `db/tenant-migrations/` (não em `db/migration/`)
- Testar com múltiplos tenants para validar isolamento

❌ **O que NÃO fazer:**
- Criar `EntityManagerFactory` sem configurar multi-tenancy
- Esquecer de limpar `TenantContext.clear()` em filtros
- Usar `@Transactional` em métodos que fazem `CREATE SCHEMA`
- Hard-codar schema names (sempre usar `TenantContext.getTenantId()`)

### Documentação Completa:

Consulte [MULTI-TENANT-ARCHITECTURE.md](./MULTI-TENANT-ARCHITECTURE.md) para:
- Fluxo completo de requisições
- Estrutura do banco de dados
- Como criar novos tenants
- Segurança e boas práticas
- Troubleshooting comum

## Recomendações mínimas:
- Manter este arquivo como fonte global de políticas comuns (estilo de commit, testes obrigatórios, CI, regras de lint).
- Configurar a ferramenta para carregar ambos os diretórios de instruções como contexto:
  - Ex.: export COPILOT_CUSTOM_INSTRUCTIONS_DIRS="src/backend,src/frontend,.github/instructions"
- Opcional: adicionar referência a este arquivo no README e no pipeline de CI para validação automática de sugestões.

Como contribuir:
- Atualize os arquivos GEMINI.md locais com detalhes específicos do domínio.
- Atualize este arquivo com regras transversais (commit message, políticas de PR, testes de aceitação).

Resultado esperado: agentes terão contexto específico por domínio e um conjunto consistente de regras globais para interações mais assertivas.
