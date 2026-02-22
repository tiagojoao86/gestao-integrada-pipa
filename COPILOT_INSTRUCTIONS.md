# Copilot/Gemini — Instruções do Projeto Gestão Integrada Pipa

> **IMPORTANTE:** O arquivo principal de instruções para agentes de IA é o **`CLAUDE.md`** na raiz do projeto.
> Leia-o primeiro. Este arquivo contém complementos específicos para Copilot/Gemini.

## Arquivos de Documentação (LEIA SEMPRE)

Consulte estes arquivos quando trabalhar nas respectivas áreas:

- **`CLAUDE.md`** → Regras críticas, padrões de código, checklist de entidades, comandos
- **`ENTITY_CREATE_PROMPT.md`** → Template completo para geração de novas entidades (backend + frontend + migrations)
- **`MULTI-TENANT-ARCHITECTURE.md`** → Arquitetura multi-tenant completa (Schema-per-Tenant, TenantFilter, etc.)
- **`MULTI-TENANT.md`** → Filtro automático por UnidadeNegocio (UnidadeNegocioFiltravel, Specification)
- **`TENANT-HEADER.md`** → Header X-Tenant-ID obrigatório, rotas públicas, interceptor frontend
- **`MIGRATION-EVOLUTION-GUIDE.md`** → Evolução segura de schema em ambiente multi-tenant
- **`GUIA-FORMATACAO.md`** → Formatação de código (máx 120 chars/linha, Checkstyle, EditorConfig)

## Quando Consultar Cada Arquivo

- **Backend:** Leia `CLAUDE.md` (padrões) + `ENTITY_CREATE_PROMPT.md` (se criar entidade) antes de implementar
- **Frontend:** Leia `CLAUDE.md` (padrões) antes de criar/modificar componentes, services, guards, i18n
- **Multi-tenancy:** Leia `MULTI-TENANT-ARCHITECTURE.md` para tenancy, migrations, schemas
- **Componentes base:** Veja guias em `src/frontend/src/app/components/base/*/`

## Value Objects Disponíveis

Verifique o pacote `core.valueobject` **ANTES** de criar novos Value Objects.

| VO | Uso |
|----|-----|
| `Nome` | Nomes (máx 255 chars, não vazio) |
| `CPF` | CPF brasileiro com dígitos verificadores |
| `CNPJ` | CNPJ brasileiro com dígitos verificadores |
| `Email` | Formato de e-mail |
| `PhoneNumber` | Telefone brasileiro |
| `Money` | Valores monetários |

**Sempre usar método fábrica:** `Nome.of(str)`, `Money.of(valor)` — NUNCA `new Nome(str)`.

## Regras Multi-tenant (Resumo)

✅ **SEMPRE:**
- Incluir `X-Tenant-ID` em chamadas HTTP do frontend (via interceptor — já configurado)
- Adicionar migrations em `db/tenant-migrations/` (não em `db/migration/`)
- Usar `TenantContext.getTenantId()` para logs e debug

❌ **NUNCA:**
- Criar `EntityManagerFactory` sem configurar multi-tenancy
- Esquecer de limpar `TenantContext.clear()` em filtros
- Usar `@Transactional` em métodos que fazem `CREATE SCHEMA`
- Hardcodar schema names

## Padrão de Autorização Frontend

```typescript
// Rota com guard de autenticação + módulo
{
  path: 'minha-entidade',
  canActivate: [authGuard, moduleAuthorityGuard],
  data: { module: 'MODULO_MINHA_ENTIDADE' }
}
```
