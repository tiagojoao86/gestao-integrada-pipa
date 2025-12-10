# Filtro Automático por Unidade de Negócio

## Padrão

Todas entidades que devem ser restritas por unidade de negócio devem implementar a interface marker `UnidadeNegocioFiltravel`. O filtro é aplicado automaticamente via Specification no service.

### Exemplo

```java
public interface UnidadeNegocioFiltravel {
        UnidadeNegocio getUnidadeNegocio();
}

// No service
if (UnidadeNegocioFiltravel.class.isAssignableFrom(entityClass)) {
        Set<UUID> permitidas = Session.getUnidadeNegocioIds();
        spec = spec.and(UnidadeNegocioSpecification.permitidasParaUsuario(permitidas));
}
```

---

# Endpoints Dedicados para Vinculação

## Padrão

Cada controller deve expor endpoints específicos para vinculação de entidades, já filtrando por unidade de negócio e status ativo.

### Exemplo

```java
@GetMapping("/titulo/pessoas-disponiveis")
public List<PessoaDTO> listarPessoasDisponiveis() { ... }

@GetMapping("/titulo/planos-disponiveis")
public List<PlanoContasDTO> listarPlanosDisponiveis(@RequestParam UUID unidadeNegocioId) { ... }
```

---

# Integração Frontend/Backend

## Fluxo

1. Backend retorna lista de unidades de negócio (id, nome, código, isDefault) no login/autenticação
2. Frontend armazena unidades no AuthService/sessionStorage
3. Componentes carregam unidades e setam valor default no form

### Exemplo Angular

```typescript
const defaultUnidade = this.authService.getDefaultUnidadeNegocio();
if (defaultUnidade) {
  this.form.get("unidadeNegocio")?.setValue(defaultUnidade.id);
}
```

---

# Arquitetura Multi-Tenant - Gestão Integrada Pipa

## 📋 Visão Geral

Este documento detalha a implementação completa de multi-tenancy (multi-inquilino) do sistema Gestão Integrada Pipa, utilizando a estratégia de **SCHEMA-PER-TENANT** no PostgreSQL.

### Estratégia Adotada: Schema-per-Tenant

Cada tenant (cliente/empresa) possui seu próprio schema no banco de dados, proporcionando:

- ✅ **Isolamento total de dados** entre tenants
- ✅ **Segurança** - Um tenant não pode acessar dados de outro
- ✅ **Escalabilidade** - Fácil adicionar novos tenants
- ✅ **Manutenção** - Migrations gerenciadas automaticamente via Flyway
- ✅ **Performance** - PostgreSQL otimiza queries por schema

---

## 🎯 Componentes Principais

### 1. Gestão de Contexto

**Arquivo:** `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/tenant/context/TenantContext.java`

```java
public class TenantContext {
    private static final ThreadLocal<String> currentTenantSchema = new ThreadLocal<>();

    public static void setTenantId(String schemaName)
    public static String getTenantId()
    public static void clear()
}
```

**Função:** Armazena o schema do tenant atual usando `ThreadLocal` para isolamento por requisição.

**Por que ThreadLocal?** Cada requisição HTTP roda em uma thread diferente. ThreadLocal garante que cada thread tenha seu próprio valor isolado, evitando que requisições simultâneas interfiram umas nas outras.

---

### 2. Filtro de Tenant (Primeira Linha de Defesa)

**Arquivo:** `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/tenant/filter/TenantFilter.java`

```java
@Component
@Order(1) // Executa PRIMEIRO na cadeia de filtros
public class TenantFilter implements Filter
```

**Função:** Define o tenant ANTES de qualquer autenticação ou validação do Spring Security.

**Fluxo de Execução:**

1. **Extrai `X-Tenant-ID` do header** (ex: `empresa_lunar`)
2. **Normaliza para schema_name** (ex: `tenant_empresa_lunar`)
3. **Valida segurança do JWT** (se presente):
   - Extrai `tenant_id` do token
   - Compara com header: `token.tenant_id == header.X-Tenant-ID`?
   - Se diferente → **HTTP 403 Forbidden**
4. **Define no contexto:** `TenantContext.setTenantId("tenant_empresa_lunar")`
5. **Continua cadeia** → Spring Security → Controller
6. **Finally:** Limpa contexto `TenantContext.clear()`

**Rotas públicas (não exigem tenant):**

- `/admin/tenants/**` - Criação e gerenciamento de tenants
- `/health` - Health check
- `/actuator/**` - Métricas do sistema

---

### 3. Resolver de Tenant (Hibernate pergunta: qual tenant?)

**Arquivo:** `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/tenant/config/TenantIdentifierResolver.java`

```java
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId : "public";
    }
}
```

**Função:** O Hibernate chama este resolver **TODA VEZ** que precisa saber qual tenant usar.

**Fluxo:**

1. Hibernate precisa fazer uma query SQL
2. Chama `resolveCurrentTenantIdentifier()`
3. Retorna `TenantContext.getTenantId()` → Ex: `"tenant_empresa_lunar"`
4. Se null → retorna `"public"` (schema padrão)

---

### 4. Provider de Conexões (Como aplicar o tenant?)

**Arquivo:** `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/tenant/config/TenantConnectionProvider.java`

```java
@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();
        connection.createStatement().execute(
            String.format("SET search_path TO %s, public", tenantIdentifier)
        );
        return connection;
    }
}
```

**Função:** Fornece conexões ao Hibernate com o `search_path` correto.

**Fluxo:**

1. Hibernate pede uma conexão: `getConnection("tenant_empresa_lunar")`
2. Provider pega conexão do pool
3. **Executa SQL:** `SET search_path TO tenant_empresa_lunar, public`
4. Retorna conexão configurada
5. Ao devolver: `SET search_path TO public` (reset)

**Por que `search_path`?** É um recurso do PostgreSQL que define em qual schema procurar as tabelas. Quando você faz `SELECT * FROM usuario`, o PostgreSQL procura em `tenant_empresa_lunar.usuario` automaticamente.

---

### 5. Configuração de DataSource

**Arquivo:** `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/config/DataSourceConfig.java`

```java
@Bean
public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        TenantConnectionProvider tenantConnectionProvider,
        TenantIdentifierResolver tenantIdentifierResolver) {

    // ... configuração

    // ⭐ CONFIGURAÇÃO DE MULTI-TENANCY
    properties.put("hibernate.multiTenancy", "SCHEMA");
    properties.put("hibernate.multi_tenant_connection_provider", tenantConnectionProvider);
    properties.put("hibernate.tenant_identifier_resolver", tenantIdentifierResolver);

    return factory;
}
```

**CRÍTICO:** Sem essas configurações, o Hibernate não sabe que deve usar multi-tenancy e usa sempre o schema `public`!

**Por que configurar manualmente?** Quando você cria `LocalContainerEntityManagerFactoryBean` manualmente, as propriedades do `application.properties` com prefixo `spring.jpa.properties.*` são **ignoradas**. É necessário injetar os beans diretamente.

---

### 6. Serviço de Tenant

**Arquivo:** `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/tenant/service/TenantService.java`

**Função:** Criar novos tenants (schema + migrations).

**Fluxo de criação:**

```java
public Tenant criarTenant(String tenantId, String nome, String numeroDocumento, TenantPlano plano) {
    // 1. Salva registro na tabela public.tenant
    tenant = salvarTenant(tenant);

    // 2. Cria schema no PostgreSQL
    criarSchema(schemaName); // CREATE SCHEMA tenant_xyz

    // 3. Executa Flyway migrations no schema criado
    executarMigrationsTenant(schemaName);

    return tenant;
}
```

**Por que AUTOCOMMIT?** `CREATE SCHEMA` é uma operação DDL que não pode estar em transação. Usar `connection.setAutoCommit(true)` garante que o schema seja criado imediatamente, sem risco de rollback.

---

### 7. Entidades e Repositórios

**Arquivos:**

- `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/tenant/entity/Tenant.java` - Entidade JPA
- `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/tenant/enums/TenantStatus.java` - Enum de status
- `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/tenant/enums/TenantPlano.java` - Enum de planos
- `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/tenant/repository/TenantRepository.java` - Repository Spring Data JPA

**Função:** Gerenciar metadados dos tenants no schema `public`.

**Importante:** A tabela `tenant` fica no schema `public` porque contém informações sobre **TODOS** os tenants do sistema.

**Estrutura da tabela:**

```sql
CREATE TABLE public.tenant (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) UNIQUE NOT NULL,
    nome VARCHAR(200) NOT NULL,
    numero_documento VARCHAR(20),
    schema_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    plano VARCHAR(20) NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    data_expiracao TIMESTAMP,
    max_usuarios INTEGER,
    observacoes TEXT
);
```

---

### 8. Runner de Migrations Automáticas

**Arquivo:** `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/tenant/config/TenantMigrationRunner.java`

```java
@Component
public class TenantMigrationRunner {

    @EventListener(ApplicationReadyEvent.class)
    public void migrateTenants() {
        // Busca todos os tenants
        // Para cada tenant: executa migrations pendentes
        // Logs detalhados do processo
    }
}
```

**Função:** Garante que **todos os tenants existentes** recebam novas migrations ao iniciar o backend.

**Fluxo de Execução:**

1. Spring Boot inicializa completamente
2. Evento `ApplicationReadyEvent` é disparado
3. `TenantMigrationRunner` é acionado
4. Busca todos os tenants em `public.tenant`
5. Para cada tenant ativo:
   - Configura Flyway para o schema do tenant
   - Executa `flyway.migrate()` (aplica apenas migrations pendentes)
   - Loga resultado (sucessos/erros)
6. Relatório final: tenants migrados, pulados, com erro

**Logs de exemplo:**

```
======================================================
🔄 INICIANDO MIGRATIONS AUTOMÁTICAS DE TENANTS
======================================================
📋 Encontrados 3 tenant(s) no sistema
🔍 Verificando migrations para: empresa-lunar (schema: tenant_empresa_lunar)
✅ SUCCESS: empresa-lunar - 1 migration(s) aplicada(s)
🔍 Verificando migrations para: empresa-solar (schema: tenant_empresa_solar)
✓ OK: empresa-solar - Nenhuma migration pendente
⏭️  SKIP: empresa-teste - Status: SUSPENDED (suspenso/cancelado)
======================================================
✅ Migrations concluídas - Sucesso: 1 | Skip: 1 | Erro: 0
======================================================
```

**Por que é necessário?**

- Sem isso: novos tenants teriam a estrutura atualizada, mas tenants antigos ficariam desatualizados
- Com isso: **zero intervenção manual** para evoluir o sistema

**Quando roda:**

- Toda inicialização do backend
- Se não há migrations pendentes: processo é rápido (Flyway detecta e não faz nada)
- Se há migrations pendentes: aplica em todos os tenants ativos

**Tenants ignorados:**

- Status `SUSPENDED` (suspensos temporariamente)
- Status `CANCELLED` (cancelados definitivamente)

---

### 9. Controller Administrativo

**Arquivo:** `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/tenant/controller/TenantAdminController.java`

**Função:** API REST para gerenciar tenants.

**Endpoints:**

| Método | Endpoint                              | Descrição         | Autenticação  |
| ------ | ------------------------------------- | ----------------- | ------------- |
| POST   | `/admin/tenants`                      | Criar novo tenant | X-Admin-Token |
| GET    | `/admin/tenants/{tenantId}`           | Buscar tenant     | X-Admin-Token |
| PUT    | `/admin/tenants/{tenantId}/ativar`    | Ativar tenant     | X-Admin-Token |
| PUT    | `/admin/tenants/{tenantId}/suspender` | Suspender tenant  | X-Admin-Token |

**Segurança:** Todos os endpoints exigem o header `X-Admin-Token` com valor configurado em `application.properties` (`app.admin.token`).

**Exemplo de uso:**

```bash
curl -X POST http://localhost:8080/gestao-integrada-pipa/api/admin/tenants \
  -H "Content-Type: application/json" \
  -H "X-Admin-Token: 78821f5a117485cb4fb4b6a207420fc5ed0e9f770e97aa8fba8982b6076f7650" \
  -d '{
    "tenantId": "empresa-abc",
    "nome": "Empresa ABC Ltda",
    "numeroDocumento": "12345678000199",
    "plano": "PROFESSIONAL"
  }'
```

---

### 9. Segurança JWT com Tenant

**Arquivos:**

- `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/config/security/JwtService.java`
- `src/backend/src/main/java/br/com/grupopipa/gestaointegradapipa/config/security/AuthenticationService.java`

**Função:** Gerar tokens JWT com `tenant_id` embutido para prevenir uso cruzado entre tenants.

**Claim adicionado ao JWT:**

```json
{
  "sub": "admin",
  "tenant_id": "tenant_empresa_lunar",
  "scope": "ROLE_ADMIN ROLE_USER",
  "iss": "gestao-integrada-pipa",
  "exp": 1732998765
}
```

**Validação no TenantFilter:**

```java
String tokenTenantId = extractTenantIdFromToken(token);
if (tokenTenantId != null && !tokenTenantId.equals(schemaName)) {
    // ❌ Token pertence a outro tenant!
    return HTTP 403 Forbidden;
}
```

**Cenário bloqueado:**

```bash
# 1. Login no tenant empresa_lunar
POST /api/authenticate
Headers: X-Tenant-ID: empresa_lunar
Response: { "token": "eyJ...ABC" }  # token com tenant_id=tenant_empresa_lunar

# 2. Tentar usar token no tenant empresa_solar
GET /api/perfil
Headers:
  X-Tenant-ID: empresa_solar  # ← Diferente!
  Authorization: Bearer eyJ...ABC

# ❌ Resultado: HTTP 403 Forbidden
# {"error":"Token não pertence a este tenant. Acesso negado."}
```

---

## 🔄 Fluxo Completo de uma Requisição

### Cenário: Buscar perfis do tenant `empresa_lunar`

```bash
GET /api/perfil
Headers:
  X-Tenant-ID: empresa_lunar
  Authorization: Bearer eyJhbGc...
```

### Diagrama de Fluxo:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. REQUEST CHEGA NO SERVIDOR                                │
├─────────────────────────────────────────────────────────────┤
│ GET /api/perfil                                              │
│ Headers: X-Tenant-ID=empresa_lunar, Authorization=Bearer... │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. TenantFilter (@Order(1))                                 │
├─────────────────────────────────────────────────────────────┤
│ ✓ Extrai X-Tenant-ID: "empresa_lunar"                       │
│ ✓ Normaliza: "tenant_empresa_lunar"                         │
│ ✓ Valida JWT:                                               │
│   - Decodifica token                                        │
│   - Extrai tenant_id do token: "tenant_empresa_lunar"       │
│   - Compara: tenant_empresa_lunar == tenant_empresa_lunar?  │
│   - ✅ OK!                                                   │
│ ✓ TenantContext.setTenantId("tenant_empresa_lunar")        │
│ ✓ Continua para próximo filtro...                          │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Spring Security Filters                                   │
├─────────────────────────────────────────────────────────────┤
│ ✓ JwtAuthenticationFilter                                   │
│   - Valida assinatura do token                             │
│   - Extrai username: "admin"                                │
│   - Cria Authentication object                              │
│ ✓ Authorization check (roles/permissions)                   │
│ ✓ Usuário autenticado e autorizado → continua              │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Controller (PerfilController)                            │
├─────────────────────────────────────────────────────────────┤
│ @GetMapping("/perfil")                                      │
│ public List<Perfil> listar() {                              │
│     return perfilService.findAll();                         │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Service Layer                                            │
├─────────────────────────────────────────────────────────────┤
│ perfilRepository.findAll()                                  │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. Hibernate (JPA)                                          │
├─────────────────────────────────────────────────────────────┤
│ Precisa executar: SELECT * FROM perfil                      │
│ Mas... qual schema usar?                                    │
│                                                             │
│ Chama: TenantIdentifierResolver.resolveCurrentTenantId()   │
│ Retorna: "tenant_empresa_lunar"                            │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 7. TenantConnectionProvider                                 │
├─────────────────────────────────────────────────────────────┤
│ getConnection("tenant_empresa_lunar")                       │
│                                                             │
│ 1. Pega conexão do pool                                    │
│ 2. Executa: SET search_path TO tenant_empresa_lunar, public│
│ 3. Retorna conexão                                         │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 8. PostgreSQL                                               │
├─────────────────────────────────────────────────────────────┤
│ Executa: SELECT * FROM perfil                               │
│                                                             │
│ PostgreSQL procura em: tenant_empresa_lunar.perfil         │
│ (por causa do search_path)                                 │
│                                                             │
│ Retorna: [Perfil1, Perfil2, Perfil3]                       │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 9. RESPONSE                                                 │
├─────────────────────────────────────────────────────────────┤
│ HTTP 200 OK                                                 │
│ [                                                           │
│   {"id":1, "nome":"Admin"},                                 │
│   {"id":2, "nome":"Usuário"}                                │
│ ]                                                           │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ 10. TenantFilter - Finally Block                           │
├─────────────────────────────────────────────────────────────┤
│ TenantContext.clear()                                       │
│ (Limpa ThreadLocal para não vazar para próxima requisição) │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 Estrutura do Banco de Dados

### Schemas e Tabelas

```sql
-- Schema PUBLIC (metadados de tenants)
public/
  ├── tenant                    -- Tabela de controle de tenants
  └── flyway_schema_history     -- Histórico de migrations do public

-- Schema dos Tenants (dados isolados)
tenant_empresa_lunar/
  ├── usuario
  ├── perfil
  ├── modulo
  ├── perfil_modulo
  ├── usuario_perfil
  └── flyway_schema_history     -- Histórico de migrations deste tenant

tenant_empresa_solar/
  ├── usuario                   -- Isolado do empresa_lunar!
  ├── perfil
  ├── modulo
  ├── perfil_modulo
  ├── usuario_perfil
  └── flyway_schema_history
```

**Isolamento Total:** Cada tenant tem suas próprias tabelas. Um `SELECT` em `tenant_empresa_lunar.usuario` **NÃO** vê dados de `tenant_empresa_solar.usuario`.

---

## 🚀 Migrations (Flyway)

### Estrutura de Pastas

```
src/main/resources/
├── db/migration/                    ← Migrations do PUBLIC schema
│   └── V20251129000000__create_tenant_table.sql
│
└── db/tenant-migrations/            ← Migrations dos schemas de tenants
    ├── V20250910020657__cria_tabela_usuario.sql
    └── V20251013003901__cria_tabela_modulo_perfil.sql
```

### Quando Executam

**PUBLIC Schema:**

- Executado automaticamente pelo Flyway principal na **inicialização do Spring Boot**
- Configurado em `application.properties`: `spring.flyway.locations=classpath:db/migration`
- Cria/atualiza a tabela `tenant` e outras estruturas globais

**Schemas de Tenants:**

- **AUTOMÁTICO na inicialização:** `TenantMigrationRunner` aplica migrations pendentes em **todos** os tenants existentes
- **Criação de novo tenant:** `TenantService.criarTenant()` executa todas as migrations
- Flyway configurado programaticamente apontando para `classpath:db/tenant-migrations`

### ⚠️ IMPORTANTE: Migrations Evolutivas

Quando você adiciona uma **nova migration** (ex: `V20251201000000__adiciona_coluna_email.sql`):

1. **Novos tenants:** Recebem automaticamente ao serem criados
2. **Tenants existentes:** Recebem automaticamente na **próxima inicialização** do backend
3. **Zero intervenção manual** necessária!

**Como funciona:**

- `TenantMigrationRunner` é acionado no evento `ApplicationReadyEvent`
- Busca todos os tenants ativos no banco (`public.tenant`)
- Para cada tenant: executa `flyway.migrate()` no schema correspondente
- Flyway detecta migrations pendentes e aplica apenas as novas
- Logs detalhados mostram o progresso

### Exemplo de Migration de Tenant

```sql
-- V20250910020657__cria_tabela_usuario.sql
CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(50) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    nome VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

CREATE INDEX idx_usuario_login ON usuario(login);
```

**Importante:** Cada tenant recebe uma **cópia independente** desta estrutura em seu próprio schema.

---

## 🔐 Segurança Multi-Tenant

### 1. Isolamento de Dados

✅ **Garantido por:**

- Schema-per-tenant no PostgreSQL
- `SET search_path` por conexão
- Validação de tenant no TenantFilter

❌ **Impossível:**

- Tenant A acessar dados do Tenant B
- SQL injection cruzar schemas (search_path isola)

### 2. Validação de JWT com Tenant

✅ **Proteções:**

```java
// Token gerado no tenant A
{
  "sub": "usuario",
  "tenant_id": "tenant_empresa_a"
}

// Requisição para tenant B
Headers:
  X-Tenant-ID: empresa_b
  Authorization: Bearer <token_do_tenant_a>

// Resultado: HTTP 403 Forbidden
// Motivo: token.tenant_id != header.tenant_id
```

### 3. Admin Token para Criação de Tenants

✅ **Proteção:**

- Header `X-Admin-Token` obrigatório para criar/gerenciar tenants
- Token configurado em `application.properties` (deve ser alterado em produção)
- Gerar token seguro: `openssl rand -hex 32`

---

## 🔑 Pontos Críticos e Boas Práticas

### ❌ O que NÃO fazer

**1. Esquecer X-Tenant-ID no header**

```bash
GET /api/perfil
# SEM X-Tenant-ID → HTTP 400 Bad Request
# {"error":"Tenant não identificado. Header X-Tenant-ID é obrigatório."}
```

**2. Criar EntityManagerFactory sem configurar multi-tenancy**

```java
// ❌ ERRADO: properties do application.properties são ignoradas!
LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
factory.setDataSource(dataSource);
// Faltou: hibernate.multiTenancy, tenant_identifier_resolver, etc

// ✅ CORRETO: Injetar beans manualmente
public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        TenantConnectionProvider provider,
        TenantIdentifierResolver resolver) {
    properties.put("hibernate.multiTenancy", "SCHEMA");
    properties.put("hibernate.multi_tenant_connection_provider", provider);
    properties.put("hibernate.tenant_identifier_resolver", resolver);
}
```

**3. Não limpar TenantContext**

```java
// ❌ ERRADO: ThreadLocal vaza para próxima requisição!
TenantContext.setTenantId("tenant_abc");
// ... processar requisição
// FALTA: TenantContext.clear()

// ✅ CORRETO: Sempre usar try-finally
try {
    TenantContext.setTenantId(schemaName);
    chain.doFilter(request, response);
} finally {
    TenantContext.clear();
}
```

**4. CREATE SCHEMA dentro de @Transactional**

```java
// ❌ ERRADO: DDL não pode estar em transação
@Transactional
public void criarTenant() {
    jdbcTemplate.execute("CREATE SCHEMA ..."); // Pode dar rollback!
}

// ✅ CORRETO: Usar AUTOCOMMIT
try (Connection connection = dataSource.getConnection()) {
    connection.setAutoCommit(true);
    connection.createStatement().execute("CREATE SCHEMA ...");
}
```

### ✅ Boas Práticas

**1. Sempre validar tenant em operações críticas**

```java
@PreAuthorize("hasRole('ADMIN')")
public void deletarUsuario(Long id) {
    // Tenant já está no contexto via TenantFilter
    // Hibernate automaticamente usa o schema correto
    usuarioRepository.deleteById(id);
}
```

**2. Logs detalhados para debug**

```java
log.info("✅ TENANT DEFINIDO - Header: '{}', Schema: '{}', Path: {} {}",
    tenantId, schemaName, httpRequest.getMethod(), requestPath);
```

**3. Documentar rotas públicas**

```java
private boolean isPublicRoute(String path) {
    // Rotas administrativas (criação de tenants)
    if (path.contains("/admin/tenants")) return true;

    // Health check, actuator
    if (path.contains("/actuator") || path.contains("/health")) return true;

    return false;
}
```

---

## 📝 Configuração do Application Properties

```properties
# Multi-tenancy configuration
spring.jpa.properties.hibernate.multiTenancy=SCHEMA
spring.jpa.properties.hibernate.multi_tenant_connection_provider=br.com.grupopipa.gestaointegradapipa.tenant.config.TenantConnectionProvider
spring.jpa.properties.hibernate.tenant_identifier_resolver=br.com.grupopipa.gestaointegradapipa.tenant.config.TenantIdentifierResolver

# Flyway configuration for PUBLIC schema (metadata only)
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.schemas=public
# NOTE: Tenant migrations are in db/tenant-migrations/ (separate folder)
# They are executed manually by TenantService when creating a new tenant

# Admin token for tenant management
# IMPORTANTE: Altere este token em produção!
# Gere um token forte com: openssl rand -hex 32
app.admin.token=78821f5a117485cb4fb4b6a207420fc5ed0e9f770e97aa8fba8982b6076f7650
```

---

## 📖 Referências e Documentação

| Componente                   | Responsabilidade                                 | Arquivo                                        |
| ---------------------------- | ------------------------------------------------ | ---------------------------------------------- |
| **TenantFilter**             | Extrai tenant, valida JWT, define contexto       | `tenant/filter/TenantFilter.java`              |
| **TenantContext**            | Armazena schema atual (ThreadLocal)              | `tenant/context/TenantContext.java`            |
| **TenantIdentifierResolver** | Informa Hibernate qual tenant usar               | `tenant/config/TenantIdentifierResolver.java`  |
| **TenantConnectionProvider** | Aplica SET search_path na conexão                | `tenant/config/TenantConnectionProvider.java`  |
| **DataSourceConfig**         | Registra resolver e provider                     | `config/DataSourceConfig.java`                 |
| **TenantService**            | Cria tenants (schema + migrations)               | `tenant/service/TenantService.java`            |
| **TenantMigrationRunner**    | **Aplica migrations em todos tenants (startup)** | `tenant/config/TenantMigrationRunner.java`     |
| **JwtService**               | Gera tokens com tenant_id                        | `config/security/JwtService.java`              |
| **TenantAdminController**    | API REST para gerenciar tenants                  | `tenant/controller/TenantAdminController.java` |

### Outros Documentos

- [CRIAR-TENANT.md](./CRIAR-TENANT.md) - Guia rápido para criar novos tenants
- [TENANT-HEADER.md](./TENANT-HEADER.md) - Como integrar X-Tenant-ID no frontend
- [README.md](./README.md) - Documentação principal do projeto

---

## 📞 Suporte

Para dúvidas ou problemas relacionados à implementação multi-tenant, consulte este documento ou entre em contato com a equipe de desenvolvimento.

**Última atualização:** 30 de novembro de 2025
