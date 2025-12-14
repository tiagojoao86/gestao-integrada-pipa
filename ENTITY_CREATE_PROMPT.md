PRINCIPAIS CORREÇÕES (importante ler antes de usar)

1. Não use importações inline com FQCNs espalhadas no código (ex.: `br.com.grupopipa.gestaointegrada.core.exceptions.BeanValidationMessage`). Ao gerar código, inclua o import correto no topo do arquivo:

   import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;

   Observação: o pacote correto é `br.com.grupopipa.gestaointegrada.core.exception.beanvalidation`.

2. Value Objects: NÃO use `new Nome(nome)`. Use sempre o método fábrica `of` quando o VO existir:

   // errado
   new Nome(nome);

   // correto
   Nome.of(nome);

   Consulte `src/backend/src/main/java/br/com/grupopipa/gestaointegrada/core/valueobject` para exemplos.

3. DTOs são obrigatórios — o prompt deve gerar explicitamente pelo menos dois DTOs: `{{EntityName}}DTO` e `{{EntityName}}GridDTO`. Use os pacotes do projeto e os tipos padrão (`UUID`, `LocalDateTime`, etc.).

4. Interfaces de service: a interface deve estender `CrudService<DTO, GridDTO>` indicando as classes corretas. Exemplo:

   public interface CentroCustoService extends CrudService<CentroCustoDTO, CentroCustoGridDTO> {
   }

5. Mensagens de validação: gere `BeanValidationException` usando `BeanValidationMessage` com chave e mensagem. As chaves devem ser usadas posteriormente em i18n. Exemplo de chave: `centroCusto.nome.notBlank`.

6. Migrations tenant: sempre idempotentes (IF NOT EXISTS / DO $$ BEGIN ... EXCEPTION WHEN others THEN ... END $$). Nomeie constraints conforme convenção:
   - UNIQUE: `uk_<table>_<field>`
   - FK: `fk_<table>_<referenced_table>`
   - CHECK: `ck_<table>_<field>`

PROMPT (copiar inteiro para o gerador)

"""
Você é um assistente de geração de código para o repositório "Gestão Integrada Pipa". Gere todos os arquivos e scripts necessários para uma nova entidade de domínio obedecendo estritamente as convenções abaixo. Retorne um objeto JSON onde cada chave é o caminho do arquivo no repositório e o valor é o conteúdo do arquivo (string). Não execute comandos no sistema; apenas gere os conteúdos dos arquivos.

Regras obrigatórias (resumo):

- IDs: UUID (UUIDv7) em Java (`UUID`) e SQL `DEFAULT gen_random_uuid()`.
- Entidades Java devem:

  - Estar em `br.com.grupopipa.gestaointegrada.{{domain}}.{{entityPackage}}` ou `.entity` (siga padrão do repo).
  - Estender `BaseEntity` (ID provido pelo base).
  - Ter Builder pattern, método de validação central `validate(...)` retornando `ValidatedData` e lançar `BeanValidationException` para erros.
  - Usar imports no topo (não use FQCN inline). Import correto para BeanValidationMessage:

    import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;

  - Usar value objects via método fábrica `of`, por exemplo `Nome.of(value)`.

- DTOs: Gere explicitamente `{{EntityName}}DTO` e `{{EntityName}}GridDTO` no pacote `{{domain}}.{{entityPackage}}`.

- Repositórios: estender `JpaRepository<..., UUID>` e `JpaSpecificationExecutor<...>`.

- Serviços: criar interface que estende `CrudService<{{EntityName}}DTO, {{EntityName}}GridDTO>` e `ServiceImpl` que estende `CrudServiceImpl<DTO, GridDTO, Entity, Repository>` e implementa `mergeEntityAndDTO` e `buildDTOFromEntity`.

- Controllers: estender `BaseController<DTO, GridDTO, Service>` e anotar endpoints com `@PreAuthorize` seguindo padrão `<MODULE>_<ENTITY_UPPER>_<ACTION>`.

- Migrations tenant: colocar em `src/main/resources/db/tenant-migrations/`, SQL idempotente e constraints com nomes explícitos.

- Frontend: componentes `gi-` (grid + detalhe) e contratos de `gi-filter-component`, `gi-table-component`, `gi-pagination-component` e `gi-app-base` descritos no exemplo abaixo. Gere também `{{entity}}.service.ts`, DTOs TS e `{{entity}}-backend-message.service.ts` que estende `AbstractBackendMessageService`.

- i18n: Gere trans-units para `src/frontend/src/app/locale/messages.xlf` (pt-BR) e `messages.en.xlf` (en-US) com as chaves de validação e textos visíveis.

- Tests: inclua skeletons de testes de repositório/serviço que utilizem geração runtime-única para campos `codigo`.

Validação e mensagens

- Use `BeanValidationMessage` com chave (para i18n) e mensagem curta. Evite inserir mensagens literais no código que não sejam chaves (as chaves são obrigatórias).

Exemplo de `input` JSON (preencha e envie ao LLM):
{
"EntityName": "CategoriaTitulo",
"table_name": "categoria_titulo",
"domain": "financeiro",
"entityPackage": "categoria",
"ShortDescription": "Categoria para títulos financeiros",
"fields": [
{"name":"nome","javaType":"String","required":true,"maxLength":200},
{"name":"codigo","javaType":"String","required":true,"unique":true,"maxLength":20},
{"name":"unidadeNegocioId","javaType":"UUID","required":true,"fk":"unidade_negocio(id)"}
]
}

Regras extras para o gerador (validação):

- Truncar códigos que excedam limites (ex.: `codigo` <= 20) e/ou usar pattern de geração em testes para evitar colisões.
- Todas as constraints SQL devem ter nomes explícitos.
- Migrations devem ser idempotentes.
- Backend message keys para validações devem seguir `{{entity}}.{{field}}.<rule>` (ex.: `categoriaTitulo.nome.notBlank`).

Saída esperada do LLM: JSON com caminhos de arquivo e conteúdos. Exemplo:
{
"src/backend/.../CategoriaTitulo.java": "<conteúdo>",
"src/backend/.../db/tenant-migrations/V20251213000001\_\_create_categoria_titulo_table.sql": "<sql content>",
"src/frontend/.../categoria-titulo-grid.component.ts": "<content>",
"i18n/pt": [ { "key":"categoriaTitulo.nome.notBlank", "source":"Nome da categoria é obrigatório.", "target":"Nome da categoria é obrigatório." } ],
"i18n/en": [ { "key":"categoriaTitulo.nome.notBlank", "source":"Nome da categoria é obrigatório.", "target":"Category name is required." } ]
}

"""

**Fim do prompt corrigido.**

Front-end — contratos detalhados (Grid)

- Estrutura: cada grid deve usar `gi-app-base` com áreas `body-content` e `footer-content`.
- Dentro de `body-content`:
  - `<gi-filter-component [filters]="filters" [hidden]="filterHidden" (filterEvent)="onFilter($event)" (cancelEvent)="onFilterCancel()"></gi-filter-component>`
    - `filters`: objeto de filtros atual
    - `hidden`: booleano controla visibilidade
    - `filterEvent`: emite filtros aplicados
    - `cancelEvent`: emite quando usuário fecha o filtro
  - `<gi-table-component [columns]="columns" [rows]="rows" [loading]="loading" [selected]="selected" (rowSelect)="onRowSelect($event)" (edit)="onEdit($event)" (delete)="onDelete($event)"></gi-table-component>`
- Dentro de `footer-content`:
  - `<gi-pagination-component [page]="page" [pageSize]="pageSize" [total]="total" (pageChange)="onPageChange($event)"></gi-pagination-component>`
- `gi-filter-component` deve ser dumb: receber `filters`, `hidden` e emitir `filterEvent` (FilterModel) e `cancelEvent`.
- `gi-table-component` deve ser dumb: receber `columns`, `rows`, `loading`, `selected` e emitir `rowSelect`, `edit`, `delete`.
- `gi-pagination-component` deve emitir `pageChange` com `{page, pageSize}`.

Front-end — contratos detalhados (Detail)

- Usar `gi-app-base` com `body-content` contendo form (`formGroup`) e `footer-content` com botões Salvar/Cancelar.
- `ngOnInit`: se `id` presente, carregar via service e popular `form`.
- `save()`: validar form, chamar service.save(dto), tratar mensagens (BackendMessageService).

Output requerido (arquivos)

- Backend Java:
  - `src/backend/src/main/java/br/com/grupopipa/gestaointegrada/{{domain}}/{{entityPackage}}/{{EntityName}}.java` (entidade)
  - `.../{{EntityName}}Repository.java`
  - `.../service/{{EntityName}}Service.java` e `.../service/{{EntityName}}ServiceImpl.java`
  - `.../controller/{{EntityName}}Controller.java`
- Migrations (tenant):
  - `src/backend/src/main/resources/db/tenant-migrations/V{{ts}}__create_{{table_name}}_table.sql` (idempotente)
  - opcional: `V{{ts}}__add_fk_...sql` se FK for necessária posteriormente
- DTOs backend/frontend as needed
- Frontend:
  - `src/frontend/src/app/components/{{domain}}/{{entity}}/{{entity}}-grid/{{entity}}-grid.component.ts` and `.html`
  - `src/frontend/src/app/components/{{domain}}/{{entity}}/{{entity}}-detalhe/{{entity}}-detalhe.component.ts` and `.html`
  - `src/frontend/src/app/components/{{domain}}/{{entity}}/{{entity}}.service.ts`
  - `src/frontend/src/app/components/{{domain}}/{{entity}}/model/{{entity}}.dto.ts` and `{{entity}}-grid.dto.ts`
  - `src/frontend/src/app/components/{{domain}}/{{entity}}/{{entity}}-backend-message.service.ts` (extends `AbstractBackendMessageService`)
  - route entry to be added/updated in `src/frontend/src/app/components/{{domain}}/{{domain}}.routes.ts` (or `financeiro.routes.ts`) — include `moduleKey` where appropriate.
- i18n:
  - Add `trans-unit` entries to `src/frontend/src/app/locale/messages.xlf` and `messages.en.xlf` for visible strings and validation keys.
- Tests:
  - Repository and Service unit test skeletons with runtime-unique `codigo` samples.

Formato de retorno exigido

- Retorne um único JSON com todos os arquivos. Exemplo (pseudo):
  {
  "src/backend/.../CategoriaTitulo.java": "<conteúdo>",
  "src/backend/.../db/tenant-migrations/V20251213000001\_\_create_categoria_titulo_table.sql": "<sql content>",
  "src/frontend/.../categoria-titulo-grid.component.ts": "<content>",
  "i18n/pt": [ { "key":"categoriaTitulo.nome.notBlank", "source":"Nome da categoria é obrigatório.", "target":"Nome da categoria é obrigatório." } ],
  "i18n/en": [ { "key":"categoriaTitulo.nome.notBlank", "source":"Nome da categoria é obrigatório.", "target":"Category name is required." } ]
  }

Exemplo de `input` JSON (preencha e envie ao LLM):
{
"EntityName": "CategoriaTitulo",
"table_name": "categoria_titulo",
"domain": "financeiro",
"entityPackage": "categoria",
"ShortDescription": "Categoria para títulos financeiros",
"fields": [
{"name":"nome","javaType":"String","required":true,"maxLength":200},
{"name":"codigo","javaType":"String","required":true,"unique":true,"maxLength":20},
{"name":"unidadeNegocioId","javaType":"UUID","required":true,"fk":"unidade_negocio(id)"}
]
}

Regras extras para o gerador (validação):

- Truncar códigos que excedam limites (ex.: `codigo` <= 20) e/ou usar pattern de geração em testes.
- Todas as constraints SQL devem ter nomes explícitos.
- Migrations devem ser idempotentes.
- Backend message keys para validações devem seguir `{{entity}}.{{field}}.<rule>` (e.g., `categoriaTitulo.nome.notBlank`).

"""
