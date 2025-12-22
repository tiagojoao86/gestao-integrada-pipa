# Exemplos Reais de Componentes e Services

## Exemplo: Select de Unidade de Negócio no Angular

```html
<p-select
  inputId="unidadeNegocio"
  [options]="allUnidadesNegocio"
  formControlName="unidadeNegocio"
  optionLabel="codigo"
  optionValue="id"
  [filter]="true"
  filterBy="codigo,nome"
  [showClear]="false"
>
  <ng-template let-item pTemplate="item">
    <div>{{ item.codigo }} - {{ item.nome }}</div>
  </ng-template>
</p-select>
```

## Exemplo: Service de Carregamento

```typescript
loadUnidadesNegocio(setDefault = false): void {
    this.authService.getUnidadesNegocio().subscribe((unidades) => {
        this.allUnidadesNegocio = unidades;
        if (setDefault) {
            const defaultUnidade = this.authService.getDefaultUnidadeNegocio();
            if (defaultUnidade) {
                this.form.get('unidadeNegocio')?.setValue(defaultUnidade.id);
            }
        }
    });
}
```

---

# Fluxo de Autenticação e Cache de Unidades (Frontend)

## Padrão

1. Usuário faz login e recebe JWT + lista de unidades de negócio (com id, nome, código, isDefault)
2. AuthService armazena unidades no sessionStorage/localStorage
3. Métodos:
   - `getUnidadesNegocio()` retorna array completo
   - `getDefaultUnidadeNegocio()` retorna objeto `{id, codigo, nome}` da unidade default
4. Componentes usam esses métodos para preencher combos e setar valores default

---

# Prática Angular: Controle de Campos Desabilitados

## Correto

Em formulários reativos, nunca use `[disabled]` no HTML de campos com `formControlName`. Controle o estado via TypeScript:

```typescript
this.form.get("campo")?.disable(); // para desabilitar
this.form.get("campo")?.enable(); // para habilitar

// Ou já criar o FormControl desabilitado:
campo: new FormControl({ value: null, disabled: true });
```

## Errado

```html
<input formControlName="campo" [disabled]="true" />
```

---

# Estratégia de Auto-set de Unidade de Negócio Default (Frontend)

## Padrão

No cadastro de entidades, carregue a unidade de negócio default do usuário (armazenada no AuthService/sessionStorage) e defina no form:

```typescript
const defaultUnidade = this.authService.getDefaultUnidadeNegocio();
if (defaultUnidade) {
  this.form.get("unidadeNegocio")?.setValue(defaultUnidade.id);
}
```

Chame esse set dentro do subscribe do carregamento das unidades para evitar problemas de timing.

---

# Endpoints Dedicados para Vinculação

## Padrão

Cada cadastro que precisa vincular entidades (ex: Título → Pessoa, Plano de Contas) deve ter endpoints REST dedicados no respectivo controller, filtrando automaticamente por UnidadeNegocio.

### Exemplo: TituloController

```java
@GetMapping("/titulo/pessoas-disponiveis")
public List<PessoaDTO> listarPessoasDisponiveis() { ... }

@GetMapping("/titulo/planos-disponiveis")
public List<PlanoContasDTO> listarPlanosDisponiveis(@RequestParam UUID unidadeNegocioId) { ... }
```

Esses endpoints devem aplicar o filtro de UnidadeNegocio e retornar apenas entidades ativas/válidas para o usuário.

---

# Convenção de DTOs com Campos Extras

## Padrão

Quando o frontend precisa exibir informações adicionais (ex: código da unidade de negócio), inclua esses campos nos DTOs e na resposta de autenticação.

### Exemplo: UsuarioUnidadeNegocioDTO

```java
public class UsuarioUnidadeNegocioDTO {
    private UUID unidadeNegocioId;
    private String unidadeNegocioCodigo;
    private String unidadeNegocioNome;
    private boolean isDefault;
}
```

No backend, garanta que o builder/populador do DTO inclua todos os campos necessários para o frontend.

---

# Padrão de Filtro Automático por Unidade de Negócio (Multi-tenant)

## Visão Geral

Para garantir que cada usuário só acesse dados das unidades de negócio permitidas, utilize o padrão de filtro automático baseado em marker interface e Specification no backend.

### 1. Marker Interface

Crie uma interface marker chamada `UnidadeNegocioFiltravel`:

```java
public interface UnidadeNegocioFiltravel {
    UnidadeNegocio getUnidadeNegocio();
}
```

Implemente essa interface em todas as entidades que devem ser filtradas por unidade de negócio (ex: `ContaBancaria`, `Titulo`, `PlanoContas`).

### 2. Specification

Implemente uma Specification genérica:

```java
public class UnidadeNegocioSpecification {
    public static <T extends BaseEntity & UnidadeNegocioFiltravel> Specification<T> permitidasParaUsuario(Set<UUID> unidadesPermitidas) {
        return (root, query, cb) -> {
            if (unidadesPermitidas == null || unidadesPermitidas.isEmpty()) return null;
            return root.get("unidadeNegocio").get("id").in(unidadesPermitidas);
        };
    }
}
```

### 3. Aplicação automática no Service

No `CrudServiceImpl`, aplique o filtro automaticamente nos métodos `list()` e `findById()` para entidades que implementam a interface:

```java
if (UnidadeNegocioFiltravel.class.isAssignableFrom(entityClass)) {
    Set<UUID> permitidas = Session.getUnidadeNegocioIds();
    spec = spec.and(UnidadeNegocioSpecification.permitidasParaUsuario(permitidas));
}
```

### 4. Session Helper

Extraia os IDs das unidades permitidas do JWT:

```java
public static Set<UUID> getUnidadeNegocioIds() {
    // Extrai claim do token JWT
}
```

### 5. Exemplo de uso

Ao criar um novo cadastro, o campo unidade de negócio deve ser preenchido automaticamente com a unidade default do usuário (frontend) e validado no backend.

---

# Checklist de Atualização de Instruções (Dez/2025)

## Itens obrigatórios para atualizar:

- [ ] Documentar padrão de filtro automático por UnidadeNegocio (marker interface, Specification, CrudServiceImpl)
- [ ] Exemplificar endpoints dedicados para vinculação (pessoas, planos, etc)
- [ ] Explicar prática correta de controle de campos desabilitados em Angular reativo (apenas via TypeScript)
- [ ] Detalhar estratégia de auto-set de UnidadeNegocio default no frontend
- [ ] Explicar convenção de DTOs com campos extras (ex: unidadeNegocioCodigo)
- [ ] Adicionar exemplos reais dos componentes e services já ajustados
- [ ] Documentar fluxo de autenticação e cache de unidades no frontend

---

# Copilot/Gemini - Instruções do Projeto

Propósito: centralizar regras e contexto compartilhado para agentes (Copilot / Gemini) e referenciar guias de domínio já existentes.

## 📚 ARQUIVOS DE DOCUMENTAÇÃO (LEIA SEMPRE)

Você DEVE consultar estes arquivos quando trabalhar em suas respectivas áreas:

- **`.github/instructions/backend-GEMINI.md`** → Contexto backend completo (Java / Spring / DDD / Value Objects)
- **`.github/instructions/frontend-GEMINI.md`** → Contexto frontend completo (Angular / TypeScript / i18n)
- **`.github/instructions/PROJECT.md`** → Visão geral do projeto (contexto centralizado)
- **`MULTI-TENANT-ARCHITECTURE.md`** → Documentação completa da arquitetura multi-tenant

**IMPORTANTE:** Esses arquivos contêm informações COMPLEMENTARES e MAIS DETALHADAS do que este COPILOT_INSTRUCTIONS.md. Sempre verifique o arquivo específico da área antes de implementar algo.

### 🎯 Quando Consultar Cada Arquivo:

- **Backend**: Leia `backend-GEMINI.md` antes de criar/modificar entidades, services, repositories, validações
- **Frontend**: Leia `frontend-GEMINI.md` antes de criar/modificar componentes, services, guards, i18n
- **Arquitetura**: Leia `MULTI-TENANT-ARCHITECTURE.md` quando trabalhar com tenancy, migrations, schemas

## 📦 VALUE OBJECTS EXISTENTES

**SEMPRE verifique os ValueObjects existentes no pacote `core.valueobject` ANTES de criar novos.**

### ValueObjects Disponíveis:

- **Nome** (`core.valueobject.Nome`): Valida e armazena nomes (max 255 chars, não vazio)
- **CPF** (`core.valueobject.CPF`): Valida CPF brasileiro com dígitos verificadores
- **CNPJ** (`core.valueobject.CNPJ`): Valida CNPJ brasileiro com dígitos verificadores
- **Email** (`core.valueobject.Email`): Valida formato de e-mail
- **PhoneNumber** (`core.valueobject.PhoneNumber`): Valida telefone brasileiro
- **Money** (`core.valueobject.Money`): Valida e armazena valores monetários

### Como Usar em Entidades:

```java
@Entity
public class Pessoa extends BaseEntity {
    // ValueObject embutido
    @Embedded
    private Nome nome;

    @Embedded
    private CPF cpf;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email"))
    private Email email;
}
```

### Como Usar no Builder:

```java
public Builder nome(String nomeStr) {
    Set<BeanValidationMessage> violations = new HashSet<>();
    this.nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
    if (!violations.isEmpty()) {
        throw new BeanValidationException("Entidade", violations);
    }
    return this;
}
```

### Regras:

✅ **O que fazer:**

- Sempre verificar `core.valueobject` antes de criar novos ValueObjects
- Usar `@Embedded` para incorporar ValueObjects em entidades
- Usar `ValidationUtils.validateAndGet()` no Builder para criar ValueObjects
- Criar getters que retornam `String` usando `.getValue()`

❌ **O que NÃO fazer:**

- Criar ValueObjects duplicados em outros pacotes
- Usar tipos primitivos (String, Long) para campos que têm ValueObject disponível
- Ignorar a estrutura do projeto ao criar novos componentes

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

## 🔧 PADRÃO DE VALIDAÇÃO COM BeanValidationException

### Objetivo:

Centralizar todas as validações de negócio utilizando `BeanValidationException` para fornecer chaves de erro estruturadas que o frontend pode usar para i18n.

### Como Implementar:

#### Em Entidades e Value Objects:

```java
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import java.util.HashSet;
import java.util.Set;

// Exemplo em Value Object
public Email(String value) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    if (value == null || value.isBlank()) {
        violations.add(new BeanValidationMessage("email", "Email não pode ser vazio"));
        throw new BeanValidationException(violations);
    }

    // Outras validações...
    if (!EMAIL_PATTERN.matcher(emailTrimmed).matches()) {
        violations.add(new BeanValidationMessage("email", "Email inválido: " + value));
        throw new BeanValidationException(violations);
    }
}

// Exemplo em Entity
private void validarCamposObrigatorios() {
    Set<BeanValidationMessage> violations = new HashSet<>();

    if (tipo == null) {
        violations.add(new BeanValidationMessage("tipo", "Tipo é obrigatório"));
    }
    if (descricao == null || descricao.isBlank()) {
        violations.add(new BeanValidationMessage("descricao", "Descrição é obrigatória"));
    }

    if (!violations.isEmpty()) {
        throw new BeanValidationException("titulo", violations);
    }
}
```

### Integração com Frontend:

O `RestExceptionHandler` captura `BeanValidationException` e transforma em resposta JSON:

```json
{
  "timestamp": "2025-12-03T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação",
  "userMessageKey": ["titulo.tipo", "titulo.descricao"],
  "path": "/api/titulos"
}
```

O frontend usa essas chaves (`userMessageKey`) para buscar mensagens traduzidas.

### Regras:

✅ **SEMPRE usar BeanValidationException para:**

- Validações de campos obrigatórios
- Validações de formato (email, telefone, CPF, CNPJ)
- Validações de regras de negócio
- Validações de Value Objects

❌ **NUNCA usar:**

- `IllegalArgumentException` para validações
- `throw new Exception("mensagem hardcoded")`
- Validações sem chaves estruturadas

### Benefícios:

1. **Consistência**: Todas as validações seguem o mesmo padrão
2. **I18n**: Frontend pode traduzir mensagens automaticamente
3. **Múltiplos erros**: Retorna todos os erros de validação de uma vez
4. **Manutenibilidade**: Fácil localizar e modificar validações

### Testes:

Sempre atualizar os testes para esperar `BeanValidationException`:

```java
@Test
void deveRejeitarEmailInvalido() {
    assertThrows(BeanValidationException.class, () -> new Email("invalido"));
}
```

## 🗄️ PADRÃO DE CONSTRAINTS DE BANCO DE DADOS

### Objetivo:

Nomear todas as constraints do banco de dados (UNIQUE, FOREIGN KEY, CHECK) para permitir tratamento de erros estruturado e mensagens i18n no frontend.

### Nomenclatura de Constraints:

Todas as constraints devem seguir um padrão de nomenclatura consistente:

- **UNIQUE**: `uk_<tabela>_<campo>` (ex: `uk_usuario_login`, `uk_perfil_nome`)
- **FOREIGN KEY**: `fk_<tabela>_<tabela_referenciada>` (ex: `fk_titulo_pessoa`)
- **CHECK**: `ck_<tabela>_<campo>` (ex: `ck_titulo_valor_positivo`)

### Implementação em Migrations:

```sql
-- Exemplo de constraint UNIQUE nomeada
ALTER TABLE usuario
ADD CONSTRAINT uk_usuario_login UNIQUE (login);

-- Exemplo de constraint FOREIGN KEY nomeada
ALTER TABLE titulo
ADD CONSTRAINT fk_titulo_pessoa
FOREIGN KEY (pessoa_id) REFERENCES pessoa(id);

-- Exemplo de constraint CHECK nomeada
ALTER TABLE titulo
ADD CONSTRAINT ck_titulo_valor_positivo
CHECK (valor_original > 0);
```

### DatabaseConstraintsEnum:

Registrar todas as constraints no enum `DatabaseConstraintsEnum`:

```java
public enum DatabaseConstraintsEnum {

    DEFAULT("errors.internalServerError"),

    // Constraints de Usuario
    UK_USUARIO_LOGIN("usuario.login.unique"),

    // Constraints de Perfil
    UK_PERFIL_NOME("perfil.nome.unique"),

    // Constraints de Pessoa
    UK_PESSOA_FISICA_CPF("pessoaFisica.cpf.unique"),
    UK_PESSOA_JURIDICA_CNPJ("pessoaJuridica.cnpj.unique"),

    // Constraints de Plano de Contas
    UK_PLANO_CONTAS_CODIGO("planoContas.codigo.unique"),

    // Constraints de Unidade de Negócio
    UK_UNIDADE_NEGOCIO_CODIGO("unidadeNegocio.codigo.unique");

    String userMessageKey;

    DatabaseConstraintsEnum(String userMessageKey) {
        this.userMessageKey = userMessageKey;
    }

    public String getUserMessageKey() {
        return userMessageKey;
    }

    public static DatabaseConstraintsEnum getByKey(String key) {
        for (DatabaseConstraintsEnum constraint : values()) {
            if (constraint.name().equalsIgnoreCase(key)) {
                return constraint;
            }
        }
        return DEFAULT;
    }
}
```

### RestExceptionHandler:

O método `handleDataIntegrityViolationException` já está configurado para capturar constraints:

```java
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<Object> handleDataIntegrityViolationException(
        DataIntegrityViolationException ex,
        WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String title = INVALID_DATA;
    List<String> userMessageKeys = List.of(ErrorKeys.INTERNAL_SERVER_ERROR);

    if (ex.getCause() instanceof ConstraintViolationException) {
        String constraintName = ((ConstraintViolationException) ex.getCause()).getConstraintName();
        userMessageKeys = List.of(
            DatabaseConstraintsEnum.getByKey(constraintName).getUserMessageKey()
        );
    }

    ApiError apiError = ApiError.builder()
            .status(status.value())
            .timestamp(OffsetDateTime.now())
            .title(title)
            .userMessageKey(userMessageKeys)
            .build();

    return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
}
```

### Resposta JSON para Frontend:

```json
{
  "timestamp": "2025-12-03T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "title": "Invalid Data",
  "userMessageKey": ["usuario.login.unique"],
  "path": "/api/usuarios"
}
```

### Regras:

✅ **SEMPRE fazer:**

- Nomear todas as constraints no banco de dados usando o padrão estabelecido
- Adicionar a constraint no `DatabaseConstraintsEnum` com chave i18n apropriada
- Testar violações de constraints para garantir que a mensagem correta é retornada
- Documentar a constraint no comentário da migration

❌ **NUNCA fazer:**

- Criar constraints sem nome (constraints anônimas)
- Usar nomes genéricos como `constraint_1`, `fk_1`, etc.
- Esquecer de adicionar a constraint no `DatabaseConstraintsEnum`
- Hard-codar mensagens de erro de constraints

### Benefícios:

1. **Mensagens Amigáveis**: Erros de banco viram mensagens i18n para o usuário
2. **Rastreabilidade**: Fácil identificar qual constraint foi violada
3. **Manutenibilidade**: Centralizado no enum, fácil adicionar novas constraints
4. **Consistência**: Padrão único para todas as constraints do sistema

### Fluxo Completo:

1. **Migration**: Cria constraint nomeada (`uk_usuario_login`)
2. **Violação**: Usuário tenta criar login duplicado
3. **Hibernate**: Lança `DataIntegrityViolationException` → `ConstraintViolationException`
4. **RestExceptionHandler**: Captura exceção, busca constraint no enum
5. **Resposta**: Retorna JSON com `userMessageKey: ["usuario.login.unique"]`
6. **Frontend**: Usa chave para buscar mensagem traduzida e mostrar ao usuário

## 🏗️ PADRÃO BUILDER PARA ENTIDADES

### Objetivo:

Utilizar o padrão Builder em todas as entidades do domínio para criar instâncias válidas com validação centralizada, seguindo o modelo do UsuarioEntity.

### Estrutura do Padrão:

Todas as entidades devem seguir esta estrutura:

```java
@Entity
public class MinhaEntity extends BaseEntity {

    // Atributos privados
    private String campo1;
    private TipoEnum campo2;

    // Construtor privado (usado apenas pelo Builder)
    private MinhaEntity(String campo1, TipoEnum campo2) {
        this.campo1 = campo1;
        this.campo2 = campo2;
    }

    // Construtor protected vazio (JPA)
    protected MinhaEntity() {}

    // Classe ValidatedData interna privada
    private static class ValidatedData {
        final String campo1;
        final TipoEnum campo2;

        ValidatedData(String campo1, TipoEnum campo2) {
            this.campo1 = campo1;
            this.campo2 = campo2;
        }
    }

    // Método validate privado estático
    private static ValidatedData validate(String campo1, TipoEnum campo2) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        // Validações usando ValidationUtils para Value Objects
        Campo1VO campo1VO = ValidationUtils.validateAndGet(
            () -> new Campo1VO(campo1), violations
        );

        // Validações manuais para campos simples
        if (campo2 == null) {
            violations.add(new BeanValidationMessage("campo2", "Campo2 é obrigatório"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("minhaEntity", violations);
        }

        return new ValidatedData(campo1, campo2);
    }

    // Método update público para atualização
    public void atualizar(String campo1, TipoEnum campo2) {
        ValidatedData data = validate(campo1, campo2);
        this.campo1 = data.campo1;
        this.campo2 = data.campo2;
    }

    // Getters públicos
    public String getCampo1() {
        return campo1;
    }

    public TipoEnum getCampo2() {
        return campo2;
    }

    // Builder público estático
    public static class Builder {
        private String campo1;
        private TipoEnum campo2;

        public Builder campo1(String campo1) {
            this.campo1 = campo1;
            return this;
        }

        public Builder campo2(TipoEnum campo2) {
            this.campo2 = campo2;
            return this;
        }

        public MinhaEntity build() {
            ValidatedData data = validate(this.campo1, this.campo2);
            return new MinhaEntity(data.campo1, data.campo2);
        }
    }
}
```

### Uso nos Services:

```java
@Service
public class MinhaEntityServiceImpl extends CrudServiceImpl<...> {

    @Override
    protected MinhaEntity mergeEntityAndDTO(MinhaEntity entity, MinhaEntityDTO dto) {
        if (Objects.isNull(entity)) {
            // Criar nova entidade usando Builder
            entity = new MinhaEntity.Builder()
                    .campo1(dto.getCampo1())
                    .campo2(dto.getCampo2())
                    .build();
            return entity;
        }

        // Atualizar entidade existente
        entity.atualizar(dto.getCampo1(), dto.getCampo2());
        return entity;
    }
}
```

### Entidades com Herança:

Para entidades que herdam de uma classe base (ex: PessoaFisica extends Pessoa):

```java
@Entity
public abstract class Pessoa extends BaseEntity {

    // Classe ValidatedDataBase para campos comuns
    protected static class ValidatedDataBase {
        final String nome;
        final Email email;
        final PhoneNumber telefone;

        ValidatedDataBase(String nome, Email email, PhoneNumber telefone) {
            this.nome = nome;
            this.email = email;
            this.telefone = telefone;
        }
    }

    // Método validate protegido para reutilização
    protected static ValidatedDataBase validateBase(String nomeStr, String emailStr,
                                                     String telefoneStr, String entityName) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        // Validações...
        Email email = ValidationUtils.validateAndGet(() -> new Email(emailStr), violations);
        PhoneNumber telefone = ValidationUtils.validateAndGet(() -> new PhoneNumber(telefoneStr), violations);

        if (!violations.isEmpty()) {
            throw new BeanValidationException(entityName, violations);
        }

        return new ValidatedDataBase(nomeStr, email, telefone);
    }

    // Método abstrato para nome da entidade (usado em validações)
    protected abstract String getEntityName();
}

@Entity
public class PessoaFisica extends Pessoa {

    private CPF cpf;
    private LocalDate dataNascimento;

    private PessoaFisica(String nome, Email email, PhoneNumber telefone,
                         CPF cpf, LocalDate dataNascimento) {
        super(nome, email, telefone);
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
    }

    @Override
    protected String getEntityName() {
        return "pessoaFisica";
    }

    private static class ValidatedData {
        final ValidatedDataBase base;
        final CPF cpf;
        final LocalDate dataNascimento;

        ValidatedData(ValidatedDataBase base, CPF cpf, LocalDate dataNascimento) {
            this.base = base;
            this.cpf = cpf;
            this.dataNascimento = dataNascimento;
        }
    }

    private static ValidatedData validate(String nomeStr, String emailStr, String telefoneStr,
                                         String cpfStr, LocalDate dataNascimento) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        // Validar campos base
        ValidatedDataBase base = null;
        try {
            base = validateBase(nomeStr, emailStr, telefoneStr, "pessoaFisica");
        } catch (BeanValidationException e) {
            violations.addAll(e.getViolations());
        }

        // Validar campos específicos
        CPF cpf = ValidationUtils.validateAndGet(() -> new CPF(cpfStr), violations);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("pessoaFisica", violations);
        }

        return new ValidatedData(base, cpf, dataNascimento);
    }

    public static class Builder {
        private String nome;
        private String email;
        private String telefone;
        private String cpf;
        private LocalDate dataNascimento;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder telefone(String telefone) {
            this.telefone = telefone;
            return this;
        }

        public Builder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public Builder dataNascimento(LocalDate dataNascimento) {
            this.dataNascimento = dataNascimento;
            return this;
        }

        public PessoaFisica build() {
            ValidatedData data = validate(this.nome, this.email, this.telefone,
                                         this.cpf, this.dataNascimento);
            return new PessoaFisica(data.base.nome, data.base.email, data.base.telefone,
                                   data.cpf, data.dataNascimento);
        }
    }
}
```

### Regras:

✅ **SEMPRE fazer:**

- Usar Builder para criar novas entidades nos Services
- Centralizar validações no método `validate()`
- Usar `ValidationUtils.validateAndGet()` para Value Objects
- Manter construtor privado (apenas Builder pode usar)
- Fornecer método `atualizar()` público para updates
- Retornar Strings nos getters de Value Objects (não expor VOs diretamente)

❌ **NUNCA fazer:**

- Criar entidades com `new Entity()` diretamente
- Validar campos em múltiplos lugares
- Expor Value Objects nos getters (retornar `String` usando `vo.getValue()`)
- Usar construtores públicos
- Fazer validações fora do método `validate()`
- **NUNCA validar auto-referências no Builder** (ex: `planoPai.equals(this)`) - o objeto ainda não existe!

### ⚠️ Validações que Dependem do Objeto Criado:

Algumas validações **NÃO PODEM** ser feitas no `validate()` do Builder porque dependem do objeto já existir:

❌ **Exemplo ERRADO** (auto-referência):

```java
private static ValidatedData validate(..., PlanoContas planoPai) {
    // ERRO: "this" não existe ainda!
    if (planoPai != null && this.equals(planoPai)) {
        violations.add(...);
    }
}
```

✅ **Solução CORRETA** (usar JPA lifecycle):

```java
@PrePersist
@PreUpdate
private void validarAutoReferencia() {
    if (planoPai != null && planoPai.equals(this)) {
        throw new BeanValidationException("planoContas",
            Set.of(new BeanValidationMessage("planoPai",
                "Plano de contas não pode ser pai de si mesmo")));
    }
}
```

**Quando usar `@PrePersist/@PreUpdate`:**

- Validações que dependem do ID da entidade
- Validações de auto-referência (entidade comparada consigo mesma)
- Validações que dependem do estado persistido anterior
- Validações que precisam de `getId()` ou outros campos gerados

### Benefícios:

1. **Validação Centralizada**: Todas as validações em um único lugar
2. **Imutabilidade**: Construtor privado garante que só o Builder cria instâncias
3. **Fluent API**: Builder permite código mais legível
4. **Agregação de Erros**: Valida todos os campos e retorna todos os erros de uma vez
5. **Consistência**: Padrão único em todas as entidades do sistema
6. **Testabilidade**: Fácil criar entidades válidas em testes

### Exemplo Completo de Teste:

```java
@Test
void deveCriarPessoaFisicaComBuilder() {
    PessoaFisica pessoa = new PessoaFisica.Builder()
            .nome("João Silva")
            .email("joao@example.com")
            .telefone("11987654321")
            .cpf("12345678901")
            .dataNascimento(LocalDate.of(1990, 1, 1))
            .build();

    assertNotNull(pessoa);
    assertEquals("João Silva", pessoa.getNome());
    assertEquals("12345678901", pessoa.getCpf());
}

@Test
void deveRejeitarCpfInvalido() {
    PessoaFisica.Builder builder = new PessoaFisica.Builder()
            .nome("João Silva")
            .email("joao@example.com")
            .telefone("11987654321")
            .cpf("invalido")
            .dataNascimento(LocalDate.of(1990, 1, 1));

    assertThrows(BeanValidationException.class, builder::build);
}
```

## 🔧 PADRÕES DE ENTIDADES E MIGRATIONS (BACKEND)

### UUIDs Obrigatórios:

- **TODA entidade deve estender `BaseEntity`**, que fornece:

  - `id` do tipo `UUID` (UUIDv7) - **NUNCA use `Long` ou `BigSerial` para IDs**
  - `createdAt`, `updatedAt` (auditoria de timestamps)
  - `createdBy`, `updatedBy` (auditoria de usuários)

- **Migrations devem usar `UUID` para PKs e FKs**, não `BIGSERIAL`:

```sql
-- ✅ CORRETO
CREATE TABLE pessoa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL
);

-- ❌ ERRADO
CREATE TABLE pessoa (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);
```

## 🎨 PADRÕES FRONTEND (ANGULAR)

### Componentização:

- **Sempre componentize** funcionalidades reutilizáveis
- Componentes devem ter responsabilidade única e bem definida
- Prefira componentes menores e especializados
- Use `@Input()` e `@Output()` para comunicação

### Baixo Acoplamento:

- **Priorize baixo acoplamento** entre componentes
- Use serviços para compartilhar estado e lógica
- Evite acessar DOM de componentes filhos diretamente
- Prefira comunicação por eventos (`@Output`)
- Mantenha lógica de negócio nos serviços, não nos componentes
- Componentes devem ser testáveis isoladamente

### Prefixos e Nomenclatura:

- **Componentes**: Prefixo `gi-` em kebab-case (ex: `<gi-usuario-form>`)
- **Diretivas**: Prefixo `gi` em camelCase (ex: `[giHighlight]`)
- **ESLint configurado** para validar estes padrões automaticamente

### Internacionalização (i18n):

1. **Marcar strings para tradução**:

   ```typescript
   // Em .ts
   const titulo = $localize`Meu Título`;

   // Em .html
   <h1 i18n>Meu Título</h1>;
   ```

2. **Extrair strings**:

   ```bash
   ng extract-i18n --output-path src/locale
   ```

3. **Atualizar traduções**:
   - `messages.xlf` (português): Adicionar `<target>` igual ao `<source>`
   - `messages.en.xlf` (inglês): Copiar `<trans-unit>` e traduzir `<target>`

### Autorização:

- **`authGuard`**: Valida se usuário está logado
- **`moduleAuthorityGuard`**: Valida permissão para módulo específico
  ```typescript
  {
    path: 'usuario',
    canActivate: [authGuard, moduleAuthorityGuard],
    data: { module: 'CADASTRO_USUARIO' }
  }
  ```
- **`groupAuthorityGuard`**: Valida permissão para grupo de funcionalidades
  ```typescript
  {
    path: 'cadastros',
    canActivate: [authGuard, groupAuthorityGuard],
    data: { group: 'CADASTROS' }
  }
  ```

### Estrutura de DTOs:

- **DTOs específicos do componente**: No diretório do componente
- **DTOs de backend diferentes**: Usar nomenclatura `backend-*.dto.ts`
- **Interfaces de FormGroup**: Extrair para arquivos separados em `model/`
- **DTOs compartilhados**: Em `src/app/components/base/model/`

### Testes Frontend (Jest)

**Framework**: O projeto usa **Jest** (não Karma/Jasmine) para testes de componentes Angular.

**Configuração**:
- `jest.config.js`: Configuração principal do Jest
- `setup-jest.ts`: Polyfills e mocks globais (TextEncoder, window.matchMedia, IntersectionObserver)
- Preset: `jest-preset-angular`

**Comandos**:
```bash
npm test                    # Executar todos os testes
npm run test:watch          # Modo watch (reexecuta ao salvar)
npm run test:coverage       # Gerar relatório de cobertura
npm test -- arquivo.spec.ts # Executar teste específico
```

**Estrutura de Testes**:

```typescript
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { RouteConstants } from '../../../base/constants/route-constants';
import { ExecutionCallbacks } from '../../../base/base-service';

describe('MeuComponenteDetalhe', () => {
  let component: MeuComponenteDetalhe;
  let fixture: ComponentFixture<MeuComponenteDetalhe>;
  let service: jest.Mocked<MeuService>;
  let messageService: jest.Mocked<MessageService>;

  beforeEach(async () => {
    const serviceMock = {
      findById: jest.fn(),
      save: jest.fn(),
    };

    const messageServiceMock = {
      sucesso: jest.fn(),
      erro: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [MeuComponenteDetalhe],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageServiceMock },
      ],
    })
    .overrideComponent(MeuComponenteDetalhe, {
      set: {
        providers: [{ provide: MeuService, useValue: serviceMock }]
      }
    })
    .compileComponents();

    fixture = TestBed.createComponent(MeuComponenteDetalhe);
    component = fixture.componentInstance;
    service = fixture.debugElement.injector.get(MeuService) as jest.Mocked<MeuService>;
    messageService = TestBed.inject(MessageService) as jest.Mocked<MessageService>;
  });

  afterEach(() => {
    jest.clearAllMocks();
  });
});
```

**O que testar em componentes detalhe**:

1. **Inicialização**:
   - Componente criado
   - Formulário inicializado com campos corretos
   - Toolbar configurada com ações

2. **Carregamento de dados**:
   - Comboboxes populados corretamente
   - Autocomplete filtrando corretamente
   - findById carregando dados na edição

3. **Validações**:
   - Campos obrigatórios vazios exibem erro
   - Formulário inválido não permite salvar
   - Mensagens de erro corretas

4. **Salvamento**:
   - Sucesso: dados enviados, mensagem exibida, navegação ocorre
   - Erro: mensagem de erro exibida, não navega

**Padrões importantes**:

✅ **SEMPRE fazer**:
- Use `.overrideComponent()` para mockar serviços component-level
- Limpe mocks com `jest.clearAllMocks()` no `afterEach`
- Verifique chamadas com `toHaveBeenCalled()` e `toHaveBeenCalledWith()`
- Para strings, use `.toContain()` em vez de matchers complexos
- Mock ExecutionCallbacks com assinatura correta:
  ```typescript
  service.save.mockImplementation(
    (_data: DTO, callbacks: ExecutionCallbacks<DTO>) => {
      if (callbacks.onSuccess) {
        callbacks.onSuccess(mockData);
      }
    }
  );
  ```

❌ **NUNCA fazer**:
- Usar matchers do Jasmine (expect.stringContaining, expect.objectContaining)
- Esquecer de mockar serviços obrigatórios (MessageService, AuthService)
- Usar valores hardcoded sem verificar o comportamento
- Testar implementação interna ao invés de comportamento

**Exemplo de teste de validação**:
```typescript
describe('Validações ao Salvar', () => {
  it('NÃO deve salvar se nome está vazio', () => {
    component.form.patchValue({ nome: '', codigo: '001' });
    component.salvar();

    expect(messageService.erro).toHaveBeenCalled();
    const callArgs = messageService.erro.mock.calls[0][0];
    expect(callArgs).toContain('nome');
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
      (_data: DTO, callbacks: ExecutionCallbacks<DTO>) => {
        if (callbacks.onSuccess) {
          callbacks.onSuccess({ id: 'test-id' } as DTO);
        }
      }
    );

    const backEventSpy = jest.fn();
    component.backEvent.subscribe(backEventSpy);

    component.salvar();

    expect(service.save).toHaveBeenCalled();
    expect(messageService.sucesso).toHaveBeenCalled();
    expect(backEventSpy).toHaveBeenCalled();
  });
});
```

**Priorização de testes** (ordem de importância):

1. 🔴 **ALTA - Componentes de Detalhe** (OBRIGATÓRIO)
   - Testes de validação (campos obrigatórios, regras de negócio)
   - Testes de salvamento (sucesso e erro)
   - Testes de mensagens do backend
   - **Por quê**: Contém lógica crítica de validação e integração com backend

2. 🟡 **MÉDIA - Serviços**
   - Conversão de DTOs com `plainToInstance`
   - Métodos customizados (não herdados de BaseService)
   - **Por quê**: Garante transformações corretas de dados

3. 🟢 **BAIXA - Componentes de Grid** (OPCIONAL - apenas se houver lógica customizada)
   - Inicialização do componente
   - Configuração de colunas
   - **Apenas testar se**: Grid tem lógica customizada complexa, filtros especiais, ou cálculos
   - **Não testar**: Renderização, estilos, formatação (usar testes manuais)
   - **Por quê**: Grid é principalmente apresentação, delegada a componentes reutilizáveis já testados

4. ⚪ **MUITO BAIXA - Componente Principal** (SKIP)
   - Componente orquestrador que alterna entre Grid/Detalhe
   - **Por quê**: Lógica trivial, melhor coberta por testes E2E

**Exemplo de teste minimalista para Grid** (se necessário):
```typescript
describe('TituloGridComponent', () => {
  // Setup básico...

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve inicializar colunas corretas', () => {
    component.ngOnInit();
    expect(component.columns.length).toBeGreaterThan(0);
    expect(component.columns.some(c => c.field === 'descricao')).toBe(true);
  });

  it('deve atualizar página ao paginar', () => {
    service.list.mockReturnValue(of({ body: { content: [], totalElements: 0 } } as any));
    component.onPageChange({ page: 1, rows: 10 });
    expect(component.pageRequest.page).toBe(1);
  });

  // Apenas isso é suficiente para Grid!
}
```

**Decisão rápida**: Componente tem validações ou salva dados? **Teste!** Apenas exibe dados? **Opcional.**

## 📋 DOMAIN-DRIVEN DESIGN (DDD)

### Princípios:

- **Entidades contêm regras de negócio** - não criar classes "Business" ou "Validator" separadas
- **EVITAR modelos anêmicos** (entidades que são apenas DTOs com getters/setters)
- **Validações dentro dos objetos de domínio** (entidades e value objects)
- **Services apenas orquestram** - não contêm regras de negócio

### Arquitetura de Camadas:

```
┌─────────────────────────────────────┐
│  Controller (REST APIs)             │ ← Recebe requisições HTTP
├─────────────────────────────────────┤
│  Application Services               │ ← Orquestração, transações
├─────────────────────────────────────┤
│  Domain Layer (Entities, VOs)      │ ← ⭐ REGRAS DE NEGÓCIO AQUI
├─────────────────────────────────────┤
│  Repository (Persistence)           │ ← Acesso ao banco de dados
└─────────────────────────────────────┘
```

### Princípios DDD a Seguir:

1. **Aggregates**: Identifique raízes de agregados e garanta consistência transacional
2. **Bounded Contexts**: Separe contextos (`financeiro`, `cadastro`, `atendimento`)
3. **Ubiquitous Language**: Use termos do negócio no código
4. **Domain Events**: Comunique mudanças importantes entre agregados
5. **Repositories**: Uma interface por agregado raiz
6. **Specifications**: Para queries complexas com lógica de negócio

### Anti-Padrões a Evitar:

- ❌ Getters/setters públicos sem validação
- ❌ Entidades anêmicas (só dados, sem comportamento)
- ❌ Services fazendo validações de domínio
- ❌ Lógica de negócio nos controllers
- ❌ Usar tipos primitivos ao invés de Value Objects

## 🚀 COMANDOS ESSENCIAIS

### Backend (executar de `src/backend`):

```bash
./mvnw compile              # Compilar
./mvnw test                 # Rodar testes
./mvnw spring-boot:run      # Executar aplicação
./mvnw package              # Build de produção
```

### Frontend (executar de `src/frontend`):

```bash
npm install                 # Instalar dependências
ng serve                    # Executar aplicação
npm test                    # Rodar testes (Jest)
npm run test:watch          # Rodar testes em modo watch
npm run test:coverage       # Rodar testes com cobertura
ng lint                     # Verificar código
ng lint -- --fix            # Corrigir erros automaticamente
ng build                    # Build de produção
ng extract-i18n --output-path src/locale  # Extrair i18n
```

## 📖 Recomendações:

- Manter este arquivo como fonte global de políticas comuns
- **SEMPRE consultar** `backend-GEMINI.md` e `frontend-GEMINI.md` para detalhes específicos
- Adicionar referência a este arquivo no README
- Validação automática no pipeline de CI

Como contribuir:

- Atualize os arquivos GEMINI.md locais com detalhes específicos do domínio.
- Atualize este arquivo com regras transversais (commit message, políticas de PR, testes de aceitação).

Resultado esperado: agentes terão contexto específico por domínio e um conjunto consistente de regras globais para interações mais assertivas.
