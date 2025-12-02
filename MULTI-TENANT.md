# Arquitetura Multi-Tenant - Gestão Integrada Pipa

## Visão Geral

O **Gestão Integrada Pipa** é uma aplicação SaaS (Software as a Service) que utiliza arquitetura **multi-tenant** para isolar dados de diferentes clientes (tenants) de forma segura e eficiente.

## Estratégia: Schema per Tenant

### Por que Schema per Tenant?

Escolhemos a estratégia de **schema por tenant** (PostgreSQL nativo) pelos seguintes motivos:

1. **Isolamento Forte**: Cada tenant possui seu próprio schema no PostgreSQL, garantindo isolamento de dados a nível de banco
2. **Performance**: Usa recursos nativos do PostgreSQL (search_path), sem overhead adicional
3. **Escalabilidade**: Permite gerenciar dezenas a milhares de tenants em um único banco de dados
4. **Backup Seletivo**: Possibilidade de fazer backup de schemas individuais
5. **Manutenção**: Migrations e updates podem ser aplicados schema a schema
6. **Segurança**: Menor risco de vazamento de dados entre tenants comparado a shared schema

### Alternativas Consideradas

- **Database per Tenant**: Muito recursos intensivo, difícil de gerenciar
- **Shared Schema com Discriminator**: Risco de vazamento de dados, queries complexas

## Arquitetura

### 1. Estrutura de Schemas

```
PostgreSQL Database (gestao_integrada_pipa_db)
│
├── public (schema público)
│   └── tenant (tabela de metadados dos tenants)
│
├── tenant_empresa1
│   ├── usuario
│   ├── perfil
│   ├── recurso
│   ├── perfil_recurso
│   └── usuario_perfil
│
├── tenant_empresa2
│   ├── usuario
│   ├── perfil
│   ├── recurso
│   ├── perfil_recurso
│   └── usuario_perfil
│
└── tenant_empresa3
    └── ...
```

### 2. Modelo de Dados - Tenant (schema public)

Tabela `public.tenant`:
```sql
id              BIGSERIAL PRIMARY KEY
tenant_id       VARCHAR(50) UNIQUE NOT NULL
nome            VARCHAR(200) NOT NULL
cnpj            VARCHAR(18)
schema_name     VARCHAR(100) UNIQUE NOT NULL
status          VARCHAR(20) NOT NULL (ACTIVE, SUSPENDED, INACTIVE, TRIAL, CANCELLED)
plano           VARCHAR(20) NOT NULL (BASIC, PROFESSIONAL, ENTERPRISE, CUSTOM)
data_criacao    TIMESTAMP NOT NULL
data_expiracao  TIMESTAMP
max_usuarios    INTEGER
```

### 3. Fluxo de Identificação do Tenant

```
HTTP Request
    ↓
[TenantInterceptor]
    ↓ (extrai tenant_id do header X-Tenant-ID ou JWT)
[TenantContext.setTenantId(tenantId)]
    ↓
[Controller/Service Layer]
    ↓
[TenantConnectionProvider]
    ↓ (executa SET search_path TO tenant_schema)
[Hibernate]
    ↓
[PostgreSQL - acessa schema correto]
```

## Componentes

### 1. TenantContext
```java
// ThreadLocal para armazenar tenant_id da requisição atual
TenantContext.setTenantId("empresa1");
String currentTenant = TenantContext.getTenantId();
TenantContext.clear();
```

### 2. TenantInterceptor
Intercepta requisições HTTP e identifica o tenant:
- **Header HTTP**: `X-Tenant-ID: empresa1`
- **Subdomain** (futuro): `empresa1.gestao-integrada-pipa.com.br`
- **JWT Claims** (futuro): `{ "tenant_id": "empresa1" }`

### 3. TenantConnectionProvider
Implementação do Hibernate `MultiTenantConnectionProvider`:
- Obtém conexão do pool
- Executa `SET search_path TO tenant_schema, public`
- Retorna conexão configurada

### 4. TenantIdentifierResolver
Resolve qual tenant usar para a requisição atual:
```java
public String resolveCurrentTenantIdentifier() {
    return TenantContext.getTenantId() != null 
        ? TenantContext.getTenantId() 
        : "public";
}
```

### 5. TenantService
Serviço para gerenciar tenants:
- `criarTenant()`: Cria registro + schema + migrations
- `ativarTenant()`: Ativa tenant suspenso
- `suspenderTenant()`: Suspende tenant por falta de pagamento

## Criando um Novo Tenant

### 1. Via API REST

**Endpoint**: `POST /admin/tenants`

**Request**:
```json
{
  "tenantId": "empresa-teste",
  "nome": "Empresa Teste LTDA",
  "cnpj": "12.345.678/0001-90",
  "plano": "PROFESSIONAL"
}
```

**Response**:
```json
{
  "id": 1,
  "tenantId": "empresa-teste",
  "nome": "Empresa Teste LTDA",
  "cnpj": "12.345.678/0001-90",
  "schemaName": "tenant_empresa_teste",
  "status": "TRIAL",
  "plano": "PROFESSIONAL",
  "dataCriacao": "2025-01-15T10:30:00",
  "dataExpiracao": "2025-02-15T10:30:00",
  "maxUsuarios": null
}
```

### 2. O que acontece automaticamente:

1. **Validação**: Verifica se tenant_id já existe
2. **Criação do registro**: Salva na tabela `public.tenant`
3. **Criação do schema**: `CREATE SCHEMA tenant_empresa_teste`
4. **Migrations**: Executa `tenant_schema_template.sql`:
   - Cria tabelas: `usuario`, `perfil`, `recurso`, `perfil_recurso`, `usuario_perfil`
   - Insere usuário admin padrão: `admin@empresa-teste.com` / `admin123`
   - Insere perfil ADMIN com todas as permissões
   - Insere recursos padrão (menu items)

### 3. Estrutura Criada

```sql
-- Schema criado
CREATE SCHEMA tenant_empresa_teste;

-- Tabelas criadas no schema
tenant_empresa_teste.usuario
tenant_empresa_teste.perfil
tenant_empresa_teste.recurso
tenant_empresa_teste.perfil_recurso
tenant_empresa_teste.usuario_perfil

-- Dados iniciais
-- Usuário admin
-- Perfil ADMIN
-- Recursos do menu
```

## Usando a Aplicação como Tenant

### 1. Frontend - Adicionar Header

No Angular, criar um interceptor HTTP:

```typescript
// tenant.interceptor.ts
@Injectable()
export class TenantInterceptor implements HttpInterceptor {
  
  constructor(private authService: AuthService) {}
  
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const tenantId = this.authService.getTenantId(); // pegar do JWT ou storage
    
    if (tenantId) {
      req = req.clone({
        setHeaders: {
          'X-Tenant-ID': tenantId
        }
      });
    }
    
    return next.handle(req);
  }
}
```

### 2. Backend - Validação Automática

Todas as requisições passam pelo `TenantInterceptor`:

```java
@Override
public boolean preHandle(HttpServletRequest request, 
                         HttpServletResponse response, 
                         Object handler) {
    String tenantId = request.getHeader("X-Tenant-ID");
    
    if (tenantId != null && !tenantId.isEmpty()) {
        TenantContext.setTenantId(tenantId);
        log.debug("Tenant identificado: {}", tenantId);
    } else {
        log.warn("Tenant não identificado na requisição");
    }
    
    return true;
}
```

### 3. Isolamento Automático

Após o tenant ser identificado, **TODAS** as operações de banco são executadas no schema correto:

```java
// Código da aplicação (sem referência a tenant)
Usuario usuario = usuarioRepository.findById(1L);

// Hibernate automaticamente executa:
SET search_path TO tenant_empresa_teste, public;
SELECT * FROM usuario WHERE id = 1;

// Resultado: busca APENAS na tabela tenant_empresa_teste.usuario
```

## Gerenciamento de Tenants

### Endpoints Administrativos

```
POST   /admin/tenants                  # Criar novo tenant
GET    /admin/tenants/{tenantId}       # Buscar tenant
POST   /admin/tenants/{tenantId}/ativar      # Ativar tenant
POST   /admin/tenants/{tenantId}/suspender   # Suspender tenant
```

### Status dos Tenants

- **TRIAL**: Período de teste (30 dias padrão)
- **ACTIVE**: Ativo e com pagamento em dia
- **SUSPENDED**: Suspenso (falta de pagamento)
- **INACTIVE**: Inativo (cancelado temporariamente)
- **CANCELLED**: Cancelado definitivamente

### Planos

- **BASIC**: Funcionalidades básicas
- **PROFESSIONAL**: Funcionalidades avançadas
- **ENTERPRISE**: Todas as funcionalidades + SLA
- **CUSTOM**: Plano customizado

## Migrations

### Schema Public (Flyway)

Migrations para schema público (metadados):
```
src/main/resources/db/migration/
├── V1__create_initial_tables.sql
└── V1_1__create_tenant_table.sql
```

### Schema Tenant (Template SQL)

Template executado para cada novo tenant:
```
src/main/resources/db/migration/tenant/
└── tenant_schema_template.sql
```

## Segurança

### 1. Isolamento de Dados
- Cada tenant possui schema próprio
- PostgreSQL garante isolamento a nível de banco
- Impossível acessar dados de outro tenant sem trocar search_path

### 2. Validação de Tenant
- Interceptor valida tenant_id em toda requisição
- JWT pode conter tenant_id para validação adicional
- Logs registram tenant_id de todas as operações

### 3. Auditoria
- Todas as operações ficam associadas ao tenant
- Logs de acesso por tenant
- Rastreabilidade completa

## Performance

### 1. Connection Pool
- Pool compartilhado entre todos os tenants
- Search_path configurado por conexão
- Overhead mínimo (< 1ms por requisição)

### 2. Índices
- Cada schema possui seus próprios índices
- Performance não degrada com número de tenants
- Queries otimizadas por schema

### 3. Limites Recomendados
- **Ideal**: 100-500 tenants por banco
- **Máximo**: 1000-2000 tenants por banco
- **Além disso**: Considerar sharding horizontal

## Monitoramento

### Métricas Importantes

1. **Número de tenants ativos**
2. **Uso de storage por tenant**
3. **Número de usuários por tenant**
4. **Queries lentas por tenant**
5. **Taxa de erro por tenant**

### Logs

Todas as operações incluem tenant_id:
```
2025-01-15 10:30:00 INFO  [tenant=empresa1] Usuario criado: joao@empresa1.com
2025-01-15 10:31:00 ERROR [tenant=empresa2] Erro ao processar pagamento
```

## Backup e Restore

### Backup de um Tenant

```bash
# Backup de schema específico
pg_dump -h localhost -U gestao_integrada_pipa -n tenant_empresa1 gestao_integrada_pipa_db > empresa1_backup.sql

# Restore
psql -h localhost -U gestao_integrada_pipa gestao_integrada_pipa_db < empresa1_backup.sql
```

### Backup Completo

```bash
# Backup do banco inteiro (todos os tenants)
pg_dump -h localhost -U gestao_integrada_pipa gestao_integrada_pipa_db > full_backup.sql
```

## Troubleshooting

### Tenant não identificado

**Problema**: Requisições não encontram tenant

**Solução**:
1. Verificar se header `X-Tenant-ID` está sendo enviado
2. Verificar logs do `TenantInterceptor`
3. Verificar se tenant existe na tabela `public.tenant`

### Dados não aparecem

**Problema**: Usuário criado mas não aparece

**Solução**:
1. Verificar qual schema está sendo usado: `SHOW search_path;`
2. Verificar se dados estão no schema correto: `SELECT * FROM tenant_empresa1.usuario;`
3. Verificar logs do `TenantConnectionProvider`

### Performance degradada

**Problema**: Aplicação lenta com muitos tenants

**Solução**:
1. Verificar índices nos schemas dos tenants
2. Analisar queries lentas: `pg_stat_statements`
3. Considerar sharding se passar de 1000 tenants

## Próximos Passos

1. **JWT Integration**: Extrair tenant_id do token JWT
2. **Subdomain Routing**: Identificar tenant por subdomínio
3. **Admin Dashboard**: Interface para gerenciar tenants
4. **Billing Integration**: Integração com sistema de pagamentos
5. **Usage Metrics**: Métricas de uso por tenant
6. **Tenant Isolation Tests**: Testes automatizados de isolamento

## Referências

- [PostgreSQL Schemas](https://www.postgresql.org/docs/current/ddl-schemas.html)
- [Hibernate Multi-Tenancy](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#multitenacy)
- [Multi-Tenant SaaS Patterns](https://docs.aws.amazon.com/wellarchitected/latest/saas-lens/multi-tenant-saas-patterns.html)
