# ENTITY GENERATION PROMPT - Gestão Integrada Pipa

## PRINCIPAIS CORREÇÕES (IMPORTANTE - leia antes de usar)

Resumo rápido das lições aprendidas ao gerar entidades neste repositório:

### Backend (Java)

- **Imports**: Use imports no topo dos arquivos — NÃO embuta FQCNs inline. Exemplo:
  ```java
  import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
  ```

- **Value Objects**: NÃO use construtores diretos. Sempre use o método fábrica `of` quando o VO existir (ex.: `Nome.of(value)`). Consulte `src/backend/src/main/java/br/com/grupopipa/gestaointegrada/core/valueobject`.

- **DTOs obrigatórios**: Gere sempre `{{EntityName}}DTO` e `{{EntityName}}GridDTO` no pacote backend com Lombok `@Builder` e `@Data`.

- **Serviços**:
  - Interface: estenda `CrudService<DTO, GridDTO>`
  - Implementação: estenda `CrudServiceImpl<DTO, GridDTO, Entity, Repository>` e implemente `mergeEntityAndDTO` e `buildDTOFromEntity`

- **Mensagens de validação**: Use `BeanValidationMessage` com **chave** e **mensagem curta**. As chaves são usadas no frontend para i18n (ex.: `centroCusto.nome.notBlank`).

- **Entity Packages & DataSourceConfig**:
  - Coloque entidades em pacote dedicado `entity`: `br.com.grupopipa.gestaointegrada.{domain}.{entityPackage}.entity`
  - NÃO misture DTOs, services ou controllers no pacote `entity`
  - **IMPORTANTE**: Adicione o novo pacote à constante `ENTITY_PACKAGES` em `DataSourceConfig`:
    ```java
    "br.com.grupopipa.gestaointegrada.{domain}.{entityPackage}.entity",
    ```
  - Sem esse passo, os testes falharão com "Not a managed type"

### Migrations (Tenant)

- **Idempotência obrigatória**: Use `IF NOT EXISTS` ou blocos `DO $$ BEGIN ... EXCEPTION WHEN others THEN ... END $$`
- **Localização**: `src/main/resources/db/tenant-migrations/`
- **Naming de constraints** (obrigatório):
  - UNIQUE: `uk_<table>_<field>`
  - FOREIGN KEY: `fk_<table>_<referenced_table>`
  - CHECK: `ck_<table>_<field>` ou `chk_<table>_<field>`

### DatabaseConstraintsEnum ⚠️ CRÍTICO

**OBRIGATÓRIO**: Toda constraint criada DEVE ser registrada em `DatabaseConstraintsEnum.java`

- **Localização**: `src/backend/src/main/java/br/com/grupopipa/gestaointegrada/core/dao/DatabaseConstraintsEnum.java`
- **Sem esse registro**: Erros de constraint retornarão mensagem genérica ao usuário
- **Padrão do enum**: `UK_CENTRO_CUSTO_NOME("centroCusto.nome.unique")`
- **Fluxo**: Constraint violada → Enum mapeia → Frontend exibe mensagem traduzida
- **Ver seção completa** sobre DatabaseConstraintsEnum na seção de Migrations do prompt

### Frontend (Angular + TypeScript)

#### Estrutura de Diretórios

```
src/frontend/src/app/components/{{domain}}/{{entity}}/
├── {{entity}}.component.ts/html/css (orquestrador principal)
├── grid/
│   └── {{entity}}-grid.component.ts/html/css
├── detalhe/
│   └── {{entity}}-detalhe.component.ts/html/css
├── model/
│   ├── {{entity}}.dto.ts
│   ├── {{entity}}-grid.dto.ts
│   └── {{entity}}-tipo.enum.ts (se aplicável)
├── {{entity}}.service.ts
└── {{entity}}-backend-message.service.ts
```

#### DTOs TypeScript (CRÍTICO - Nova Estrutura)

**SEMPRE use CLASSES (não interfaces) com decorators do `class-transformer`:**

```typescript
import { Exclude, Expose, Transform, TransformationType, TransformFnParams } from 'class-transformer';

@Exclude()
export class {{Entity}}DTO {
  @Expose()
  id?: string;

  @Expose()
  nome: string;

  @Expose()
  descricao?: string;

  // Para campos enum, use @Transform para conversão bidirecional
  @Transform((params: TransformFnParams) => {
    const { type, value } = params;

    if (TransformationType.PLAIN_TO_CLASS === type) {
      return {{Entity}}TipoEnum.getByKey(value);
    }

    if (TransformationType.CLASS_TO_PLAIN === type) {
      return value.key;
    }

    return value;
  })
  @Expose()
  tipo: {{Entity}}TipoEnum;

  constructor(nome: string, tipo: {{Entity}}TipoEnum, descricao?: string, id?: string) {
    this.nome = nome;
    this.tipo = tipo;
    this.descricao = descricao;
    this.id = id;
  }
}
```

**Por que usar classes com `class-transformer`?**
- ✅ Transformação automática de enums (string ↔ objeto)
- ✅ Controle de exposição de campos (`@Exclude`/`@Expose`)
- ✅ Instâncias reais de classe (não apenas type cast)
- ✅ Serialização/deserialização bidirecional
- ✅ Type safety em runtime (não apenas compile-time)

#### Enums TypeScript

```typescript
export class {{Entity}}TipoEnum {
  public static readonly OPCAO1 = new {{Entity}}TipoEnum(
    'OPCAO1',
    $localize`Opção 1`
  );
  public static readonly OPCAO2 = new {{Entity}}TipoEnum(
    'OPCAO2',
    $localize`Opção 2`
  );

  constructor(public readonly key: string, public readonly label: string) {}

  public static getList(): {{Entity}}TipoEnum[] {
    return [this.OPCAO1, this.OPCAO2];
  }

  public static getByKey(key: string): {{Entity}}TipoEnum | undefined {
    return {{Entity}}TipoEnum.getList().find((tipo) => tipo.key === key);
  }
}
```

#### Serviços Frontend

**SEMPRE estenda `BaseService<DTO, GridDTO>` e implemente os métodos abstratos de conversão:**

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { {{Entity}}BackendMessageService } from './{{entity}}-backend-message.service';
import { BaseService } from '../../base/base-service';
import { {{Entity}}DTO } from './model/{{entity}}.dto';
import { {{Entity}}GridDTO } from './model/{{entity}}-grid.dto';

@Injectable()
export class {{Entity}}Service extends BaseService<{{Entity}}DTO, {{Entity}}GridDTO> {
  private static readonly DOMINIO = '{{entity-kebab}}';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject({{Entity}}BackendMessageService)
    );
  }

  // OBRIGATÓRIO: Conversão DTO (com plainToInstance para transformações)
  protected override convertToDto(body: unknown): {{Entity}}DTO {
    return plainToInstance({{Entity}}DTO, body as object) as {{Entity}}DTO;
  }

  // OBRIGATÓRIO: Conversão GridDTO (com plainToInstance para transformações)
  protected override convertToGrid(item: {{Entity}}GridDTO): {{Entity}}GridDTO {
    return plainToInstance({{Entity}}GridDTO, item as object) as {{Entity}}GridDTO;
  }

  getDomain(): string {
    return {{Entity}}Service.DOMINIO;
  }

  // Adicione métodos customizados apenas se necessário
  // NÃO reimplemente list(), save(), findById(), delete()
}
```

**IMPORTANTE**:
- NÃO reimplemente métodos CRUD básicos (`list`, `save`, `findById`, `delete`)
- Eles já estão implementados no `BaseService`
- `plainToInstance` é **essencial** para aplicar decorators `@Transform` e criar instâncias reais de classe

#### Contratos de Componentes do Frontend

**Grid:**
- Usa `gi-app-base` com `body-content` e `footer-content`
- `gi-filter-component`: `[filters]` (FilterProperty[]), `[hidden]`, emite `(filterEvent)` e `(cancelEvent)`
- `gi-table-component`: `[columns]` (ColumnModel[]), `[data]`, `[actions]`, emite `(sortingEvent)`
- `gi-pagination-component`: `[itemsPerPage]`, `[totalElements]`, emite `(paginationEvent)`

**Detalhe:**
- Usa `gi-app-base` com `[actions]` ou `[goBackFn]`
- Ações (Salvar/Cancelar) inicializadas no TS e passadas via `[actions]`
- NÃO coloque botões brutos dentro de `footer-content`
- `ngOnInit`: se `id` presente, carregar via `service.findById(id)`
- `save()`: chamar `service.save(dto, { onSuccess })` - NÃO forneça `onError` (já tratado pelo BaseService)

#### Response Types

Use os tipos definidos em `src/frontend/src/app/components/base/model/response.ts`:

```typescript
// Para findById
this.service.findById(id).subscribe((response: Response<MyDTO>) => {
  if (response.body) {
    this.form.patchValue(response.body);
  }
});

// Para save
this.service.save(dto, {
  onSuccess: (data: MyDTO) => {
    this.messageService.sucesso('Salvo com sucesso!');
    this.goBack();
  }
  // onError é opcional - BaseService já trata
});
```

### i18n

- Marque strings com `i18n` no HTML: `<label i18n>Nome</label>`
- NÃO use pipe `translate` para rótulos estáticos
- Adicione `trans-unit` em `src/frontend/src/app/locale/messages.xlf` (pt-BR) e `messages.en.xlf` (en-US)
- Inclua chaves de validação do backend (ex.: `{{entity}}.{{field}}.notBlank`)

### Testes

#### Backend (Java)

- **NÃO use valores determinísticos** para campos `codigo`
- Use geração runtime: `"test-" + System.nanoTime()` ou `UUID.randomUUID().toString().substring(0, 12)`
- **Testes de integração**: persista entidades pai (FKs) antes da entidade filha
- **Testes unitários**: use `UUID` concreto para IDs, mocqueie `findById()` para entidades referenciadas
- Estenda `AbstractIntegrationTest` quando disponível
- Use AssertJ (`assertThat(...)`) para asserções

#### Frontend (Jest)

**Configuração**:
- Framework: Jest com `jest-preset-angular`
- Arquivo de setup: `setup-jest.ts` (polyfills e mocks globais)
- Comando: `npm test` ou `npm run test:watch`

**Estrutura de Testes**:
```typescript
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';

describe('{{Entity}}DetalheComponent', () => {
  let component: {{Entity}}DetalheComponent;
  let fixture: ComponentFixture<{{Entity}}DetalheComponent>;
  let service: jest.Mocked<{{Entity}}Service>;
  let messageService: jest.Mocked<MessageService>;

  beforeEach(async () => {
    const serviceMock = {
      findById: jest.fn(),
      save: jest.fn(),
      // outros métodos mockados
    };

    const messageServiceMock = {
      sucesso: jest.fn(),
      erro: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [{{Entity}}DetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
      ],
    })
    .overrideComponent({{Entity}}DetalheComponent, {
      set: {
        providers: [{ provide: {{Entity}}Service, useValue: serviceMock }]
      }
    })
    .compileComponents();

    fixture = TestBed.createComponent({{Entity}}DetalheComponent);
    component = fixture.componentInstance;
    service = fixture.debugElement.injector.get({{Entity}}Service) as jest.Mocked<{{Entity}}Service>;
    messageService = TestBed.inject(MessageService) as jest.Mocked<MessageService>;
  });

  afterEach(() => {
    jest.clearAllMocks();
  });
});
```

**O que testar**:

1. **Inicialização**: Criação do componente, formulário, toolbar
2. **Carregamento de dados**: Comboboxes, autocomplete, findById
3. **Validações**: Mensagens de erro quando campos obrigatórios vazios
4. **Salvamento**:
   - Sucesso: verificar dados enviados, mensagem de sucesso, navegação
   - Erro: verificar mensagens de validação exibidas
5. **Interações**: Filtros, seleção de itens, mudanças de valores

**Exemplo de testes de validação**:
```typescript
describe('Validações ao Salvar', () => {
  it('NÃO deve salvar se campo obrigatório está vazio', () => {
    component.form.patchValue({ nome: '' });
    component.salvar();

    expect(messageService.erro).toHaveBeenCalled();
    const callArgs = messageService.erro.mock.calls[0][0];
    expect(callArgs).toContain('obrigatório');
    expect(service.save).not.toHaveBeenCalled();
  });
});
```

**Exemplo de teste de sucesso**:
```typescript
describe('Salvamento com Sucesso', () => {
  it('deve salvar e exibir mensagem', () => {
    component.form.patchValue({ nome: 'Test', codigo: '001' });

    service.save.mockImplementation(
      (_data: {{Entity}}DTO, callbacks: ExecutionCallbacks<{{Entity}}DTO>) => {
        if (callbacks.onSuccess) {
          callbacks.onSuccess({ id: 'test-id' } as {{Entity}}DTO);
        }
      }
    );

    component.salvar();

    expect(service.save).toHaveBeenCalled();
    expect(messageService.sucesso).toHaveBeenCalled();
  });
});
```

**Mocking de serviços com component-level providers**:
- Use `.overrideComponent()` para substituir providers declarados no componente
- Isso é necessário quando o serviço está em `providers: [...]` do `@Component`

**Boas práticas**:
- Use `jest.clearAllMocks()` no `afterEach` para evitar interferência entre testes
- Verifique chamadas de métodos com `toHaveBeenCalled()` e `toHaveBeenCalledWith()`
- Para verificar conteúdo de strings, use `.toContain()` em vez de matchers complexos
- Para objetos aninhados, use verificação condicional (`if (obj.prop)`) para evitar erros de undefined
- Nomeie testes descritivamente: "deve...", "NÃO deve..."

---

## PROMPT PRINCIPAL (cole inteiro no gerador)

```
Você é um assistente gerador de código para o repositório "Gestão Integrada Pipa".
Gere um conjunto completo de arquivos necessários para uma nova entidade de domínio,
obedecendo estritamente as convenções abaixo.

Sua saída deve ser um JSON onde cada chave é o caminho relativo do arquivo no
repositório e o valor é o conteúdo (string). NÃO execute comandos no sistema.

## 1. BACKEND (Java)

### Entidade
- Pacote: `br.com.grupopipa.gestaointegrada.{domain}.{entityPackage}.entity`
- Estender `BaseEntity` (fornece UUID id, createdAt, updatedAt, createdBy, updatedBy, deleted, deletedAt, deletedBy)
- Usar Builder pattern com validação centralizada
- Método `validate()` privado retornando `ValidatedData`
- Lançar `BeanValidationException` com `BeanValidationMessage` em violações
- Value Objects: usar método fábrica `Nome.of(...)`, não `new Nome(...)`
- **Soft Delete**: BaseEntity já implementa soft delete - NÃO adicionar campos manualmente

```java
@Entity
@Table(name = "{{table_name}}")
public class {{EntityName}} extends BaseEntity {

    @Embedded
    private Nome nome;

    @Column(name = "codigo", length = 20, nullable = false)
    private String codigo;

    // Construtor privado (apenas Builder)
    private {{EntityName}}(Nome nome, String codigo) {
        this.nome = nome;
        this.codigo = codigo;
    }

    protected {{EntityName}}() {} // JPA

    private static class ValidatedData {
        final Nome nome;
        final String codigo;

        ValidatedData(Nome nome, String codigo) {
            this.nome = nome;
            this.codigo = codigo;
        }
    }

    private static ValidatedData validate(String nomeStr, String codigo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        // IMPORTANTE: Use ValidationUtils.validateAndGet para Value Objects
        // Isso captura BeanValidationExceptions e adiciona ao set de violations
        Nome nome = ValidationUtils.validateAndGet(
            () -> Nome.of(nomeStr), violations
        );

        // Validações simples podem ser inline
        if (codigo == null || codigo.isBlank()) {
            violations.add(new BeanValidationMessage("codigo", "Código é obrigatório"));
        }

        // Para VOs opcionais como CNPJ, use o mesmo padrão:
        // CNPJ cnpj = null;
        // if (cnpjStr != null && !cnpjStr.isBlank()) {
        //     cnpj = ValidationUtils.validateAndGet(() -> new CNPJ(cnpjStr), violations);
        // }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("{{entityCamel}}", violations);
        }

        return new ValidatedData(nome, codigo);
    }

    public void atualizar(String nomeStr, String codigo) {
        ValidatedData data = validate(nomeStr, codigo);
        this.nome = data.nome;
        this.codigo = data.codigo;
    }

    // Getters retornam String (não expõe VOs)
    public String getNome() {
        return nome != null ? nome.getValue() : null;
    }

    public String getCodigo() {
        return codigo;
    }

    public static class Builder {
        private String nome;
        private String codigo;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public {{EntityName}} build() {
            ValidatedData data = validate(this.nome, this.codigo);
            return new {{EntityName}}(data.nome, data.codigo);
        }
    }
}
```

### DTOs Backend
```java
@Data
@Builder
public class {{EntityName}}DTO {
    private UUID id;
    private String nome;
    private String codigo;
    // Adicione outros campos conforme necessário
}

@Data
@Builder
public class {{EntityName}}GridDTO implements GridDTO {
    private UUID id;
    private String nome;
    private String codigo;
    private Boolean deleted; // OBRIGATÓRIO para soft delete visual
    // Campos para exibição em grid (pode ser subset do DTO)
}
```

**IMPORTANTE**: GridDTO DEVE implementar interface `GridDTO` e incluir campo `deleted` para destaque visual de registros excluídos.

### Repository
```java
public interface {{EntityName}}Repository extends
    JpaRepository<{{EntityName}}, UUID>,
    JpaSpecificationExecutor<{{EntityName}}> {

    Optional<{{EntityName}}> findByCodigo(String codigo);
}
```

### Service
```java
public interface {{EntityName}}Service extends
    CrudService<{{EntityName}}DTO, {{EntityName}}GridDTO> {
    // Métodos customizados se necessário
}

@Service
@Transactional(readOnly = true)
public class {{EntityName}}ServiceImpl extends
    CrudServiceImpl<{{EntityName}}DTO, {{EntityName}}GridDTO, {{EntityName}}, {{EntityName}}Repository>
    implements {{EntityName}}Service {

    @Override
    protected {{EntityName}} mergeEntityAndDTO({{EntityName}} entity, {{EntityName}}DTO dto) {
        if (Objects.isNull(entity)) {
            return new {{EntityName}}.Builder()
                .nome(dto.getNome())
                .codigo(dto.getCodigo())
                .build();
        }
        entity.atualizar(dto.getNome(), dto.getCodigo());
        return entity;
    }

    @Override
    protected {{EntityName}}DTO buildDTOFromEntity({{EntityName}} entity) {
        return {{EntityName}}DTO.builder()
            .id(entity.getId())
            .nome(entity.getNome())
            .codigo(entity.getCodigo())
            .build();
    }

    @Override
    protected {{EntityName}}GridDTO buildGridDTOFromEntity({{EntityName}} entity) {
        return {{EntityName}}GridDTO.builder()
            .id(entity.getId())
            .nome(entity.getNome())
            .codigo(entity.getCodigo())
            .deleted(entity.getDeleted()) // OBRIGATÓRIO para soft delete
            .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "codigo"); // Adicionar campos filtráveis
    }
}
```

### Controller
```java
@RestController
@RequestMapping("/{{entity-kebab}}")
public class {{EntityName}}Controller extends
    BaseController<{{EntityName}}DTO, {{EntityName}}GridDTO, {{EntityName}}Service> {

    @Override
    @PreAuthorize("hasAuthority('{{MODULE}}_{{ENTITY_UPPER}}_LISTAR')")
    public ResponseEntity<Response<PageResponse<{{EntityName}}GridDTO>>> query(
        @RequestBody PageRequest filterPageRequest) {
        return super.query(filterPageRequest);
    }

    @Override
    @PreAuthorize("hasAuthority('{{MODULE}}_{{ENTITY_UPPER}}_VISUALIZAR')")
    public ResponseEntity<Response<{{EntityName}}DTO>> findById(@RequestParam UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('{{MODULE}}_{{ENTITY_UPPER}}_EDITAR')")
    public ResponseEntity<Response<{{EntityName}}DTO>> save(@RequestBody {{EntityName}}DTO dto) {
        return super.save(dto);
    }

    @Override
    @PreAuthorize("hasAuthority('{{MODULE}}_{{ENTITY_UPPER}}_DELETAR')")
    public ResponseEntity<ResponseString> delete(@PathVariable UUID id) {
        return super.delete(id);
    }
}
```

## 2. MIGRATIONS (Tenant)

**Arquivo**: `src/main/resources/db/tenant-migrations/V{timestamp}__create_{{table_name}}_table.sql`

```sql
-- Idempotente: verifica existência antes de criar
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_schema = current_schema()
                   AND table_name = '{{table_name}}') THEN

        CREATE TABLE {{table_name}} (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            nome VARCHAR(255) NOT NULL,
            codigo VARCHAR(20) NOT NULL,

            -- Campos de auditoria (BaseEntity)
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP,
            created_by VARCHAR(255),
            updated_by VARCHAR(255),

            -- Campos de soft delete (BaseEntity) - OBRIGATÓRIOS
            deleted BOOLEAN NOT NULL DEFAULT FALSE,
            deleted_at TIMESTAMP,
            deleted_by VARCHAR(255)
        );

        -- Constraints nomeadas
        ALTER TABLE {{table_name}}
            ADD CONSTRAINT uk_{{table_name}}_codigo UNIQUE (codigo);

        -- Índice para soft delete (melhora performance de queries com filtro deleted)
        CREATE INDEX idx_{{table_name}}_deleted ON {{table_name}} (deleted);

        -- Se tiver FK
        -- ALTER TABLE {{table_name}}
        --     ADD CONSTRAINT fk_{{table_name}}_unidade_negocio
        --     FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio(id);

    END IF;
END $$;

-- Inserir módulo no sistema (idempotente)
INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), '{{MODULE}}_{{ENTITY_UPPER}}', '{{EntityDescription}}', '{{MODULE}}'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = '{{MODULE}}_{{ENTITY_UPPER}}');

-- Vincular ao perfil Administrador Geral (idempotente)
INSERT INTO perfil_modulo (
    id, perfil_id, modulo_id,
    pode_listar, pode_visualizar, pode_editar, pode_deletar, pode_auditar,
    created_at, created_by
)
SELECT
    gen_random_uuid(),
    p.id,
    m.id,
    TRUE, TRUE, TRUE, TRUE, TRUE, -- pode_auditar = TRUE para admin
    CURRENT_TIMESTAMP,
    'migration'
FROM perfil p
CROSS JOIN modulo m
WHERE p.nome = 'Administrador Geral'
  AND m.chave = '{{MODULE}}_{{ENTITY_UPPER}}'
  AND NOT EXISTS (
    SELECT 1 FROM perfil_modulo pm
    WHERE pm.perfil_id = p.id AND pm.modulo_id = m.id
  );
```

### DatabaseConstraintsEnum

**CRÍTICO**: Toda constraint criada na migration DEVE ser registrada no enum `DatabaseConstraintsEnum`.

**Localização**: `src/backend/src/main/java/br/com/grupopipa/gestaointegrada/core/dao/DatabaseConstraintsEnum.java`

**Padrão de nomenclatura**:
- Enum: `UK_{{TABLE_NAME}}_{{FIELD_NAME}}` (para UNIQUE)
- Enum: `FK_{{TABLE_NAME}}_{{REFERENCED_TABLE}}` (para FOREIGN KEY)
- Enum: `CHK_{{TABLE_NAME}}_{{FIELD_NAME}}` (para CHECK)
- MessageKey: `{{entityCamel}}.{{field}}.{{type}}` (ex: `centroCusto.nome.unique`)

**Exemplo**:
```java
// Após criar constraint na migration:
// ALTER TABLE centro_custo ADD CONSTRAINT uk_centro_custo_nome UNIQUE (nome);

// Adicionar no DatabaseConstraintsEnum.java:
// Constraints de Centro de Custo
UK_CENTRO_CUSTO_NOME("centroCusto.nome.unique"),
FK_CENTRO_CUSTO_UNIDADE_NEGOCIO("centroCusto.unidadeNegocio.foreignKey"),
CHK_CENTRO_CUSTO_ATIVO("centroCusto.ativo.invalid"),
```

**Tipos de mensagens comuns**:
- UNIQUE: `.unique` (ex: `centroCusto.codigo.unique`)
- FOREIGN KEY: `.foreignKey` (ex: `titulo.pessoa.foreignKey`)
- CHECK (enum): `.invalid` (ex: `titulo.tipo.invalid`)
- CHECK (valor positivo): `.positive` (ex: `titulo.valor.positive`)
- CHECK (datas): `.after{{Campo}}` (ex: `titulo.dataVencimento.afterEmissao`)

**Fluxo de tratamento de erros**:
1. Constraint violada no banco → `DataIntegrityViolationException`
2. `RestExceptionHandler` captura e extrai nome da constraint
3. `DatabaseConstraintsEnum.getByKey()` mapeia para `userMessageKey`
4. Response JSON inclui `userMessageKey: ["centroCusto.nome.unique"]`
5. Frontend usa `BackendMessageService` para traduzir e exibir

**IMPORTANTE**:
- Se a constraint não estiver no enum, a mensagem será genérica (`errors.internalServerError`)
- SEMPRE adicione a constraint ao enum junto com a migration
- SEMPRE adicione a tradução correspondente no `BackendMessageService` do frontend

## 3. FRONTEND (Angular + TypeScript)

### DTOs TypeScript (CLASSES com decorators)

**{{entity}}.dto.ts**:
```typescript
import { Exclude, Expose, Transform, TransformationType, TransformFnParams } from 'class-transformer';
import { {{Entity}}TipoEnum } from './{{entity}}-tipo.enum';

@Exclude()
export class {{Entity}}DTO {
  @Expose()
  id?: string;

  @Expose()
  nome: string;

  @Expose()
  codigo: string;

  @Expose()
  descricao?: string;

  // Se tiver enum, use @Transform para conversão
  @Transform((params: TransformFnParams) => {
    const { type, value } = params;
    if (TransformationType.PLAIN_TO_CLASS === type) {
      return {{Entity}}TipoEnum.getByKey(value);
    }
    if (TransformationType.CLASS_TO_PLAIN === type) {
      return value.key;
    }
    return value;
  })
  @Expose()
  tipo?: {{Entity}}TipoEnum;

  constructor(nome: string, codigo: string, descricao?: string, tipo?: {{Entity}}TipoEnum, id?: string) {
    this.nome = nome;
    this.codigo = codigo;
    this.descricao = descricao;
    this.tipo = tipo;
    this.id = id;
  }
}
```

**{{entity}}-grid.dto.ts**: Similar ao DTO, mas com campos para grid

### Enum TypeScript

**{{entity}}-tipo.enum.ts**:
```typescript
export class {{Entity}}TipoEnum {
  public static readonly OPCAO1 = new {{Entity}}TipoEnum('OPCAO1', $localize`Opção 1`);
  public static readonly OPCAO2 = new {{Entity}}TipoEnum('OPCAO2', $localize`Opção 2`);

  constructor(public readonly key: string, public readonly label: string) {}

  public static getList(): {{Entity}}TipoEnum[] {
    return [this.OPCAO1, this.OPCAO2];
  }

  public static getByKey(key: string): {{Entity}}TipoEnum | undefined {
    return this.getList().find(t => t.key === key);
  }
}
```

### Service Frontend

**{{entity}}.service.ts**:
```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { {{Entity}}BackendMessageService } from './{{entity}}-backend-message.service';
import { BaseService } from '../../base/base-service';
import { {{Entity}}DTO } from './model/{{entity}}.dto';
import { {{Entity}}GridDTO } from './model/{{entity}}-grid.dto';

@Injectable()
export class {{Entity}}Service extends BaseService<{{Entity}}DTO, {{Entity}}GridDTO> {
  private static readonly DOMINIO = '{{entity-kebab}}';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject({{Entity}}BackendMessageService)
    );
  }

  protected override convertToDto(body: unknown): {{Entity}}DTO {
    return plainToInstance({{Entity}}DTO, body as object) as {{Entity}}DTO;
  }

  protected override convertToGrid(item: {{Entity}}GridDTO): {{Entity}}GridDTO {
    return plainToInstance({{Entity}}GridDTO, item as object) as {{Entity}}GridDTO;
  }

  getDomain(): string {
    return {{Entity}}Service.DOMINIO;
  }
}
```

### Backend Message Service

**{{entity}}-backend-message.service.ts**:
```typescript
import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable()
export class {{Entity}}BackendMessageService extends AbstractBackendMessageService {
  override messages(): Record<string, string> {
    return {
      '{{entityCamel}}.nome.notBlank': $localize`:@@{{entityCamel}}.nome.notBlank:Nome é obrigatório`,
      '{{entityCamel}}.codigo.notBlank': $localize`:@@{{entityCamel}}.codigo.notBlank:Código é obrigatório`,
      '{{entityCamel}}.codigo.unique': $localize`:@@{{entityCamel}}.codigo.unique:Código já cadastrado`,
    };
  }
}
```

### Componentes (Grid, Detalhe, Principal)

Siga o padrão dos componentes existentes:
- Grid: `gi-app-base`, `gi-filter-component`, `gi-table-component`, `gi-pagination-component`
- Detalhe: `gi-app-base` com `[actions]`, ReactiveForm
- Principal: ViewMode (GRID/DETAIL), orquestra grid e detalhe

## 4. i18n

Adicione em `src/frontend/src/app/locale/messages.xlf` e `messages.en.xlf`:

```xml
<!-- messages.xlf (pt-BR) -->
<trans-unit id="{{entityCamel}}.nome.notBlank" datatype="html">
  <source>Nome é obrigatório</source>
  <target>Nome é obrigatório</target>
</trans-unit>

<!-- messages.en.xlf (en-US) -->
<trans-unit id="{{entityCamel}}.nome.notBlank" datatype="html">
  <source>Nome é obrigatório</source>
  <target>Name is required</target>
</trans-unit>
```

## 5. TESTES

### Backend (Java)

#### Repository Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public class {{EntityName}}RepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private {{EntityName}}Repository repository;

    @Test
    void deveSalvarERecuperar{{EntityName}}() {
        String codigo = "test-" + System.nanoTime();

        {{EntityName}} entity = new {{EntityName}}.Builder()
            .nome("Test Nome")
            .codigo(codigo)
            .build();

        {{EntityName}} saved = repository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCodigo()).isEqualTo(codigo);

        Optional<{{EntityName}}> found = repository.findByCodigo(codigo);
        assertThat(found).isPresent();
    }
}
```

#### Service Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public class {{EntityName}}ServiceTest extends AbstractIntegrationTest {

    @Autowired
    private {{EntityName}}Service service;

    @Test
    void deveCriar{{EntityName}}() {
        String codigo = UUID.randomUUID().toString().substring(0, 12);

        {{EntityName}}DTO dto = {{EntityName}}DTO.builder()
            .nome("Test Nome")
            .codigo(codigo)
            .build();

        {{EntityName}}DTO saved = service.save(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCodigo()).isEqualTo(codigo);
    }
}
```

### Frontend (Jest)

#### Componente Detalhe Test
**Arquivo**: `src/frontend/src/app/components/{{domain}}/{{entity}}/{{entity}}-detalhe/{{entity}}-detalhe.component.spec.ts`

```typescript
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { {{Entity}}DetalheComponent } from './{{entity}}-detalhe.component';
import { {{Entity}}Service } from '../{{entity}}.service';
import { MessageService } from '../../../base/messages/messages.service';
import { AuthService } from '../../../base/auth/auth-service';
import { of } from 'rxjs';
import { RouteConstants } from '../../../base/constants/route-constants';
import { {{Entity}}DTO } from '../model/{{entity}}-dto';
import { ExecutionCallbacks } from '../../../base/base-service';

describe('{{Entity}}DetalheComponent', () => {
  let component: {{Entity}}DetalheComponent;
  let fixture: ComponentFixture<{{Entity}}DetalheComponent>;
  let service: jest.Mocked<{{Entity}}Service>;
  let messageService: jest.Mocked<MessageService>;
  let authService: jest.Mocked<AuthService>;

  beforeEach(async () => {
    const serviceMock = {
      findById: jest.fn(),
      save: jest.fn(),
    };

    const messageServiceMock = {
      sucesso: jest.fn(),
      erro: jest.fn(),
    };

    const authServiceMock = {
      hasAuthorityEditarToModulo: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [{{Entity}}DetalheComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    })
    .overrideComponent({{Entity}}DetalheComponent, {
      set: {
        providers: [{ provide: {{Entity}}Service, useValue: serviceMock }]
      }
    })
    .compileComponents();

    fixture = TestBed.createComponent({{Entity}}DetalheComponent);
    component = fixture.componentInstance;
    service = fixture.debugElement.injector.get({{Entity}}Service) as jest.Mocked<{{Entity}}Service>;
    messageService = TestBed.inject(MessageService) as jest.Mocked<MessageService>;
    authService = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    authServiceMock.hasAuthorityEditarToModulo.mockReturnValue(true);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Inicialização', () => {
    it('deve criar o componente', () => {
      expect(component).toBeTruthy();
    });

    it('deve inicializar formulário com campos', () => {
      component.id = RouteConstants.P_ADD;
      component.ngOnInit();

      expect(component.form.get('nome')).toBeTruthy();
      expect(component.form.get('codigo')).toBeTruthy();
    });
  });

  describe('Validações ao Salvar', () => {
    beforeEach(() => {
      component.id = RouteConstants.P_ADD;
      component.ngOnInit();
    });

    it('NÃO deve salvar se nome está vazio', () => {
      component.form.patchValue({ nome: '', codigo: '001' });
      component.salvar();

      expect(messageService.erro).toHaveBeenCalled();
      const callArgs = messageService.erro.mock.calls[0][0];
      expect(callArgs).toContain('nome');
      expect(service.save).not.toHaveBeenCalled();
    });

    it('NÃO deve salvar se codigo está vazio', () => {
      component.form.patchValue({ nome: 'Test', codigo: '' });
      component.salvar();

      expect(messageService.erro).toHaveBeenCalled();
      const callArgs = messageService.erro.mock.calls[0][0];
      expect(callArgs).toContain('codigo');
      expect(service.save).not.toHaveBeenCalled();
    });
  });

  describe('Salvamento com Sucesso', () => {
    beforeEach(() => {
      component.id = RouteConstants.P_ADD;
      component.ngOnInit();
    });

    it('deve salvar com sucesso e exibir mensagem', () => {
      component.form.patchValue({ nome: 'Test Nome', codigo: '001' });

      service.save.mockImplementation(
        (_data: {{Entity}}DTO, callbacks: ExecutionCallbacks<{{Entity}}DTO>) => {
          if (callbacks.onSuccess) {
            callbacks.onSuccess({ id: 'test-id', nome: 'Test Nome', codigo: '001' } as {{Entity}}DTO);
          }
        }
      );

      const backEventSpy = jest.fn();
      component.backEvent.subscribe(backEventSpy);

      component.salvar();

      expect(service.save).toHaveBeenCalled();
      const callArgs = service.save.mock.calls[0][0];
      expect(callArgs.nome).toBe('Test Nome');
      expect(callArgs.codigo).toBe('001');
      expect(messageService.sucesso).toHaveBeenCalled();
      expect(backEventSpy).toHaveBeenCalled();
    });
  });
});
```

**IMPORTANTE**:
- Crie arquivo `{{entity}}-detalhe.component.spec.ts` para cada componente detalhe
- Teste inicialização, validações e salvamento
- Use `.overrideComponent()` para mockar serviços component-level
- Verifique mensagens de erro e sucesso
- Teste navegação de volta após salvar

## 6. OUTPUT FORMAT

Retorne um JSON com:
- Chave: caminho relativo do arquivo
- Valor: conteúdo do arquivo

```json
{
  "src/backend/src/main/java/.../{{EntityName}}.java": "...",
  "src/backend/src/main/resources/db/tenant-migrations/V...sql": "...",
  "src/frontend/src/app/components/{{domain}}/{{entity}}/model/{{entity}}.dto.ts": "...",
  "databaseConstraints": [
    {"enum": "UK_{{TABLE_NAME}}_{{FIELD}}", "messageKey": "{{entityCamel}}.{{field}}.unique"},
    {"enum": "FK_{{TABLE_NAME}}_{{REF_TABLE}}", "messageKey": "{{entityCamel}}.{{refEntity}}.foreignKey"},
    {"enum": "CHK_{{TABLE_NAME}}_{{FIELD}}", "messageKey": "{{entityCamel}}.{{field}}.invalid"}
  ],
  "i18n/pt": [
    {"key": "{{entityCamel}}.nome.notBlank", "source": "Nome é obrigatório", "target": "Nome é obrigatório"},
    {"key": "{{entityCamel}}.nome.unique", "source": "Nome já cadastrado", "target": "Nome já cadastrado"}
  ],
  "i18n/en": [
    {"key": "{{entityCamel}}.nome.notBlank", "source": "Nome é obrigatório", "target": "Name is required"},
    {"key": "{{entityCamel}}.nome.unique", "source": "Nome já cadastrado", "target": "Name already exists"}
  ]
}
```

## EXEMPLO DE INPUT JSON

```json
{
  "EntityName": "TituloCategoria",
  "entityCamel": "tituloCategoria",
  "entity-kebab": "titulo-categoria",
  "table_name": "titulo_categoria",
  "domain": "financeiro",
  "entityPackage": "titulocategoria",
  "MODULE": "FINANCEIRO",
  "ENTITY_UPPER": "TITULO_CATEGORIA",
  "EntityDescription": "Categoria de Títulos Financeiros",
  "fields": [
    {"name": "nome", "javaType": "String", "required": true, "maxLength": 200},
    {"name": "codigo", "javaType": "String", "required": true, "unique": true, "maxLength": 20},
    {"name": "descricao", "javaType": "String", "required": false, "maxLength": 500},
    {"name": "tipo", "javaType": "TituloCategoriaTipoEnum", "required": true, "enum": true,
     "enumValues": ["RECEITA", "DESPESA"]}
  ]
}
```

## INSTRUÇÕES FINAIS

1. NÃO execute comandos no sistema
2. Gere TODOS os arquivos necessários
3. Siga RIGOROSAMENTE os padrões descritos
4. Use `plainToInstance` nos serviços frontend
5. Use classes (não interfaces) para DTOs TypeScript
6. Migrations DEVEM ser idempotentes
7. Adicione pacote entity ao `DataSourceConfig.ENTITY_PACKAGES`
8. Testes com valores únicos em runtime
9. **CRÍTICO**: Registre TODAS as constraints no `DatabaseConstraintsEnum`
```

---

## CHECKLIST DE VALIDAÇÃO

Antes de usar o código gerado, verifique:

### Backend
- [ ] Entidade usa Builder pattern com validação centralizada
- [ ] Value Objects via método fábrica (`Nome.of(...)`)
- [ ] DTOs com Lombok @Builder/@Data
- [ ] Service estende CrudServiceImpl corretamente
- [ ] Controller com @PreAuthorize correto

### Migrations
- [ ] Idempotente (IF NOT EXISTS ou DO$$)
- [ ] Constraints nomeadas (uk_, fk_, ck_)
- [ ] Módulo inserido na tabela `modulo`
- [ ] Perfil vinculado em `perfil_modulo`

### DatabaseConstraintsEnum ⚠️ CRÍTICO
- [ ] **TODAS as constraints registradas no enum**
- [ ] Nomenclatura correta (UK_, FK_, CHK_)
- [ ] MessageKeys seguem padrão `{{entityCamel}}.{{field}}.{{type}}`
- [ ] Constraints de UNIQUE, FK e CHECK mapeadas

### Frontend
- [ ] DTOs são CLASSES com @Exclude/@Expose
- [ ] Service usa `plainToInstance` nos métodos convert*
- [ ] Enum com getByKey() para conversão
- [ ] Backend message service implementado
- [ ] Mensagens de constraints incluídas no BackendMessageService

### i18n
- [ ] Trans-units de validação em pt-BR (messages.xlf)
- [ ] Trans-units de validação em en-US (messages.en.xlf)
- [ ] **Mensagens de constraints incluídas** (ex: `.unique`, `.foreignKey`)

### Testes Backend
- [ ] Valores únicos em runtime (não determinísticos)
- [ ] Repository test implementado
- [ ] Service test implementado

### Testes Frontend (Jest)
- [ ] Arquivo `{{entity}}-detalhe.component.spec.ts` criado
- [ ] Testes de inicialização implementados
- [ ] Testes de validação (campos obrigatórios) implementados
- [ ] Testes de salvamento com sucesso implementados
- [ ] Mocks configurados corretamente (service, messageService, authService)
- [ ] Usa `.overrideComponent()` para mockar component-level providers
- [ ] Testes verificam mensagens de erro e sucesso
- [ ] Testes verificam navegação após salvar

### Configuração
- [ ] Pacote entity adicionado a `DataSourceConfig.ENTITY_PACKAGES`
- [ ] Rotas adicionadas ao arquivo de rotas do módulo
