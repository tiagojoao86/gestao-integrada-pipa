# Copilot/Gemini - Instruções do Projeto

Propósito: centralizar regras e contexto compartilhado para agentes (Copilot / Gemini) e referenciar guias de domínio já existentes.

Arquivos de domínio (existentes):

- src/backend/GEMINI.md -> Contexto e comandos específicos do backend (Java / Spring).
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

## Recomendações mínimas:

- Manter este arquivo como fonte global de políticas comuns (estilo de commit, testes obrigatórios, CI, regras de lint).
- Configurar a ferramenta para carregar ambos os diretórios de instruções como contexto:
  - Ex.: export COPILOT_CUSTOM_INSTRUCTIONS_DIRS="src/backend,src/frontend,.github/instructions"
- Opcional: adicionar referência a este arquivo no README e no pipeline de CI para validação automática de sugestões.

Como contribuir:

- Atualize os arquivos GEMINI.md locais com detalhes específicos do domínio.
- Atualize este arquivo com regras transversais (commit message, políticas de PR, testes de aceitação).

Resultado esperado: agentes terão contexto específico por domínio e um conjunto consistente de regras globais para interações mais assertivas.
