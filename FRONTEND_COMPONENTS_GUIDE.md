# Guia de Implementação — Componentes Frontend (Grid & Detalhe)

Objetivo

- Descrever de forma precisa o que deve existir dentro dos componentes `Grid` e `Detalhe` para manter consistência com o padrão do projeto.

Visão geral

- Todos os grids seguem a mesma composição baseada em `gi-app-base` que provê áreas `body-content` e `footer-content`.
- No `body-content` ficam o componente de filtros (`gi-filter-component`) e a tabela (`gi-table-component`).
- No `footer-content` fica o componente de paginação (`gi-pagination-component`).

Padrões e responsabilidades

1. Estrutura HTML (Grid)

- Arquitetura geral (pseudocódigo):

```html
<gi-app-base [title]="'Título da Tela' | translate">
  <div body-content>
    <gi-filter-component
      [filters]="filters"
      [hidden]="filterHidden"
      (filterEvent)="onFilter($event)"
      (cancelEvent)="onFilterCancel()"
    >
    </gi-filter-component>

    <gi-table-component
      [columns]="columns"
      [rows]="rows"
      [loading]="loading"
      [selected]="selected"
      (rowSelect)="onRowSelect($event)"
      (edit)="onEdit($event)"
      (delete)="onDelete($event)"
    >
    </gi-table-component>
  </div>

  <div footer-content>
    <gi-pagination-component
      [page]="page"
      [pageSize]="pageSize"
      [total]="total"
      (pageChange)="onPageChange($event)"
    >
    </gi-pagination-component>
  </div>
</gi-app-base>
```

- Observações:
  - `gi-app-base` trata título, toolbar (adicionar/atualizar) e áreas visuais padronizadas.
  - Use `| translate` para todas as strings visíveis.

2. `gi-filter-component` (contrato)

- Inputs:
  - `@Input() filters: FilterModel` — objeto com as definições atuais de filtro (pode ser `any` ou interface específica do domínio).
  - `@Input() hidden: boolean` — controla se o filtro está visível.
- Outputs:
  - `@Output() filterEvent = new EventEmitter<FilterModel>()` — emite quando o usuário aplica um filtro.
  - `@Output() cancelEvent = new EventEmitter<void>()` — emite quando o usuário clica no X/fechar.
- Comportamento esperado:
  - Ao receber `filters`, preencher os campos do formulário.
  - Em `filterEvent` enviar o objeto com os filtros aplicados (mesmo formato aceito pela API).
  - Em `cancelEvent` apenas fechar (o pai controla `hidden`).

3. `gi-table-component` (contrato)

- Inputs (mínimos recomendados):
  - `@Input() columns: ColumnDef[]` — definição de colunas (field, header, type, width, formatter).
  - `@Input() rows: any[]` — dados a exibir.
  - `@Input() loading: boolean` — indicador de carregamento.
  - `@Input() selected: any | null` — item selecionado (quando aplicável).
- Outputs (mínimos recomendados):
  - `@Output() rowSelect = new EventEmitter<any>()` — seleção de linha.
  - `@Output() edit = new EventEmitter<any>()` — acionado pelo botão Edit na linha.
  - `@Output() delete = new EventEmitter<any>()` — acionado pelo botão Delete na linha.
- Observações:
  - A tabela deve ser "dumb" — apenas receber inputs e emitir outputs. Lógica de negócios/requests ficam no componente pai ou no service.
  - Formatação (dates, money) deve reutilizar pipes do projeto.

4. `gi-pagination-component` (contrato)

- Inputs:
  - `@Input() page: number` (0-based ou 1-based, padronizar com o restante do projeto) — sugerimos 0-based.
  - `@Input() pageSize: number`
  - `@Input() total: number`
- Outputs:
  - `@Output() pageChange = new EventEmitter<{page:number,pageSize:number}>()`
- Observações:
  - Ao trocar página emitir `pageChange` com novo `page` e `pageSize`.

5. Lógica Typescript (Grid component)

- Estado típico no `grid.component.ts`:

  - `filters: FilterModel = {}`
  - `filterHidden = false`
  - `columns: ColumnDef[] = [...]` (definir colunas do grid)
  - `rows: EntityGridDTO[] = []`
  - `loading = false`
  - `page = 0; pageSize = 20; total = 0; selected = null`

- Métodos principais:
  - `onFilter(filter)` — merge com `this.filters`, reset `page` para 0 e chamar `load()`.
  - `onFilterCancel()` — `this.filterHidden = true`.
  - `onPageChange(p)` — atualizar `page/pageSize` e chamar `load()`.
  - `load()` — chama service list com `filters,page,pageSize` e popula `rows,total`.
  - `onEdit(item)` — navegar para rota detalhe com `router.navigate([..., item.id])`.
  - `onDelete(item)` — confirmar e chamar service.delete; após sucesso, chamar `load()`.

6. Detalhe (Detail) — layout e responsabilidades

- O componente detalhe mostra formulário para criar/atualizar entidade.
- Estrutura comum:

```html
<gi-app-base [title]="'Entidade: ' + (model?.nome || '') | translate">
  <div body-content>
    <form [formGroup]="form" (ngSubmit)="save()">
      <!-- campos do form: use formControlName e componentes reusáveis -->
    </form>
  </div>

  <div footer-content>
    <button gi-button (click)="save()">{{ 'Salvar' | translate }}</button>
    <button gi-button (click)="cancel()">{{ 'Cancelar' | translate }}</button>
  </div>
</gi-app-base>
```

- Lógica TS (detalhe):
  - `ngOnInit`: se `route.params.id` existe, chamar `service.get(id)` e popular `form`.
  - `save()`: se `form.valid` então chamar `service.save(dto)`; tratar erros e mostrar mensagens via `messageService`.
  - Em caso de validação backend, o `BackendMessageService` deve mapear chaves para mensagens amigáveis.

7. Backend-message service (frontend)

- Criar `{{entity}}-backend-message.service.ts` estendendo `AbstractBackendMessageService`.
- Implementar `messages()` retornando `Map<string,string>` com chaves do backend -> chaves i18n.
- Nome exportado do serviço deve seguir padrão `{{EntityName}}BackendMessageService`.

8. DTOs e nomes de arquivos

- Grid DTO: `{{entity}}-grid.dto.ts` exportando `{{EntityName}}GridDTO`.
- Detail DTO: `{{entity}}.dto.ts` exportando `{{EntityName}}DTO`.
- Services: `{{entity}}.service.ts` com métodos `list(filters,page,pageSize)`, `get(id)`, `save(dto)`, `delete(id)`.

9. Boas práticas

- Componentes presentation-only (table, filter) não fazem chamadas HTTP.
- Toda comunicação com o backend via `{{entity}}.service.ts`.
- Use `translate` pipe para textos e adicione chaves i18n tanto em `messages.xlf` quanto em `messages.en.xlf`.
- Evite variáveis globais: use inputs/outputs e serviços injetáveis.
- Nomeie arquivos e símbolos consistentemente (`kebab-case` para arquivos/paths, `CamelCase` para classes).

10. Checklist para criar um novo Grid + Detail

- [ ] Criar DTOs (`entity` & `entity-grid`)
- [ ] Criar `entity.service.ts` com métodos list/get/save/delete
- [ ] Criar `entity-grid.component.ts/html` seguindo este guia
- [ ] Criar `entity-detalhe.component.ts/html` seguindo este guia
- [ ] Adicionar rota em `financeiro.routes.ts` (ou domínio correto)
- [ ] Criar `entity-backend-message.service.ts` que estenda `AbstractBackendMessageService`
- [ ] Adicionar entradas i18n (pt-BR e en-US)
- [ ] Rodar `npm run build` e checar warnings de i18n
- [ ] Rodar testes frontend/build e backend testes relevantes

---

Se quiser, eu posso agora:

- Gerar um _boilerplate_ de `grid` e `detalhe` preenchido para `CategoriaTitulo` com base neste guia, criando os arquivos TS/HTML/DTOs e atualizando rotas.
- Ou apenas inserir trechos de exemplo direto no componente `conta-bancaria-grid.component.html` aberto no seu editor.

Qual prefere que eu faça em seguida?
