# CLAUDE.md — Gestão Integrada Pipa

Sistema multi-tenant de gestão integrada com backend Spring Boot (Java 22) e frontend Angular 19.

## Stack e Estrutura

**Multi-tenant:** Schema-per-tenant no PostgreSQL. Todo request HTTP **deve** incluir o header `X-Tenant-ID`
(validado pelo `TenantFilter` antes do Spring Security). O JWT contém `tenant_id` para prevenir acesso cruzado.

**Stack:**
- Backend: Spring Boot 3.5.8, Java 22, JPA/Hibernate, Flyway, PostgreSQL, JWT (RS256)
- Frontend: Angular 19, PrimeNG 19, TypeScript 5.5, i18n (pt-BR/en-US)
- Infra: Docker, Nginx reverse proxy

**Estrutura do projeto:**

```
src/backend/src/main/java/br/com/grupopipa/gestaointegrada/
  ├── cadastro/      # Domínio de cadastros (pessoa, usuario, perfil, unidadenegocio)
  ├── financeiro/    # Domínio financeiro (titulo, planocontas, contabancaria)
  ├── tenant/        # Infraestrutura multi-tenancy
  └── core/          # Base compartilhada (entities, DTOs, services, valueobjects, exceptions)

src/frontend/src/app/
  ├── components/base/        # Componentes reutilizáveis (guards, interceptors, base services)
  ├── components/cadastro/    # Features de cadastro
  ├── components/financeiro/  # Features financeiras
  └── model/                  # Models e DTOs compartilhados
```

## Arquivos de Referência (LEIA ANTES DE IMPLEMENTAR)

| Área | Arquivo |
|------|---------|
| Criar nova entidade | **`ENTITY_CREATE_PROMPT.md`** — template completo backend+frontend+migrations |
| Multi-tenancy | **`MULTI-TENANT-ARCHITECTURE.md`** — arquitetura, fluxo, troubleshooting |
| Unidade de Negócio | **`MULTI-TENANT.md`** — filtro automático via `UnidadeNegocioFiltravel` |
| Header X-Tenant-ID | **`TENANT-HEADER.md`** — header obrigatório, rotas públicas, interceptor |
| Migrations (evolução) | **`MIGRATION-EVOLUTION-GUIDE.md`** — evolução segura de schema multi-tenant |
| Formatação de código | **`GUIA-FORMATACAO.md`** — máx 120 chars/linha, EditorConfig, Checkstyle |
| Dialog components | `src/frontend/src/app/components/base/dialog/DIALOG-USAGE.md` |
| Entity Search component | `src/frontend/src/app/components/base/entity-search/ENTITY-SEARCH-USAGE.md` |
| Deploy produção | `DEPLOY.md` |
| Deploy local | `DEPLOY-LOCAL.md` |
| CI/CD | `CI-CD.md` |

## Regras Críticas (NUNCA VIOLAR)

### Backend

- **IDs:** SEMPRE `UUID` (via `BaseEntity`). NUNCA `Long`/`BigSerial`.
- **Entidades:** SEMPRE usar Builder pattern com validação centralizada. NUNCA `new Entity()` diretamente.
- **Value Objects:** SEMPRE usar método fábrica `Nome.of(...)`, `Money.of(...)`. NUNCA `new Nome(...)`.
  - VOs disponíveis: `Nome`, `CPF`, `CNPJ`, `Email`, `PhoneNumber`, `Money` (pacote `core.valueobject`)
- **Validações:** SEMPRE `BeanValidationException` com `BeanValidationMessage`. NUNCA `IllegalArgumentException` com mensagem hardcoded.
- **Constraints DB:** SEMPRE nomear (`uk_<tabela>_<campo>`, `fk_<tabela>_<ref>`, `ck_<tabela>_<campo>`) E registrar no `DatabaseConstraintsEnum`.
- **Migrations tenant:** SEMPRE em `db/tenant-migrations/` (NUNCA em `db/migration/`). SEMPRE idempotentes (`IF NOT EXISTS`).
- **Entity packages:** SEMPRE adicionar novo pacote `entity` em `DataSourceConfig.ENTITY_PACKAGES` (sem isso testes falham com "Not a managed type").
- **Soft delete:** Filtrar com `getDeleted() == null || !getDeleted()` em campos `@Transient`.
- **Imports:** SEMPRE no topo do arquivo. NUNCA FQCNs inline.
- **Checkstyle:** máx 120 caracteres/linha. Quebrar `extends` longas se necessário.
- **Auto-referência no Builder:** NUNCA validar `this.equals(outro)` no `validate()` — o objeto ainda não existe. Use `@PrePersist/@PreUpdate` para isso.

### Frontend

- **DTOs:** SEMPRE classes com `@Exclude`/`@Expose` do `class-transformer`. NUNCA interfaces.
- **Services:** SEMPRE estender `BaseService<DTO, GridDTO>`. NUNCA reimplementar `list()`, `save()`, `findById()`, `delete()`.
- **`plainToInstance`:** OBRIGATÓRIO nos métodos `convertToDto()` e `convertToGrid()` para aplicar os decorators `@Transform`.
- **Campos desabilitados:** Controlar via TypeScript (`form.get('campo')?.disable()`). NUNCA `[disabled]="true"` no HTML com `formControlName`.
- **Testes:** O projeto usa **Jest** (não Karma/Jasmine). Usar `.overrideComponent()` para mockar services component-level.
- **Prefixos:** Componentes usam `gi-` (kebab-case). Diretivas usam `gi` (camelCase).

## Padrões de Código

### Entidade (Backend)

A validação **sempre** fica em uma classe separada `*Validator.java` — NUNCA como inner class da entidade.

```java
// MinhaEntidadeValidator.java (classe separada, no mesmo pacote pai da entidade)
public class MinhaEntidadeValidator {
    private MinhaEntidadeValidator() {}

    public static ValidatedData validate(String nomeStr) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
        if (!violations.isEmpty()) throw new BeanValidationException("minhaEntidade", violations);
        return new ValidatedData(nome);
    }

    public static class ValidatedData {
        public final Nome nome;
        ValidatedData(Nome nome) { this.nome = nome; }
    }
}
```

```java
// MinhaEntidade.java
@Entity
@Table(name = "minha_entidade")
public class MinhaEntidade extends BaseEntity {
    @Embedded
    private Nome nome;  // Value Object

    private MinhaEntidade(MinhaEntidadeValidator.ValidatedData data) { this.nome = data.nome; }
    protected MinhaEntidade() {} // JPA

    public void atualizar(String nomeStr) {
        MinhaEntidadeValidator.ValidatedData data = MinhaEntidadeValidator.validate(nomeStr);
        this.nome = data.nome;
    }

    public String getNome() { return nome != null ? nome.getValue() : null; } // getter retorna String

    public static class Builder {
        private String nome;
        public Builder nome(String nome) { this.nome = nome; return this; }
        public MinhaEntidade build() {
            return new MinhaEntidade(MinhaEntidadeValidator.validate(this.nome));
        }
    }
}
```

### Service (Backend)

```java
@Service
@Transactional(readOnly = true)
public class MinhaEntidadeServiceImpl
        extends CrudServiceImpl<MinhaEntidadeDTO, MinhaEntidadeGridDTO,
                                MinhaEntidade, MinhaEntidadeRepository>
        implements MinhaEntidadeService {

    @Override
    protected MinhaEntidade mergeEntityAndDTO(MinhaEntidade entity, MinhaEntidadeDTO dto) {
        if (Objects.isNull(entity)) return criarNovaMinhaEntidade(dto);
        return atualizarMinhaEntidade(entity, dto);
    }

    // Cada método faz UMA coisa (Clean Code)
    private MinhaEntidade criarNovaMinhaEntidade(MinhaEntidadeDTO dto) {
        return new MinhaEntidade.Builder().nome(dto.getNome()).build();
    }

    private MinhaEntidade atualizarMinhaEntidade(MinhaEntidade entity, MinhaEntidadeDTO dto) {
        entity.atualizar(dto.getNome());
        return entity;
    }
}
```

### BeanValidationMessage e i18n

Chaves seguem o formato `entityCamel.field.type` (3 partes):
```java
// Gerado pelo BeanValidationException:
violations.add(new BeanValidationMessage("nome", "Nome é obrigatório"));
// → RestExceptionHandler monta: "minhaEntidade.nome" → frontend busca tradução

// DatabaseConstraintsEnum:
UK_MINHA_ENTIDADE_CODIGO("minhaEntidade.codigo.unique")
// → Frontend: "Código já cadastrado"
```

### DTO Frontend (TypeScript)

```typescript
@Exclude()
export class MinhaEntidadeDTO {
  @Expose() id?: string;
  @Expose() nome: string;

  @Transform((params) => {
    if (params.type === TransformationType.PLAIN_TO_CLASS) return TipoEnum.getByKey(params.value);
    if (params.type === TransformationType.CLASS_TO_PLAIN) return params.value?.key;
    return params.value;
  })
  @Expose() tipo: TipoEnum;

  constructor(nome: string, tipo: TipoEnum, id?: string) {
    this.nome = nome; this.tipo = tipo; this.id = id;
  }
}
```

### Service Frontend

```typescript
@Injectable()
export class MinhaEntidadeService extends BaseService<MinhaEntidadeDTO, MinhaEntidadeGridDTO> {
  private static readonly DOMINIO = 'minha-entidade';

  constructor() {
    super(inject(HttpClient), inject(MessageService), inject(MinhaEntidadeBackendMessageService));
  }

  protected override convertToDto(body: unknown): MinhaEntidadeDTO {
    return plainToInstance(MinhaEntidadeDTO, body as object) as MinhaEntidadeDTO;
  }

  protected override convertToGrid(item: MinhaEntidadeGridDTO): MinhaEntidadeGridDTO {
    return plainToInstance(MinhaEntidadeGridDTO, item as object) as MinhaEntidadeGridDTO;
  }

  getDomain(): string { return MinhaEntidadeService.DOMINIO; }
}
```

### Clean Code nos Componentes Frontend

```typescript
// ngOnInit como orquestrador — sem lógica inline
ngOnInit(): void {
  this.initForm();
  this.createToolbarActions();
  this.loadDependencias();
  if (this.id === 'add') this.prepareForNew();
  else if (this.id) this.prepareForEdit();
}

// salvar() delega para métodos específicos
salvar(): void {
  this.validateBeforeSave();
  this.populateDTOBeforeSend();
  this.service.save(this.dto, { onSuccess: (data) => {
    this.messages.sucesso($localize`Salvo com sucesso.`);
    this.goBackFn();
  }});
}
```

## Auditoria (Obrigatória em todos os Controllers e Grids)

**Controller:**
```java
@Override
@PreAuthorize("hasAuthority('MODULO_ENTIDADE_AUDITAR')")
public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
    return super.getAuditInfo(id);
}
```

**Grid component** deve incluir `AuditInfoComponent` — veja template completo em `ENTITY_CREATE_PROMPT.md`.

## Filtro por Unidade de Negócio

Entidades que precisam de filtro automático por unidade de negócio devem implementar `UnidadeNegocioFiltravel`.
O `CrudServiceImpl` aplica o filtro automaticamente. Veja `MULTI-TENANT.md` para detalhes.

## Migrations

```sql
-- Tenant (db/tenant-migrations/): obrigatoriamente idempotente
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'minha_entidade') THEN
    CREATE TABLE minha_entidade (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      -- campos de auditoria e soft delete vêm do BaseEntity
      deleted BOOLEAN NOT NULL DEFAULT FALSE, deleted_at TIMESTAMP, deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, ...
    );
    -- Índice obrigatório para soft delete
    CREATE INDEX idx_minha_entidade_deleted ON minha_entidade (deleted);
  END IF;
END $$;

-- Módulo (idempotente) - OBRIGATÓRIO em toda nova entidade
INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'MODULO_ENTIDADE', 'Descrição', 'MODULO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'MODULO_ENTIDADE');
```

## Testes

### Backend
- Estender `AbstractIntegrationTest` (`@SpringBootTest`, `@Testcontainers`)
- Valores únicos em runtime: `"test-" + System.nanoTime()`
- Soft delete: mock `findById()` + `save()`, NÃO `deleteById()`
- Asserções com AssertJ: `assertThat(...)`

### Frontend (Jest)
- Framework: **Jest** com `jest-preset-angular`
- Prioridade: Detalhe > Services > Grid (opcional) > Principal (skip)
- Mockar services com `.overrideComponent()` (para component-level providers)
- Limpar mocks com `jest.clearAllMocks()` no `afterEach`
- Sem `as any` — usar `as Response<T>` ou `as unknown as HttpErrorResponse`

## Comandos Essenciais

```bash
# Backend (de src/backend/)
./mvnw spring-boot:run          # Dev (porta 8080)
./mvnw test                     # Todos os testes
./mvnw test -Dtest=MinhaTest    # Teste específico
./mvnw package                  # Build JAR produção

# Frontend (de src/frontend/)
npm install                     # Instalar dependências
ng serve                        # Dev (porta 4200)
npm test                        # Testes Jest
npm run test:watch              # Testes em modo watch
npm run test:coverage           # Cobertura
ng lint -- --fix                # Lint com auto-fix
ng extract-i18n --output-path src/locale  # Extrair strings i18n
```

## Checklist — Nova Entidade

Para criar uma nova entidade do zero, use `ENTITY_CREATE_PROMPT.md` como template completo. Resumo:

- [ ] `Entity.java` (Builder + ValidatedData + validate + atualizar)
- [ ] Migration SQL idempotente em `db/tenant-migrations/`
- [ ] `DatabaseConstraintsEnum` — registrar TODAS as constraints
- [ ] `DataSourceConfig.ENTITY_PACKAGES` — adicionar pacote da entidade
- [ ] `DTO.java` + `GridDTO.java` (Lombok @Builder + @Data, GridDTO com campo `deleted`)
- [ ] `Repository`, `Service`, `ServiceImpl`, `Controller` (com `@PreAuthorize`)
- [ ] Frontend: DTOs (classes), Service, BackendMessageService
- [ ] Frontend: componentes `main`, `grid` (com AuditInfo), `detalhe`
- [ ] `SystemModuleKey` enum + rotas + menu
- [ ] i18n: `messages.xlf` (pt-BR) e `messages.en.xlf` (en-US)
- [ ] Testes backend (Repository + Service)
- [ ] Testes frontend (Detalhe: inicialização + validação + salvamento)
