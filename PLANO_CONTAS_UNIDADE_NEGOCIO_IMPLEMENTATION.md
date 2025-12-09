# Implementação UnidadeNegocio em PlanoContas

## Resumo

Implementação completa da associação obrigatória entre PlanoContas e UnidadeNegocio, seguindo o padrão estabelecido com Titulo.

## Backend (✅ COMPLETO)

### Entidade

- **PlanoContas.java**
  - Campo `unidadeNegocio` adicionado com `@ManyToOne` e `nullable = false`
  - Validação no método `validate()`: verifica se UnidadeNegocio não é nula
  - Builder atualizado para incluir UnidadeNegocio

### DTOs

- **PlanoContasDTO.java**

  - `UUID unidadeNegocioId`
  - `String unidadeNegocioNome`

- **PlanoContasGridDTO.java**
  - `String unidadeNegocioCodigo`

### Service

- **PlanoContasService.java**

  - Interface: `List<UnidadeNegocioDTO> listarUnidadesDisponiveis()`

- **PlanoContasServiceImpl.java**
  - Injeção de `UnidadeNegocioRepository`
  - `mergeEntityAndDTO()`: busca e valida UnidadeNegocio
  - `buildDTOFromEntity()`: inclui unidadeNegocioId e unidadeNegocioNome
  - `buildGridDTOFromEntity()`: inclui unidadeNegocioCodigo
  - `listarUnidadesDisponiveis()`: retorna unidades ativas

### Controller

- **PlanoContasController.java**
  - Endpoint: `GET /plano-contas/unidades-disponiveis`
  - Autorização: `@PreAuthorize("hasAuthority('CADASTRO_PLANO_CONTAS_EDITAR')")`
  - Retorna lista de UnidadeNegocioDTO

### Migration

- **V20251209000001\_\_add_unidade_negocio_to_plano_contas.sql**

  ```sql
  -- Adiciona coluna
  ALTER TABLE plano_contas ADD COLUMN unidade_negocio_id UUID;

  -- Atualiza registros existentes com unidade padrão
  UPDATE plano_contas
  SET unidade_negocio_id = '019b010e-6348-74cb-acf7-2aebca002b44'
  WHERE unidade_negocio_id IS NULL;

  -- Torna campo obrigatório
  ALTER TABLE plano_contas ALTER COLUMN unidade_negocio_id SET NOT NULL;

  -- Adiciona constraints
  ALTER TABLE plano_contas
  ADD CONSTRAINT fk_plano_contas_unidade_negocio
  FOREIGN KEY (unidade_negocio_id) REFERENCES unidade_negocio(id);

  -- Cria índice
  CREATE INDEX idx_plano_contas_unidade_negocio ON plano_contas(unidade_negocio_id);
  ```

## Frontend (✅ COMPLETO)

### Service

- **plano-contas.service.ts**
  - Método `listarUnidadesDisponiveis()`: retorna Observable com lista de unidades
  - Usa `map()` e `take(1)` para processar resposta
  - Corrigido lint: removido uso de `any` em `listarParaVinculo()`

### DTOs

- **plano-contas-dto.ts**

  - `unidadeNegocioId?: string`
  - `unidadeNegocioNome?: string`

- **plano-contas-grid-dto.ts**
  - `unidadeNegocioCodigo?: string`

### Componente Detalhe

- **plano-contas-detalhe.component.ts**

  - Arrays: `allUnidadesNegocio`, `unidadeNegocioSuggestions`, `unidadeNegocioInput`
  - `loadUnidadesNegocio()`: carrega unidades no ngOnInit
  - `searchUnidadesNegocio()`: filtra por codigo ou nome
  - `onUnidadeNegocioSelect()`: atualiza entity com id e nome
  - `onSave()`: validação obrigatória de UnidadeNegocio
  - `fillForm()`: inicializa unidadeNegocioInput se existir

- **plano-contas-detalhe.component.html**
  - Campo `p-autoComplete` para UnidadeNegocio
  - Posicionado após campo Plano Pai
  - Template exibe: "{{ item.codigo }} - {{ item.nome }}"
  - ForceSelection habilitado
  - MinLength = 1

### Componente Grid

- **plano-contas-grid.component.ts**
  - Nova coluna: `unidadeNegocioCodigo`
  - Label: "Unidade"
  - Exibe código ou "-" se ausente
  - Posicionada após coluna Plano Pai

## Padrão Arquitetural

### Context-Based Endpoints

Cada contexto (Titulo, PlanoContas) possui seu próprio endpoint de unidades disponíveis:

- `/api/titulo/unidades-disponiveis` - autorização: `FINANCEIRO_TITULO_EDITAR`
- `/api/plano-contas/unidades-disponiveis` - autorização: `CADASTRO_PLANO_CONTAS_EDITAR`

**Vantagens**:

- Segregação de responsabilidades
- Autorização contextual
- Facilita futuras customizações por contexto
- Mais RESTful

### Unidade Padrão

- UUID: `019b010e-6348-74cb-acf7-2aebca002b44`
- Código: `UNIDADE1`
- Nome: `Unidade 1 (Padrão Sistema)`
- CNPJ: `22076133000184`
- Criada automaticamente por migration
- Usada para atualizar registros existentes

## Compilação e Validação

### Backend

```bash
./mvnw compile -DskipTests
# BUILD SUCCESS - 0.750s
# "Nothing to compile - all classes are up to date"
```

### Frontend

```bash
npx tsc --noEmit
# Sem erros de compilação TypeScript

npm run lint -- --fix
# PlanoContas sem erros
# Erros pré-existentes em pessoa.service.ts (não relacionados)
```

## Próximos Passos

1. **ContaBancaria**: Implementar UnidadeNegocio seguindo mesmo padrão

   - Backend: Entity, Service, Controller, DTOs, Migration
   - Frontend: Service, Detalhe (autocomplete), Grid (coluna)

2. **MovimentacaoFinanceira**: Verificar se já possui UnidadeNegocio

   - Pode estar completo de implementações anteriores

3. **Testes End-to-End**:

   - Titulo: CRUD com UnidadeNegocio
   - PlanoContas: CRUD com UnidadeNegocio
   - ContaBancaria: Após implementação

4. **JWT Filtering** (futuro):
   - Filtrar unidades por claim `unidade_negocio_ids` do token
   - Garantir isolamento multi-tenant completo

## Arquivos Modificados

### Backend (8 arquivos)

- `PlanoContas.java`
- `PlanoContasDTO.java`
- `PlanoContasGridDTO.java`
- `PlanoContasService.java`
- `PlanoContasServiceImpl.java`
- `PlanoContasController.java`
- `V20251209000001__add_unidade_negocio_to_plano_contas.sql`

### Frontend (5 arquivos)

- `plano-contas.service.ts`
- `plano-contas-dto.ts`
- `plano-contas-grid-dto.ts`
- `plano-contas-detalhe.component.ts`
- `plano-contas-detalhe.component.html`
- `plano-contas-grid.component.ts`

## Notas Importantes

1. **Padrão Titulo**: PlanoContas segue exatamente o mesmo padrão de implementação do Titulo
2. **Validação Frontend**: Mensagem de erro: `planoContas.unidadeNegocioObrigatoria`
3. **Display no Grid**: Apenas código (economia de espaço)
4. **Display no Detalhe**: Código + Nome completo
5. **Autocomplete**: Busca por código OU nome
6. **ForceSelection**: Impede valores inválidos
7. **Lint**: Código sem warnings (exceto pessoa.service.ts pré-existente)
