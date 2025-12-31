# ADENDO - ENTITY GENERATION PROMPT
## Padrões Avançados Genéricos

> **Este documento complementa o ENTITY_CREATE_PROMPT.md com padrões genéricos aplicáveis a qualquer entidade.**
> **NÃO inclui regras de negócio específicas de domínio.**

---

## 1. VALUE OBJECTS - Padrão de Uso

### Money Value Object

**❌ ERRADO - Construtor privado**
```java
Money valor = new Money(BigDecimal.valueOf(100.00));
```

**✅ CORRETO - Método fábrica**
```java
Money valor = Money.of(BigDecimal.valueOf(100.00));
```

**Em testes (asserções)**
```java
// ❌ ERRADO
assertEquals(new Money(BigDecimal.valueOf(100.00)), resultado.getValor());

// ✅ CORRETO
assertEquals(Money.of(BigDecimal.valueOf(100.00)), resultado.getValor());
```

### Outros Value Objects

O mesmo padrão se aplica a **todos** os Value Objects que possuem construtor privado:

```java
// ✅ Nome
Nome nome = Nome.of("João Silva");

// ✅ CPF (se opcional)
CPF cpf = null;
if (cpfStr != null && !cpfStr.isBlank()) {
    cpf = ValidationUtils.validateAndGet(() -> CPF.of(cpfStr), violations);
}

// ✅ CNPJ (se opcional)
CNPJ cnpj = null;
if (cnpjStr != null && !cnpjStr.isBlank()) {
    cnpj = ValidationUtils.validateAndGet(() -> CNPJ.of(cnpjStr), violations);
}
```

---

## 2. RELACIONAMENTOS BIDIRECIONAIS

### Quando usar

Use sincronização bidirecional quando:
- Há relacionamento `@ManyToMany` ou `@OneToMany`/@ManyToOne
- A entidade "filha" precisa estar no Set/List da entidade "pai"
- Cálculos ou validações dependem de ambos os lados do relacionamento

### Padrão de Implementação

**No construtor privado da entidade:**

```java
private MinhaEntidade(Set<EntidadeRelacionada> relacionadas, /* outros campos */) {
    this.relacionadas = relacionadas;
    // ... outros campos

    // ⚠️ IMPORTANTE: Sincronizar relacionamento bidirecional
    if (relacionadas != null) {
        relacionadas.forEach(rel -> {
            rel.getMinhasEntidades().add(this); // Adiciona ao Set da outra entidade
            // Se houver lógica adicional, executar aqui
            // rel.atualizar(...);
        });
    }
}
```

### Exemplo Genérico

```java
// Entidade A tem ManyToMany com Entidade B
private EntidadeA(Set<EntidadeB> entidadesB, ...) {
    this.entidadesB = entidadesB;

    // Sincronização: adicionar esta instância ao Set de cada EntidadeB
    if (entidadesB != null) {
        entidadesB.forEach(b -> b.getEntidadesA().add(this));
    }
}
```

**Benefícios:**
- Consistência antes da persistência
- Cálculos em memória funcionam imediatamente
- Evita problemas de lazy loading em testes

---

## 3. CAMPOS TRANSIENTES CALCULADOS

### Quando usar @Transient

Use `@Transient` para campos que:
- São calculados a partir de outros dados
- NÃO devem ser persistidos no banco
- Precisam estar sempre atualizados

### Padrão com Soft Delete

**⚠️ REGRA OBRIGATÓRIA: Sempre filtrar registros deletados ao calcular**

```java
/**
 * Calcula [DESCRIÇÃO] a partir de [FONTE]
 * Campo transiente - não é armazenado no banco de dados
 */
@Transient
public TipoRetorno getValorCalculado() {
    if (colecaoRelacionada == null || colecaoRelacionada.isEmpty()) {
        return valorPadrao(); // ou Money.zero(), 0, etc
    }

    return colecaoRelacionada.stream()
            .filter(item -> item.getDeleted() == null || !item.getDeleted()) // ⚠️ CRÍTICO
            .map(Item::getValor)
            .reduce(valorInicial, TipoRetorno::operacao);
}
```

### Exemplos por Tipo

**Para Money:**
```java
@Transient
public Money getTotalCalculado() {
    if (itens == null || itens.isEmpty()) {
        return Money.zero();
    }
    return itens.stream()
            .filter(item -> item.getDeleted() == null || !item.getDeleted())
            .map(Item::getValor)
            .reduce(Money.zero(), Money::add);
}
```

**Para Integer/Long (contagem):**
```java
@Transient
public Integer getQuantidadeAtiva() {
    if (itens == null) {
        return 0;
    }
    return (int) itens.stream()
            .filter(item -> item.getDeleted() == null || !item.getDeleted())
            .count();
}
```

**Para Boolean (algum ativo?):**
```java
@Transient
public Boolean hasItensAtivos() {
    if (itens == null) {
        return false;
    }
    return itens.stream()
            .anyMatch(item -> (item.getDeleted() == null || !item.getDeleted())
                           && item.isAtivo());
}
```

---

## 4. SOFT DELETE EM PROJEÇÕES SQL (JPA Criteria API)

### Quando usar

Use CASE WHEN em queries quando:
- Está criando projeções customizadas (não carrega entidade completa)
- Precisa calcular agregações (SUM, COUNT, etc) no SQL
- Quer performance otimizada (filtrar no banco, não em memória)

### Padrão para Agregações

**Para SUM com filtro de soft delete:**

```java
// Em CustomRepositoryImpl - método buildQuery()
Expression<BigDecimal> valorCalculadoExpr = cb.coalesce(
    cb.sum(
        cb.<BigDecimal>selectCase()
            .when(
                cb.or(
                    cb.isFalse(joinTabela.get("deleted")),
                    cb.isNull(joinTabela.get("deleted"))
                ),
                joinTabela.get("campoValor").get("value") // Para Value Objects
                // OU apenas: joinTabela.get("campoValor") para tipos primitivos
            )
            .otherwise(BigDecimal.ZERO) // ou 0, 0L, etc conforme tipo
    ),
    BigDecimal.ZERO // valor padrão se não houver registros
);
```

**Para COUNT com filtro de soft delete:**

```java
Expression<Long> quantidadeExpr = cb.coalesce(
    cb.sum(
        cb.<Long>selectCase()
            .when(
                cb.or(
                    cb.isFalse(joinTabela.get("deleted")),
                    cb.isNull(joinTabela.get("deleted"))
                ),
                cb.literal(1L)
            )
            .otherwise(0L)
    ),
    0L
);
```

### Estrutura Completa de Query com Projeção

```java
private CriteriaQuery<MeuProjecaoDTO> buildQuery(
    CriteriaBuilder cb,
    Root<MinhaEntidade> root,
    Predicate... predicates
) {
    // 1. LEFT JOIN (não FETCH - é projeção)
    Join<MinhaEntidade, EntidadeRelacionada> joinRelacionada =
        root.join("entidadesRelacionadas", JoinType.LEFT);

    // 2. Expressão com CASE WHEN para filtrar soft deletes
    Expression<BigDecimal> valorExpr = cb.coalesce(
        cb.sum(
            cb.<BigDecimal>selectCase()
                .when(
                    cb.or(
                        cb.isFalse(joinRelacionada.get("deleted")),
                        cb.isNull(joinRelacionada.get("deleted"))
                    ),
                    joinRelacionada.get("valor").get("value")
                )
                .otherwise(BigDecimal.ZERO)
        ),
        BigDecimal.ZERO
    );

    // 3. Criar CriteriaQuery com select de múltiplos campos
    CriteriaQuery<MeuProjecaoDTO> query = cb.createQuery(MeuProjecaoDTO.class);
    query.select(cb.construct(
        MeuProjecaoDTO.class,
        root.get("id"),
        root.get("nome").get("value"), // Se for Value Object
        valorExpr // Campo calculado
    ));

    // 4. WHERE (predicates)
    if (predicates.length > 0) {
        query.where(cb.and(predicates));
    }

    // 5. GROUP BY (necessário quando usa agregações)
    query.groupBy(root.get("id"));

    return query;
}
```

---

## 5. TESTES - MOCK DE SOFT DELETE

### Testes Unitários - Delete Service

**⚠️ IMPORTANTE: Soft delete chama `findById()` + `save()`, NÃO `deleteById()`**

```java
@Test
@DisplayName("Deve deletar [entidade]")
void deveDeletar[Entidade]() {
    // Given
    UUID id = UUID.randomUUID();

    // ⚠️ OBRIGATÓRIO: Mock findById e save
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(any(MinhaEntidade.class))).thenReturn(entity);

    // When
    UUID resultado = service.delete(id);

    // Then
    assertEquals(id, resultado);

    // ⚠️ Verificar que chamou findById e save, NÃO deleteById
    verify(repository, times(1)).findById(id);
    verify(repository, times(1)).save(any(MinhaEntidade.class));
}
```

### Testes de Integração - Soft Delete em Relacionamentos

**Para testar que soft deletes são filtrados em cálculos:**

```java
@Test
@DisplayName("Deve desconsiderar registros deletados ao calcular [campo]")
void deveDesconsiderarDeletadosAoCalcular() {
    // Given - criar entidades relacionadas
    EntidadeFilha ativa = new EntidadeFilha.Builder()
        .valor(Money.of(BigDecimal.valueOf(100)))
        .build();
    entityManager.persist(ativa);

    EntidadeFilha deletada = new EntidadeFilha.Builder()
        .valor(Money.of(BigDecimal.valueOf(200)))
        .build();
    entityManager.persist(deletada);

    // Marcar como deletada usando reflexão (simula soft delete)
    try {
        Field deletedField = BaseEntity.class.getDeclaredField("deleted");
        deletedField.setAccessible(true);
        deletedField.set(deletada, true);
    } catch (Exception e) {
        fail("Erro ao configurar soft delete: " + e.getMessage());
    }

    EntidadePai pai = new EntidadePai.Builder()
        .filhas(Set.of(ativa, deletada))
        .build();
    entityManager.persist(pai);
    entityManager.flush();

    // When - calcular valor total
    Money total = pai.getValorTotal();

    // Then - deve considerar apenas a ativa
    assertEquals(Money.of(BigDecimal.valueOf(100)), total);
}
```

---

## 6. TESTES - DEPENDÊNCIAS EM INTEGRATION TESTS

### Ordem de Criação de Entidades

**Sempre criar entidades pai antes de filhas:**

```java
@BeforeEach
void setUp() {
    // 1. Criar entidades independentes (sem FK)
    UnidadeNegocio unidade = new UnidadeNegocio.Builder()
        .codigo("UN001")
        .nome("Unidade Teste")
        .build();
    entityManager.persist(unidade);

    // 2. Criar entidades que dependem das anteriores
    Categoria categoria = new Categoria.Builder()
        .codigo("CAT001")
        .nome("Categoria Teste")
        .unidadeNegocio(unidade) // FK
        .build();
    entityManager.persist(categoria);

    // 3. Criar entidade principal (depende de todas acima)
    MinhaEntidade entidade = new MinhaEntidade.Builder()
        .nome("Teste")
        .categoria(categoria) // FK
        .unidadeNegocio(unidade) // FK
        .build();
    entityManager.persist(entidade);

    // 4. Flush para garantir que foram persistidas
    entityManager.flush();
}
```

### Adicionar rateioAutomatico se aplicável

**Para entidades financeiras com rateio de setores:**

```java
MinhaEntidadeFinanceira entidade = new MinhaEntidadeFinanceira.Builder()
    .nome("Teste")
    .valorOriginal(Money.of(BigDecimal.valueOf(1000)))
    .rateioAutomatico(false) // ⚠️ Obrigatório em entidades com rateio
    .build();
```

---

## 7. VALIDAÇÃO COM VALUE OBJECTS OPCIONAIS

### Padrão para VOs que podem ser null

```java
private static ValidatedData validate(
    String nomeStr,
    String cpfStr,     // Opcional
    String cnpjStr     // Opcional
) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    // Campo obrigatório - usar ValidationUtils.validateAndGet
    Nome nome = ValidationUtils.validateAndGet(
        () -> Nome.of(nomeStr),
        violations
    );

    // Campo OPCIONAL - verificar se está presente antes de validar
    CPF cpf = null;
    if (cpfStr != null && !cpfStr.isBlank()) {
        cpf = ValidationUtils.validateAndGet(
            () -> CPF.of(cpfStr),
            violations
        );
    }

    // Campo OPCIONAL - mesmo padrão
    CNPJ cnpj = null;
    if (cnpjStr != null && !cnpjStr.isBlank()) {
        cnpj = ValidationUtils.validateAndGet(
            () -> CNPJ.of(cnpjStr),
            violations
        );
    }

    if (!violations.isEmpty()) {
        throw new BeanValidationException("minhaEntidade", violations);
    }

    return new ValidatedData(nome, cpf, cnpj);
}
```

---

## CHECKLIST - Padrões Genéricos

Use este checklist ao criar qualquer entidade:

### Value Objects
- [ ] Usar `Money.of()`, `Nome.of()`, etc (nunca `new`)
- [ ] Usar `ValidationUtils.validateAndGet()` para VOs obrigatórios
- [ ] Verificar `!= null && !isBlank()` antes de criar VOs opcionais

### Relacionamentos
- [ ] Sincronizar bidirecionais no construtor privado (se aplicável)
- [ ] Usar LEFT JOIN (não FETCH) em projeções customizadas

### Soft Delete
- [ ] Filtrar `.getDeleted() == null || !.getDeleted()` em campos `@Transient`
- [ ] Usar CASE WHEN em agregações SQL (SUM, COUNT)
- [ ] Criar índice `idx_[tabela]_deleted` na migration

### Testes Unitários
- [ ] Mock `findById()` e `save()` para testes de delete
- [ ] Usar `Money.of()` em asserções (não `new Money()`)
- [ ] Verificar que variáveis declaradas são realmente usadas no teste

### Testes Integração
- [ ] Criar entidades pai antes de filhas no `@BeforeEach`
- [ ] Usar reflexão para simular soft delete quando necessário
- [ ] Adicionar `rateioAutomatico(false)` se entidade tiver rateio

### Qualidade de Código (Backend)
- [ ] Usar imports no topo (NUNCA FQCN inline)
- [ ] Remover imports não utilizados
- [ ] Remover variáveis declaradas mas não referenciadas
- [ ] Executar `./mvnw test` antes de commit para validar

### Qualidade de Código (Frontend/TypeScript)
- [ ] Evitar `any` - usar tipos explícitos (`Response<T>`, `HttpErrorResponse`)
- [ ] Usar `as unknown as Type` para mocks parciais ao invés de `as any`
- [ ] Adicionar tipos explícitos em callbacks (`.find()`, `.filter()`, `.map()`)
- [ ] Import de tipos necessários (`Response`, `HttpErrorResponse`)
- [ ] Remover imports não utilizados
- [ ] Executar `npm test` antes de commit para validar

---

## 8. BOAS PRÁTICAS DE CÓDIGO

### Imports vs FQCN (Fully Qualified Class Names)

**❌ ERRADO - Usar FQCN inline**
```java
// NÃO faça isso
br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException exception =
    assertThrows(br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException.class,
                () -> service.save(dto));

// NÃO faça isso
TituloCategoria categoria =
    new br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria.Builder()
        .tipo(br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaTipoEnum.DESPESA)
        .build();
```

**✅ CORRETO - Usar imports no topo do arquivo**
```java
// No topo do arquivo
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;
import br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaTipoEnum;

// No código
BeanValidationException exception =
    assertThrows(BeanValidationException.class,
                () -> service.save(dto));

TituloCategoria categoria =
    new TituloCategoria.Builder()
        .tipo(TituloCategoriaTipoEnum.DESPESA)
        .build();
```

**Benefícios:**
- ✅ Código mais limpo e legível
- ✅ Facilita refatoração
- ✅ IDEs detectam imports não utilizados
- ✅ Segue convenções Java padrão

**TypeScript/Angular - Mesmo Padrão:**
```typescript
// ❌ ERRADO - Inline type assertions complexas
const serviceMock = {
  findById: jest.fn().mockReturnValue(
    of({ body: new MyDTO() } as any)  // Evitar 'as any'
  )
};

// ✅ CORRETO - Import e tipos explícitos
import { Response } from '../../../base/model/response';
import { HttpErrorResponse } from '@angular/common/http';

const serviceMock = {
  findById: jest.fn().mockReturnValue(
    of({ body: new MyDTO() } as Response<MyDTO>)
  )
};
```

### Variáveis Não Utilizadas em Testes

**⚠️ ATENÇÃO: Revisar variáveis declaradas mas não usadas**

```java
// ❌ ERRADO - Variável declarada mas não usada
MovimentacaoFinanceira mov1 = new MovimentacaoFinanceira.Builder()
    .valor(Money.of(BigDecimal.valueOf(300.00)))
    .build();

MovimentacaoFinanceira mov2 = new MovimentacaoFinanceira.Builder()
    .valor(Money.of(BigDecimal.valueOf(200.00)))
    .build();

// Teste só usa mov2, mov1 nunca é referenciada
assertEquals(Money.of(BigDecimal.valueOf(200.00)), titulo.getValorPago());
```

**✅ CORRETO - Opção 1: Remover atribuição se não for necessária**
```java
// Se a movimentação é adicionada automaticamente ao título via construtor
new MovimentacaoFinanceira.Builder()
    .titulos(Set.of(titulo))
    .valor(Money.of(BigDecimal.valueOf(300.00)))
    .build();

MovimentacaoFinanceira mov2 = new MovimentacaoFinanceira.Builder()
    .titulos(Set.of(titulo))
    .valor(Money.of(BigDecimal.valueOf(200.00)))
    .build();

// Ambas são consideradas no cálculo
assertEquals(Money.of(BigDecimal.valueOf(500.00)), titulo.getValorPago());
```

**✅ CORRETO - Opção 2: Usar a variável no teste**
```java
MovimentacaoFinanceira mov1 = new MovimentacaoFinanceira.Builder()
    .valor(Money.of(BigDecimal.valueOf(300.00)))
    .build();

MovimentacaoFinanceira mov2 = new MovimentacaoFinanceira.Builder()
    .valor(Money.of(BigDecimal.valueOf(200.00)))
    .build();

// Testar ambas explicitamente
assertFalse(mov1.getDeleted());
assertTrue(mov2.getDeleted());
assertEquals(Money.of(BigDecimal.valueOf(300.00)), mov1.getValor());
```

### Evitar `any` em Testes TypeScript/Angular

**⚠️ PROBLEMA: Uso excessivo de `any` oculta erros de tipo**

```typescript
// ❌ ERRADO - Perda total de type safety
const mockError = { status: 400, error: { message: 'Erro' } };
callbacks.onError(mockError as any); // Qualquer coisa passa!

unidadeService.findById.mockReturnValue(
  of({ body: mockUnidade } as any) // Sem validação de estrutura
);
```

**✅ CORRETO - Tipos explícitos com type assertions mínimas**

```typescript
import { Response } from '../../../base/model/response';
import { HttpErrorResponse } from '@angular/common/http';

// Mock de Response com tipo genérico
unidadeService.findById.mockReturnValue(
  of({ body: mockUnidade } as Response<UnidadeNegocioDTO>)
);

// Mock de erro HTTP (tipo parcial requer unknown intermediário)
const mockError = {
  status: 400,
  error: { message: 'unidade-negocio.codigo.unique' }
};
callbacks.onError(mockError as unknown as HttpErrorResponse);
```

**Por que `as unknown as HttpErrorResponse`?**
- `HttpErrorResponse` tem muitos campos obrigatórios (headers, url, etc)
- Em testes, só precisamos mockar os campos relevantes (status, error)
- `unknown` é mais seguro que `any` - força verificação de tipo intermediária
- TypeScript permite conversão `unknown → TipoEspecífico` com verificação

**Padrão Recomendado:**
```typescript
// 1. Import de tipos necessários
import { Response } from '../../../base/model/response';
import { HttpErrorResponse } from '@angular/common/http';

// 2. Mocks de sucesso - tipo explícito
service.save.mockImplementation(
  (_data: MyDTO, callbacks: ExecutionCallbacks<MyDTO>) => {
    if (callbacks.onSuccess) {
      callbacks.onSuccess(new MyDTO(...));
    }
  }
);

// 3. Mocks de erro - as unknown as HttpErrorResponse
service.save.mockImplementation(
  (_data: MyDTO, callbacks: ExecutionCallbacks<MyDTO>) => {
    if (callbacks.onError) {
      const mockError = { status: 400, error: { message: 'erro.validacao' } };
      callbacks.onError(mockError as unknown as HttpErrorResponse);
    }
  }
);
```

**Benefícios:**
- ✅ Type safety mantido onde possível
- ✅ Erros de tipo detectados em compile-time
- ✅ Autocomplete e refatoração funcionam
- ✅ Código auto-documentado (tipos explicitam estrutura)
- ✅ Evita bugs causados por estruturas incorretas

### Tipos Explícitos em Callbacks e Arrow Functions

**⚠️ PROBLEMA: Parâmetro implícito `any` em callbacks**

```typescript
// ❌ ERRADO - TypeScript infere 'any' para parâmetro 'u'
callArgs.unidadesNegocio?.find((u) => u.unidadeNegocioId === '1')
```

**✅ CORRETO - Tipo explícito no parâmetro**

```typescript
// Tipo explícito evita 'any' implícito
callArgs.unidadesNegocio?.find((u: UsuarioUnidadeNegocioDTO) => u.unidadeNegocioId === '1')

// Ou destructuring com tipo
callArgs.unidadesNegocio?.find(({ unidadeNegocioId }: UsuarioUnidadeNegocioDTO) => unidadeNegocioId === '1')
```

**Quando adicionar tipos explícitos:**
- ✅ Callbacks de `.find()`, `.filter()`, `.map()`, etc quando TypeScript não consegue inferir
- ✅ Arrow functions em testes que acessam propriedades de objetos
- ✅ Event handlers que recebem objetos customizados

---

## QUANDO CONSULTAR ESTE ADENDO

Consulte este documento quando:
- ✅ Estiver criando campos calculados (`@Transient`)
- ✅ Precisar validar Value Objects opcionais
- ✅ Implementar relacionamentos bidirecionais
- ✅ Criar projeções customizadas com agregações
- ✅ Escrever testes que envolvem soft delete
- ✅ Tiver dúvidas sobre uso correto de Value Objects
- ✅ Receber warnings de imports não utilizados ou variáveis não referenciadas

**NÃO consulte** para regras de negócio específicas de domínio (ex: cálculo de juros, validação de datas de vencimento, etc).
