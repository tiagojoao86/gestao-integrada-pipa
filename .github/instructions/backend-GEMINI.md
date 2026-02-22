# Multi-tenant: Filtro Automático por Unidade de Negócio

Implemente a interface marker `UnidadeNegocioFiltravel` nas entidades que exigem restrição por unidade de negócio. Use Specification para aplicar o filtro automaticamente no service.

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

# Endpoints Dedicados

Exponha endpoints REST específicos para vinculação de entidades, filtrando por unidade de negócio e status ativo.

```java
@GetMapping("/titulo/pessoas-disponiveis")
public List<PessoaDTO> listarPessoasDisponiveis() { ... }

@GetMapping("/titulo/planos-disponiveis")
public List<PlanoContasDTO> listarPlanosDisponiveis(@RequestParam UUID unidadeNegocioId) { ... }
```

---

# DTOs com Campos Extras

Inclua campos adicionais necessários para o frontend (ex: unidadeNegocioCodigo) nos DTOs e nas respostas de autenticação.

```java
public class UsuarioUnidadeNegocioDTO {
    private UUID unidadeNegocioId;
    private String unidadeNegocioCodigo;
    private String unidadeNegocioNome;
    private boolean isDefault;
}
```

---

# Gemini - Backend

> **Nota:** Eu sou um assistente de IA. Se eu identificar informações importantes que possam ser adicionadas a este arquivo para melhorar nossas interações futuras (como novos comandos, convenções ou detalhes de arquitetura), irei sugerir atualizações. Sinta-se à vontade para me perguntar como melhorá-lo.

## Resumo do Projeto

Este projeto contém a API RESTful para o sistema Gestão Integrada. É responsável pela lógica de negócios, acesso ao banco de dados e segurança.

## Stack Tecnológica

- Java
- Spring Boot
- Maven
- Flyway (para migrações de banco de dados)
- JPA/Hibernate

## Comandos Essenciais

**Nota:** Execute os comandos a partir do diretório `src/backend`.

- **Compilar o Projeto:**

  ```bash
  ./mvnw compile
  ```

- **Executar os Testes:**

  ```bash
  ./mvnw test
  ```

- **Executar a Aplicação (desenvolvimento):**

  ```bash
  ./mvnw spring-boot:run
  ```

- **Verificação de Estilo (Lint):**
  _(Confirme o comando no `pom.xml`, mas geralmente é algo como:)_

  ```bash
  ./mvnw checkstyle:check
  ```

- **Gerar o Build de Produção:**
  ```bash
  ./mvnw package
  ```

## Arquitetura e Convenções

- A arquitetura segue o padrão Model-View-Controller (MVC), comum em aplicações Spring.
- As entidades do banco de dados estão em `src/main/java/br/com/grupopipa/gestaointegrada/core/entity`.
- Os serviços (lógica de negócio) estão em `src/main/java/br/com/grupopipa/gestaointegrada/core/service`.
- As migrações de banco de dados com Flyway estão em `src/main/resources/db/migration`. Crie novos scripts de migração para qualquer alteração no schema.

### Padrões de Entidades

- **TODA entidade deve estender `BaseEntity`**, que já fornece:
  - `id` do tipo `UUID` (UUIDv7) - **NUNCA use `Long` ou `BigSerial` para IDs**
  - `createdAt`, `updatedAt` (auditoria de timestamps)
  - `createdBy`, `updatedBy` (auditoria de usuários)
- **Migrations devem usar `UUID` para chaves primárias e estrangeiras**, não `BIGSERIAL`

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

## Princípios de Desenvolvimento

### Clean Code

- Escreva código legível e autoexplicativo.
- Use nomes descritivos para classes, métodos e variáveis.
- Mantenha métodos pequenos e com responsabilidade única.
- Evite comentários desnecessários; prefira código que se explica.
- Remova código morto e duplicações.

### Value Objects

- **EVITE tipos primitivos** (`String`, `Integer`, `BigDecimal`, etc.) sempre que possível.
- **Encapsule conceitos de domínio em Value Objects** imutáveis.
- Value Objects de uso comum devem estar em: `br.com.grupopipa.gestaointegrada.core.valueobject`
- Value Objects específicos de um módulo ficam no pacote do módulo (ex: `financeiro.valueobject`)

**Exemplos de Value Objects:**

```java
// ✅ CORRETO - Value Object com validações
@Embeddable
public class Email {
    @Column(name = "email")
    private final String value;

    protected Email() { this.value = null; } // JPA only

    public Email(String value) {
        if (value == null || !value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email inválido: " + value);
        }
        this.value = value.toLowerCase();
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) { /* implementar */ }

    @Override
    public int hashCode() { /* implementar */ }
}

// Uso na entidade
@Entity
public class Pessoa extends BaseEntity {
    @Embedded
    private Email email;

    private Money saldo; // Outro Value Object
}

// ❌ ERRADO - Tipos primitivos sem validação
@Entity
public class Pessoa extends BaseEntity {
    private String email; // Sem validação
    private BigDecimal saldo; // Sem semântica de negócio
}
```

**Value Objects Comuns:**

- `Email` - Validação de formato de email
- `CPF` - Validação de CPF com dígitos verificadores
- `CNPJ` - Validação de CNPJ
- `Money` - Valores monetários com precisão e validações
- `Percentage` - Percentuais (0-100)
- `PhoneNumber` - Telefones brasileiros
- `CEP` - Código postal
- `TaxId` - ID fiscal genérico

**Benefícios:**

- Type safety (compilador detecta erros)
- Validações centralizadas e reutilizáveis
- Expressividade do domínio
- Imutabilidade por design
- Menos bugs de validação

### Validação de Entidades — Padrão Obrigatório

Use a API fluente `Validator.of()` para todas as validações de campos. Para Value Objects, use `ValidationUtils.validateAndGet()` para acumular erros sem interromper na primeira falha.

```java
private static ValidatedData validate(String nomeStr, String descricao, UnidadeNegocio un) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    // Primitivos/objetos: Validator fluente
    Validator.of(descricao, "descrição", violations).notBlank().maxLength(500);
    Validator.of(un, "unidade de negócio", violations).notNull();

    // Value Objects: sempre via factory method + ValidationUtils
    Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);

    // Regras de negócio específicas: BeanValidationMessage com chave registrada em messages.properties
    if (dataVenc != null && dataVenc.isBefore(dataEmissao)) {
        violations.add(new BeanValidationMessage(
            "validation.titulo.dataVencimentoInvalida",
            "Data de vencimento não pode ser anterior à data de emissão."));
    }

    if (!violations.isEmpty()) {
        throw new BeanValidationException("entidade", violations);
    }
    return new ValidatedData(nome, descricao, un);
}
```

**Chaves geradas pelo `Validator.of()`:** `validation.field.required`, `validation.field.notBlank`, `validation.field.maxLength`
**Resolvidas por:** `RestExceptionHandler` via Spring `MessageSource` → `messages.properties` (PT) / `messages_en.properties` (EN)
**NÃO usar:** `new BeanValidationMessage("campo", "mensagem hardcoded")` para validações padrão.

### Domain-Driven Design (DDD)

- **PRIORIZE o uso de DDD** para modelar o domínio rico.
- **Entidades devem conter as regras de negócio** - não crie classes "Business" ou "Validator" separadas.
- **EVITE modelos anêmicos** (entidades que são apenas DTOs com getters/setters).
- As **validações devem estar DENTRO dos objetos de domínio** (entidades e value objects).
- Services devem apenas **orquestrar** a lógica, não conter regras de negócio.

**Arquitetura de Camadas:**

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

**Exemplo - Validações no Domínio:**

```java
// ✅ CORRETO - Lógica de negócio na entidade
@Entity
public class Titulo extends BaseEntity {
    @Embedded
    private Money valorOriginal;

    @Embedded
    private Money valorPago;

    @Enumerated(EnumType.STRING)
    private StatusTitulo status;

    private LocalDate dataVencimento;

    // Método de negócio - validação interna
    public void pagar(Money valor, LocalDate dataPagamento) {
        if (this.status == StatusTitulo.PAGO) {
            throw new DomainException("Título já está pago");
        }

        if (valor.isNegative()) {
            throw new DomainException("Valor de pagamento não pode ser negativo");
        }

        Money novoValorPago = this.valorPago.add(valor);

        if (novoValorPago.isGreaterThan(this.valorOriginal)) {
            throw new DomainException("Valor pago excede valor original do título");
        }

        this.valorPago = novoValorPago;
        this.status = this.valorPago.equals(this.valorOriginal)
            ? StatusTitulo.PAGO
            : StatusTitulo.PARCIAL;
    }

    public Money getSaldo() {
        return valorOriginal.subtract(valorPago);
    }

    public boolean isVencido() {
        return status == StatusTitulo.ABERTO
            && dataVencimento.isBefore(LocalDate.now());
    }
}

// Service apenas orquestra
@Service
public class TituloService {
    public void pagarTitulo(UUID tituloId, Money valor, LocalDate data) {
        Titulo titulo = repository.findById(tituloId)
            .orElseThrow(() -> new NotFoundException("Título não encontrado"));

        // A lógica está NO DOMÍNIO, não no service
        titulo.pagar(valor, data);

        repository.save(titulo);
        eventPublisher.publish(new TituloPagoEvent(titulo));
    }
}

// ❌ ERRADO - Lógica no service (modelo anêmico)
@Service
public class TituloBusinessService {
    public void pagarTitulo(Titulo titulo, BigDecimal valor) {
        // NÃO FAÇA ISSO - validações não devem estar no service
        if (titulo.getStatus().equals("PAGO")) {
            throw new Exception("Já pago");
        }
        if (valor < 0) {
            throw new Exception("Valor negativo");
        }
        // ...mais 50 linhas de validações...

        titulo.setValorPago(valor);
        titulo.setStatus("PAGO");
    }
}
```

**Princípios DDD a Seguir:**

1. **Aggregates:** Identifique raízes de agregados e garanta consistência transacional
2. **Bounded Contexts:** Separe contextos (ex: `financeiro`, `cadastro`, `atendimento`)
3. **Ubiquitous Language:** Use termos do negócio no código (ex: `Titulo`, não `BillPayable`)
4. **Domain Events:** Comunique mudanças importantes entre agregados
5. **Repositories:** Uma interface por agregado raiz
6. **Specifications:** Para queries complexas com lógica de negócio

**Anti-Padrões a Evitar:**

- ❌ Getters/setters públicos sem validação
- ❌ Entidades anêmicas (só dados, sem comportamento)
- ❌ Services fazendo validações de domínio
- ❌ Lógica de negócio nos controllers
- ❌ Usar tipos primitivos ao invés de Value Objects

## Estrutura de Diretórios

- `src/main/java`: Código-fonte da aplicação.
- `src/main/resources`: Arquivos de configuração, scripts SQL e chaves.
- `src/test/java`: Testes unitários e de integração.
