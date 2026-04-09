# Plano de Implementação — Módulo de Atendimento

**Contexto:** Clínica especializada em atendimento de crianças com autismo.  
**Data de criação:** 2026-03-30  
**Status:** Aguardando implementação

> **Regras críticas a seguir em cada passo:**  
> - CLAUDE.md (regras gerais + padrões de código)  
> - ENTITY_CREATE_PROMPT.md (template completo de entidade)  
> - MEMORY.md: serviços frontend usam `super(inject(HttpClient), inject(MessageService))` — **apenas 2 args**, sem `BackendMessageService`  

---

## Domínio

```
Backend:  br.com.grupopipa.gestaointegrada.atendimento.*
Frontend: src/frontend/src/app/components/atendimento/
```

---

## Ordem de implementação

| # | Entidade | Tipo | Depende de |
|---|----------|------|-----------|
| 1 | Alterar `Pessoa` | modificação | — |
| 2 | `Profissional` | nova entidade | Pessoa |
| 3 | `Convenio` | nova entidade | Pessoa |
| 4 | `ConvenioCategoria` | nova entidade | Convenio |
| 5 | `Procedimento` | nova entidade | — |
| 6 | `CodigoConvenio` | nova entidade | Procedimento, Convenio |
| 7 | `Tabela` + `TabelaItem` | nova entidade | Procedimento |
| 8 | `Atendimento` | nova entidade | tudo acima + Setor |

---

## Passo 1 — Alterar `Pessoa` (adicionar responsável)

### Objetivo
Adicionar o campo `responsavel` (auto-referência para Pessoa) ao cadastro de pessoa.
No atendimento esse valor é carregado como padrão, mas pode ser sobrescrito.

### Backend — `Pessoa.java`
- Adicionar campo: `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "responsavel_id", ...) private Pessoa responsavel;`
- Constraint: `fk_pessoa_responsavel` → registrar em `DatabaseConstraintsEnum`
- Adicionar ao `Builder`: `public Builder responsavel(Pessoa responsavel)`
- Adicionar ao `atualizar(...)`: parâmetro `Pessoa responsavel`
- Adicionar getter: `getResponsavel()` → retorna `Pessoa`
- `getResponsavelId()` → retorna `UUID` (útil para DTO)

### Backend — `PessoaDTO` e `PessoaGridDTO`
- `PessoaDTO`: adicionar `responsavelId` (UUID) e `responsavelNome` (String, somente leitura)
- Não expor o objeto Pessoa inteiro para evitar recursão

### Backend — `PessoaServiceImpl`
- Em `mergeEntityAndDTO`: buscar `Pessoa` pelo `responsavelId` via `PessoaRepository` e setar

### Backend — Migration
```
Arquivo: src/backend/src/main/resources/db/tenant-migrations/
Nome: V{YYYYMMDDHHMMSS}__add_responsavel_to_pessoa.sql
```
```sql
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'pessoa' AND column_name = 'responsavel_id'
  ) THEN
    ALTER TABLE pessoa ADD COLUMN responsavel_id UUID;
    ALTER TABLE pessoa ADD CONSTRAINT fk_pessoa_responsavel
      FOREIGN KEY (responsavel_id) REFERENCES pessoa(id);
  END IF;
END $$;
```

### Backend — `DatabaseConstraintsEnum`
```java
FK_PESSOA_RESPONSAVEL("pessoa.responsavel.invalid")
```

### Frontend — `pessoa.dto.ts`
- Adicionar `responsavelId?: string`
- Adicionar `responsavelNome?: string`

### Frontend — `pessoa-detalhe.component`
- Adicionar campo `EntitySearch` de Pessoa para selecionar o responsável
- Exibir somente para `tipoPessoa === FISICA`

### Checklist Passo 1
- [ ] `Pessoa.java` — campo `responsavel`, Builder, `atualizar()`, getter
- [ ] `PessoaDTO.java` — campos `responsavelId`, `responsavelNome`
- [ ] `PessoaServiceImpl.java` — buscar e setar responsável
- [ ] Migration SQL idempotente
- [ ] `DatabaseConstraintsEnum` — `FK_PESSOA_RESPONSAVEL`
- [ ] `PessoaDTO.ts` / `PessoaGridDTO.ts` — campos adicionados
- [ ] `pessoa-detalhe.component` — campo de seleção de responsável

---

## Passo 2 — `Profissional`

### Modelo
```
Pacote backend: br.com.grupopipa.gestaointegrada.atendimento.profissional.entity
Tabela SQL:     profissional
```

### Campos
| Campo | Tipo Java | Coluna SQL | Obrigatório |
|-------|-----------|------------|-------------|
| `pessoa` | `@ManyToOne Pessoa` | `pessoa_id UUID` | Sim |
| `conselho` | `String` | `conselho VARCHAR(20)` | Sim (ex: CRP, CRM, CREFONO) |
| `codigoConselho` | `String` | `codigo_conselho VARCHAR(30)` | Sim (ex: 12345/SP) |
| `tipoRemuneracao` | `TipoRemuneracao` (enum) | `tipo_remuneracao VARCHAR(20)` | Sim |
| `banco` | `String` | `banco VARCHAR(100)` | Não |
| `conta` | `String` | `conta VARCHAR(50)` | Não |
| `chavePix` | `String` | `chave_pix VARCHAR(150)` | Não |
| `ativo` | `Boolean` | `ativo BOOLEAN NOT NULL DEFAULT TRUE` | Sim |

### Enum `TipoRemuneracao`
```java
CLT, PJ, HORA
```

### Constraints SQL
- `fk_profissional_pessoa` → `profissional.pessoa_id → pessoa.id`
- `uk_profissional_pessoa` → `(pessoa_id)` — uma pessoa pode ser profissional só uma vez
- `ck_profissional_tipo_remuneracao` → valores válidos do enum

### `DatabaseConstraintsEnum`
```java
FK_PROFISSIONAL_PESSOA("profissional.pessoa.invalid"),
UK_PROFISSIONAL_PESSOA("profissional.pessoa.unique")
```

### Migration
```
Arquivo: V{YYYYMMDDHHMMSS}__create_profissional.sql
```
```sql
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'profissional') THEN
    CREATE TABLE profissional (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      pessoa_id UUID NOT NULL,
      conselho VARCHAR(20) NOT NULL,
      codigo_conselho VARCHAR(30) NOT NULL,
      tipo_remuneracao VARCHAR(20) NOT NULL,
      banco VARCHAR(100),
      conta VARCHAR(50),
      chave_pix VARCHAR(150),
      ativo BOOLEAN NOT NULL DEFAULT TRUE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT fk_profissional_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
      CONSTRAINT uk_profissional_pessoa UNIQUE (pessoa_id)
    );
    CREATE INDEX idx_profissional_deleted ON profissional (deleted);
  END IF;
END $$;

-- Módulo
INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO_PROFISSIONAL', 'Profissional', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO_PROFISSIONAL');
```

### `DataSourceConfig.ENTITY_PACKAGES`
```java
"br.com.grupopipa.gestaointegrada.atendimento.profissional.entity",
```

### `SystemModuleKey` (frontend enum)
```typescript
ATENDIMENTO_PROFISSIONAL = 'ATENDIMENTO_PROFISSIONAL'
```

### Arquivos Backend a criar
```
atendimento/profissional/
  entity/Profissional.java
  TipoRemuneracao.java                ← enum
  ProfissionalRepository.java
  ProfissionalService.java
  ProfissionalServiceImpl.java
  ProfissionalController.java
  ProfissionalConstants.java
  dto/ProfissionalDTO.java
  dto/ProfissionalGridDTO.java
```

### Arquivos Frontend a criar
```
components/atendimento/profissional/
  profissional.component.ts/html/css
  grid/profissional-grid.component.ts/html/css
  detalhe/profissional-detalhe.component.ts/html/css
  model/profissional.dto.ts
  model/profissional-grid.dto.ts
  model/tipo-remuneracao.enum.ts
  profissional.service.ts
```

### Checklist Passo 2
- [ ] `Profissional.java` — Builder, ValidatedData, validate(), atualizar()
- [ ] `TipoRemuneracao.java` (enum Java)
- [ ] Migration SQL idempotente + modulo INSERT
- [ ] `DatabaseConstraintsEnum` — FK + UK
- [ ] `DataSourceConfig.ENTITY_PACKAGES`
- [ ] `ProfissionalDTO.java` + `ProfissionalGridDTO.java` (Lombok)
- [ ] `ProfissionalRepository.java`
- [ ] `ProfissionalService.java` + `ProfissionalServiceImpl.java`
- [ ] `ProfissionalController.java` com `@PreAuthorize` incluindo `getAuditInfo`
- [ ] `ProfissionalConstants.java`
- [ ] Frontend: DTOs (classes com @Exclude/@Expose)
- [ ] Frontend: `profissional.service.ts` (2 args no super)
- [ ] Frontend: componentes main, grid (com AuditInfo + confirmação exclusão), detalhe
- [ ] `SystemModuleKey` + rotas + menu
- [ ] i18n `messages.xlf` (pt-BR) + `messages.en.xlf`
- [ ] Testes backend: `ProfissionalRepositoryTest` + `ProfissionalServiceImplTest`
- [ ] Testes frontend: `profissional-detalhe.component.spec.ts`

---

## Passo 3 — `Convenio`

### Modelo
```
Pacote backend: br.com.grupopipa.gestaointegrada.atendimento.convenio.entity
Tabela SQL:     convenio
```

### Campos
| Campo | Tipo Java | Coluna SQL | Obrigatório |
|-------|-----------|------------|-------------|
| `nome` | `Nome` (VO) | `nome VARCHAR(100)` | Sim |
| `pessoa` | `@ManyToOne Pessoa` | `pessoa_id UUID NOT NULL` | Sim |
| `registroAns` | `String` | `registro_ans VARCHAR(20)` | Não |
| `ativo` | `Boolean` | `ativo BOOLEAN NOT NULL DEFAULT TRUE` | Sim |

### Constraints SQL
- `uk_convenio_nome` → `(nome)` — nome único por tenant
- `uk_convenio_registro_ans` → `(registro_ans)` WHERE `registro_ans IS NOT NULL`
- `fk_convenio_pessoa` → `convenio.pessoa_id → pessoa.id`

### `DatabaseConstraintsEnum`
```java
UK_CONVENIO_NOME("convenio.nome.unique"),
UK_CONVENIO_REGISTRO_ANS("convenio.registroAns.unique"),
FK_CONVENIO_PESSOA("convenio.pessoa.invalid")
```

### Migration
```
Arquivo: V{YYYYMMDDHHMMSS}__create_convenio.sql
```
```sql
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'convenio') THEN
    CREATE TABLE convenio (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      nome VARCHAR(100) NOT NULL,
      pessoa_id UUID NOT NULL,
      registro_ans VARCHAR(20),
      ativo BOOLEAN NOT NULL DEFAULT TRUE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT uk_convenio_nome UNIQUE (nome),
      CONSTRAINT fk_convenio_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
    );
    CREATE UNIQUE INDEX uk_convenio_registro_ans ON convenio (registro_ans)
      WHERE registro_ans IS NOT NULL;
    CREATE INDEX idx_convenio_deleted ON convenio (deleted);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO_CONVENIO', 'Convênio', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO_CONVENIO');
```

### `DataSourceConfig.ENTITY_PACKAGES`
```java
"br.com.grupopipa.gestaointegrada.atendimento.convenio.entity",
```

### Arquivos Backend a criar
```
atendimento/convenio/
  entity/Convenio.java
  ConvenioRepository.java
  ConvenioService.java
  ConvenioServiceImpl.java
  ConvenioController.java
  ConvenioConstants.java
  dto/ConvenioDTO.java
  dto/ConvenioGridDTO.java
```

### Arquivos Frontend a criar
```
components/atendimento/convenio/
  convenio.component.ts/html/css
  grid/convenio-grid.component.ts/html/css
  detalhe/convenio-detalhe.component.ts/html/css
  model/convenio.dto.ts
  model/convenio-grid.dto.ts
  convenio.service.ts
```

### Checklist Passo 3
- [ ] `Convenio.java` — Builder, ValidatedData, validate(), atualizar()
- [ ] Migration SQL idempotente + modulo INSERT
- [ ] `DatabaseConstraintsEnum` — UK nome, UK registroAns, FK pessoa
- [ ] `DataSourceConfig.ENTITY_PACKAGES`
- [ ] DTOs backend (Lombok)
- [ ] Repository, Service, ServiceImpl, Controller, Constants
- [ ] Frontend: DTOs classes, service (2 args), componentes
- [ ] `SystemModuleKey` + rotas + menu
- [ ] i18n
- [ ] Testes backend + frontend

---

## Passo 4 — `ConvenioCategoria`

### Modelo
```
Pacote backend: br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity
Tabela SQL:     convenio_categoria
```

### Campos
| Campo | Tipo Java | Coluna SQL | Obrigatório |
|-------|-----------|------------|-------------|
| `convenio` | `@ManyToOne Convenio` | `convenio_id UUID NOT NULL` | Sim |
| `nome` | `Nome` (VO) | `nome VARCHAR(100)` | Sim |
| `codigoAnsPlano` | `String` | `codigo_ans_plano VARCHAR(20)` | Não |
| `ativo` | `Boolean` | `ativo BOOLEAN NOT NULL DEFAULT TRUE` | Sim |

### Constraints SQL
- `fk_convenio_categoria_convenio` → `convenio_categoria.convenio_id → convenio.id`
- `uk_convenio_categoria_nome_convenio` → `(convenio_id, nome)` — nome único por convênio

### `DatabaseConstraintsEnum`
```java
FK_CONVENIO_CATEGORIA_CONVENIO("convenioCategoria.convenio.invalid"),
UK_CONVENIO_CATEGORIA_NOME_CONVENIO("convenioCategoria.nome.unique")
```

### Migration
```
Arquivo: V{YYYYMMDDHHMMSS}__create_convenio_categoria.sql
```
```sql
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'convenio_categoria') THEN
    CREATE TABLE convenio_categoria (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      convenio_id UUID NOT NULL,
      nome VARCHAR(100) NOT NULL,
      codigo_ans_plano VARCHAR(20),
      ativo BOOLEAN NOT NULL DEFAULT TRUE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT fk_convenio_categoria_convenio FOREIGN KEY (convenio_id) REFERENCES convenio(id),
      CONSTRAINT uk_convenio_categoria_nome_convenio UNIQUE (convenio_id, nome)
    );
    CREATE INDEX idx_convenio_categoria_deleted ON convenio_categoria (deleted);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO_CONVENIO_CATEGORIA', 'Categoria de Convênio', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO_CONVENIO_CATEGORIA');
```

### `DataSourceConfig.ENTITY_PACKAGES`
```java
"br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity",
```

### Checklist Passo 4
- [ ] `ConvenioCategoria.java` — Builder, ValidatedData, validate(), atualizar()
- [ ] Migration SQL idempotente + modulo INSERT
- [ ] `DatabaseConstraintsEnum`
- [ ] `DataSourceConfig.ENTITY_PACKAGES`
- [ ] DTOs backend
- [ ] Repository, Service, ServiceImpl, Controller, Constants
- [ ] Frontend: DTOs, service, componentes (detalhe inclui EntitySearch de Convênio)
- [ ] `SystemModuleKey` + rotas + menu
- [ ] i18n
- [ ] Testes

---

## Passo 5 — `Procedimento`

### Modelo
```
Pacote backend: br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity
Tabela SQL:     procedimento
```

### Campos
| Campo | Tipo Java | Coluna SQL | Obrigatório |
|-------|-----------|------------|-------------|
| `codigo` | `String` | `codigo VARCHAR(30) NOT NULL` | Sim (código interno da clínica) |
| `codigoTiss` | `String` | `codigo_tiss VARCHAR(20)` | Não (padrão TISS — intercâmbio eletrônico ANS) |
| `codigoTuss` | `String` | `codigo_tuss VARCHAR(20)` | Não (padrão TUSS — Terminologia Unificada) |
| `descricao` | `String` | `descricao VARCHAR(200) NOT NULL` | Sim (nome/descrição do procedimento) |
| `ativo` | `Boolean` | `ativo BOOLEAN NOT NULL DEFAULT TRUE` | Sim |

### Constraints SQL
- `uk_procedimento_codigo` → `(codigo)`

### `DatabaseConstraintsEnum`
```java
UK_PROCEDIMENTO_CODIGO("procedimento.codigo.unique")
```

### Migration
```
Arquivo: V{YYYYMMDDHHMMSS}__create_procedimento.sql
```
```sql
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'procedimento') THEN
    CREATE TABLE procedimento (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      codigo VARCHAR(30) NOT NULL,
      codigo_tiss VARCHAR(20),
      codigo_tuss VARCHAR(20),
      descricao VARCHAR(200) NOT NULL,
      ativo BOOLEAN NOT NULL DEFAULT TRUE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT uk_procedimento_codigo UNIQUE (codigo)
    );
    CREATE INDEX idx_procedimento_deleted ON procedimento (deleted);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO_PROCEDIMENTO', 'Procedimento', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO_PROCEDIMENTO');
```

### `DataSourceConfig.ENTITY_PACKAGES`
```java
"br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity",
```

### Checklist Passo 5
- [ ] `Procedimento.java` — Builder, ValidatedData, validate(), atualizar()
- [ ] Migration SQL idempotente + modulo INSERT
- [ ] `DatabaseConstraintsEnum`
- [ ] `DataSourceConfig.ENTITY_PACKAGES`
- [ ] DTOs backend
- [ ] Repository, Service, ServiceImpl, Controller, Constants
- [ ] Frontend: DTOs, service, componentes
- [ ] `SystemModuleKey` + rotas + menu
- [ ] i18n
- [ ] Testes

---

## Passo 6 — `CodigoConvenio`

### Modelo
```
Pacote backend: br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.entity
Tabela SQL:     codigo_convenio
```

### Objetivo
Permite registrar o código específico que um convênio utiliza para um determinado procedimento.
Quando presente, esse código deve ser usado no faturamento ao invés dos códigos TISS/TUSS do procedimento.

### Campos
| Campo | Tipo Java | Coluna SQL | Obrigatório |
|-------|-----------|------------|-------------|
| `convenio` | `@ManyToOne Convenio` | `convenio_id UUID NOT NULL` | Sim |
| `procedimento` | `@ManyToOne Procedimento` | `procedimento_id UUID NOT NULL` | Sim |
| `codigo` | `String` | `codigo VARCHAR(30) NOT NULL` | Sim |

### Constraints SQL
- `fk_codigo_convenio_convenio` → `codigo_convenio.convenio_id → convenio.id`
- `fk_codigo_convenio_procedimento` → `codigo_convenio.procedimento_id → procedimento.id`
- `uk_codigo_convenio_convenio_procedimento` → `(convenio_id, procedimento_id)` — um código por procedimento por convênio

### `DatabaseConstraintsEnum`
```java
FK_CODIGO_CONVENIO_CONVENIO("codigoConvenio.convenio.invalid"),
FK_CODIGO_CONVENIO_PROCEDIMENTO("codigoConvenio.procedimento.invalid"),
UK_CODIGO_CONVENIO_CONVENIO_PROCEDIMENTO("codigoConvenio.procedimento.duplicate")
```

### Migration
```
Arquivo: V{YYYYMMDDHHMMSS}__create_codigo_convenio.sql
```
```sql
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'codigo_convenio') THEN
    CREATE TABLE codigo_convenio (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      convenio_id UUID NOT NULL,
      procedimento_id UUID NOT NULL,
      codigo VARCHAR(30) NOT NULL,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT fk_codigo_convenio_convenio FOREIGN KEY (convenio_id) REFERENCES convenio(id),
      CONSTRAINT fk_codigo_convenio_procedimento FOREIGN KEY (procedimento_id) REFERENCES procedimento(id),
      CONSTRAINT uk_codigo_convenio_convenio_procedimento UNIQUE (convenio_id, procedimento_id)
    );
    CREATE INDEX idx_codigo_convenio_deleted ON codigo_convenio (deleted);
  END IF;
END $$;
```
> Não gera módulo próprio — gerenciado inline na tela de detalhe do `Convenio`.

### `DataSourceConfig.ENTITY_PACKAGES`
```java
"br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.entity",
```

### Arquivos Backend a criar
```
atendimento/codigoconvenio/
  entity/CodigoConvenio.java
  CodigoConvenioRepository.java
  CodigoConvenioService.java
  CodigoConvenioServiceImpl.java      ← sem Controller REST próprio
  dto/CodigoConvenioDTO.java
```
> O `ConvenioController` ou um sub-resource `/convenios/{id}/codigos` gerencia as operações.
> `ConvenioDTO` deve incluir `List<CodigoConvenioDTO> codigos`.

### Frontend
Gerenciado inline no detalhe de `Convenio` — lista editável de procedimentos e seus códigos específicos.
Não há componente dedicado para `CodigoConvenio`.

```
// Adicionado ao ConvenioDTO:
codigos: CodigoConvenioDTO[]

// Novo arquivo no módulo de convenio:
model/codigo-convenio.dto.ts
```

### Checklist Passo 6
- [ ] `CodigoConvenio.java` — Builder, ValidatedData, validate(), atualizar()
- [ ] Migration SQL idempotente (sem modulo INSERT)
- [ ] `DatabaseConstraintsEnum` — FK convenio, FK procedimento, UK convenio+procedimento
- [ ] `DataSourceConfig.ENTITY_PACKAGES`
- [ ] `CodigoConvenioDTO.java` (Lombok)
- [ ] `CodigoConvenioRepository.java`
- [ ] `CodigoConvenioServiceImpl.java`
- [ ] `ConvenioDTO.java` — adicionar `List<CodigoConvenioDTO> codigos`
- [ ] `ConvenioServiceImpl.java` — salvar/atualizar lista de códigos ao salvar convênio
- [ ] Frontend: `codigo-convenio.dto.ts` (classe com @Exclude/@Expose)
- [ ] Frontend: `convenio-detalhe.component` — seção inline para gestão de códigos por procedimento
- [ ] Testes backend: `CodigoConvenioRepositoryTest` + atualizar `ConvenioServiceImplTest`

---

## Passo 7 — `Tabela` e `TabelaItem`

### Por que dois passos juntos?
`TabelaItem` é o detalhe de `Tabela`. No frontend, os itens serão gerenciados dentro do detalhe
da Tabela (lista inline), sem tela própria. No backend, TabelaItem é uma entidade separada
com seu próprio repositório, mas sem Controller REST autônomo — as operações de item
serão gerenciadas pelo `TabelaController` ou por um sub-resource `/tabelas/{id}/itens`.

---

### 6A — `Tabela`

```
Pacote backend: br.com.grupopipa.gestaointegrada.atendimento.tabela.entity
Tabela SQL:     tabela
```

| Campo | Tipo Java | Coluna SQL | Obrigatório |
|-------|-----------|------------|-------------|
| `nome` | `Nome` (VO) | `nome VARCHAR(100)` | Sim |
| `tipo` | `TipoTabela` (enum) | `tipo VARCHAR(20)` | Sim |
| `ativo` | `Boolean` | `ativo BOOLEAN NOT NULL DEFAULT TRUE` | Sim |

**Enum `TipoTabela`:** `PARTICULAR`, `CONVENIO`

**Constraints:** `uk_tabela_nome` → `(nome)`

**`DatabaseConstraintsEnum`:**
```java
UK_TABELA_NOME("tabela.nome.unique")
```

---

### 6B — `TabelaItem`

```
Pacote backend: br.com.grupopipa.gestaointegrada.atendimento.tabela.entity
Tabela SQL:     tabela_item
```
> Mesmo pacote `entity` de `Tabela` — ambos ficam em `atendimento.tabela.entity`

| Campo | Tipo Java | Coluna SQL | Obrigatório |
|-------|-----------|------------|-------------|
| `tabela` | `@ManyToOne Tabela` | `tabela_id UUID NOT NULL` | Sim |
| `procedimento` | `@ManyToOne Procedimento` | `procedimento_id UUID NOT NULL` | Sim |
| `valor` | `Money` (VO) | `valor NUMERIC(12,2) NOT NULL` | Sim |
| `vigenciaInicio` | `LocalDate` | `vigencia_inicio DATE NOT NULL` | Sim |
| `vigenciaFim` | `LocalDate` | `vigencia_fim DATE` | Não (null = sem vencimento) |

**Regra de negócio:** Um procedimento não pode ter dois itens ativos (sem vigenciaFim ou com vigenciaFim no futuro)
na mesma tabela ao mesmo tempo. Validar no Service, não só por constraint SQL.

**Constraints:**
- `fk_tabela_item_tabela` → `tabela_item.tabela_id → tabela.id`
- `fk_tabela_item_procedimento` → `tabela_item.procedimento_id → procedimento.id`
- `uk_tabela_item_tabela_procedimento_inicio` → `(tabela_id, procedimento_id, vigencia_inicio)`

**`DatabaseConstraintsEnum`:**
```java
FK_TABELA_ITEM_TABELA("tabelaItem.tabela.invalid"),
FK_TABELA_ITEM_PROCEDIMENTO("tabelaItem.procedimento.invalid"),
UK_TABELA_ITEM_TABELA_PROCEDIMENTO_INICIO("tabelaItem.vigencia.duplicate")
```

### Migration
```
Arquivo: V{YYYYMMDDHHMMSS}__create_tabela_and_tabela_item.sql
```
```sql
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela') THEN
    CREATE TABLE tabela (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      nome VARCHAR(100) NOT NULL,
      tipo VARCHAR(20) NOT NULL,
      ativo BOOLEAN NOT NULL DEFAULT TRUE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT uk_tabela_nome UNIQUE (nome)
    );
    CREATE INDEX idx_tabela_deleted ON tabela (deleted);
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tabela_item') THEN
    CREATE TABLE tabela_item (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      tabela_id UUID NOT NULL,
      procedimento_id UUID NOT NULL,
      valor NUMERIC(12,2) NOT NULL,
      vigencia_inicio DATE NOT NULL,
      vigencia_fim DATE,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT fk_tabela_item_tabela FOREIGN KEY (tabela_id) REFERENCES tabela(id),
      CONSTRAINT fk_tabela_item_procedimento FOREIGN KEY (procedimento_id) REFERENCES procedimento(id),
      CONSTRAINT uk_tabela_item_tabela_procedimento_inicio
        UNIQUE (tabela_id, procedimento_id, vigencia_inicio)
    );
    CREATE INDEX idx_tabela_item_deleted ON tabela_item (deleted);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO_TABELA', 'Tabela de Preços', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO_TABELA');
```

### `DataSourceConfig.ENTITY_PACKAGES`
```java
"br.com.grupopipa.gestaointegrada.atendimento.tabela.entity",
```

### Arquivos Backend a criar
```
atendimento/tabela/
  entity/Tabela.java
  entity/TabelaItem.java
  TipoTabela.java                      ← enum
  TabelaRepository.java
  TabelaItemRepository.java
  TabelaService.java
  TabelaServiceImpl.java               ← inclui lógica de itens
  TabelaController.java
  TabelaConstants.java
  dto/TabelaDTO.java                   ← inclui List<TabelaItemDTO> itens
  dto/TabelaGridDTO.java
  dto/TabelaItemDTO.java
```

### Frontend
O detalhe de `Tabela` exibirá a lista de itens inline (tabela editável ou p-table).
Não há tela dedicada para `TabelaItem`.

```
components/atendimento/tabela/
  tabela.component.ts/html/css
  grid/tabela-grid.component.ts/html/css
  detalhe/tabela-detalhe.component.ts/html/css   ← inclui gestão de itens
  model/tabela.dto.ts
  model/tabela-grid.dto.ts
  model/tabela-item.dto.ts
  model/tipo-tabela.enum.ts
  tabela.service.ts
```

### Checklist Passo 7
- [ ] `Tabela.java` — Builder, ValidatedData, validate(), atualizar()
- [ ] `TabelaItem.java` — Builder, ValidatedData, validate(), atualizar()
- [ ] `TipoTabela.java` (enum Java)
- [ ] Migration SQL idempotente (tabela + tabela_item) + modulo INSERT
- [ ] `DatabaseConstraintsEnum` — todas constraints de ambas tabelas
- [ ] `DataSourceConfig.ENTITY_PACKAGES` — pacote `atendimento.tabela.entity`
- [ ] `TabelaDTO.java` (inclui `List<TabelaItemDTO>`) + `TabelaGridDTO.java` + `TabelaItemDTO.java`
- [ ] `TabelaRepository.java` + `TabelaItemRepository.java`
- [ ] `TabelaServiceImpl.java` — lógica de salvar itens + validação de vigência sobreposta
- [ ] `TabelaController.java` com `@PreAuthorize` e `getAuditInfo`
- [ ] Frontend: DTOs classes, enums, service
- [ ] Frontend: detalhe com gerenciamento inline de `TabelaItem` (adicionar/remover itens)
- [ ] `SystemModuleKey` + rotas + menu
- [ ] i18n
- [ ] Testes backend: `TabelaRepositoryTest` + `TabelaServiceImplTest` (inclui vigência)
- [ ] Testes frontend: `tabela-detalhe.component.spec.ts`

---

## Passo 8 — `Atendimento`

### Modelo
```
Pacote backend: br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity
Tabela SQL:     atendimento
```

### Campos
| Campo | Tipo Java | Coluna SQL | Obrigatório |
|-------|-----------|------------|-------------|
| `dataHora` | `LocalDateTime` | `data_hora TIMESTAMP NOT NULL` | Sim |
| `setor` | `@ManyToOne Setor` | `setor_id UUID NOT NULL` | Sim |
| `paciente` | `@ManyToOne Pessoa` | `paciente_id UUID NOT NULL` | Sim |
| `responsavel` | `@ManyToOne Pessoa` | `responsavel_id UUID` | Não (sobrescreve Pessoa.responsavel) |
| `convenio` | `@ManyToOne Convenio` | `convenio_id UUID` | Não (null = particular) |
| `convenioCategoria` | `@ManyToOne ConvenioCategoria` | `convenio_categoria_id UUID` | Não |
| `profissionalAtendimento` | `@ManyToOne Profissional` | `profissional_atendimento_id UUID NOT NULL` | Sim |
| `profissionalResponsavel` | `@ManyToOne Profissional` | `profissional_responsavel_id UUID NOT NULL` | Sim |
| `procedimento` | `@ManyToOne Procedimento` | `procedimento_id UUID NOT NULL` | Sim |
| `tabelaItem` | `@ManyToOne TabelaItem` | `tabela_item_id UUID` | Não (resolvido automaticamente) |
| `status` | `StatusAtendimento` (enum) | `status VARCHAR(20) NOT NULL` | Sim |
| `observacoes` | `String` | `observacoes TEXT` | Não |

### Enum `StatusAtendimento`
```java
AGENDADO, REALIZADO, CANCELADO, FALTOU
```

### Regras de negócio no Service
- Se `convenio` for informado, `convenioCategoria` deve pertencer a esse convênio (validar no Service)
- Ao salvar, se `responsavel` for null, copiar de `paciente.responsavel`
- O `tabelaItem` pode ser resolvido automaticamente: buscar na tabela do convênio (ou PARTICULAR)
  o item vigente para o `procedimento` na `dataHora` do atendimento

### Constraints SQL
```
fk_atendimento_setor                → atendimento.setor_id → setor.id
fk_atendimento_paciente             → atendimento.paciente_id → pessoa.id
fk_atendimento_responsavel          → atendimento.responsavel_id → pessoa.id
fk_atendimento_convenio             → atendimento.convenio_id → convenio.id
fk_atendimento_convenio_categoria   → atendimento.convenio_categoria_id → convenio_categoria.id
fk_atendimento_prof_atendimento     → atendimento.profissional_atendimento_id → profissional.id
fk_atendimento_prof_responsavel     → atendimento.profissional_responsavel_id → profissional.id
fk_atendimento_procedimento         → atendimento.procedimento_id → procedimento.id
fk_atendimento_tabela_item          → atendimento.tabela_item_id → tabela_item.id
ck_atendimento_status               → status IN ('AGENDADO','REALIZADO','CANCELADO','FALTOU')
```

### `DatabaseConstraintsEnum`
```java
FK_ATENDIMENTO_SETOR("atendimento.setor.invalid"),
FK_ATENDIMENTO_PACIENTE("atendimento.paciente.invalid"),
FK_ATENDIMENTO_RESPONSAVEL("atendimento.responsavel.invalid"),
FK_ATENDIMENTO_CONVENIO("atendimento.convenio.invalid"),
FK_ATENDIMENTO_CONVENIO_CATEGORIA("atendimento.convenioCategoria.invalid"),
FK_ATENDIMENTO_PROF_ATENDIMENTO("atendimento.profissionalAtendimento.invalid"),
FK_ATENDIMENTO_PROF_RESPONSAVEL("atendimento.profissionalResponsavel.invalid"),
FK_ATENDIMENTO_PROCEDIMENTO("atendimento.procedimento.invalid"),
FK_ATENDIMENTO_TABELA_ITEM("atendimento.tabelaItem.invalid")
```

### Migration
```
Arquivo: V{YYYYMMDDHHMMSS}__create_atendimento.sql
```
```sql
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'atendimento') THEN
    CREATE TABLE atendimento (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      data_hora TIMESTAMP NOT NULL,
      setor_id UUID NOT NULL,
      paciente_id UUID NOT NULL,
      responsavel_id UUID,
      convenio_id UUID,
      convenio_categoria_id UUID,
      profissional_atendimento_id UUID NOT NULL,
      profissional_responsavel_id UUID NOT NULL,
      procedimento_id UUID NOT NULL,
      tabela_item_id UUID,
      status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
      observacoes TEXT,
      deleted BOOLEAN NOT NULL DEFAULT FALSE,
      deleted_at TIMESTAMP,
      deleted_by VARCHAR(255),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP,
      created_by VARCHAR(255),
      updated_by VARCHAR(255),
      CONSTRAINT fk_atendimento_setor FOREIGN KEY (setor_id) REFERENCES setor(id),
      CONSTRAINT fk_atendimento_paciente FOREIGN KEY (paciente_id) REFERENCES pessoa(id),
      CONSTRAINT fk_atendimento_responsavel FOREIGN KEY (responsavel_id) REFERENCES pessoa(id),
      CONSTRAINT fk_atendimento_convenio FOREIGN KEY (convenio_id) REFERENCES convenio(id),
      CONSTRAINT fk_atendimento_convenio_categoria
        FOREIGN KEY (convenio_categoria_id) REFERENCES convenio_categoria(id),
      CONSTRAINT fk_atendimento_prof_atendimento
        FOREIGN KEY (profissional_atendimento_id) REFERENCES profissional(id),
      CONSTRAINT fk_atendimento_prof_responsavel
        FOREIGN KEY (profissional_responsavel_id) REFERENCES profissional(id),
      CONSTRAINT fk_atendimento_procedimento FOREIGN KEY (procedimento_id) REFERENCES procedimento(id),
      CONSTRAINT fk_atendimento_tabela_item FOREIGN KEY (tabela_item_id) REFERENCES tabela_item(id),
      CONSTRAINT ck_atendimento_status
        CHECK (status IN ('AGENDADO','REALIZADO','CANCELADO','FALTOU'))
    );
    CREATE INDEX idx_atendimento_deleted ON atendimento (deleted);
    CREATE INDEX idx_atendimento_data_hora ON atendimento (data_hora);
    CREATE INDEX idx_atendimento_paciente ON atendimento (paciente_id);
    CREATE INDEX idx_atendimento_profissional_atendimento ON atendimento (profissional_atendimento_id);
  END IF;
END $$;

INSERT INTO modulo (id, chave, nome, grupo)
SELECT gen_random_uuid(), 'ATENDIMENTO', 'Atendimento', 'ATENDIMENTO'
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE chave = 'ATENDIMENTO');
```

### `DataSourceConfig.ENTITY_PACKAGES`
```java
"br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity",
```

### Arquivos Backend a criar
```
atendimento/atendimento/
  entity/Atendimento.java
  StatusAtendimento.java               ← enum
  AtendimentoRepository.java
  AtendimentoService.java
  AtendimentoServiceImpl.java
  AtendimentoController.java
  AtendimentoConstants.java
  dto/AtendimentoDTO.java
  dto/AtendimentoGridDTO.java
```

### Arquivos Frontend a criar
```
components/atendimento/atendimento/
  atendimento.component.ts/html/css
  grid/atendimento-grid.component.ts/html/css
  detalhe/atendimento-detalhe.component.ts/html/css
  model/atendimento.dto.ts
  model/atendimento-grid.dto.ts
  model/status-atendimento.enum.ts
  atendimento.service.ts
```

### Comportamentos especiais no frontend (detalhe)
1. Ao selecionar `paciente`, pré-carregar `responsavel` do DTO da pessoa (se existir)
2. Campo `responsavel` é editável mesmo após auto-preenchimento
3. Campo `convenioCategoria` só habilita após `convenio` ser selecionado; ao trocar convênio, limpar categoria
4. Campo `tabelaItem` pode ser exibido como informativo (read-only) — resolvido pelo backend

### Checklist Passo 8
- [ ] `Atendimento.java` — Builder, ValidatedData, validate(), atualizar()
- [ ] `StatusAtendimento.java` (enum Java)
- [ ] Migration SQL idempotente + modulo INSERT
- [ ] `DatabaseConstraintsEnum` — todas constraints
- [ ] `DataSourceConfig.ENTITY_PACKAGES`
- [ ] `AtendimentoDTO.java` + `AtendimentoGridDTO.java` (Lombok)
- [ ] `AtendimentoRepository.java`
- [ ] `AtendimentoServiceImpl.java` — lógica de negócio: responsavel fallback, validar categoria vs convênio, resolver tabelaItem
- [ ] `AtendimentoController.java` com `@PreAuthorize` e `getAuditInfo`
- [ ] Frontend: DTOs, enum `StatusAtendimento`, service (2 args)
- [ ] Frontend: detalhe com comportamentos especiais (auto-fill responsável, cascade convênio→categoria)
- [ ] Frontend: grid com filtros por data, paciente, status
- [ ] `SystemModuleKey` + rotas + menu
- [ ] i18n
- [ ] Testes backend: `AtendimentoRepositoryTest` + `AtendimentoServiceImplTest`
- [ ] Testes frontend: `atendimento-detalhe.component.spec.ts`

---

## Resumo — `DataSourceConfig.ENTITY_PACKAGES` (todos os passos)

```java
// Adicionar ao array existente:
"br.com.grupopipa.gestaointegrada.atendimento.profissional.entity",
"br.com.grupopipa.gestaointegrada.atendimento.convenio.entity",
"br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity",
"br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity",
"br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.entity",
"br.com.grupopipa.gestaointegrada.atendimento.tabela.entity",
"br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity",
```

---

## Resumo — `DatabaseConstraintsEnum` (todos os passos)

```java
// Passo 1
FK_PESSOA_RESPONSAVEL("pessoa.responsavel.invalid"),

// Passo 2
FK_PROFISSIONAL_PESSOA("profissional.pessoa.invalid"),
UK_PROFISSIONAL_PESSOA("profissional.pessoa.unique"),

// Passo 3
UK_CONVENIO_NOME("convenio.nome.unique"),
UK_CONVENIO_REGISTRO_ANS("convenio.registroAns.unique"),
FK_CONVENIO_PESSOA("convenio.pessoa.invalid"),

// Passo 4
FK_CONVENIO_CATEGORIA_CONVENIO("convenioCategoria.convenio.invalid"),
UK_CONVENIO_CATEGORIA_NOME_CONVENIO("convenioCategoria.nome.unique"),

// Passo 5
UK_PROCEDIMENTO_CODIGO("procedimento.codigo.unique"),

// Passo 6
FK_CODIGO_CONVENIO_CONVENIO("codigoConvenio.convenio.invalid"),
FK_CODIGO_CONVENIO_PROCEDIMENTO("codigoConvenio.procedimento.invalid"),
UK_CODIGO_CONVENIO_CONVENIO_PROCEDIMENTO("codigoConvenio.procedimento.duplicate"),

// Passo 7
UK_TABELA_NOME("tabela.nome.unique"),
FK_TABELA_ITEM_TABELA("tabelaItem.tabela.invalid"),
FK_TABELA_ITEM_PROCEDIMENTO("tabelaItem.procedimento.invalid"),
UK_TABELA_ITEM_TABELA_PROCEDIMENTO_INICIO("tabelaItem.vigencia.duplicate"),

// Passo 8
FK_ATENDIMENTO_SETOR("atendimento.setor.invalid"),
FK_ATENDIMENTO_PACIENTE("atendimento.paciente.invalid"),
FK_ATENDIMENTO_RESPONSAVEL("atendimento.responsavel.invalid"),
FK_ATENDIMENTO_CONVENIO("atendimento.convenio.invalid"),
FK_ATENDIMENTO_CONVENIO_CATEGORIA("atendimento.convenioCategoria.invalid"),
FK_ATENDIMENTO_PROF_ATENDIMENTO("atendimento.profissionalAtendimento.invalid"),
FK_ATENDIMENTO_PROF_RESPONSAVEL("atendimento.profissionalResponsavel.invalid"),
FK_ATENDIMENTO_PROCEDIMENTO("atendimento.procedimento.invalid"),
FK_ATENDIMENTO_TABELA_ITEM("atendimento.tabelaItem.invalid"),
```

---

## Resumo — `SystemModuleKey` (frontend enum)

```typescript
ATENDIMENTO_PROFISSIONAL = 'ATENDIMENTO_PROFISSIONAL',
ATENDIMENTO_CONVENIO = 'ATENDIMENTO_CONVENIO',
ATENDIMENTO_CONVENIO_CATEGORIA = 'ATENDIMENTO_CONVENIO_CATEGORIA',
ATENDIMENTO_PROCEDIMENTO = 'ATENDIMENTO_PROCEDIMENTO',
ATENDIMENTO_TABELA = 'ATENDIMENTO_TABELA',
ATENDIMENTO = 'ATENDIMENTO',
```

---

## Dependências de serviços no detalhe de Atendimento

O componente `atendimento-detalhe` precisará injetar os seguintes services:
- `AtendimentoService`
- `PessoaService` — busca paciente e responsável
- `SetorService` — lista setores
- `ProfissionalService` — lista profissionais
- `ConvenioService` — lista convênios
- `ConvenioCategoriaService` — lista categorias (filtrado por convênio selecionado)
- `ProcedimentoService` — lista procedimentos

Considerar `EntitySearch` para Pessoa (paciente e responsável) dado que o volume pode ser grande.
