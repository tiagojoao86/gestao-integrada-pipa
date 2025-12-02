# Estratégia de Multi-Tenancy: Migrations e Frontend

## 📋 Resumo da Arquitetura

### Problema Identificado
As migrations existentes criavam tabelas no schema `public`, mas em uma arquitetura multi-tenant, cada cliente precisa ter suas próprias tabelas isoladas.

### Solução Implementada
**Schema per Tenant com Separação de Responsabilidades**

```
PostgreSQL Database
│
├── public (metadados compartilhados)
│   └── tenant ← tabela com informações dos clientes
│
├── tenant_empresa1 (dados isolados do cliente 1)
│   ├── usuario
│   ├── perfil
│   ├── modulo
│   ├── usuario_perfil
│   └── perfil_modulo
│
└── tenant_empresa2 (dados isolados do cliente 2)
    └── [mesma estrutura]
```

---

## 🗄️ Como Funciona: Migrations

### 1. Migrations do Flyway (Schema PUBLIC)

**Localização**: `src/main/resources/db/migration/`

**O que o Flyway executa automaticamente**:
```
V1_1__create_tenant_table.sql  ← Cria APENAS a tabela 'tenant'
```

**Resultado após iniciar a aplicação**:
```sql
-- Schema: public
public.tenant          ← Metadados dos clientes (nome, CNPJ, plano, status)
public.flyway_schema_history  ← Histórico de migrations do Flyway
```

### 2. Migrations Antigas (Agora OBSOLETAS para Multi-Tenant)

Estas migrations criavam tabelas no `public`:
```
❌ V20250910020657__cria_tabela_usuario.sql
❌ V20251013003901__cria_tabela_modulo_perfil.sql
```

**Problema**: Criavam usuario, perfil, modulo no schema `public`, mas em multi-tenant essas tabelas devem estar no schema de cada tenant!

**Solução**: 
- Opção A: **Manter as migrations antigas** se você já tem dados em produção (dados no public ficam como "tenant legado")
- Opção B: **Remover as migrations antigas** se está começando do zero (RECOMENDADO)

### 3. Template SQL para Tenants

**Localização**: `src/main/resources/db/migration/tenant/tenant_schema_template.sql`

**NÃO é executado pelo Flyway!** É um arquivo SQL lido e executado manualmente pelo `TenantService`.

**Conteúdo**: Estrutura COMPLETA que cada tenant precisa ter:
- Tabela `usuario`
- Tabela `modulo`
- Tabela `perfil`
- Tabela `usuario_perfil`
- Tabela `perfil_modulo`
- Dados iniciais (admin, perfis, módulos)

---

## ⚙️ Fluxo Completo de Criação de Tenant

### Passo a Passo

```
1. API REST recebe requisição
   POST /admin/tenants
   {
     "tenantId": "empresa-teste",
     "nome": "Empresa Teste LTDA",
     "cnpj": "12.345.678/0001-90",
     "plano": "PROFESSIONAL"
   }

2. TenantService.criarTenant()
   ↓
   2.1. Validação
       - Verifica se tenant_id já existe
       - Valida dados do tenant
   
   2.2. Salva na tabela public.tenant
       INSERT INTO public.tenant (...) VALUES (...)
       
   2.3. Cria schema do tenant
       CREATE SCHEMA tenant_empresa_teste
       
   2.4. Executa tenant_schema_template.sql
       - Lê arquivo do classpath
       - Substitui placeholder do schema (se houver)
       - Executa SQL no novo schema
       - SET search_path TO tenant_empresa_teste
       - CREATE TABLE usuario ...
       - CREATE TABLE modulo ...
       - INSERT INTO perfil ...
       - INSERT INTO usuario ...

3. Retorna tenant criado
   {
     "id": 1,
     "tenantId": "empresa-teste",
     "schemaName": "tenant_empresa_teste",
     "status": "TRIAL",
     ...
   }
```

### Código do TenantService

```java
@Transactional
public Tenant criarTenant(String tenantId, String nome, String cnpj, TenantPlano plano) {
    // 1. Validação
    if (tenantRepository.existsByTenantId(tenantId)) {
        throw new RuntimeException("Tenant já existe");
    }
    
    // 2. Criar registro
    Tenant tenant = new Tenant();
    tenant.setTenantId(tenantId);
    tenant.setNome(nome);
    tenant.setCnpj(cnpj);
    tenant.setPlano(plano);
    tenant.setStatus(TenantStatus.TRIAL);
    tenant.setDataCriacao(LocalDateTime.now());
    tenant = tenantRepository.save(tenant);
    
    // 3. Criar schema
    String schemaName = tenant.getSchemaName(); // tenant_empresa_teste
    criarSchema(schemaName);
    
    // 4. Executar template
    executarMigrationsTenant(schemaName);
    
    return tenant;
}

private void criarSchema(String schemaName) {
    jdbcTemplate.execute("CREATE SCHEMA " + schemaName);
}

private void executarMigrationsTenant(String schemaName) {
    // Lê template SQL do classpath
    Resource resource = new ClassPathResource("db/migration/tenant/tenant_schema_template.sql");
    String sql = new String(resource.getInputStream().readAllBytes());
    
    // Configura search_path para o schema do tenant
    jdbcTemplate.execute("SET search_path TO " + schemaName + ", public");
    
    // Executa SQL no schema do tenant
    jdbcTemplate.execute(sql);
    
    // Volta para public
    jdbcTemplate.execute("SET search_path TO public");
}
```

---

## 🎨 Frontend: Como Integrar

### Problema do Frontend

Quando um usuário faz login, o **frontend precisa saber qual tenant ele pertence** e enviar isso em todas as requisições.

### Solução: HTTP Interceptor no Angular

#### 1. Armazenar Tenant no Login

```typescript
// auth.service.ts
export class AuthService {
  
  login(credentials: LoginCredentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', credentials)
      .pipe(
        tap(response => {
          // Salvar token JWT
          localStorage.setItem('access_token', response.token);
          
          // Salvar tenant_id (vem no JWT ou no response)
          const tenantId = this.extractTenantFromToken(response.token);
          localStorage.setItem('tenant_id', tenantId);
        })
      );
  }
  
  private extractTenantFromToken(token: string): string {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.tenant_id; // ou payload.tenantId
  }
  
  getTenantId(): string | null {
    return localStorage.getItem('tenant_id');
  }
}
```

#### 2. Criar HTTP Interceptor

```typescript
// tenant.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable()
export class TenantInterceptor implements HttpInterceptor {
  
  constructor(private authService: AuthService) {}
  
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Obter tenant_id do storage
    const tenantId = this.authService.getTenantId();
    
    // Se tiver tenant, adiciona header
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

#### 3. Registrar Interceptor

```typescript
// app.config.ts
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors, HTTP_INTERCEPTORS } from '@angular/common/http';
import { TenantInterceptor } from './interceptors/tenant.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TenantInterceptor,
      multi: true
    }
  ]
};
```

### Resultado

Todas as requisições HTTP automaticamente incluem:
```
GET /api/usuarios HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
X-Tenant-ID: empresa-teste          ← Adicionado automaticamente
```

---

## 🔐 Backend: Validação de Tenant

### TenantInterceptor (já implementado)

```java
@Override
public boolean preHandle(HttpServletRequest request, 
                         HttpServletResponse response, 
                         Object handler) {
    String tenantId = request.getHeader("X-Tenant-ID");
    
    if (tenantId != null && !tenantId.isEmpty()) {
        // Valida se tenant existe
        if (!tenantRepository.existsByTenantId(tenantId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        
        // Define tenant no contexto
        TenantContext.setTenantId(tenantId);
        log.debug("Tenant identificado: {}", tenantId);
    } else {
        log.warn("Tenant não identificado na requisição");
        // Decidir: bloquear ou permitir?
    }
    
    return true;
}
```

---

## 🚀 Tela de Administração (Sugestão)

### Componente: Admin Tenants

```typescript
// admin-tenants.component.ts
export class AdminTenantsComponent implements OnInit {
  
  tenants: Tenant[] = [];
  
  constructor(private tenantService: TenantService) {}
  
  ngOnInit() {
    this.loadTenants();
  }
  
  loadTenants() {
    this.tenantService.listarTodos().subscribe(
      tenants => this.tenants = tenants
    );
  }
  
  criarTenant(form: TenantForm) {
    this.tenantService.criar(form).subscribe(
      tenant => {
        this.showSuccess('Tenant criado com sucesso!');
        this.loadTenants();
      },
      error => this.showError('Erro ao criar tenant: ' + error.message)
    );
  }
  
  ativarTenant(tenantId: string) {
    this.tenantService.ativar(tenantId).subscribe(
      () => {
        this.showSuccess('Tenant ativado!');
        this.loadTenants();
      }
    );
  }
  
  suspenderTenant(tenantId: string) {
    this.tenantService.suspender(tenantId).subscribe(
      () => {
        this.showWarning('Tenant suspenso!');
        this.loadTenants();
      }
    );
  }
}
```

### Service

```typescript
// tenant.service.ts
@Injectable({ providedIn: 'root' })
export class TenantService {
  
  private apiUrl = '/admin/tenants';
  
  constructor(private http: HttpClient) {}
  
  listarTodos(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(this.apiUrl);
  }
  
  buscar(tenantId: string): Observable<Tenant> {
    return this.http.get<Tenant>(`${this.apiUrl}/${tenantId}`);
  }
  
  criar(form: CriarTenantRequest): Observable<Tenant> {
    return this.http.post<Tenant>(this.apiUrl, form);
  }
  
  ativar(tenantId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${tenantId}/ativar`, {});
  }
  
  suspender(tenantId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${tenantId}/suspender`, {});
  }
}
```

---

## ✅ Checklist de Implementação Frontend

- [ ] Criar `auth.service.ts` para extrair tenant do JWT
- [ ] Criar `tenant.interceptor.ts` para adicionar header X-Tenant-ID
- [ ] Registrar interceptor no `app.config.ts`
- [ ] Criar `tenant.service.ts` para gerenciar tenants
- [ ] Criar componente `admin-tenants.component` (tela de admin)
- [ ] Criar formulário de criação de tenant
- [ ] Adicionar listagem de tenants com status
- [ ] Implementar ações: ativar, suspender, visualizar
- [ ] Adicionar guard para rotas administrativas
- [ ] Testar isolamento de dados entre tenants

---

## 🧪 Testando a Integração

### 1. Criar Tenant via Script
```bash
./test-tenant.sh empresa-teste "Empresa Teste" "12.345.678/0001-90"
```

### 2. Fazer Login no Frontend
- Login: `admin`
- Senha: `admin123`
- Tenant é detectado e armazenado

### 3. Navegar pela Aplicação
- Todas as requisições incluem `X-Tenant-ID: empresa-teste`
- Dados são isolados automaticamente

### 4. Criar Segundo Tenant
```bash
./test-tenant.sh outra-empresa "Outra Empresa" "98.765.432/0001-10"
```

### 5. Fazer Login com Outro Tenant
- Login: `admin`
- Senha: `admin123`
- Header: `X-Tenant-ID: outra-empresa`
- Dados são completamente isolados!

---

## 📊 Diagrama de Fluxo Completo

```
┌─────────────┐
│  Frontend   │
│   Angular   │
└──────┬──────┘
       │
       │ 1. Login (email/senha)
       ↓
┌──────────────────┐
│  POST /auth/login│
└──────┬───────────┘
       │
       │ 2. Backend valida
       │    Retorna JWT com tenant_id
       ↓
┌─────────────────────┐
│  JWT: { sub: "admin",│
│   tenant_id: "emp1" }│
└──────┬──────────────┘
       │
       │ 3. Frontend extrai tenant_id
       │    Salva em localStorage
       ↓
┌────────────────────┐
│ TenantInterceptor  │
│ Adiciona header:   │
│ X-Tenant-ID: emp1  │
└──────┬─────────────┘
       │
       │ 4. Requisições normais
       │    GET /api/usuarios
       ↓
┌──────────────────────┐
│ Backend Interceptor  │
│ TenantInterceptor    │
│ - Lê header          │
│ - TenantContext.set()│
└──────┬───────────────┘
       │
       │ 5. Hibernate
       │    TenantConnectionProvider
       ↓
┌─────────────────────────┐
│ SET search_path TO      │
│   tenant_emp1, public   │
└──────┬──────────────────┘
       │
       │ 6. Query no schema correto
       ↓
┌─────────────────────────┐
│ SELECT * FROM usuario   │
│ ↓ busca em tenant_emp1  │
└─────────────────────────┘
```

---

## 🎯 Próximos Passos

1. **Implementar JWT com tenant_id**: Incluir tenant no token JWT durante login
2. **Criar interceptor Angular**: Adicionar header X-Tenant-ID automaticamente
3. **Tela de Admin**: Interface para gerenciar tenants
4. **Guard de Admin**: Proteger rotas administrativas
5. **Subdomain routing**: `empresa1.gestao-integrada-pipa.com` → auto-detecta tenant
6. **Billing**: Integrar com sistema de pagamentos
7. **Métricas**: Dashboard de uso por tenant

---

## 🐛 Troubleshooting

### Erro: Tabela não encontrada
```
ERROR: relation "usuario" does not exist
```
**Causa**: Schema não foi criado ou template não foi executado
**Solução**: Verificar logs do TenantService.criarTenant()

### Erro: Dados de outro tenant aparecem
```
Vejo usuários que não deveria ver
```
**Causa**: Header X-Tenant-ID não está sendo enviado
**Solução**: Verificar TenantInterceptor do Angular

### Erro: Tenant não encontrado
```
403 Forbidden - Tenant não identificado
```
**Causa**: Tenant_id inválido ou não existe
**Solução**: Verificar tabela public.tenant

---

**📖 Documentação Completa**: [MULTI-TENANT.md](./MULTI-TENANT.md)
